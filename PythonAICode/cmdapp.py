from Chat import ChatSession
import torch
import sys

def run_chat_loop(session):
    print("Commands: /reset to reset chat history, /clear to clear GPU memory and exit.")
    sys.stdout.flush()

    while True:
        try:
            user_input = sys.stdin.readline()
            if not user_input:
                break

            user_input = user_input.strip()

            # Handle exit commands
            if user_input.lower() in ("exit", "quit"):
                print("Goodbye!")
                sys.stdout.flush()
                break

            # Handle manual commands
            if user_input == "/reset":
                session.chat_history_ids = None
                print("Chat history reset.")
                sys.stdout.flush()
                continue

            if user_input == "/clear":
                print("Clearing GPU memory and exiting...")
                session.clear_gpu_memory()
                sys.stdout.flush()
                break

            # Treat everything else as chat input string
            reply = session.chat(user_input)
            print(reply)
            sys.stdout.flush()

#         except KeyboardInterrupt:
#             print(json.dumps({"status": "interrupt", "message": "Interrupted by user."}))
#             sys.stdout.flush()
#             break
        except Exception as e:
            print(f"Error: {e}")
            sys.stdout.flush()


#Pretrained model and tokenizer paths
# session_1 = ChatSession(
#     ckpt_path='//10.0.1.3/storage/LLM/Checkpoints/Test_1/CheckPoint/transformer_epoch_34.pth',
#     vocab_path='//10.0.1.3/storage/LLM/CheckPoints/Test_1/Tokenizer/vocab.json',
#     merges_path='//10.0.1.3/storage/LLM/CheckPoints/Test_1/Tokenizer/merges.txt',
#     device="cuda:0",
#     max_length=100,
#     temperature=1.0,
#     top_k=100,
#     top_p=0.9,
#     model_max_len=128,
#     summary_every=5
# )

#Training model steps and tokenizer
session_2 = ChatSession(
    ckpt_path='//10.0.1.3/storage/LLM/Checkpoints/Steps/2/checkpoint_20250615-231823_58498.pth',
    vocab_path='//10.0.1.3/storage/LLM/Tokenizer/vocab.json',
    merges_path='//10.0.1.3/storage/LLM/Tokenizer/merges.txt',
    device="cuda:0",
    max_length=100,
    temperature=1.0,
    top_k=100,
    top_p=0.9,
    model_max_len=512,
    summary_every=5
)

if __name__ == "__main__":
    run_chat_loop(session_2)