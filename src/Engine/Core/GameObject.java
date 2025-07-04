package Engine.Core;

import Engine.Component.Component;
import Engine.Component.RenderComponent;
import Engine.Component.Transform;
import Engine.GUI.Inspector;
import Tools.ScriptLoader;
import imgui.ImGui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameObject {

    public String Name = "GameObject";

    public List<GameObject> subObjects = new ArrayList<>();
    public List<Component> componentList = new ArrayList<>();
    public transient RenderComponent render;

    public String path;
    public boolean isActive = true;

    public void init(Renderer renderer, String path) {
        componentList.add(new Transform(this));

        if (path != null) {
            this.path = path;
            render = new RenderComponent(path, this);
            renderer.add(render);
            componentList.add(render);
        }
    }

    public void Update(float deltaTime){
        for (Component component : componentList) {
            if(component.isActive)component.update(deltaTime);
        }
    }

    public void AddComponent(Component component){
        component.gameObject = this;
        componentList.add(component);
        component.start();
    }

    public void AddCustomComponent(String name, GameObject gameObject) throws Exception {
        this.AddComponent(Objects.requireNonNull(Inspector.GetCachedComponen(name, gameObject)));
    }

    public void RemoveComponent(Component component){
        componentList.remove(component);
    }

    public <T extends  Component> T getComponent(Class<T> componentClass){
        for(Component c : componentList){
            if(componentClass.isInstance(c)){
                return componentClass.cast(c);
            }
        }
        return  null;
    }

    public void CleanUp(){
        // Cleanup each component
        for (Component component : componentList) {
            if (component.isActive) {
                component.cleanup();
            }
        }
        // Additional cleanup logic (e.g., for the render component)
        if (render != null) {
            render.destroyed = true;
            render.Delete(); // Ensure RenderComponent is also cleaned up
        }
    }
}

