package Engine.Core;
import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

public class VertexBufferLayout {

    private final List<VertexBufferElement> elements = new ArrayList<>();
    private int stride;
    private int offset;

    public void pushFloat(int count) {
        elements.add(new VertexBufferElement(GL_FLOAT, count, false));
        stride += VertexBufferElement.getSizeOfType(GL_FLOAT) * count;
    }

    public void pushUInt(int count) {
        elements.add(new VertexBufferElement(GL_UNSIGNED_INT, count, false));
        stride += VertexBufferElement.getSizeOfType(GL_UNSIGNED_INT) * count;
    }

    public void pushUByte(int count) {
        elements.add(new VertexBufferElement(GL_UNSIGNED_BYTE, count, true));
        stride += VertexBufferElement.getSizeOfType(GL_UNSIGNED_BYTE) * count;
    }

    public List<VertexBufferElement> getElements() {
        return elements;
    }

    public int getStride() {
        return stride;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}
