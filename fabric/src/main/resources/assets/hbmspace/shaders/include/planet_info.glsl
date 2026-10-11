#version 330

layout(std140) uniform PlanetInfo {
    vec4 PlanetNukeShockTime;
    vec4 PlanetNukeShockCenterX;
    vec4 PlanetNukeShockCenterY;
    vec4 PlanetNukeShockStrength;
    float atmosphereColorR;
    float atmosphereColorG;
    float atmosphereColorB;
    float cloudColorR;
    float cloudColorG;
    float cloudColorB;
    float cloudTintStrength;
    float cloudStormDarkness;
    float atmosphereAlpha;
    float atmosphereTime;
    float patternOffset;
    float impactTime;
    float phase;
    float offset;
    float atmosphereDensity;
    float cloudLightningStrength;
    float eveFlashStrength;
    int nukeShockCount;
    int atmosphereStyle;
    int lightningMode;
    int blackouts;
    int useBodyAlphaMask;
};

#define nukeShockTime PlanetNukeShockTime
#define nukeShockCenterX PlanetNukeShockCenterX
#define nukeShockCenterY PlanetNukeShockCenterY
#define nukeShockStrength PlanetNukeShockStrength
