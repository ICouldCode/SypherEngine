package Engine.GUI;

import Engine.Component.Component;
import Engine.Core.GameObject;
import Engine.Core.Scene;
import Scenes.Editor;
import imgui.ImGui;

import java.util.List;

public class SceneObjects {

    public static void Render(List<GameObject> objList, Editor scene){

        ImGui.begin("Scene");

        int objId = 0;

        for (GameObject obj : objList) {
            String headerLabel = obj.Name + "##" + objId;
            if (ImGui.collapsingHeader(headerLabel)) {

                boolean isParentSelected = scene.selected == obj;
                if (ImGui.selectable(obj.Name + "##" + objId + "_p", isParentSelected)) {
                    scene.setSelected(obj);
                }

                int subId = 0;
                for (GameObject subOb : obj.subObjects) {
                    ImGui.indent();
                    boolean isSubSelected = scene.selected == subOb;
                    if (ImGui.selectable(subOb.Name + "##" + objId + "_s" + subId, isSubSelected)) {
                        scene.setSelected(subOb);
                    }
                    ImGui.unindent();
                    subId++;
                }
            }
            objId++;
        }

        ImGui.end();
    }
}
