#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec3 aColor;
layout (location=2) in vec3 aTexCoords;

uniform mat4 uView;
uniform mat4 uProjection;

out vec3 fColor;
out vec3 fTexCoords;

void main()
{
    fColor = aColor;
    fTexCoords = aTexCoords;
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}

#type fragment
#version 460 core
in vec3 fColor;
in vec3 fTexCoords;

uniform sampler2DArray uTexture;

out vec4 color;

void main()
{
    if (fTexCoords.z >= 0) {
        color = vec4(fColor, 1.0) * texture(uTexture, fTexCoords);
    } else {
        color = vec4(fColor, 1.0);
    }

    if (color.a < 0.3) {
        discard;
    }
}