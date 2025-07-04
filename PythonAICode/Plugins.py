from datetime import datetime
import re
import os


class CommandPlugin:
    def match(self, text: str) -> bool:
        raise NotImplementedError("CommandPlugin.match() must be implemented by subclasses")

    def run(self, text: str) -> str:
        raise NotImplementedError("CommandPlugin.run() must be implemented by subclasses")
    
    
class PluginManager:
    def __init__(self):
        self.plugins = [
            #Register Plugins
            TimePlugin(),
            DayPlugin(),
            MathPlugin(),
            FileSystemPlugin(),
        ]

    def run_plugins(self, text: str) -> str:
        for plugin in self.plugins:
            if plugin.match(text):
                return plugin.run(text)
        return None

    def clear_plugins(self):
        self.plugins.clear()


class TimePlugin(CommandPlugin):
    def match(self, text: str) -> bool:
        keywords = ["time", "date", "what time is it", "what's the time", "what date is it", "current time", "current date", "what's today's date", "what day is it"]
        return any(kw in text.lower() for kw in keywords)
    
    def run(self, text: str) -> str:
        now = datetime.now()
        return f"The current time is {now.strftime('%I:%M %p')} on {now.strftime('%A, %B %d, %Y')}."


class DayPlugin:
    def match(self, text: str) -> bool:
        keywords = ["what day is it", "day of the week", "today's day"]
        return any(kw in text.lower() for kw in keywords)

    def run(self, text: str) -> str:
        day = datetime.now().strftime("%A")
        return f"Today is {day}."
    
class MathPlugin:
    def match(self, text: str) -> bool:
        return bool(re.search(r"\bwhat\s+is\b|\bcalculate\b|\d+\s*[\+\-\*/]\s*\d+", text.lower()))

    def run(self, text: str) -> str:
        try:
            # Remove prompt words to isolate expression
            cleaned = re.sub(r"\b(what\s+is|calculate)\b", "", text.lower())
            
            # Only allow numbers, math operators, parentheses, and dots
            expression = re.findall(r"[0-9\.\+\-\*/\(\)\s]+", cleaned)
            if not expression:
                return "I couldn't find a valid math expression."
            
            expression = "".join(expression).strip()
            
            # Evaluate the expression safely
            result = eval(expression, {"__builtins__": {}})
            return f"The result is {result}."
        except Exception as e:
            return f"Error evaluating math: {e}"


class FileSystemPlugin:
    def match(self, text: str) -> bool:
        keywords = ["list files", "show my files", "check files", "list directory"]
        return any(kw in text.lower() for kw in keywords)

    def run(self, text: str) -> str:
        path = "."  # default current directory
        try:
            files = os.listdir(path)
            if not files:
                return "The directory is empty."
            return "Files:\n" + "\n".join(files)
        except Exception as e:
            return f"Error listing files: {e}"