package Engine.GUI;

import Engine.Core.Console;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class ConsolePanel {

    public static void Render(){
        ImGui.begin("Console", ImGuiWindowFlags.AlwaysAutoResize);

        if (ImGui.button("Clear")) {
            Console.clear();
        }

        ImGui.separator();

        ImGui.beginChild("ConsoleScroll", 0, 200, true);

        for (String log : Console.getLogs()) {
            if (log.contains("[ERROR]")) {
                ImGui.textColored(1.0f, 0.4f, 0.4f, 1.0f, log); // Red-ish for error
            } else if (log.contains("[WARNING]")) {
                ImGui.textColored(1.0f, 1.0f, 0.4f, 1.0f, log); // Yellow-ish for warning
            } else if (log.contains("[DEBUG]")) {
                ImGui.textColored(0.4f, 0.8f, 1.0f, 1.0f, log); // Cyan-ish for debug
            } else {
                ImGui.text(log);
            }
        }

        ImGui.endChild();
        ImGui.end();
    }
}
