package Engine.Component;

import Engine.Core.*;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.glGetIntegerv;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM;
import static org.lwjgl.opengl.GL20.glUniform1i;

public class RenderComponent extends Component{

    public VAO vao;
    public Shader shader;
    public List<Texture2D> textureHandler = new ArrayList<>();
    private String path;
    public GameObject gameObject;
    public boolean destroyed = false;

    public RenderComponent(String Path, GameObject gameObject){
        this.gameObject = gameObject;
        this.path = Path;
    }

    @Override
    public void start() {
        vao = new VAO();
        shader = new Shader("Shaders/VertexShader.glsl", "Shaders/FragmentShader.glsl");
        Loader loader = new Loader(path);
        vao.LoadObject(loader.getconvertedVertices());
        addTexture(path);
        activateTextures();
    }

    public void Draw(Matrix4f viewProjection){
        shader.useProgram();
        activateTextures();

        IntBuffer currentProgram = BufferUtils.createIntBuffer(1);  // Create an IntBuffer with space for 1 integer
        glGetIntegerv(GL_CURRENT_PROGRAM, currentProgram);

        if (currentProgram.get(0) == 0) {
            System.out.println("No active program. Check if the shader is correctly loaded.");
        }

        Matrix4f model = gameObject.getComponent(Transform.class).getModelMatrix();
        Matrix4f mvp = new Matrix4f(viewProjection).mul(model);

        shader.setUniformMat4f("u_ViewProjection", mvp);
        vao.Render();
        vao.Unbind();
        shader.Unbind();
    }

    public void addTexture(String path){
        textureHandler.add(new Texture2D(path));
    }

    public void Delete(){
        vao.Delete();
        shader.deleteProgram();
        for(int i = 0; i < textureHandler.size(); i++) {
            textureHandler.get(i).delete();
        }
    }

    private void activateTextures(){
        for(int i = 0; i < textureHandler.size(); i++){
            glActiveTexture(GL_TEXTURE0 + i);
            textureHandler.get(i).bind();
            glUniform1i(shader.GetUniformLocation(String.format("u_Texture%d", i)), i);
        }
    }

    @Override
    public void cleanup() {
        // Cleanup resources used by the RenderComponent
        if (vao != null) {
            vao.Delete();
        }
        if (shader != null) {
            shader.deleteProgram();
        }
        for (Texture2D texture : textureHandler) {
            texture.delete();
        }
    }
}
