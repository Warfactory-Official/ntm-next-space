#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <hbmspace:shader_time.glsl>

layout(location = 0) in vec3 Position;

layout(location = 0) out vec4 vColor;

float hash(float x){ return fract(cos(x*124.123)*412.0); }

void main() {
	vColor = ColorModulator;

	float v = float(gl_VertexIndex - BaseVertex);
	float t = v + iTime;
	float r = hash(v);
	float r2 = hash(v + 0.5);
	float r3 = hash(v + 0.75);

	float y = (cos(t) * r + sin(t) * (1.0 - r)) * 0.2 * r2 * r2;
	float o = cos(y * 2.0) * (1.0 - ((r3 - 0.5) * 0.05));

	gl_Position = ProjMat * ModelViewMat * (vec4(Position.x * o, y, Position.z * o, 1.0));
	gl_PointSize = 1.0;
}
