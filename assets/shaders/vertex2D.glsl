#type vertex
#version 460 core
layout (location=0) in vec2 aPos;
layout (location=1) in vec3 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in float aTexId;

out vec3 fColor;
out vec2 fTexCoords;
out float fTexId;

uniform int zIndex;
uniform mat4 uProjection;

void main()
{
    fTexId = aTexId;
    fTexCoords = aTexCoords;
    fColor = aColor;
    gl_Position = uProjection * vec4(aPos, zIndex, 1);
}

#type fragment
#version 460 core
#define NUM_TEXTURES 8
in vec3 fColor;
in vec2 fTexCoords;
in float fTexId;

uniform sampler2D uTextures[NUM_TEXTURES];

out vec4 color;

void main()
{
    int texId = int(fTexId);
    if (texId > -1) {
        color = vec4(fColor, 1) * texture(uTextures[texId], fTexCoords);
    } else {
        color = vec4(fColor, 1);
    }
}