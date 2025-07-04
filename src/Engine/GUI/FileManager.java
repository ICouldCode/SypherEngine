package Engine.GUI;

import Engine.Component.Component;
import Engine.Core.Console;
import Engine.Core.GameObject;
import Engine.Core.Scene;
import Scenes.Editor;
import Tools.ScriptLoader;
import imgui.ImGui;

import java.io.File;
import java.util.*;

public class FileManager {
    // Keep CurrentDirectory and fileList static so their state persists across frames
    private static File CurrentDirectory;
    private static List<File> fileList = new ArrayList<>();
    private static ScriptLoader loader = new ScriptLoader();

    // Call this once at start (or the first Render call)
    private static boolean initialized = false;

    public static void Render(Editor scene) {
        if (!initialized) {

            CurrentDirectory = new File(System.getProperty("user.dir") + "\\UserFiles");

            updateFiles();
            initialized = true;
        }

        ImGui.begin("Files");

        // Show current directory
        ImGui.text("Current: " + CurrentDirectory.getAbsolutePath());

        // Up button
        if (ImGui.button(".. (Up)")) {
            File parent = CurrentDirectory.getParentFile();
            if (parent != null && parent.exists()) {
                CurrentDirectory = parent;
                updateFiles();
            }
        }

        // Iterate over a copy of fileList to avoid concurrency issues if updating while iterating
        List<File> filesCopy = new ArrayList<>(fileList);

        for (File file : filesCopy) {
            if (file.isDirectory()) {
                String treeNodeId = file.getName() + "_" + file.hashCode();
                String label = "[DIR] " + file.getName();

                if (ImGui.treeNode(treeNodeId, label)) {
                    if (ImGui.isItemClicked()) {
                        CurrentDirectory = file;
                        updateFiles();
                        ImGui.treePop();
                        break;  // exit loop because fileList changed
                    }
                    ImGui.treePop();
                }
            } else {
                if (ImGui.selectable(file.getName())) {
                    int dotIndex = file.getName().lastIndexOf(".");
                    String extension = file.getName().substring(dotIndex + 1);
                    if(scene.selected != null && !scene.selected.Name.equals("Main_Camera")){
                        switch(extension){
                            case "png":
                            case "jpg":
                                scene.selected.render.textureHandler.remove(0);
                                scene.selected.render.addTexture(file.getAbsolutePath());
                                scene.selected.path = file.getAbsolutePath();
                                Console.info("Loaded Texture");
                                break;
                        }
                    }
                    switch(extension){
                        case "lua":
                            Console.warn("Still in development");
                            break;
                        case "java":
                            ScriptEditor.openScript(file.getAbsolutePath());
                            break;
                    }
                }
            }
        }

        ImGui.end();
    }

    public static void updateFiles() {
        fileList.clear();
        File[] files = CurrentDirectory.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            fileList.addAll(Arrays.asList(files));
        }
    }

}
