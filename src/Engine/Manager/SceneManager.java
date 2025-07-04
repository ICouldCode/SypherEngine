package Engine.Manager;

import Engine.Core.Scene;

import java.util.Stack;

public class SceneManager {
    private Stack<Scene> sceneStack;

    public SceneManager() {
        sceneStack = new Stack<>();
    }

    // Push a new scene onto the stack (switch to it)
    public void pushScene(Scene scene) {
        if (!sceneStack.isEmpty()) {
            sceneStack.peek().cleanup();  // Clean up the current scene
        }
        sceneStack.push(scene);
        scene.init();
    }

    // Pop the current scene and switch to the previous one
    public void popScene() {
        if (!sceneStack.isEmpty()) {
            sceneStack.peek().cleanup();
            sceneStack.pop();
        }

        if (!sceneStack.isEmpty()) {
            sceneStack.peek().init();
        }
    }

    // Get the current scene (top of the stack)
    public Scene getCurrentScene() {
        return sceneStack.isEmpty() ? null : sceneStack.peek();
    }

    // Update the current scene
    public void update(float deltaTime) {
        if (!sceneStack.isEmpty()) {
            sceneStack.peek().update(deltaTime);
        }
    }

    // Render the current scene
    public void render() {
        if (!sceneStack.isEmpty()) {
            sceneStack.peek().renderUI();
            sceneStack.peek().render();
        }
    }

}