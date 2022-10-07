#type vertex
#version 460 core
layout (location=0) in vec3 aPos;

out vec3 uv;

uniform mat4 uView;
uniform mat4 uProjection;

void main()
{
    uv = aPos;
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}

#type fragment
#version 460 core
in vec3 uv;

layout (location=0) out vec4 color;

uniform samplerCube uDayCubemap;
uniform samplerCube uNightCubemap;
uniform float uBlend;

void main()
{
    color = mix(vec4(texture(uDayCubemap, uv).rgb, 1), vec4(texture(uNightCubemap, uv).rgb, 1), uBlend);
}
