package Engine.Core;

import java.util.ArrayList;
import java.util.List;

public class Console {

    public enum logLevel{
        INFO, DEBUG, WARNING, ERROR
    }

    private static final List<String> logBuffer = new ArrayList<>();

    public static void Log(logLevel level, String message){
        String prefix = getLogLevel(level);
        String output = prefix + message;

        logBuffer.add(output);
    }

    private static String getLogLevel(logLevel level){
        switch (level){
            case INFO ->
            {
                return  "[INFO] ";
            }
            case DEBUG ->
            {
                return  "[DEBUG] ";
            }
            case ERROR ->
            {
                return "[ERROR] ";
            }
            case WARNING ->
            {
                return "[WARNING] " ;
            }
            default ->
            {
                return "[LOG] ";
            }
        }
    }

    public static void info(String message) {
        Log(logLevel.INFO, message);
    }

    public static void debug(String message) {
        Log(logLevel.DEBUG, message);
    }

    public static void warn(String message) {
        Log(logLevel.WARNING, message);
    }

    public static String error(String message) {
        Log(logLevel.ERROR, message);
        return message;
    }

    public static List<String> getLogs() {
        return logBuffer;
    }

    public static void clear() {
        logBuffer.clear();
    }
}
