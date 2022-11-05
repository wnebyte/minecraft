#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec3 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in float aTexId;

uniform mat4 uView;
uniform mat4 uProjection;

out vec3 fColor;
out vec2 fTexCoords;
out float fTexId;

void main()
{
    fColor = aColor;
    fTexCoords = aTexCoords;
    fTexId = aTexId;
    gl_Position = uProjection * vec4(aPos, 1.0);
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
    if (texId >= 0) {
        color = vec4(fColor, 1.0) * texture(uTextures[texId], fTexCoords);
    } else {
        color = vec4(fColor, 1.0);
    }
}
