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

uniform mat4 uLightSpaceMatrix;

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

void extractVertex(in uint data, out uint vertex)
{
    vertex = (data & VERTEX_BITMASK);
}

void main()
{
    vec3 pos;
    Face face;
    uint vertex;
    // extract attributes
    extractPosition(aData, pos);
    extractFace(aData, face);
    extractVertex(aData, vertex);
    // offset position
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
    pos.x += float(aChunkPos.x) * 16.0;
    pos.z += float(aChunkPos.y) * 16.0;
    // set position
    gl_Position = uLightSpaceMatrix * vec4(pos, 1.0);
}

#type fragment
#version 460 core

void main()
{
    // gl_FragDepth = gl_FragCoord.z;
}