package Engine.GUI;

import Engine.Component.*;
import Engine.Core.Console;
import Engine.Core.GameObject;
import Tools.ScriptLoader;
import imgui.ImGui;
import imgui.type.ImString;

import java.io.File;
import java.util.*;


public class Inspector {

    private static boolean showCompList = false;

    private final static List<Class<? extends Component>> BuiltInComponents = new ArrayList<>(){{
        add(Rigidbody2D.class);
    }};

    public static List<Class<? extends Component>> CreatedComponents = new ArrayList<>();

    private static Map<File, String> Compiled = new HashMap<>();
    public static Map<File, Component> CachedComponents = new HashMap<>();

    private static GameObject selected;

    public static void Render(GameObject sceneSelected){
        ImGui.begin("Inspector");

        selected = sceneSelected;

        if(selected != null){

            ImString nameBuffer = new ImString(selected.Name, 256);
            if (ImGui.inputText("Name", nameBuffer)) {
                selected.Name = nameBuffer.get();
            }
            for (int i = 0; i < selected.componentList.size(); i++) {
                Component c = selected.componentList.get(i);

                ImGui.separator();
                ImGui.text(c.getClass().getSimpleName());
                c.drawImGUI();

                ImGui.sameLine();
                if (!(c instanceof Transform) && !(c instanceof RenderComponent) && !(c instanceof Collider2D)) { // Optional: protect Transform from deletion
                    if (ImGui.button("Remove##" + i)) {
                        selected.componentList.remove(i);
                        i--;
                    }
                }
            }
            ImGui.newLine();
            if(ImGui.button("Add Components")){
                showCompList = !showCompList;
            }

            if(showCompList){
                float itemHeight = ImGui.getTextLineHeightWithSpacing();
                float listHeight = itemHeight * 2 * (BuiltInComponents.size() + CreatedComponents.size());
                ImGui.beginChild("Components",200, listHeight, true);

                for(Class<?extends Component> clas: BuiltInComponents){
                    String componentName = clas.getSimpleName();

                    if(ImGui.button(componentName)){
                        try{
                            Component componentInstance = clas.getDeclaredConstructor().newInstance();
                            selected.AddComponent(componentInstance);
                            showCompList = false;
                        }catch (Exception e){
                            Console.error("Could not get instance " + componentName + ": " + e);

                        }
                    }
                }
                File scriptsDir = new File("UserFiles/Scripts/");
                File[] scriptFiles = scriptsDir.listFiles((dir, name) -> name.endsWith(".java") || name.endsWith(".lua"));

                if (scriptFiles != null) {
                    for (File file : scriptFiles) {
                        try{
                            int dotIndex = file.getName().lastIndexOf(".");
                            String extension = file.getName().substring(dotIndex + 1);
                            String className = file.getName().substring(0, dotIndex);
                            if (extension.equals("java")) {
                                if (!Compiled.containsKey(file)) {
                                    Compiled.put(file, "not Compiled");
                                }

                                if (!Compiled.get(file).equals("Compiled")) {
                                    Component comp = (Component) Objects.requireNonNull(
                                            ScriptLoader.compileAndLoad(file.getAbsolutePath(), className, new Class[]{GameObject.class}, new GameObject[]{selected})
                                    );
                                    Compiled.put(file, "Compiled");

                                    CreatedComponents.add(comp.getClass());
                                    CachedComponents.put(file, comp);
                                }

                                if (ImGui.selectable(file.getName())) {
                                    Component instance;
                                    if (CachedComponents.containsKey(file)) {
                                        Component original = CachedComponents.get(file);
                                        instance = original.getClass().getDeclaredConstructor(GameObject.class)
                                                .newInstance(selected);
                                    } else {
                                        //safe reload if somehow not cached
                                        Component comp = (Component) Objects.requireNonNull(
                                                ScriptLoader.compileAndLoad(file.getAbsolutePath(), className, new Class[]{GameObject.class}, new GameObject[]{selected})
                                        );
                                        instance = comp;
                                    }

                                    selected.AddComponent(instance);
                                }
                            }
                        }catch (Exception e) {
                            Console.error(e.toString());
                        }

                    }
                }

                ImGui.endChild();

            }

        }
        ImGui.end();
    }

    public static Component GetCachedComponen(String componentName, GameObject gameObject) throws Exception {
        File scriptsDir = new File("UserFiles/Scripts/");
        File[] scriptFiles = scriptsDir.listFiles((dir, name) -> name.endsWith(".java") || name.endsWith(".lua"));

        if (scriptFiles != null) {
            for (File file : scriptFiles) {
                Component instance;
                int dotIndex = file.getName().lastIndexOf(".");
                String className = file.getName().substring(0, dotIndex);
                if(className.equalsIgnoreCase(componentName)){
                    if (CachedComponents.containsKey(file)) {
                        Component original = CachedComponents.get(file);
                        instance = original.getClass().getDeclaredConstructor(GameObject.class)
                                .newInstance(gameObject);
                    } else {
                        //safe reload if somehow not cached
                        Component comp = (Component) Objects.requireNonNull(
                                ScriptLoader.compileAndLoad(file.getAbsolutePath(), className, new Class[]{GameObject.class}, new GameObject[]{selected})
                        );
                        instance = comp;
                    }
                    return instance;
                }
            }
        }
        return null;
    }

}
