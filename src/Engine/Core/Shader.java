package Engine.Core;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;


public class Shader {

    private int renderID;

    public Shader(String vertexPath, String fragmentPath) {
        renderID = glCreateProgram();

        String vertexSource = readFile(vertexPath);
        String fragmentSource = readFile(fragmentPath);

        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSource);

        glAttachShader(renderID, vertexShader);
        glAttachShader(renderID, fragmentShader);
        glLinkProgram(renderID);
        checkLink(renderID);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int compileShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        checkCompile(shader, source);

        return shader;
    }

    private String readFile(String filePath) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
        } catch (Exception e) {
            Console.error("Failed to load shader file: " + filePath + " " + e.getMessage());
        }
        return Console.error("Could not load file");
    }

    public void useProgram() {
        glUseProgram(renderID);
    }

    public void Unbind() {
        glUseProgram(0);
    }

    public void deleteProgram() {
        glDeleteProgram(renderID);
    }

    private void checkLink(int program) {
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("SHADER PROGRAM LINK ERROR: " + glGetProgramInfoLog(program));
        }
    }

    private void checkCompile(int shader, String source) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            throw new RuntimeException("SHADER COMPILE ERROR:\n" + log + "\nSource:\n" + source);
        }
    }

    public int GetUniformLocation(String name) {
        return glGetUniformLocation(renderID, name);
    }

    public int GetAttributeLocation(String name) {
        return glGetAttribLocation(renderID, name);
    }

    public void setUniformMat4f(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            int location = glGetUniformLocation(renderID, name);
            glUniformMatrix4fv(location, false, buffer);
        }
    }
}