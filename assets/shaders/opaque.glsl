#type vertex
#version 460 core
layout (location=0) in uint aData;
layout (location=1) in ivec2 aChunkPos;

out vec3 pos;
out vec3 uv;
out struct Face {
    int id;
    vec3 tl;
    vec3 tr;
    vec3 bl;
    vec3 br;
} face;
out vec3 fColor;
flat out uint vertex;
out vec4 fPosLightSpace;

uniform mat4 uView;
uniform mat4 uProjection;
uniform mat4 uLightSpaceMatrix;
uniform samplerBuffer uTexCoordsTexture;

const vec3 VERTS[8] = {
    vec3(-0.5f,  0.5f,  0.5f),
    vec3( 0.5f,  0.5f,  0.5f),
    vec3(-0.5f, -0.5f,  0.5f),
    vec3( 0.5f, -0.5f,  0.5f),
    vec3(-0.5f,  0.5f, -0.5f),
    vec3( 0.5f,  0.5f, -0.5f),
    vec3(-0.5f, -0.5f, -0.5f),
    vec3( 0.5f, -0.5f, -0.5f)
};

const int[6][6] INDICES = {
    { 1, 0, 2, 3, 1, 2 },
    { 5, 1, 3, 7, 5, 3 },
    { 7, 6, 4, 5, 7, 4 },
    { 0, 4, 6, 2, 0, 6 },
    { 5, 4, 0, 1, 5, 0 },
    { 3, 2, 6, 7, 3, 6 }
};

const Face FRONT  = { 0, VERTS[INDICES[0][1]], VERTS[INDICES[0][0]], VERTS[INDICES[0][2]], VERTS[INDICES[0][3]] };
const Face RIGHT  = { 1, VERTS[INDICES[1][1]], VERTS[INDICES[1][0]], VERTS[INDICES[1][2]], VERTS[INDICES[1][3]] };
const Face BACK   = { 2, VERTS[INDICES[2][1]], VERTS[INDICES[2][0]], VERTS[INDICES[2][2]], VERTS[INDICES[2][3]] };
const Face LEFT   = { 3, VERTS[INDICES[3][1]], VERTS[INDICES[3][0]], VERTS[INDICES[3][2]], VERTS[INDICES[3][3]] };
const Face TOP    = { 4, VERTS[INDICES[4][1]], VERTS[INDICES[4][0]], VERTS[INDICES[4][2]], VERTS[INDICES[4][3]] };
const Face BOTTOM = { 5, VERTS[INDICES[5][1]], VERTS[INDICES[5][0]], VERTS[INDICES[5][2]], VERTS[INDICES[5][3]] };

#define WIDTH uint(16)
#define HEIGHT uint(256)
#define DEPTH uint(16)

#define POSITION_BITMASK uint(0xFFFF0000)
#define UV_BITMASK uint(0xFFC0)
#define FACE_BITMASK uint(0x38)
#define COLOR_BY_BIOME_BITMASK uint(0x4)
#define VERTEX_BITMASK uint(0x3)

void extractPosition(in uint data, out vec3 pos)
{
    uint index = (data & POSITION_BITMASK) >> 16;
    uint z = index / (WIDTH * HEIGHT);
    index -= (z * WIDTH * HEIGHT);
    uint y = index / WIDTH;
    uint x = index % WIDTH;
    pos = vec3(float(x), float(y), float(z));
}

void extractTexCoords(in uint data, out vec3 uv)
{
    uint index = (data & UV_BITMASK) >> 6;
    int uvIndex = int((index * uint(8)) + (vertex * uint(2)));
    uv.x = texelFetch(uTexCoordsTexture, uvIndex + 0).r;
    uv.y = texelFetch(uTexCoordsTexture, uvIndex + 1).r;
    uv.z = float(index);
}

void extractFace(in uint data, out Face face)
{
    uint id = (data & FACE_BITMASK) >> 3;
    switch (id)
    {
        case uint(0):
        face = FRONT;
        break;
        case uint(1):
        face = RIGHT;
        break;
        case uint(2):
        face = BACK;
        break;
        case uint(3):
        face = LEFT;
        break;
        case uint(4):
        face = TOP;
        break;
        case uint(5):
        face = BOTTOM;
        break;
    }
}

