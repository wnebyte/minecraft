#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec2 aTexCoords;
layout (location=2) in float aTexId;
layout (location=3) in vec3 aNormal;
layout (location=4) in vec3 aTangent;
layout (location=5) in vec3 aBiTangent;

uniform mat4 uView;
uniform mat4 uProjection;

out vec2 fTexCoords;
out float fTexId;
out vec3 fNormal;
out mat3 fTBN;

void main(){
    fTexCoords = aTexCoords;
    fTexId = aTexId;
    fNormal = aNormal;
    vec3 T = normalize(vec3(vec4(aTangent,   0.0)));
    vec3 B = normalize(vec3(vec4(aBiTangent, 0.0)));
    vec3 N = normalize(vec3(vec4(aNormal,    0.0)));
    fTBN = mat3(T, B, N);

    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}

#type fragment
#version 460 core

in vec2 fTexCoords;
in float fTexId;
in vec3 fNormal;
in mat3 fTBN;

uniform sampler2D uTextures[8];
//uniform sampler2D uNormalTexture;

out vec4 color;

const vec3 sunlightDir = normalize(vec3(-0.3, -0.5, -0.2));
const float ambientOcclusion = 0.3f;

void main() {
    /*
    int id = int(fTexId);
    vec4 albedo = texture(uTextures[id], fTexCoords);
    float lightStrength = clamp(-dot(fNormal, sunlightDir), 0, 1);
    lightStrength = max(ambientOcclusion, lightStrength);

    vec3 textureNormal = normalize(texture(uNormalTexture, fTexCoords).xyz * 2.0 - 1.0);
    textureNormal = normalize(fTBN * textureNormal);
    float incidentAngle = -dot(textureNormal, sunlightDir);
    float normalLightStrength = clamp(incidentAngle, 0.0, 1.0);
    normalLightStrength = (normalLightStrength * 0.7) + 0.3;

    if (incidentAngle < 0) {
        normalLightStrength -= ((incidentAngle * 0.7) + 0.3);
        normalLightStrength = clamp(normalLightStrength, 0, 1);
    }

    color = albedo * normalLightStrength
    */

    int id = int(fTexId);
    color = texture(uTextures[id], fTexCoords);
}