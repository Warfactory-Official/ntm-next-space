#version 330

#moj_import <minecraft:dynamictransforms.glsl>

#ifdef TEXTURED
uniform sampler2D Sampler0;

in vec2 texCoord0;
#endif
#ifdef COLORED
in vec4 vertexColor;
#endif

out vec4 fragColor;

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
