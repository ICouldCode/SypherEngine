package Engine.Core;

import static org.lwjgl.opengl.GL30.*;

import java.util.List;

public class VAO {

    private int vaoID;
    private int vertexCount;

    private VBO vbo;
    private VertexBufferLayout layout;

    public void LoadObject(float[] vertices) {
        vbo = new VBO(vertices);

        vertexCount = vertices.length / 8;
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);
        vbo.Bind();

        layout = new VertexBufferLayout();
        layout.pushFloat(3); //Vector3 Position
        layout.pushFloat(3); //RGB Color
        layout.pushFloat(2); //Tex Coords

        List<VertexBufferElement> elements = layout.getElements();
        for(int i = 0; i < elements.size(); i++) {
            VertexBufferElement e = elements.get(i);
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, e.count, e.type, e.normalized, layout.getStride(), layout.getOffset());
            layout.setOffset(layout.getOffset() + e.count * VertexBufferElement.getSizeOfType(e.type));
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void Bind() {
        glBindVertexArray(vaoID);
    }

    public void Render() {
        Bind();
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void Unbind() {
        glBindVertexArray(0);
    }

    public void Delete() {
        vbo.Delete();
        glDeleteVertexArrays(vaoID);
    }


}
