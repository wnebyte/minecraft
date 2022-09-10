#type vertex
#version 460 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in float aTexId;
layout (location=4) in float aMatId;
layout (location=5) in vec3 aNormal;

uniform mat4 uView;
uniform mat4 uProjection;

out vec3 fPos;
out vec4 fColor;
out vec2 fTexCoords;
out float fTexId;
out float fMatId;
out vec3 fNormal;

void main() {
    fPos = aPos;
    fColor = aColor;
    fTexCoords = aTexCoords;
    fTexId = aTexId;
    fMatId = aMatId;
    fNormal = aNormal;
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}

#type fragment
#version 460 core
struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

struct Light {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

in vec3 fPos;
in vec4 fColor;
in vec2 fTexCoords;
in float fTexId;
in float fMatId;
in vec3 fNormal;

uniform sampler2D uTextures[8];
uniform vec3 uViewPos;
uniform Light uLight;
uniform Material uMaterials[8];

out vec4 color;

void main() {
    // sample texture
    vec4 tmpColor;
    int id = int(fTexId);
    if (id > 0) {
        tmpColor = texture(uTextures[id], fTexCoords);
    } else {
        tmpColor = fColor;
    }

    // get material
    id = int(fMatId);
    Material material = uMaterials[id];

    // ambient
    // We take the light's color, multiply it with a small constant ambient factor, multiply this with the object's color.
    vec3 ambient = uLight.ambient * material.ambient;

    // diffuse
    // The first thing we need to calculate is the direction vector between the light source and the fragment's position.
    vec3 norm = normalize(fNormal);
    vec3 lightDir = normalize(uLight.position - fPos);
    // Next we need to calculate the diffuse impact of the light on the current fragment
    // by taking the dot product between the norm and lightDir vectors.
    // The resulting value is then multiplied with the light's color to get the diffuse component,
    // resulting in a darker diffuse component the greater the angle between both vectors:
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = uLight.diffuse * (diff * material.diffuse);

    // specular
    // Next we calculate the view direction vector and the corresponding reflect vector along the normal axis:
    vec3 viewDir = normalize(uViewPos - fPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    // Then what's left to do is to actually calculate the specular component.
    // This is accomplished with the following formula:
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    vec3 specular = uLight.specular * (spec * material.specular);

    // Now that we have both an ambient a diffuse and a specular component we add all colors to each other
    // and then multiply the result with the color of the object to get the resulting fragment's output color:
    vec3 result = (ambient + diffuse + specular) * tmpColor.rgb;
    color = vec4(result, 1.0);
}