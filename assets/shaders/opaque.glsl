#type vertex
#version 460 core
layout (location=0) in uint aData;
layout (location=1) in ivec2 aChunkPos;

struct Face {
    int id;
    vec3 tl;
    vec3 tr;
    vec3 bl;
    vec3 br;
};

out vec3 pos;
out vec2 uv;
out Face face;
flat out uint vertex;

uniform mat4 uView;
uniform mat4 uProjection;
uniform samplerBuffer uTexCoordsTexture;

const vec3 verts[8] = {
    vec3(-0.5f,  0.5f,  0.5f),
    vec3( 0.5f,  0.5f,  0.5f),
    vec3(-0.5f, -0.5f,  0.5f),
    vec3( 0.5f, -0.5f,  0.5f),
    vec3(-0.5f,  0.5f, -0.5f),
    vec3( 0.5f,  0.5f, -0.5f),
    vec3(-0.5f, -0.5f, -0.5f),
    vec3( 0.5f, -0.5f, -0.5f)
};

const int[6][6] indices = {
    { 1, 0, 2, 3, 1, 2 },
    { 5, 1, 3, 7, 5, 3 },
    { 7, 6, 4, 5, 7, 4 },
    { 0, 4, 6, 2, 0, 6 },
    { 5, 4, 0, 1, 5, 0 },
    { 3, 2, 6, 7, 3, 6 }
};

const Face front  = { 0, verts[indices[0][1]], verts[indices[0][0]], verts[indices[0][2]], verts[indices[0][3]] };
const Face right  = { 1, verts[indices[1][1]], verts[indices[1][0]], verts[indices[1][2]], verts[indices[1][3]] };
const Face back   = { 2, verts[indices[2][1]], verts[indices[2][0]], verts[indices[2][2]], verts[indices[2][3]] };
const Face left   = { 3, verts[indices[3][1]], verts[indices[3][0]], verts[indices[3][2]], verts[indices[3][3]] };
const Face top    = { 4, verts[indices[4][1]], verts[indices[4][0]], verts[indices[4][2]], verts[indices[4][3]] };
const Face bottom = { 5, verts[indices[5][1]], verts[indices[5][0]], verts[indices[5][2]], verts[indices[5][3]] };

#define WIDTH uint(16)
#define HEIGHT uint(256)
#define DEPTH uint(16)

#define POSITION_BITMASK uint(0xFFFF0000)
#define UV_BITMASK uint(0xFFC0)
#define FACE_BITMASK uint(0x38)
#define VERTEX_BITMASK uint(0x7)

void extractPosition(in uint data, out vec3 pos)
{
    uint index = (data & POSITION_BITMASK) >> 16;
    uint z = index / (WIDTH * HEIGHT);
    index -= (z * WIDTH * HEIGHT);
    uint y = index / WIDTH;
    uint x = index % WIDTH;
    pos = vec3(float(x), float(y), float(z));
}

void extractTexCoords(in uint data, out vec2 uv)
{
    uint index = (data & UV_BITMASK) >> 6;
    int uvIndex = int((index * uint(8)) + (vertex * uint(2)));
    uv.x = texelFetch(uTexCoordsTexture, uvIndex + 0).r;
    uv.y = texelFetch(uTexCoordsTexture, uvIndex + 1).r;
}

void extractFace(in uint data, out Face face)
{
    uint id = (data & FACE_BITMASK) >> 3;
    switch (id)
    {
        case uint(0):
        face = front;
        break;
        case uint(1):
        face = right;
        break;
        case uint(2):
        face = back;
        break;
        case uint(3):
        face = left;
        break;
        case uint(4):
        face = top;
        break;
        case uint(5):
        face = bottom;
        break;
    }
}

void extractVertex(in uint data, out uint vertex)
{
    vertex = (data & VERTEX_BITMASK);
}

void main()
{
    extractPosition(aData, pos);
    extractVertex(aData, vertex);
    extractTexCoords(aData, uv);
    extractFace(aData, face);

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

    float xOffset = float(aChunkPos.x) * 16.0;
    float zOffset = float(aChunkPos.y) * 16.0;
    gl_Position = uProjection * uView * vec4(pos.x + xOffset, pos.y, pos.z + zOffset, 1.0);
}

#type fragment
#version 460 core
struct Face {
    int id;
    vec3 tl;
    vec3 tr;
    vec3 bl;
    vec3 br;
};

in vec3 pos;
in vec2 uv;
in Face face;
flat in uint vertex;

out vec4 color;

uniform sampler2D uTexture;

void main()
{
    color = texture(uTexture, uv);
    if (color.w < 0.3)
        discard;
}