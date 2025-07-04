package Engine.Core;

import static org.lwjgl.opengl.GL11.*;

public class VertexBufferElement {

    public final int type;
    public final int count;
    public final boolean normalized;

    public VertexBufferElement(int type, int count, boolean normalized) {
        this.type = type;
        this.count = count;
        this.normalized = normalized;
    }

    public static int getSizeOfType(int type) {
        switch(type) {
            case GL_FLOAT : return 4;
            case GL_UNSIGNED_INT: return 4;
            case GL_UNSIGNED_BYTE: return 1;
            default: throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
