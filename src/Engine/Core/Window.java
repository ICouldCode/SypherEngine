package Engine.Core;

import Engine.Component.Camera2D;
import Engine.GUI.*;
import Engine.Manager.InputManager;
import Engine.Manager.SceneManager;
import Scenes.Editor;

import Tools.MainThread;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {

    // The window handle
    public long window = 0;

    public int height, width, vSync;

    private String title;

    private final SceneManager sceneManager;
    private static ImGUILayer imGuiLayer;
    public static FrameBuffer frameBuffer;

    public boolean fullScreened = false;

    public Window(int width, int height, String title, int vSync) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.vSync = vSync;
        sceneManager = new SceneManager();
        new InputManager();
        new Console();
        new MainThread();
    }

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // Create the window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            assert vidmode != null;
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(vSync);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        /*GL43.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL43.glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> System.err.println("GL DEBUG: " + GLDebugMessageCallback.getMessage(length, message)), 0);*/

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);


        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetWindowSize(window, pWidth, pHeight);
            frameBuffer = new FrameBuffer(pWidth.get(0), pHeight.get(0));
        }


        long lastTime = System.nanoTime();
        InputManager.init(window);
        //MainScene scene = new MainScene(this);
        Editor editor = new Editor(this);
        sceneManager.pushScene(editor);
        setWindowResizeCallback(window, editor.renderer.camera);
        imGuiLayer = new ImGUILayer(this);
        imGuiLayer.init();

        while ( !glfwWindowShouldClose(window) ) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;  // Convert from nanoseconds to seconds
            deltaTime = Math.max(deltaTime, 0.0001f);
            // FPS counter
            title = String.format("Engine FPS:%d", (int)(1.0 / deltaTime));
            glfwSetWindowTitle(window, title);

            glfwPollEvents(); // Poll events
            InputManager.update();

            MainThread.processTasks();

            frameBuffer.bind();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear screen
            glClearColor(0f, 0f, 0f, 1f);

            imGuiLayer.startFrame();
            sceneManager.render();
            WindowInput();
            sceneManager.update(deltaTime);
            imGuiLayer.endFrame();
            frameBuffer.Unbind();

            lastTime = currentTime;
            glfwSwapBuffers(window); // Swap buffers
        }
    }

    private void WindowInput() {
        if (glfwGetKey(window, GLFW_KEY_F11) == GLFW_PRESS) {
            try {
                // Simple debounce so holding the key doesn't trigger multiple times
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            fullScreened = !fullScreened;

            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = glfwGetVideoMode(monitor);

            glfwSetWindowAttrib(window, GLFW_DECORATED, fullScreened ? GLFW_FALSE : GLFW_TRUE);

            if (fullScreened) {
                // Resize and position to cover entire screen
                assert vidMode != null;
                glfwSetWindowMonitor(window, NULL, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
                width = vidMode.width();
                height = vidMode.height();
            } else {
                // Restore to default size
                int windowedWidth = 1280;
                int windowedHeight = 720;
                width = windowedWidth;
                height = windowedHeight;
                assert vidMode != null;
                glfwSetWindowMonitor(window, NULL,
                        (vidMode.width() - windowedWidth) / 2,
                        (vidMode.height() - windowedHeight) / 2,
                        windowedWidth, windowedHeight,
                        vidMode.refreshRate());
            }

            glViewport(0, 0, width, height);
        }
    }
    private void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        imGuiLayer.dispose();
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
    public static void setWindowResizeCallback(long window, Camera2D camera) {
        glfwSetFramebufferSizeCallback(window, (w, width, height) -> onWindowResized(width, height, camera));
    }
    public static void onWindowResized(int width, int height, Camera2D camera) {
        glViewport(0, 0, width, height);
        imGuiLayer.io.setDisplaySize(width, height);
        frameBuffer.resize(width, height);
        camera.setProjection(width, height);
        camera.recalculateView();
    }

}