package Engine.Core;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Loader {

    float[] convertedVertices;
    public List<Vertex> vertices;

    public Loader(String path) {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        ByteBuffer image = STBImage.stbi_load(path, w, h, channels, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load image from path: " + path +
                    "\nReason: " + STBImage.stbi_failure_reason());
        }

        int width = w.get();
        int height = h.get();

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        vertices = new ArrayList<>();

        // Iterate through the image's pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (x + y * width) * 4;
                int r = image.get(index) & 0xFF;
                int g = image.get(index + 1) & 0xFF;
                int b = image.get(index + 2) & 0xFF;
                int a = image.get(index + 3) & 0xFF;

                if (a > 0) {  // Only process pixels that are not transparent

                    float fx = (x - centerX) ;
                    float fy = ((height - y - 1) - centerY); // Flip Y and center

                    // Convert color values to normalized RGB
                    float fr = r / 255.0f;
                    float fg = g / 255.0f;
                    float fb = b / 255.0f;

                    // Texture coordinates (UVs)
                    float u0 = (float) x / width;
                    float v0 = (float) y / height;
                    float u1 = (float) (x + 1) / width;
                    float v1 = (float) (y + 1) / height;

                    // Add quads for the non-transparent pixels
                    vertices.addAll(createQuad(fx, fy, u0, v0, u1, v1, fr, fg, fb));
                }
            }
        }

        // Free the image buffer after use
        STBImage.stbi_image_free(image);
    }

    // Creates a quad (two triangles) for a given pixel
    public List<Vertex> createQuad(float x, float y, float u0, float v0, float u1, float v1, float r, float g, float b) {
        List<Vertex> quad = new ArrayList<>();

        // Bottom-left triangle (flipping Y)
        quad.add(new Vertex(x, y, 0f, r, g, b, u0, v1)); // bottom-left
        quad.add(new Vertex(x + 1, y, 0f, r, g, b, u1, v1)); // bottom-right
        quad.add(new Vertex(x + 1, y + 1, 0f, r, g, b, u1, v0)); // top-right

        // Top-left triangle (flipping Y)
        quad.add(new Vertex(x, y, 0f, r, g, b, u0, v1)); // bottom-left
        quad.add(new Vertex(x + 1, y + 1, 0f, r, g, b, u1, v0)); // top-right
        quad.add(new Vertex(x, y + 1, 0f, r, g, b, u0, v0)); // top-left

        return quad;
    }

    // Convert vertices to an array for rendering
    public float[] getconvertedVertices() {
        convertedVertices = new float[vertices.size() * 8];

        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            int index = i * 8;
            convertedVertices[index] = v.x;
            convertedVertices[index + 1] = v.y;
            convertedVertices[index + 2] = v.z;
            convertedVertices[index + 3] = v.r;
            convertedVertices[index + 4] = v.g;
            convertedVertices[index + 5] = v.b;
            convertedVertices[index + 6] = v.u;
            convertedVertices[index + 7] = v.v;
        }

        return convertedVertices;
    }
}
