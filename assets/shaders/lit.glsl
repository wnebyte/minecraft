#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in float aMatId;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in vec3 aNormal;

uniform mat4 uView;
uniform mat4 uProjection;

out vec3 fPos;
out float fMatId;
out vec2 fTexCoords;
out vec3 fNormal;

void main()
{
    fPos = aPos;
    fMatId = aMatId;
    fTexCoords = aTexCoords;
    fNormal = aNormal;
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}

#type fragment
#version 460 core
struct DirLight {
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct PointLight {
    vec3 position;
    float constant;
    float linear;
    float quadratic;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct SpotLight {
    vec3 direction;
    float cutoff;
    float outerCutoff;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

#define NUM_POINT_LIGHTS 0
#define NUM_MATERIALS 8

in vec3 fPos;
in float fMatId;
in vec2 fTexCoords;
in vec3 fNormal;

uniform vec3 uViewPos;
uniform DirLight uDirLight;

uniform sampler2D uDiffuseMaps[NUM_MATERIALS];
uniform sampler2D uSpecularMaps[NUM_MATERIALS];

out vec4 color;

vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, int matId);
vec3 calcPointLight(PointLight light, vec3 normal, vec3 fPos, vec3 viewDir, int matId);

void main()
{
    // properties
    int matId = int(fMatId);
    vec3 norm = normalize(fNormal);
    vec3 viewDir = normalize(uViewPos - fPos);

    // phase 1: Directional lighting
    vec3 result = calcDirLight(uDirLight, norm, viewDir, matId);
    // phase 2: Point lights
    for (int i = 0; i < NUM_POINT_LIGHTS; i++)
    {
       // result += calcPointLight(uPointLights[i], norm, fPos, viewDir, matId);
    }

    color = vec4(result, 1.0);
}

vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, int matId)
{
    vec3 lightDir = normalize(-light.direction);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0f);
    // combine results
    vec3 ambient = light.ambient * vec3(texture(uDiffuseMaps[matId], fTexCoords));
    vec3 diffuse = light.diffuse * diff * vec3(texture(uDiffuseMaps[matId], fTexCoords));
    vec3 specular = light.specular * spec * vec3(texture(uSpecularMaps[matId], fTexCoords));
    return (ambient + diffuse + specular);
}

vec3 calcPointLight(PointLight light, vec3 normal, vec3 fPos, vec3 viewDir, int matId)
{
    vec3 lightDir = normalize(light.position - fPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0f);
    // attentuation
    float distance     = length(light.position - fPos);
    float attentuation = 1.0 /
                        (light.constant + light.linear * distance + light.quadratic * (distance * distance));
    // combine results
    vec3 ambient = light.ambient * vec3(texture(uDiffuseMaps[matId], fTexCoords));
    vec3 diffuse = light.diffuse * diff * vec3(texture(uDiffuseMaps[matId], fTexCoords));
    vec3 specular = light.specular * spec * vec3(texture(uSpecularMaps[matId], fTexCoords));
    ambient  *= attentuation;
    diffuse  *= attentuation;
    specular *= attentuation;
    return (ambient + diffuse + specular);
}
