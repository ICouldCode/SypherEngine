package Tools;

import CustomScripts.CameraControls;
import Engine.Component.*;
import Engine.Core.Console;
import Engine.Core.GameObject;
import Engine.Core.Renderer;
import Engine.Core.Scene;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class JSON {

    public static Gson gson = ReCompileGson();

    public static Gson ReCompileGson(){

        GsonBuilder builder = new GsonBuilder();

        RuntimeTypeAdapterFactory<Component> factory =
                RuntimeTypeAdapterFactory.of(Component.class, "Type")
                        //Base Engine
                        .registerSubtype(Transform.class, "Transform")
                        .registerSubtype(Rigidbody2D.class, "Rigidbody2D")
                        .registerSubtype(Collider2D.class, "Collider2D")
                        .registerSubtype(Camera2D.class, "Camera2D")
                        .registerSubtype(RenderComponent.class, "Render")
                        .registerSubtype(CameraControls.class, "CameraControls")
                ;

        for (Class<? extends Component> compClass : ComponentRegistration.getRegisteredComponents()) {
            factory.registerSubtype(compClass, compClass.getSimpleName());
        }

        builder.setPrettyPrinting();
        builder.registerTypeAdapterFactory(factory);
        builder.addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaredType() == GameObject.class;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });

        Gson g = builder.create();
        return g;
    }


    public static void WriteJSON(String path, List<GameObject> sceneObjects) {
        MainThread.invokeLater(() -> {

            try (Writer writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
                gson.toJson(sceneObjects, writer);
                Console.info("Scene Saved!");
            } catch (Throwable e) { // catch ALL possible errors, not just IOException
                e.printStackTrace();
                Console.error("Scene save failed: " + e.getMessage());
            }
        });
    }

    public static void LoadJSONScene(String path, Renderer renderer, Scene scene) {
        MainThread.invokeLater(() -> {
            try {
                Console.info("Reading file data...");
                String json = Files.readString(Paths.get(path));
                Type listType = new TypeToken<List<GameObject>>() {}.getType();
                List<GameObject> loadedObjects = gson.fromJson(json, listType);

                Console.info("Clearing render data...");
                for (GameObject obj : scene.sceneObjects) {
                    obj.CleanUp();
                    if (obj.render != null) renderer.remove(obj.render);
                    scene.remove(obj);
                }

                Console.info("Linking Objects...");
                for (GameObject obj : loadedObjects) {
                    for (Component component : obj.componentList) {
                        component.gameObject = obj;
                        component.start();
                    }

                    // Re-link render component
                    obj.render = obj.getComponent(RenderComponent.class);
                    if (obj.render != null) {
                        obj.render.gameObject = obj;
                        obj.render.start();
                        renderer.add(obj.render);
                    }
                    scene.sceneObjects.add(obj);

                    // Handle camera
                    if ("Main_Camera".equals(obj.Name)) {
                        Camera2D cam = obj.getComponent(Camera2D.class);
                        if (cam != null) {
                            scene.renderer.setCamera(cam);
                            cam.recalculateView();
                            Console.info("New camera set: " + cam);
                        } else {
                            Console.error("No Camera2D found on Main_Camera");
                        }

                        if (obj.getComponent(CameraControls.class) != null) {
                            Console.info("CameraMove component attached");
                        } else {
                            Console.error("CameraMove missing on Main_Camera");
                        }
                    }

                    Console.info(String.format("Loaded %d/%d...", loadedObjects.indexOf(obj) + 1, loadedObjects.size()));
                }

                Console.info("All objects loaded!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void UpdateGsonBuilder(){

    }
}
