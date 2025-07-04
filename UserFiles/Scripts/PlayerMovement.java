import Engine.Component.*;
import Engine.Core.*;
import Engine.GUI.*;
import Engine.Manager.*;
import com.google.gson.annotations.Expose;
import Scenes.Editor;


import static org.lwjgl.glfw.GLFW.*;

public class PlayerMovement extends Component {

    public float basespeed = 500;

    public PlayerMovement(GameObject gameObject){
        this.gameObject = gameObject;
    }

    @Override
    public void start() {
    }

    @Override
    public void update(float deltaTime) {
        float speed = basespeed * deltaTime;

        if(gameObject.getComponent(Collider2D.class) != null){
            if(InputManager.isKeyPressed(GLFW_KEY_D)){
                gameObject.getComponent(Transform.class).position.x += speed;
            }
            if(InputManager.isKeyPressed(GLFW_KEY_A)){
                gameObject.getComponent(Transform.class).position.x -= speed;
            }

            if(InputManager.isKeyJustPressed(GLFW_KEY_N)){
                GameObject obj = Editor.CreateGameObject("Bullet", "D:/Java2DEngineV2/SypherEngine/UserFiles/Textures/zelda.jpg");
                obj.getComponent(Transform.class).position.x = gameObject.getComponent(Transform.class).position.x;
                obj.getComponent(Transform.class).position.y = gameObject.getComponent(Transform.class).position.y + 100;
                try{
                    obj.AddCustomComponent("Shoot", obj);
                } catch (java.lang.Exception e) {
                    Console.error(e.toString());
                }
            }
        }
    }
}
