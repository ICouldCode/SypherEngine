package Engine.GUI;

import Engine.Component.Component;
import Engine.Core.Console;
import Engine.Core.GameObject;
import Scenes.Editor;
import Tools.ComponentRegistration;
import Tools.JSON;
import Tools.ScriptLoader;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import imgui.ImGui;
import imgui.type.ImString;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ScriptEditor {

    public static boolean isFocused = false;

    public static ImString currentScriptText = new ImString(32 * 1024);
    public static String currentScriptPath = null;
    public static String currentScriptFileName = "No File Opened";

    public static void openScript(String path) {
        try {
            currentScriptText.set(Files.readString(Paths.get(path)));
            currentScriptPath = path;
            currentScriptFileName = new File(path).getName();
            Console.info("Opened script: " + currentScriptFileName);
        } catch (IOException e) {
            Console.error("Failed to open script: " + e.getMessage());
        }
    }

    public static void Render(Editor scene){
        ImGui.begin("Editor");

        ImGui.inputTextMultiline("##script", currentScriptText, ImGui.getContentRegionAvailX(), ImGui.getWindowHeight() - 80.0f, 0, null);

        if (ImGui.button("Save") && currentScriptPath != null) {
            try {
                // 1. Save the script file
                Files.writeString(Paths.get(currentScriptPath), currentScriptText.get());
                Console.info("Saved script: " + currentScriptFileName);

                // 2. Extract class name from filename
                int dotIndex = currentScriptFileName.lastIndexOf(".");
                if (dotIndex == -1) {
                    Console.error("Invalid file name: " + currentScriptFileName);
                    return;
                }

                String className = currentScriptFileName.substring(0, dotIndex);

                // 3. Compile and load class
                String engineClasspath = ScriptLoader.buildClasspath("out/production/SypherEngine/", "libs/");
                Class<?> compiledClass = ScriptLoader.compileAndReturnClass(currentScriptPath, className, engineClasspath);

                if (compiledClass == null) {
                    Console.error("Failed to compile and load class: " + className);
                    return;
                }

                // 4. Update all GameObjects using this component
                for (GameObject go : scene.sceneObjects) {
                    List<Component> toRemove = new ArrayList<>();
                    List<Component> toAdd = new ArrayList<>();

                    for (Component comp : go.componentList) {
                        if (comp.getClass().getSimpleName().equals(className)) {
                            // Schedule old component for removal
                            toRemove.add(comp);

                            // Create new instance of recompiled class
                            //Add more class types
                            Constructor<?> ctor = compiledClass.getConstructor(GameObject.class);
                            Component newComp = (Component) ctor.newInstance(go);
                            toAdd.add(newComp);

                            Console.info("Hot-swapped " + className + " on " + go.Name);
                        }
                    }

                    // Actually update the component list
                    for (Component oldComp : toRemove) go.RemoveComponent(oldComp);
                    for (Component newComp : toAdd) go.AddComponent(newComp);
                }

            } catch (Exception e) {
                Console.error("Save or Reload Failed: " + e.getMessage());
            }
        }
        isFocused = ImGui.isWindowFocused();
        ImGui.end();
    }
}
