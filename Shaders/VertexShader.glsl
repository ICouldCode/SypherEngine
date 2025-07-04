//VertexShader
#version 330 core

uniform mat4 u_ViewProjection;

layout(location = 0) in vec3 vPos;
layout(location = 1) in vec3 vCol;
layout(location = 2) in vec2 a_TexCoord;

out vec3 color;
out vec2 v_TexCoord;

void main() {
    gl_Position = u_ViewProjection * vec4(vPos, 1.0);
    color = vCol;
    v_TexCoord = a_TexCoord;
}