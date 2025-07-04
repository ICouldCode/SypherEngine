package Engine.Component;

import Engine.Core.GameObject;
import com.google.gson.annotations.Expose;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class Camera2D extends Component{
    public float zoom;
    public float nearPlane;
    private final float farPlane;
    private final Matrix4f projection;
    private final Matrix4f view;
    private float width, height;

    public Camera2D(float width, float height) {
        this.width = width;
        this.height = height;
        this.zoom = 1.0f;
        this.nearPlane = -1f;
        this.farPlane = 1f;
        this.gameObject = gameObject;
        this.projection = new Matrix4f();
        this.view = new Matrix4f();
    }

    public void setPosition(float x, float y) {
        gameObject.getComponent(Transform.class).position.set((x == 0) ? gameObject.getComponent(Transform.class).position.x : x,(y == 0) ? gameObject.getComponent(Transform.class).position.y : y, gameObject.getComponent(Transform.class).position.z);
        recalculateView();
    }

    public void setZoom(float zoom) {
        this.zoom = Math.max(0.1f, Math.min(zoom, 10.0f));
        setProjection(width, height);  // Recalculate projection when zoom changes
    }

    @Override
    public void start(){
        setProjection(width, height); // Set the initial projection matrix
        recalculateView();
    }

    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
    }

    public void setProjection(float actualWidth, float actualHeight) {
        this.width = actualWidth;
        this.height = actualHeight;

        float halfWidth = (actualWidth * 0.5f) / zoom;
        float halfHeight = (actualHeight * 0.5f) / zoom;

        projection.identity().ortho(
                -halfWidth, halfWidth,
                -halfHeight, halfHeight,
                nearPlane, farPlane
        );
    }

    public void recalculateView() {
        view.identity()
                .translate(-gameObject.getComponent(Transform.class).position.x, -gameObject.getComponent(Transform.class).position.y, 0f) // Camera position
                .rotateZ((float) Math.toRadians(gameObject.getComponent(Transform.class).rotation.z)); // Rotate around Z (2D)
    }

    public Matrix4f getViewProjection() {
        return new Matrix4f(projection).mul(view); // proj * view
    }

    public Vector2f screenToWorld(double mouseX, double mouseY, float viewportWidth, float viewportHeight) {
        float x = (2.0f * (float) (mouseX)) / viewportWidth - 1.0f;
        float y = 1.0f - (2.0f * (float) (mouseY)) / viewportHeight;

        Vector4f clipCoords = new Vector4f(x, y, 0f, 1.0f);

        Matrix4f viewProjection = new Matrix4f();
        projection.mul(view, viewProjection); // P * V

        Matrix4f inverseVP = viewProjection.invert();

        Vector4f worldPos = clipCoords.mulProject(inverseVP);
        return new Vector2f(worldPos.x, worldPos.y);
    }

    public void follow(float x, float y) {
        Vector2f tmp = new Vector2f(x, y);
        setPosition(tmp.x, tmp.y);
    }
}
