package Engine.Core;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;

public class VBO {

    private int vboID;

    public VBO(float[] vertices) {
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(buffer);
    }

    public void Bind() {
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
    }

    public void UnBind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void Delete() {
        glDeleteBuffers(vboID);
    }
}