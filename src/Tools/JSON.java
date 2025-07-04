package Tools;

import CustomScripts.CameraControls;
import Engine.Component.Camera2D;
import Engine.Component.Component;
import Engine.Component.RenderComponent;
import Engine.Component.Transform;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JSON {

    private static Factory factory = new Factory();

    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(factory.factory)
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaredType() == GameObject.class;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    public static void WriteJSON(String path, List<GameObject> sceneObjects) {
        Thread thread = new Thread(() -> {

            try (Writer writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
                gson.toJson(sceneObjects, writer);
                Console.info("Scene Saved!");
            } catch (Throwable e) { // catch ALL possible errors, not just IOException
                e.printStackTrace();
                Console.error("Scene save failed: " + e.getMessage());
            }
        });

        // Catch things like StackOverflowError that wouldn't show otherwise
        thread.setUncaughtExceptionHandler((t, e) -> {
            Console.error("Uncaught error during scene save: " + e);
            e.printStackTrace();
        });

        thread.start();
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


}
