package CustomScripts;

import Engine.Component.*;
import Engine.Core.*;
import Engine.Manager.*;
import Engine.GUI.*;
import com.google.gson.annotations.*;

import static org.lwjgl.glfw.GLFW.*;

public class CameraControls extends Component {

    public float panSpeed;
    Camera2D camera;
    Transform transform;

    public CameraControls(GameObject gameObject){
        this.gameObject = gameObject;
    }

    @Override
    public void start() {
        camera = this.gameObject.getComponent(Camera2D.class);
        transform = this.gameObject.getComponent(Transform.class);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if(ViewPort.focused){
            panSpeed = 300f * deltaTime;

            // Handle zooming using mouse scroll
            float scrollDelta = (float) InputManager.scrollYOffset * deltaTime;
            if (scrollDelta != 0.0f) {
                float zoomSpeed = 1.0f; // Adjust zoom speed if needed
                float newZoom = camera.zoom - scrollDelta * zoomSpeed;
                camera.setZoom(Math.max(0.1f, newZoom)); // Clamp zoom if needed
                InputManager.scrollYOffset = 0.0f; // Reset scroll value
            }

            // Moving the camera based on key presses
            if (InputManager.isKeyPressed(GLFW_KEY_W)) {
                camera.setPosition(transform.position.x, transform.position.y + panSpeed);
            }
            if (InputManager.isKeyPressed(GLFW_KEY_S)) {
                camera.setPosition(transform.position.x, transform.position.y - panSpeed);
            }
            if (InputManager.isKeyPressed(GLFW_KEY_A)) {
                camera.setPosition(transform.position.x - panSpeed, transform.position.y);
            }
            if (InputManager.isKeyPressed(GLFW_KEY_D)) {
                camera.setPosition(transform.position.x + panSpeed, transform.position.y);
            }
        }
    }
}
