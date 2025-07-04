package Engine.Component;

import Engine.Core.GameObject;
import com.google.gson.annotations.Expose;
import org.joml.Vector3f;

public class Rigidbody2D extends Component{

    public Vector3f velocity;
    public Vector3f acceleration;
    public float mass;
    public boolean isStatic;

    public float Gravity = -1000;
    public boolean useGravity = false;

    public Rigidbody2D(){
        velocity = new Vector3f();
        acceleration = new Vector3f();
        mass = 1;
        isStatic = false;
    }

    @Override
    public void start() {
    }

    @Override
    public void update(float deltaTime) {
        if (isStatic) return;

        if(gameObject.getComponent(Collider2D.class).BottomCollision && useGravity){
            velocity.y += Gravity * deltaTime;
        }else if(velocity.y < 0){
            velocity.y = 0;
        }


        velocity.fma(deltaTime, acceleration); // velocity += acceleration * dt
        gameObject.getComponent(Transform.class).position.fma(deltaTime,velocity);     // position += velocity * dt
        acceleration.zero(); // reset after applying
    }

    public Rigidbody2D(float mass, GameObject gameObject) {
        this.velocity = new Vector3f(0, 0,0);
        this.acceleration = new Vector3f(0, 0,0);
        this.mass = mass;
        this.isStatic = mass <= 0;
        this.gameObject = gameObject;
    }

    public void applyForce(Vector3f force) {
        if (!isStatic) {
            Vector3f deltaAcc = new Vector3f(force).div(mass);
            acceleration.add(deltaAcc);
        }
    }
}
