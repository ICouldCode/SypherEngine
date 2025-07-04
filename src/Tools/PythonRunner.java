package Tools;

import Engine.Core.Console;
import Engine.GUI.AIAssistSubWindow;

import java.io.*;
import java.util.concurrent.*;

public class PythonRunner {

    public static PythonRunner instance;
    private static BufferedWriter writer;
    private static BufferedReader reader;
    private static Process process;

    private static final BlockingQueue<String> responses = new LinkedBlockingQueue<>();

    public PythonRunner() {
        instance = this;

        ProcessBuilder pb = new ProcessBuilder("python", "PythonAICode/cmdapp.py");
        pb.redirectErrorStream(true);
        try {
            process = pb.start();

            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Read READY line synchronously before starting thread
            String line = reader.readLine();
            AIAssistSubWindow.AddMessage("Python says: " + line);

            // Start background thread to continuously read Python output
            Thread readerThread = new Thread(() -> {
                try {
                    String responseLine;
                    while ((responseLine = reader.readLine()) != null) {
                        System.out.println("Python says async: " + responseLine);
                        responses.put(responseLine);  // enqueue response
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

        } catch (IOException e) {
            Console.error("Python Runner: " + e.getMessage());
        }
    }

    public static String TalkToPythonAI(String input) {
        try {
            // Send input with newline
            writer.write(input);
            writer.newLine();
            writer.flush();

            // Wait for response from queue (blocks here until Python replies)
            String response = responses.take();

            System.out.println("Python replied: " + response);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
