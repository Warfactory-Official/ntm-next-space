#include "hbm:instance/particle_common.glsl"

void flw_instanceVertex(in FlwInstance i) {
    hbm_particleVertex(i.previousPosition, i.position, i.previousRoll, i.roll, i.color, i.overlay, vec2(i.light), i.uvRegion, i.size, 1.);
    // 1.7 ParticleDuoDec/ParticleDuoDec2.renderParticle sets this world normal regardless of the camera billboard.
    flw_vertexNormal = vec3(0., 1., 0.);
}
