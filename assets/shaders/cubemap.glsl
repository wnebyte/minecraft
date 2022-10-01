#type vertex
#version 460 core
layout (location=0) in vec3 aPos;

out vec3 fTexCoords;

uniform mat4 uView;
uniform mat4 uProjection;

void main()
{
    fTexCoords = aPos;
    vec4 pos = uProjection * uView * vec4(aPos, 1.0);
    gl_Position = pos.xyww;
}

#type fragment
#version 460 core
in vec3 fTexCoords;

out vec4 color;

uniform samplerCube skybox;

void main()
{
    color = texture(skybox, fTexCoords);
}