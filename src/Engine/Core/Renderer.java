package Engine.Core;

import Engine.Component.Camera2D;
import Engine.Component.RenderComponent;

import java.util.ArrayList;
import java.util.List;


public class Renderer {
    public final List<RenderComponent> renderQueue = new ArrayList<>();

    public Camera2D camera;

    public void add(RenderComponent renderComponent){
        renderQueue.add(renderComponent);
    }

    public void remove(RenderComponent renderComponent){
        renderQueue.remove(renderComponent);
    }

    public void render(){
        for(RenderComponent render : renderQueue){
            if(!render.destroyed){
                render.Draw(camera.getViewProjection());
            }
            else {
                renderQueue.remove(render);
            }
        }

    }

    public void setCamera(Camera2D camera) {
        this.camera = camera;
    }
}
