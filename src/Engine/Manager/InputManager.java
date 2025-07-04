package Engine.Manager;

import org.lwjgl.glfw.GLFWCursorPosCallback;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager {

    private static long window;

    private static boolean[] keys = new boolean[GLFW_KEY_LAST + 1];
    private static boolean[] mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST];
    private static boolean[] lastKeys = new boolean[GLFW_KEY_LAST + 1];
    public static double mouseX, mouseY, scrollXOffset, scrollYOffset;

    public static void init(long glfwWindow) {
        window = glfwWindow;

        GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                mouseX = xpos;
                mouseY = ypos;
            }
        };
        glfwSetCursorPosCallback(window, cursorPosCallback);

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button >= 0 && button < mouseButtons.length) {
                mouseButtons[button] = action != GLFW_RELEASE;
            }
        });

        glfwSetScrollCallback(window, (window, xOffset, yOffset) -> {
            scrollXOffset = xOffset;
            scrollYOffset = yOffset;
        });
    }

    public static void update() {
        for (int i = 32; i < keys.length; i++) {
            lastKeys[i] = keys[i];
            keys[i] = glfwGetKey(window, i) == GLFW_PRESS;
        }
    }


    public static boolean isKeyJustPressed(int key) {
        if (key < 0 || key > GLFW_KEY_LAST) return false;
        if (keys[key] && !lastKeys[key]) {
            return true;
        }
        return false;
    }

    public static boolean isKeyPressed(int keyCode) {
        return keyCode >= 0 && keyCode < keys.length && keys[keyCode];
    }
    public static boolean isMouseButtonPressed(int button) {
        return button >= 0 && button < mouseButtons.length && mouseButtons[button];
    }
}

