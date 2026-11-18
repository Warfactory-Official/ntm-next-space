#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

#ifdef TEXTURED
uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
#endif
#ifdef COLORED
layout(location = 1) in vec4 vertexColor;
#endif

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = ColorModulator;
#ifdef TEXTURED
    color *= texture(Sampler0, texCoord0);
#endif
#ifdef COLORED
    color *= vertexColor;
#endif
    fragColor = color;
}
