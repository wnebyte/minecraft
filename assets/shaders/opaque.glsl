#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec2 aUv;
layout (location=2) in ivec2 aChunkPos;

out vec2 uv;

uniform mat4 uView;
uniform mat4 uProjection;

void main()
{
    float xOffset = float(aChunkPos.x) * 16.0;
    float zOffset = float(aChunkPos.y) * 16.0;
    gl_Position = uProjection * uView * vec4(aPos.x + xOffset, aPos.y, aPos.z + zOffset, 1.0);
    uv = aUv;
}

#type fragment
#version 460 core
in vec2 uv;

out vec4 color;

uniform sampler2D uTexture;

void main()
{
    color = texture(uTexture, uv);
    if (color.w < 0.3)
        discard;
}