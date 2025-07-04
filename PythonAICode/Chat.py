import torch
import torch.nn.functional as F
from tokenizers import ByteLevelBPETokenizer

#Custom
import Transform as tm
import Plugins as pl

def top_k_top_p_filtering(logits, top_k=100, top_p=0.7, filter_value=-float('Inf')):
    assert logits.dim() == 1

    top_k = min(top_k, logits.size(-1))
    if top_k > 0:
        indices_to_remove = logits < torch.topk(logits, top_k)[0][-1]
        logits[indices_to_remove] = filter_value

    if top_p < 1.0:
        sorted_logits, sorted_indices = torch.sort(logits, descending=True)
        cumulative_probs = torch.cumsum(F.softmax(sorted_logits, dim=-1), dim=-1)

        sorted_indices_to_remove = cumulative_probs > top_p
        sorted_indices_to_remove[..., 1:] = sorted_indices_to_remove[..., :-1].clone()
        sorted_indices_to_remove[..., 0] = 0

        indices_to_remove = sorted_indices[sorted_indices_to_remove]
        logits[indices_to_remove] = filter_value

    return logits

class ChatSession:
    def __init__(self, ckpt_path, vocab_path, merges_path, device=None,
                 max_length=100, temperature=1.0, top_k=0, top_p=0.9,
                 model_max_len=128, summary_every=5):
        self.device = device or torch.device("cuda:1" if torch.cuda.is_available() else "cpu")
        self.memory_file = "//10.0.1.3/storage/LLM/Memory/MiMisMemory.txt"
        self.chat_memory = []
        self.tokenizer = ByteLevelBPETokenizer(vocab_path, merges_path)

        vocab_size = 32000
        model_dim = 128
        n_heads = 4
        num_layers = 4
        max_len = model_max_len
        self.model = tm.GPTTransformer(vocab_size, d_model=model_dim, n_heads=n_heads,
                                       num_layers=num_layers, max_len=max_len).to(self.device)

        checkpoint = torch.load(ckpt_path, map_location=self.device)
        self.model.load_state_dict(checkpoint['model_state_dict'])
        self.model.eval()

        self.max_length = max_length
        self.temperature = temperature
        self.top_k = top_k
        self.top_p = top_p
        self.eos_token_id = self.tokenizer.token_to_id("</s>")
        self.chat_history_ids = None
        self.model_max_len = model_max_len

        # For summarization:
        self.turn_count = 0
        self.summary_every = summary_every
        self.summary_token_ids = None

        self.special_token_ids = set(
            self.tokenizer.token_to_id(token)
            for token in self.tokenizer.get_vocab()
            if self.tokenizer.id_to_token(self.tokenizer.token_to_id(token)).startswith("<")
        )
        self.special_token_ids.discard(-1)

        self.load_memory()

    def summarize_context(self, token_ids):
        """
        Use the model itself to generate a short summary of the given token_ids context.
        You can make this smarter or replace with your own summarization logic.
        """
        # For demonstration: Just truncate and decode to string as a 'summary'
        # Replace this with a call to an actual summarization model if you want.
        max_summary_len = 50  # number of tokens for summary
        truncated_ids = token_ids[:, -max_summary_len:]
        summary_text = self.tokenizer.decode([tid for tid in truncated_ids[0].tolist() if tid not in self.special_token_ids])
        # Encode back to tokens to store as summary context
        summary_ids = self.tokenizer.encode(summary_text).ids
        return torch.tensor(summary_ids, dtype=torch.long).unsqueeze(0).to(self.device)

    def truncate_history(self, token_ids):
        """
        Truncate chat history to fit model input size, leaving room for new input and generation.
        We'll keep recent tokens plus a summary of earlier context.
        """
        # Leave space for max_length generation tokens plus some buffer
        max_allowed = self.model_max_len - self.max_length - 10  # buffer of 10 tokens

        if token_ids.size(1) <= max_allowed:
            # No truncation needed
            return token_ids

        # Split history into summary part + recent tokens
        recent_tokens = token_ids[:, -max_allowed:]
        old_tokens = token_ids[:, :-max_allowed]

        # Summarize old tokens into short summary token sequence
        summary_ids = self.summarize_context(old_tokens)

        # Concatenate summary + recent tokens for new history
        new_history = torch.cat([summary_ids, recent_tokens], dim=1)

        # If still too long, truncate from the front again
        if new_history.size(1) > self.model_max_len:
            new_history = new_history[:, -self.model_max_len:]

        return new_history

    def chat(self, user_input):
        user_text = user_input.strip()

        command_response = pl.PluginManager().run_plugins(user_text)
        if command_response:
            # If a plugin handled the command, return its response
            self.chat_memory.append(f"User: {user_text}")
            self.chat_memory.append(f"MiMi: {command_response}")
            self.save_to_memory(user_text, command_response)
            return command_response
        

        # Include memory in the prompt
        memory_prompt = "\n".join(self.chat_memory[-10:])  # or adjust for your needs
        full_prompt = f"{memory_prompt}\nUser: {user_text}\nMiMi:"

        # Encode prompt with memory
        user_input_ids = self.tokenizer.encode(full_prompt).ids
        user_input_ids = torch.tensor(user_input_ids, dtype=torch.long).unsqueeze(0).to(self.device)

        if self.chat_history_ids is None:
            combined_input_ids = user_input_ids
        else:
            combined_input_ids = torch.cat([self.chat_history_ids, user_input_ids], dim=1)
            combined_input_ids = self.truncate_history(combined_input_ids)

        generated_ids = []

        with torch.no_grad():
            cur_input_ids = combined_input_ids

            if cur_input_ids.size(1) >= self.model_max_len:
                cur_input_ids = cur_input_ids[:, -self.model_max_len:]

            for _ in range(self.max_length):
                logits = self.model(cur_input_ids)
                logits = logits[:, -1, :] / self.temperature
                filtered_logits = top_k_top_p_filtering(logits[0], top_k=self.top_k, top_p=self.top_p)
                probs = F.softmax(filtered_logits, dim=-1)
                next_token = torch.multinomial(probs, num_samples=1)

                generated_ids.append(next_token.item())
                cur_input_ids = torch.cat([cur_input_ids, next_token.unsqueeze(0)], dim=1)

                # Prevent cur_input_ids from exceeding max model length
                if cur_input_ids.shape[1] > self.model_max_len:
                    cur_input_ids = cur_input_ids[:, -self.model_max_len:]

                if next_token.item() == self.eos_token_id:
                    break

        # Decode generated reply fully including special tokens, then clean up
        #raw_reply = self.tokenizer.decode(generated_ids, skip_special_tokens=False).strip()
        # Optionally remove or replace special tokens in raw_reply here if needed
        reply = self.tokenizer.decode(generated_ids, skip_special_tokens=True).strip()

        for tag in ["User:", "MiMi:"]:
            idx = reply.find(tag)
            if idx != -1:
                reply = reply[:idx].strip()

        # Update chat history: add generated tokens to the input tokens
        new_generated_ids = torch.tensor([generated_ids], dtype=torch.long).to(self.device)
        self.chat_history_ids = torch.cat([combined_input_ids, new_generated_ids], dim=1)
        self.chat_history_ids = self.truncate_history(self.chat_history_ids)

        reply = "\n".join(line for line in reply.splitlines() if line.strip('-').strip() != "")

        self.chat_memory.append(f"User: {user_text}")
        self.chat_memory.append(f"MiMi: {reply}")
        self.save_to_memory(user_text, reply)

        return reply


    def clear_gpu_memory(self):
        """Explicitly clear GPU memory used by this chat session"""
        if self.model is not None:
            self.model.cpu()
            del self.model
            self.model = None
        del self.tokenizer
        self.tokenizer = None
        del self.chat_history_ids
        self.chat_history_ids = None
        if torch.cuda.is_available():
            torch.cuda.empty_cache()

    def load_memory(self):
        pth_file = self.memory_file.replace('.txt', '.pth')
        try:
            self.chat_history_ids = torch.load(pth_file, map_location=self.device)
            
            # Ensure tensor is on the correct device
            self.chat_history_ids = self.chat_history_ids.to(self.device)
            
            # Truncate if too long
            if self.chat_history_ids.size(1) > self.model_max_len:
                self.chat_history_ids = self.chat_history_ids[:, -self.model_max_len:]
                
        except FileNotFoundError:
            self.chat_history_ids = None
        except Exception as e:
            print(f"Error loading memory file: {e}")
            self.chat_history_ids = None
   
    def save_to_memory(self, user_text, bot_text):
        with open(self.memory_file, "a", encoding="utf-8") as f:
            f.write(f"User: {user_text}\nMiMi: {bot_text}\n")
        if self.chat_history_ids is not None:
            torch.save(self.chat_history_ids.cpu(), self.memory_file.replace('.txt', '.pth'))
