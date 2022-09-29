#type vertex
#version 460 core
layout (location=0) in vec3 pos;
layout (location=1) in vec2 texCoord;

uniform mat4 uView;
uniform mat4 uProjection;

out vec2 uv;

void main()
{
    gl_Position = uProjection * uView * vec4(pos, 1.0);
    uv = texCoord;
}

#type fragment
#version 460 core
#define tMask uint(0x3FF00000);
#define sMask uint(0xFFC00);
#define bMask uint(0x3FF);
in vec2 uv;
out vec4 color;

uniform sampler2D uTextures[8];

void main()
{
    color = texture(uTextures[0], uv);
}
