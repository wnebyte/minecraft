#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec2 aUv;

out vec2 uv;

uniform mat4 uView;
uniform mat4 uProjection;

void main()
{
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
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
}