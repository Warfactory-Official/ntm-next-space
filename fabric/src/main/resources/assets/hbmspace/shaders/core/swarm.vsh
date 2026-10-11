#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <hbmspace:shader_time.glsl>

in vec3 Position;

out vec4 vColor;

float hash(float x){ return fract(cos(x*124.123)*412.0); }

void main() {
	vColor = ColorModulator;

	float v = float(gl_VertexID - BaseVertex);
	float t = v + iTime;
	float r = hash(v);
	float r2 = hash(v + 0.5);
	float r3 = hash(v + 0.75);

	float y = (cos(t) * r + sin(t) * (1.0 - r)) * 0.2 * r2 * r2;
	float o = cos(y * 2.0) * (1.0 - ((r3 - 0.5) * 0.05));

	gl_Position = ProjMat * ModelViewMat * (vec4(Position.x * o, y, Position.z * o, 1.0));
	gl_PointSize = 1.0;
}
