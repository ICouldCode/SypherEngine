package Tools;
import CustomScripts.CameraControls;
import Engine.Component.*;

public class Factory {

    public RuntimeTypeAdapterFactory<Component> factory =
            RuntimeTypeAdapterFactory.of(Component.class, "Type")
                    //Base Engine
                    .registerSubtype(Transform.class, "Transform")
                    .registerSubtype(Rigidbody2D.class, "Rigidbody2D")
                    .registerSubtype(Collider2D.class, "Collider2D")
                    .registerSubtype(Camera2D.class, "Camera2D")
                    .registerSubtype(RenderComponent.class, "Render")
                    .registerSubtype(CameraControls.class, "CameraControls")
            ;
}