void extractColorByBiome(in uint data, out bool colorByBiome)
{
    colorByBiome = bool((data & COLOR_BY_BIOME_BITMASK) >> 2);
}

void extractVertex(in uint data, out uint vertex)
{
    vertex = (data & VERTEX_BITMASK);
}

void normalizeVertex(in Face face, out uint vertex)
{
    // BACK face
    if (face.id == 2)
    {
        if (vertex % 2 == 0)
        {
            vertex++;
        }
        else
        {
            vertex--;
        }
    }
}

void main()
{
    extractPosition(aData, pos);
    extractFace(aData, face);
    extractVertex(aData, vertex);

    switch (vertex)
    {
        case uint(0):
        pos += face.tr;
        break;
        case uint(1):
        pos += face.br;
        break;
        case uint(2):
        pos += face.bl;
        break;
        case uint(3):
        pos += face.tl;
        break;
    }

    normalizeVertex(face, vertex);
    extractTexCoords(aData, uv);

    bool colorByBiome;
    extractColorByBiome(aData, colorByBiome);
    fColor = vec3(1, 1, 1);
    if (colorByBiome)
    {
        fColor = vec3(109.0 / 255.0, 184.0 / 255.0, 79.0 / 255.0);
    }

    pos.x += float(aChunkPos.x) * 16.0;
    pos.z += float(aChunkPos.y) * 16.0;
    fPosLightSpace = uLightSpaceMatrix * vec4(pos, 1.0);
    gl_Position = uProjection * uView * vec4(pos, 1.0);
}

#type fragment
#version 460 core
in vec3 pos;
in vec3 uv;
in struct Face {
    int id;
    vec3 tl;
    vec3 tr;
    vec3 bl;
    vec3 br;
} face;
in vec3 fColor;
flat in uint vertex;
in vec4 fPosLightSpace;

out vec4 color;

uniform sampler2DArray uTexture;
uniform sampler2D uShadowMap;
uniform vec3 uViewPos;
uniform vec3 uLightPos;

void getNormal(in Face face, out vec3 normal)
{
    switch (face.id)
    {
        case 0:
        normal = vec3(0, 0, 1);
        break;
        case 1:
        normal = vec3(1, 0, 0);
        break;
        case 2:
        normal = vec3(0, 0, -1);
        break;
        case 3:
        normal = vec3(-1, 0, 0);
        break;
        case 4:
        normal = vec3(0, 1, 0);
        break;
        case 5:
        normal = vec3(0, -1, 0);
        break;
    }
}

float shadowCalculation(vec4 fPosLightSpace, vec3 normal, vec3 lightDir)
{
    // perform perspective divide
    vec3 projCoords = fPosLightSpace.xyz / fPosLightSpace.w;
    // transform to [0,1] range
    projCoords = projCoords * 0.5 + 0.5;
    if (projCoords.z > 1.0) {
        return 0.0;
    }
    // get closest depth value from light's perspective (using [0,1] range fPosLightSpace as coords)
    float closestDepth = texture(uShadowMap, projCoords.xy).r;
    // get depth of current fragment from light's perspective
    float currentDepth = projCoords.z;
    // calculate bias used to counteract "shadow acne"
    float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);
    // check whether current frag pos is in shadow
    float shadow = currentDepth - bias > closestDepth ? 1.0 : 0.0;
    return shadow;
}

void main()
{
    vec4 tmp = vec4(fColor, 1.0) * texture(uTexture, uv);
    if (tmp.a < 0.3) {
        discard;
    }
    vec3 objectColor = tmp.rgb;
    vec3 normal;
    getNormal(face, normal);
    vec3 lightColor = vec3(0.3);
    // ambient
    vec3 ambient = 0.5 * lightColor;
    // diffuse
    vec3 lightDir = normalize(uLightPos - pos);
    float diff = max(dot(lightDir, normal), 0.0);
    vec3 diffuse = diff * lightColor;
    // calculate shadow
    float shadow = shadowCalculation(fPosLightSpace, normal, lightDir);
    vec3 lighting = (ambient + (1.0 - shadow) * diffuse) * objectColor;
    color = vec4(lighting, 1.0);
}
