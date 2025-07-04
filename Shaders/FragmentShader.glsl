//FragmentShader
#version 330 core

in vec2 v_TexCoord;
in vec3 color;

out vec4 fragment;

uniform sampler2D u_Texture0;
uniform sampler2D u_Texture1;
uniform sampler2D u_Texture2;
uniform sampler2D u_Texture3;

void main() {
    vec4 texColor = texture(u_Texture0, v_TexCoord);
    fragment = texColor * vec4(color, 1.0);
}