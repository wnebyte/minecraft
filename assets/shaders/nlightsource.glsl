#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in float aMatId;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in vec3 aNormal;

uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}

#type fragment
#version 460 core
out vec4 color;

const vec4 lightColor = vec4(1.0, 1.0, 1.0, 1.0);

void main() {
    color = lightColor;
}