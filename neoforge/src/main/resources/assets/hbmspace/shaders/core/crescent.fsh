#version 330

#moj_import <hbmspace:planet_info.glsl>

uniform sampler2D Sampler0;
#define bodyTex Sampler0

in vec2 texCoord0;

out vec4 fragColor;

#define PI 3.1415926538

vec2 quantize(vec2 inp, vec2 period) {
	return floor(inp / period) * period;
}

void main() {
	vec2 movingUV = texCoord0 + vec2(offset, 0);

	
	float alphaMask = 1.0;
	if (useBodyAlphaMask != 0) {
		alphaMask = texture(bodyTex, movingUV).a;
		if (alphaMask <= 0.001) {
			fragColor = vec4(0.0);
			return;
		}
	}

	vec2 fragCoord = quantize(movingUV, vec2(0.0625, 0.0625)) - vec2(offset, 0);
	vec2 uv = (2.25 * fragCoord - 1.1);
	vec2 suv = (2.0 * fragCoord - 1.0);

	vec3 light = vec3(sin(phase * PI), 0.0, cos(phase * PI));

	vec3 n = vec3(uv, sqrt(1.0 - clamp(dot(uv, uv), 0.0, 1.0)));
	float brightness = dot(n, light);

	
	brightness = max(brightness, (abs(phase) - 0.7) * clamp(dot(suv, suv), 0.0, 1.0));

	
	if (abs(phase) < 0.5) {
		if (phase < 0.0) {
			brightness = phase * 4.0 + 2.0 - uv.x;
		} else {
			brightness = -phase * 4.0 + 2.0 + uv.x;
		}
	}

	
	brightness = max(brightness, 0.05);

	fragColor = vec4(0.0, 0.0, 0.0, (1.0 - brightness) * alphaMask);
}
