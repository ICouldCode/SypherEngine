package Engine.GUI;

import Engine.Core.Scene;
import Tools.PythonRunner;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AIAssistSubWindow {

    private static List<String> Messages = new ArrayList<>();
    private static final int MaxLength = 100;
    private static final int INPUT_BUFFER_SIZE = 256;
    private static final ImString inputBuffer = new ImString(INPUT_BUFFER_SIZE);
    private static boolean canScroll = false;
    private static PythonRunner py = new PythonRunner();

    public static void Render(Scene scene){

        if(scene != null) return;

        if(MenuBar.enableAIMenu){
            ImGui.begin("AI");

            ImGui.beginChild("TextOutput", 0, -ImGui.getFrameHeightWithSpacing(), false, ImGuiWindowFlags.HorizontalScrollbar);

            for(String message : Messages){
                ImGui.textWrapped(message);
            }

            if(canScroll){
                ImGui.setScrollHereY(1.0f);
                canScroll = false;
            }
            ImGui.endChild();

            ImGui.pushItemWidth(-1);
            if (ImGui.inputText("##Input", inputBuffer, ImGuiInputTextFlags.EnterReturnsTrue)) {
                String text = "User: " +inputBuffer.get();
                String[] splitText = text.split(" ");
                if(splitText.length > 1){
                    if (!splitText[1].isEmpty()) {
                        Messages.add(text);
                        if (Messages.size() > MaxLength) {
                            Messages.remove(0);
                        }
                        inputBuffer.set("");
                        canScroll = true;
                    }

                    Messages.add(PythonRunner.TalkToPythonAI(splitText[1]));
                }
            }

            ImGui.end();
        }
    }

    public static void AddMessage(String message){
        Messages.add(message);
    }
}
