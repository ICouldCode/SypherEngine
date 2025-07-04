// New Script

import Engine.Component.*;
import Engine.Core.*;
import Engine.GUI.*;
import Engine.Manager.*;
import com.google.gson.annotations.Expose;


import static org.lwjgl.glfw.GLFW.*;
public class Test extends Component {
  
    public Test(GameObject gameObject) {
           this.gameObject = gameObject;        // Constructor
    }

    @Override
    public void start() {
        // Called when component is added
    }

    @Override
    public void update(float deltaTime) {
        // Called every frame
     
    }
}