package Engine.Component;

import Engine.Core.Console;
import Engine.Core.GameObject;
import Scenes.Editor;
import org.joml.Vector2f;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;


import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Collider2D extends Component{

    public transient List<Collider2D> Collisions = new ArrayList<>();

    public float x, y, Width, Height;
    public boolean LeftCollision, RightCollision, TopCollision, BottomCollision;

    public float thisLeft;
    public float thisRight;
    public float thisTop;
    public float thisBottom;

    public boolean isTrigger = false;

    public Collider2D(float x, float y, String path, GameObject gameObject) {
        this.x = x;
        this.y = y;
        LeftCollision = true;
        RightCollision = true;
        TopCollision = true;
        BottomCollision = true;
        getImageSize(path);
        this.gameObject = gameObject;
    }

    @Override
    public void start(){
        if(Collisions == null) {Collisions = new ArrayList<>();}
    }


    public boolean OnCollision(Collider2D other) {
        // Scale this collider's dimensions
        float thisScaledWidth = this.Width * gameObject.getComponent(Transform.class).scale.x;
        float thisScaledHeight = this.Height * gameObject.getComponent(Transform.class).scale.y;

        // Calculate this collider's bounding box
        float thisLeft = this.x - thisScaledWidth / 2f;
        float thisRight = this.x + thisScaledWidth / 2f;
        float thisTop = this.y + thisScaledHeight / 2f;
        float thisBottom = this.y - thisScaledHeight / 2f;

        // Scale other collider's dimensions
        float otherScaledWidth = other.Width * other.gameObject.getComponent(Transform.class).scale.x;
        float otherScaledHeight = other.Height * other.gameObject.getComponent(Transform.class).scale.y;

        // Calculate other collider's bounding box
        float otherLeft = other.x - otherScaledWidth / 2f;
        float otherRight = other.x + otherScaledWidth / 2f;
        float otherTop = other.y + otherScaledHeight / 2f;
        float otherBottom = other.y - otherScaledHeight / 2f;

        // Check for overlap
        return thisRight > otherLeft &&
                thisLeft < otherRight &&
                thisTop > otherBottom &&
                thisBottom < otherTop;
    }

    public boolean contains(Vector2f point) {
        Transform transform = gameObject.getComponent(Transform.class);
        Vector2f scale = transform.scale;

        // Calculate half size with scaling
        Vector2f halfSize = new Vector2f(Width, Height).mul(scale).mul(0.5f);

        // Get bounds of the object in world space
        float left = transform.position.x - halfSize.x;
        float right = transform.position.x + halfSize.x;
        float bottom = transform.position.y - halfSize.y;
        float top = transform.position.y + halfSize.y;

        // Check if the point (cursor) is inside the bounds of the object
        return point.x >= left && point.x <= right &&
                point.y >= bottom && point.y <= top;
    }

    public void handleCanMove() {
        // Reset movement flags
        LeftCollision = true;
        RightCollision = true;
        TopCollision = true;
        BottomCollision = true;

        for (Collider2D other : Collisions) {
            if (other.isTrigger) continue;

            // Apply scale to the current gameobject's dimensions
            float thisScaledWidth = this.Width * gameObject.getComponent(Transform.class).scale.x;
            float thisScaledHeight = this.Height * gameObject.getComponent(Transform.class).scale.y;

            // Get the bounding box of the current gameobject
            thisLeft = this.x - thisScaledWidth / 2f;
            thisRight = this.x + thisScaledWidth / 2f;
            thisTop = this.y + thisScaledHeight / 2f;
            thisBottom = this.y - thisScaledHeight / 2f;

            // Apply scale to the other gameobject's dimensions
            float otherScaledWidth = other.Width * other.gameObject.getComponent(Transform.class).scale.x;
            float otherScaledHeight = other.Height * other.gameObject.getComponent(Transform.class).scale.y;

            // Get the bounding box of the other collider
            float otherLeft = other.x - otherScaledWidth / 2f;
            float otherRight = other.x + otherScaledWidth / 2f;
            float otherTop = other.y + otherScaledHeight / 2f;
            float otherBottom = other.y - otherScaledHeight / 2f;

            // Check overlap
            float overlapX = Math.min(thisRight, otherRight) - Math.max(thisLeft, otherLeft);
            float overlapY = Math.min(thisTop, otherTop) - Math.max(thisBottom, otherBottom);

            if (overlapX > 0 && overlapY > 0) {
                // Determine the axis with minimum overlap
                if (overlapX < overlapY) {
                    // Block left or right movement
                    if (this.x < other.x) {
                        RightCollision = false;
                    } else {
                        LeftCollision = false;
                    }
                } else {
                    // Block up or down movement
                    if (this.y < other.y) {
                        TopCollision = false;
                    } else {
                        BottomCollision = false;
                    }
                }
            }
        }
    }

    public void getImageSize(String imagePath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer image = STBImage.stbi_load(imagePath, width, height, channels, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            STBImage.stbi_image_free(image);

            // Convert pixels to world units
            float worldWidth = width.get(0);
            float worldHeight = height.get(0);

            this.Width = worldWidth;
            this.Height = worldHeight;
        }
    }

    public void setPosition(Vector2f newPos) {
        this.x = newPos.x;
        this.y = newPos.y;
    }

    public void addCollisions(Collider2D collider2D){
        if(!Collisions.contains(collider2D) && !collider2D.isTrigger){
            Collisions.add(collider2D);
        }
    }

    private void handleCollisions(){
        Iterator<Collider2D> iterator = Collisions.iterator();
        while (iterator.hasNext()){
            Collider2D collider2D = iterator.next();
            if(!OnCollision(collider2D)){
                iterator.remove();
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        setPosition(new Vector2f(gameObject.getComponent(Transform.class).position.x, gameObject.getComponent(Transform.class).position.y));
        handleCanMove();
        handleCollisions();
    }

}
