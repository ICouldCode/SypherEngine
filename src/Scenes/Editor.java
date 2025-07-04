package Scenes;

import CustomScripts.*;
import Engine.Component.*;
import Engine.Core.*;
import Engine.GUI.*;

public class Editor extends Scene {
    public GameObject selected = null;

    public GameObject mainCamera;

    public Editor(Window window) {
        super(window);
    }

    @Override
    public void init() {
        // Only create a new camera if JSON loading didn't already add one
        mainCamera = findGameObjectByName("Main_Camera");
        if (mainCamera == null) {
            mainCamera = new GameObject();
            mainCamera.init(renderer, null);
            mainCamera.Name = "Main_Camera";
            add(mainCamera);
            Camera2D camera = new Camera2D(window.width, window.height);
            mainCamera.AddComponent(camera);
            mainCamera.AddComponent(new CameraControls(mainCamera));
            renderer.setCamera(camera);
        } else {
            // Optionally, reattach camera controls if needed
            if (mainCamera.getComponent(CameraControls.class) == null) {
                mainCamera.AddComponent(new CameraControls(mainCamera));
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        ViewPort.Update(this);
        FileManager.updateFiles();
    }

    @Override
    public void cleanup() {
        for(GameObject object : sceneObjects){
            object.CleanUp();
        }

        sceneObjects.clear();
    }

    @Override
    public void renderUI() {
        Docking.Render();
        ViewPort.Render(this);
        MenuBar.Render(this, "JSON/Scene/Scene.json");
        Inspector.Render(selected);
        ConsolePanel.Render();
        SceneObjects.Render(sceneObjects, this);
        FileManager.Render(this);
        AIAssistSubWindow.Render(this);
        ScriptEditor.Render(this);
    }


    public static GameObject CreateGameObject(String name, String path){
        Console.info("Created " + name);
        GameObject gameObject = new GameObject();
        gameObject.init(renderer, path);
        gameObject.render.start();
        gameObject.AddComponent(new Collider2D(gameObject.getComponent(Transform.class).position.x,gameObject.getComponent(Transform.class).position.y ,path, gameObject));
        gameObject.Name = name;
        toAdd.add(gameObject);

        return gameObject;
    }

    public void setSelected(GameObject selected){
        this.selected = selected;
    }

    public GameObject findGameObjectByName(String name) {
        for (GameObject obj : sceneObjects) {
            if (obj.Name != null && obj.Name.equals(name)) {
                return obj;
            }
        }
        return null;
    }

    public void removeGameObject(){
        if(selected != null){
            Console.info(selected.Name + " was deleted");
            selected.CleanUp();
            renderer.remove(selected.render);
            remove(selected);
            selected = null;
        }
    }
}

