package Engine.GUI;

import CustomScripts.CameraControls;
import Engine.Component.Camera2D;
import Engine.Component.Collider2D;
import Engine.Component.Transform;
import Engine.Core.Console;
import Engine.Core.GameObject;
import Engine.Core.Scene;
import Scenes.Editor;
import Tools.JSON;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static Engine.Core.Scene.renderer;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class MenuBar {

    public static boolean enableAIMenu = false;
    private static boolean enableCreateScriptWindow = false;
    private static final ImString newFileName = new ImString(256);


    public static void Render(Editor scene, String path){
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Exit")) {
                    glfwSetWindowShouldClose(scene.window.window, true);
                }
                if(ImGui.menuItem("Save")){
                    JSON.WriteJSON(String.format("JSON/Scene/%s", "Scene" + ".json"), scene.sceneObjects);
                }
                if(ImGui.menuItem("Load")){
                    if(scene.sceneObjects.size() == 1){
                        scene.cleanup();
                        JSON.LoadJSONScene(String.format(String.valueOf(path)), renderer, scene);
                    }
                    else{
                        Console.error("You must delete all game-objects to load a scene.(Excluding Main_Camera)");
                    }
                }
                if(ImGui.menuItem("Export")){
                    Console.info("Exporting scene...");
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Scripts")) {
                if (ImGui.menuItem("Java Script")) {
                    enableCreateScriptWindow = true;
                    newFileName.set("");
                }
                if (ImGui.menuItem("Lua Script")) {
                    Console.warn("Still in development");
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("2DGameObjects")) {
                if(ImGui.menuItem("2D Camera")){
                    GameObject mainCamera = new GameObject();
                    mainCamera.init(renderer, null);
                    mainCamera.Name = "Main_Camera";
                    scene.add(mainCamera);
                    Camera2D camera = new Camera2D(scene.window.width, scene.window.height);
                    mainCamera.AddComponent(camera);
                    mainCamera.AddComponent(new CameraControls(mainCamera));
                    renderer.setCamera(camera);
                }
                if (ImGui.menuItem("Square")) {
                    Console.info("Created Square");
                    GameObject Square = new GameObject();
                    Square.init(renderer, "UserFiles\\Textures\\Square.png");
                    Square.render.start();
                    Square.AddComponent(new Collider2D(Square.getComponent(Transform.class).position.x,Square.getComponent(Transform.class).position.y ,"UserFiles\\Textures\\Square.png", Square));
                    Square.Name = "Square";
                    scene.add(Square);
                }
                if (ImGui.menuItem("Empty GameObject")) {
                    Console.info("Created Empty object");
                    GameObject Empty = new GameObject();
                    scene.add(Empty);
                    Empty.init(renderer, null);
                    Empty.Name = "GameObject";
                }
                ImGui.endMenu();
            }
            //AI assistant system (Improve later)
            /*if (ImGui.beginMenu("Help")){
                if(ImGui.menuItem("AI")){
                    enableAIMenu = !enableAIMenu;
                    Console.info("AI assistance enabled: " + enableAIMenu);
                }
                ImGui.endMenu();
            }*/
            ImGui.endMenuBar();
        }

        if(enableCreateScriptWindow){
            ImGui.begin("Create New Script", ImGuiWindowFlags.AlwaysAutoResize);

                ImGui.text("Enter file name:");
                ImGui.inputText("###filename", newFileName, ImGuiInputTextFlags.AutoSelectAll);

                if(ImGui.button("Create")){
                    String fileName = newFileName.get().trim();

                    if(!fileName.isEmpty()){
                        Path newFile = Paths.get("UserFiles/Scripts/"+fileName+".java");
                        try {
                            Files.createDirectories(newFile.getParent());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if(!Files.exists(newFile)){
                            try {
                                Files.writeString(newFile, "// New Script\n\n" +
                                        "import Engine.Component.*;\n" +
                                        "import Engine.Core.*;\n" +
                                        "import Engine.GUI.*;\n" +
                                        "import Engine.Manager.*;\n" +
                                        "import com.google.gson.annotations.Expose;\n" +
                                        "import Scenes.Editor;\n"+
                                        "\n" +
                                        "\n" +
                                        "import static org.lwjgl.glfw.GLFW.*;\n"+
                                        "public class " + fileName + " extends Component {\n\n" +
                                        "    public " + fileName + "(GameObject gameObject) {\n" +
                                        "           this.gameObject = gameObject;"+
                                        "        // Constructor\n" +
                                        "    }\n\n" +
                                        "    @Override\n" +
                                        "    public void start() {\n" +
                                        "        // Called when component is added\n" +
                                        "    }\n\n" +
                                        "    @Override\n" +
                                        "    public void update(float deltaTime) {\n" +
                                        "        // Called every frame\n" +
                                        "    }\n" +
                                        "}");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            Console.info("File Created!");
                        }
                        enableCreateScriptWindow = false;
                    }
                }

            ImGui.end();
        }

        ImGui.end();
    }
}
