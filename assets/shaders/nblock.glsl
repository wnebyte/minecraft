#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in float aMatId;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in vec3 aNormal;

uniform mat4 uView;
uniform mat4 uProjection;

out vec3 fPos;
out vec2 fTexCoords;
out float fMatId;
out vec3 fNormal;

void main() {
    fPos = aPos;
    fMatId = aMatId;
    fTexCoords = aTexCoords;
    fNormal = aNormal;
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}

#type fragment
#version 460 core
struct Material {
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

struct Light {
    vec3 position;
   // vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float constant;
    float linear;
    float quadratic;
};

in vec3 fPos;
in float fMatId;
in vec2 fTexCoords;
in vec3 fNormal;

uniform vec3 uViewPos;
uniform Light uLight;
uniform Material uMaterials[8];
uniform float uShininess[8];
uniform sampler2D uDiffuseMaps[8];
uniform sampler2D uSpecularMaps[8];

out vec4 color;

void main() {
    // get material
    int id = int(fMatId);

    // ambient
    // We take the light's color, multiply it with a small constant ambient factor, multiply this with the object's color.
    vec3 ambient = uLight.ambient * vec3(texture(uDiffuseMaps[id], fTexCoords));

    // diffuse
    // The first thing we need to calculate is the direction vector between the light source and the fragment's position.
    vec3 norm = normalize(fNormal);
    vec3 lightDir = normalize(uLight.position - fPos);
   // vec3 lightDir = normalize(-uLight.direction);
    // Next we need to calculate the diffuse impact of the light on the current fragment
    // by taking the dot product between the norm and lightDir vectors.
    // The resulting value is then multiplied with the light's color to get the diffuse component,
    // resulting in a darker diffuse component the greater the angle between both vectors:
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = uLight.diffuse * diff * vec3(texture(uDiffuseMaps[id], fTexCoords));

    // specular
    // Next we calculate the view direction vector and the corresponding reflect vector along the normal axis:
    vec3 viewDir = normalize(uViewPos - fPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    // Then what's left to do is to actually calculate the specular component.
    // This is accomplished with the following formula:
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), uShininess[id]);
    vec3 specular = uLight.specular * spec * vec3(texture(uSpecularMaps[id], fTexCoords));

    // point light
    float distance = length(uLight.position - fPos);
    float attentuation = 1.0 /
                        (uLight.constant + uLight.linear * distance + uLight.quadratic * (distance * distance));
    ambient  *= attentuation;
    diffuse  *= attentuation;
    specular *= attentuation;

    // Now that we have both an ambient a diffuse and a specular component we add all colors to each other
    // and then multiply the result with the color of the object to get the resulting fragment's output color:
    color = vec4(ambient + diffuse + specular, 1.0);
}