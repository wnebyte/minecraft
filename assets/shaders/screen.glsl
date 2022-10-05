#type vertex
#version 460 core
layout (location=0) in vec3 pos;
layout (location=1) in vec2 uv;

out vec2 tex_coords;

void main()
{
    tex_coords = uv;
    gl_Position = vec4(pos, 1.0);
}

#type fragment
#version 460 core
in vec2 tex_coords;

layout (location=0) out vec4 frag;

// screen image
uniform sampler2D screen;

void main()
{
    frag = vec4(texture(screen, tex_coords).rgb, 1.0);
}