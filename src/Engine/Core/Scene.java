package Engine.Core;

import Engine.Component.*;
import Tools.Factory;
import Tools.RuntimeTypeAdapterFactory;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30C.*;

public abstract class Scene {

    public Camera2D camera;

    public static List<GameObject> sceneObjects = new ArrayList<>();
    public static List<GameObject> toAdd = new ArrayList<>();

    public FrameBuffer frameBuffer;

    public Window window;
    public static Renderer renderer;

    public abstract void init(); //Initialize the scene;
    public abstract void cleanup();
    public abstract void renderUI();

    public Scene(Window window) {
        this.window = window;
        renderer = new Renderer();
        frameBuffer = window.frameBuffer;
    }

    public void update(float deltaTime){
        for(GameObject obj : sceneObjects){
            if(obj.isActive)obj.Update(deltaTime);
        }

        if(!toAdd.isEmpty()){
            sceneObjects.addAll(toAdd);
            toAdd.clear();
        }
    }

    public void render(){
        frameBuffer.bind();
        renderer.render();
        checkAllCollisions();
        frameBuffer.Unbind();
    }

    public void add(GameObject object) {
        sceneObjects.add(object);
    }
    public void remove(GameObject object) {sceneObjects.remove(object); }

    private void checkAllCollisions() {
        Collider2D collider0 = null
                , collider1 = null;
        for (GameObject obj_0 : sceneObjects) {
            for (GameObject obj_1 : sceneObjects) {
                for(Component component : obj_0.componentList){
                    if(component instanceof Collider2D){
                        collider1 = (Collider2D)component;
                    }
                }
                for(Component component : obj_1.componentList){
                    if(component instanceof Collider2D){
                        collider0 = (Collider2D)component;
                    }
                }
                if (obj_0.equals(obj_1) || collider0 == null || collider1 == null) continue;
                if(collider0.isTrigger || collider1.isTrigger) continue;
                if(collider0 != collider1){
                    if(collider0.OnCollision(collider1)){
                        collider0.addCollisions(collider1);
                        collider1.addCollisions(collider0);
                    }
                }
            }
        }

    }

    public GameObject GetGameObject(String name){
        for(int i = 0; i < sceneObjects.size(); i++){
            if(sceneObjects.get(i).Name.equals(name)){
                return  sceneObjects.get(i);
            }
        }

        return null;
    }

    public Component FindComponentInGameObject(String name, Component component){
        for(int i = 0; i < sceneObjects.size(); i++){
            if(sceneObjects.get(i).Name.equals(name)){
                for(int j = 0; j < sceneObjects.get(i).componentList.size(); j++){
                    if(sceneObjects.get(i).componentList.get(j).equals(component)){
                        return sceneObjects.get(i).componentList.get(j);
                    }
                }
            }
        }

        return null;
    }
}
