package Engine.Component;

import Engine.Core.GameObject;
import org.joml.*;
import org.joml.Math;

public class Transform extends Component{
    public Vector3f position;
    public Vector2f scale;
    public Vector3f rotation; // degrees

    public Transform(GameObject gameObject) {
        this.position = new Vector3f(0f, 0f, 0f);
        this.scale = new Vector2f(1f, 1f);
        this.rotation = new Vector3f(0f, 0f, 0f);
        this.gameObject = gameObject;
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .translate(position) // 2D position
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale.x, scale.y, 1f); // 2D scale, z=1 to maintain depth
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void start() {

    }
}
