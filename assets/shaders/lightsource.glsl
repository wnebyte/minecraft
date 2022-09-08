#type vertex
#version 460
layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in float aTexId;
layout (location=4) in vec3 aNormal;

out vec4 fColor;
out vec2 fTexCoords;
out float fTexId;

uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    fColor = aColor;
    fTexCoords = aTexCoords;
    fTexId = aTexId;
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}

#type fragment
#version 460
in vec4 fColor;
in vec2 fTexCoords;
in float fTexId;

out vec4 color;

void main() {
    color = fColor;
}