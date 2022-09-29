#type vertex
#version 460 core
layout (location=0) in vec2 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in uint drawid;

out vec2 uv;
flat out uint drawID;

void main()
{
    gl_Position = vec4(position, 0.0, 1.0);
    uv = texCoord;
    drawID = drawid;
}

#type fragment
#version 460 core
in vec2 uv;
flat in uint drawID;
//layout (binding=0) uniform sampler2DArray textureArray;
out vec4 color;

uniform sampler2D uTextures[8];

void main()
{
   // color = texture(textureArray, vec3(uv.x, uv.y, drawID));
    color = texture(uTextures[drawID], uv);
}
