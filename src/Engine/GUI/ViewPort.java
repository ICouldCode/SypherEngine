package Engine.GUI;

import Engine.Component.Camera2D;
import Engine.Component.Collider2D;
import Engine.Core.GameObject;
import Engine.Manager.InputManager;
import Scenes.Editor;
import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class ViewPort {

    public static ImVec2 size = new ImVec2();
    public static ImVec2 viewPos = new ImVec2();
    public static Vector2f world = new Vector2f(0f, 0f);

    public static boolean focused;

    public static void Render(Editor scene) {
        ImGui.begin("ViewPort");

        // Get absolute screen pos of the image
        ImVec2 imageScreenPos = ImGui.getCursorScreenPos();

        // Get GLFW window position on screen
        int[] windowX = new int[1];
        int[] windowY = new int[1];
        GLFW.glfwGetWindowPos(scene.window.window, windowX, windowY); // <- Update with your window handle

        // Convert screen pos to relative position inside the GLFW window
        float viewX = imageScreenPos.x - windowX[0];
        float viewY = imageScreenPos.y - windowY[0];
        viewPos = new ImVec2(viewX, viewY);

        size = ImGui.getContentRegionAvail();
        ImGui.image(scene.frameBuffer.textureID, size.x, size.y, 0, 1, 1, 0);

        focused = ImGui.isWindowFocused();
        ImGui.end();
    }

    public static void Update(Editor scene) {
        // Convert the mouse position relative to the viewport image
        float relativeMouseX = (float) (InputManager.mouseX - viewPos.x);
        float relativeMouseY = (float) (InputManager.mouseY - viewPos.y);

        // Ensure mouse is inside the image bounds
        if (relativeMouseX >= 0 && relativeMouseX <= size.x &&
                relativeMouseY >= 0 && relativeMouseY <= size.y) {

            world = scene.mainCamera.getComponent(Camera2D.class)
                    .screenToWorld(relativeMouseX, relativeMouseY, size.x, size.y);
        }

        if(focused){
            for (GameObject object : scene.sceneObjects) {
                Collider2D collider2D = object.getComponent(Collider2D.class);
                if (collider2D != null && collider2D.contains(world)) {
                    if (InputManager.isMouseButtonPressed(0)) {
                        scene.selected = object;
                    }
                }
            }

            if(InputManager.isKeyPressed(GLFW.GLFW_KEY_DELETE) || InputManager.isKeyPressed(GLFW.GLFW_KEY_BACKSPACE)){
                 scene.removeGameObject();
            }
        }
    }
}
