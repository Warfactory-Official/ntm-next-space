#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
#ifdef TEXTURED
layout(location = 1) in vec2 UV0;

layout(location = 0) out vec2 texCoord0;
#endif
#ifdef COLORED
layout(location = 1) in vec4 Color;

layout(location = 1) out vec4 vertexColor;
#endif

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
#ifdef TEXTURED
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
#endif
#ifdef COLORED
    vertexColor = Color;
#endif
#ifdef POINTS
    gl_PointSize = 1.0;
#endif
}
