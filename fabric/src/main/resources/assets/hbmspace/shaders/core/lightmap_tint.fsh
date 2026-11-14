#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform CelestialTint {
    vec4 BoostRows[4];
    int Channel;
} tint;

uniform sampler2D LightmapRaw;
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    ivec2 cell = ivec2(floor(texCoord * 16.));
    
    vec3 bytes = floor(texelFetch(LightmapRaw, ivec2(gl_FragCoord.xy), 0).rgb * 255. + .5);
    float boost = tint.BoostRows[cell.y / 4][cell.y % 4];
    bytes[tint.Channel] = min(floor(bytes[tint.Channel] + boost), 255.);
    fragColor = vec4(bytes / 255., 1.);
}
