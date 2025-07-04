package Engine.GUI;

import Engine.Core.Window;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.*;
import imgui.gl3.*;
import imgui.glfw.*;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class ImGUILayer {

    private final Window glfwWindow;
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public ImGuiIO io;

    public ImGUILayer(Window windowHandle) {
        this.glfwWindow = windowHandle;
    }

    public void init() {
        ImGui.createContext();
        io = ImGui.getIO();

        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);

        io.getFonts().addFontDefault();
        ImFontAtlas fontAtlas = io.getFonts();
        fontAtlas.build();

        imGuiGlfw.init(glfwWindow.window, true);
        imGuiGl3.init("#version 330 core");
        ImGui.init();
        ImGui.getIO().setDisplaySize(glfwWindow.width, glfwWindow.height);
    }


    public void startFrame() {
        glfwMakeContextCurrent(glfwWindow.window);
        imGuiGlfw.newFrame();
        imGuiGl3.newFrame();
        ImGui.newFrame();
    }

    public void endFrame() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
        ImGui.renderPlatformWindowsDefault();
        ImGui.updatePlatformWindows();
    }

    public void dispose() {
        imGuiGl3.destroyDeviceObjects();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }
}
