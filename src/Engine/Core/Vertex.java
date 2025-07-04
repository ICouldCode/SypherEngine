package Engine.Core;

public class Vertex {
    public float x, y, z;     // Position
    public float r, g, b;  // Color
    public float u, v;     // Texture coordinates


    public Vertex(float x, float y, float z, float r, float g, float b, float u, float v) {
        this.x = x;
        this.y = y;
        this.z = 0;
        this.r = r;
        this.g = g;
        this.b = b;
        this.u = u;
        this.v = v;
    }
}
