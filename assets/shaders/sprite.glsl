#type vertex
#version 460 core
layout (location=0) in vec2 aPos;

out vec3 fColor;

uniform mat4 uView;
uniform mat4 uProjection;

void main()
{
    fColor = vec3(0, 0, 0);
    gl_Position = uProjection * vec4(aPos, -5, 1);
}

#type fragment
#version 460 core
#define NUM_TEXTURES 8
in vec3 fColor;

out vec4 color;

uniform sampler2D uTextures[NUM_TEXTURES];

void main()
{
    color = vec4(fColor, 1);
}
