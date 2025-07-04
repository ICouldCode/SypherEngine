package Tools;

import Engine.Core.GameObject;
import Engine.Core.Scene;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainThread {
    private static final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    public static void invokeLater(Runnable task) {
        taskQueue.offer(task);
    }

    public static void processTasks() {
        Runnable task;
        while ((task = taskQueue.poll()) != null) {
            task.run();
        }
    }
}
