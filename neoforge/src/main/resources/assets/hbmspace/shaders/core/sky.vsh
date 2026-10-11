#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
#ifdef TEXTURED
in vec2 UV0;

out vec2 texCoord0;
#endif
#ifdef COLORED
in vec4 Color;

out vec4 vertexColor;
#endif

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
#ifdef TEXTURED
    texCoord0 = UV0;
#endif
#ifdef COLORED
    vertexColor = Color;
#endif
#ifdef POINTS
    gl_PointSize = 1.0;
#endif
}
