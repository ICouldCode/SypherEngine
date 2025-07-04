// New Script

import Engine.Component.*;
import Engine.Core.*;
import Engine.GUI.*;
import Engine.Manager.*;
import com.google.gson.annotations.Expose;
import Scenes.Editor;


import static org.lwjgl.glfw.GLFW.*;
public class Shoot extends Component {

    public float speed = 125;

    public Shoot(GameObject gameObject) {
           this.gameObject = gameObject;        // Constructor
    }

    @Override
    public void start() {
        // Called when component is added
    }

    @Override
    public void update(float deltaTime) {
        // Called every frame
        gameObject.getComponent(Transform.class).position.y += speed * deltaTime;
    }
}