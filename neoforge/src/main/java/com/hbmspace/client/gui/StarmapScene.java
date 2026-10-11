// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 George Paton
// SPDX-FileCopyrightText: 2026 detree05
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbmspace.HbmSpace;
import com.hbmspace.client.gui.StardarMapRenderState.Colored;
import com.hbmspace.client.gui.StardarMapRenderState.Draw;
import com.hbmspace.client.gui.StardarMapRenderState.Textured;
import com.hbmspace.client.sky.PlanetInfo;
import com.hbmspace.client.sky.SkyPipelines;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Impact;
import com.hbmspace.dim.trait.CBT_Lights;
import com.hbmspace.handler.CelestialNukeShockHandler.ShockStatus;
import com.hbmspace.handler.CelestialNukeShockHandler;
import com.hbmspace.render.util.AtmosphereRenderUtil;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class StarmapScene {

    static final Identifier STARMAP = space("textures/gui/starmap3.png");
    static final Identifier SATELLITE = space("textures/gui/satellites/sat_scanner.png");
    private static final Identifier RINGS = space("textures/misc/space/rings.png");
    private static final Identifier IMPACT = space("textures/misc/space/impact.png");
    private static final Identifier DEFAULT_MASK = space("textures/misc/space/default_mask.png");
    private static final Identifier[] CITY_LIGHTS = {
        space("textures/misc/space/citylights_0.png"),
        space("textures/misc/space/citylights_1.png"),
        space("textures/misc/space/citylights_2.png"),
        space("textures/misc/space/citylights_3.png"),
    };

    private static final float BODY_SCALE = 0.45F;
    private static final float MOON_VISUAL_SCALE = 0.82F;

    private final Minecraft minecraft;
    private final Map<Identifier, Boolean> textureAlphaCache = new HashMap<>();
    private List<Draw> draws = new ArrayList<>();
    private float left;
    private float top;

    StarmapScene(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private static Identifier space(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }

    void begin(float left, float top) {
        this.draws = new ArrayList<>();
        this.left = left;
        this.top = top;
    }

    List<Draw> draws() {
        return draws;
    }

    void partialTex(
            RenderPipeline pipeline,
            Identifier texture,
            float r,
            float g,
            float b,
            float a,
            float x,
            float y,
            float w,
            float h,
            float u1,
            float v1,
            float u2,
            float v2) {
        draws.add(
                new Textured(
                        pipeline,
                        texture,
                        null,
                        null,
                        null,
                        r,
                        g,
                        b,
                        a,
                        x - left,
                        y - top,
                        x + w - left,
                        y + h - top,
                        u1,
                        v1,
                        u2,
                        v2,
                        0F,
                        0F,
                        0F));
    }

    void texturedQuad(
            RenderPipeline pipeline,
            Identifier texture,
            @Nullable Identifier texture1,
            @Nullable Identifier texture2,
            @Nullable PlanetInfo planet,
            float a,
            float x,
            float y,
            float size,
            float uOffset,
            float angle) {
        float half = size * 0.5F;
        float cx = x - left;
        float cy = y - top;
        draws.add(
                new Textured(
                        pipeline,
                        texture,
                        texture1,
                        texture2,
                        planet,
                        1F,
                        1F,
                        1F,
                        a,
                        cx - half,
                        cy - half,
                        cx + half,
                        cy + half,
                        uOffset,
                        0F,
                        1F + uOffset,
                        1F,
                        angle,
                        cx,
                        cy));
    }

    void rect(int x0, int y0, int x1, int y1, int argb) {
        float a = (argb >>> 24) / 255F;
        float r = (argb >> 16 & 255) / 255F;
        float g = (argb >> 8 & 255) / 255F;
        float b = (argb & 255) / 255F;
        float l = x0 - left;
        float t = y0 - top;
        float rt = x1 - left;
        float bt = y1 - top;
        draws.add(
                new Colored(
                        new float[] {
                            l, bt, r, g, b, a,
                            rt, bt, r, g, b, a,
                            rt, t, r, g, b, a,
                            l, t, r, g, b, a
                        }));
    }

    void line(
            float x0,
            float y0,
            float x1,
            float y1,
            float width,
            float r,
            float g,
            float b,
            float a) {
        float half = width * 0.5F / (float) minecraft.getWindow().getGuiScale();
        boolean xMajor = Math.abs(x1 - x0) >= Math.abs(y1 - y0);
        float dx = xMajor ? 0F : half;
        float dy = xMajor ? half : 0F;
        float ax = x0 - left;
        float ay = y0 - top;
        float bx = x1 - left;
        float by = y1 - top;
        draws.add(
                new Colored(
                        new float[] {
                            ax - dx, ay + dy, r, g, b, a, bx - dx, by + dy, r, g, b, a, bx + dx,
                            by - dy, r, g, b, a, ax + dx, ay - dy, r, g, b, a
                        }));
    }

    static float ringHalfWidth(CelestialBody body, float size) {
        return size * 0.5F * Math.max(1F, body.ringSize);
    }

    static float ringHalfHeight(CelestialBody body, float ringHalfWidth) {
        float ringTiltSin =
                Math.max(0.08F, Math.abs(Mth.sin((float) Math.toRadians(body.ringTilt))));
        return Math.max(0.5F, ringHalfWidth * ringTiltSin);
    }

    void body(
            CelestialBody body,
            float x,
            float y,
            float size,
            double dayTicks,
            float crescentPhase,
            long gameTime,
            int untexturedArgb) {
        float ringHalfWidth = body.hasRings ? ringHalfWidth(body, size) : 0F;
        float ringHalfHeight = body.hasRings ? ringHalfHeight(body, ringHalfWidth) : 0F;

        if (body.hasRings) ringHalf(body, x, y, ringHalfWidth, ringHalfHeight, false);

        if (body.texture != null) {
            if (body.parent == null) {
                texturedQuad(
                        SkyPipelines.TEXTURED_TRANSLUCENT,
                        body.texture,
                        null,
                        null,
                        null,
                        1F,
                        x,
                        y,
                        size,
                        0F,
                        0F);
            } else {
                atmosphereGlow(body, x, y, size, 1.0F);

                float phase = rotationPhase(body, dayTicks);
                float bodyRotationAngle = phase * 360F;
                boolean rotateBody = hasTransparentPixels(body.texture);
                float textureUOffset = 0F;
                if (rotateBody) {
                    texturedQuad(
                            SkyPipelines.TEXTURED_TRANSLUCENT,
                            body.texture,
                            null,
                            null,
                            null,
                            1F,
                            x,
                            y,
                            size,
                            0F,
                            bodyRotationAngle);
                } else {
                    texturedQuad(
                            SkyPipelines.TEXTURED_TRANSLUCENT,
                            body.texture,
                            null,
                            null,
                            null,
                            1F,
                            x,
                            y,
                            size,
                            phase,
                            0F);
                    textureUOffset = phase;
                }
                overlays(
                        body,
                        x,
                        y,
                        size,
                        crescentPhase,
                        rotateBody,
                        bodyRotationAngle,
                        dayTicks,
                        textureUOffset,
                        gameTime);
            }
        } else {
            float half = size * 0.5F;
            rect(
                    (int) (x - half),
                    (int) (y - half),
                    (int) (x + half),
                    (int) (y + half),
                    untexturedArgb);
        }

        if (body.hasRings) ringHalf(body, x, y, ringHalfWidth, ringHalfHeight, true);
    }

    private void ringHalf(
            CelestialBody body,
            float bodyScreenX,
            float bodyScreenY,
            float ringHalfWidth,
            float drawH,
            boolean frontHalf) {
        if (ringHalfWidth <= 0F || drawH <= 0F) return;

        float[] ringColor =
                body.ringColor != null && body.ringColor.length >= 3 ? body.ringColor : null;
        float r = ringColor != null ? ringColor[0] : 0.5F;
        float g = ringColor != null ? ringColor[1] : 0.5F;
        float b = ringColor != null ? ringColor[2] : 0.5F;
        float a = ringColor != null && ringColor.length >= 4 ? ringColor[3] : 1F;

        float drawX = bodyScreenX - ringHalfWidth;
        float drawY = frontHalf ? bodyScreenY : bodyScreenY - drawH;
        float drawW = ringHalfWidth * 2F;
        float v1 = frontHalf ? 0.5F : 0F;
        float v2 = frontHalf ? 1F : 0.5F;

        partialTex(
                SkyPipelines.TEXTURED_TRANSLUCENT,
                RINGS,
                r,
                g,
                b,
                a,
                drawX,
                drawY,
                drawW,
                drawH,
                0F,
                v1,
                1F,
                v2);
    }

    private void atmosphereGlow(
            CelestialBody body, double centerX, double centerY, double size, float visibility) {
        float glowAlpha = AtmosphereRenderUtil.getAtmosphereGlowAlpha(body) * visibility;
        if (glowAlpha <= 0.001F) return;

        Vec3 atmo = AtmosphereRenderUtil.getBodyAtmosphereColor(body);
        float r = Mth.clamp((float) atmo.x * 1.15F, 0.0F, 1.0F);
        float g = Mth.clamp((float) atmo.y * 1.15F, 0.0F, 1.0F);
        float b = Mth.clamp((float) atmo.z * 1.15F, 0.0F, 1.0F);

        double radius = size * 0.5D;
        float leading = glowAlpha;
        float trailing = glowAlpha;
        float cx = (float) centerX - left;
        float cy = (float) centerY - top;
        float inner = (float) (radius * 0.98D);
        float middle = (float) (radius * 1.075D);
        float outer = (float) (radius * 1.15D * (1.0D + glowAlpha * 0.25D));

        draws.add(
                new Colored(
                        new float[] {
                            cx - outer,
                            cy - outer,
                            r,
                            g,
                            b,
                            0F,
                            cx + outer,
                            cy - outer,
                            r,
                            g,
                            b,
                            0F,
                            cx + middle,
                            cy - middle,
                            r,
                            g,
                            b,
                            leading / 2F,
                            cx - middle,
                            cy - middle,
                            r,
                            g,
                            b,
                            trailing / 2F,
                            cx - middle,
                            cy - middle,
                            r,
                            g,
                            b,
                            trailing / 2F,
                            cx + middle,
                            cy - middle,
                            r,
                            g,
                            b,
                            leading / 2F,
                            cx + inner,
                            cy - inner,
                            r,
                            g,
                            b,
                            leading,
                            cx - inner,
                            cy - inner,
                            r,
                            g,
                            b,
                            trailing,
                            cx + outer,
                            cy - outer,
                            r,
                            g,
                            b,
                            0F,
                            cx + outer,
                            cy + outer,
                            r,
                            g,
                            b,
                            0F,
                            cx + middle,
                            cy + middle,
                            r,
                            g,
                            b,
                            leading / 2F,
                            cx + middle,
                            cy - middle,
                            r,
                            g,
                            b,
                            leading / 2F,
                            cx + middle,
                            cy - middle,
                            r,
                            g,
                            b,
                            leading / 2F,
                            cx + middle,
                            cy + middle,
                            r,
                            g,
                            b,
                            leading / 2F,
                            cx + inner,
                            cy + inner,
                            r,
                            g,
                            b,
                            leading,
                            cx + inner,
                            cy - inner,
                            r,
                            g,
                            b,
                            leading,
                            cx + outer,
                            cy + outer,
                            r,
                            g,
                            b,
                            0F,
                            cx - outer,
                            cy + outer,
                            r,
                            g,
                            b,
                            0F,
                            cx - middle,
                            cy + middle,
                            r,
                            g,
                            b,
                            trailing / 2F,
                            cx + middle,
                            cy + middle,
                            r,
                            g,
                            b,
                            leading / 2F,
                            cx + middle,
                            cy + middle,
                            r,
                            g,
                            b,
                            leading / 2F,
                            cx - middle,
                            cy + middle,
                            r,
                            g,
                            b,
                            trailing / 2F,
                            cx - inner,
                            cy + inner,
                            r,
                            g,
                            b,
                            trailing,
                            cx + inner,
                            cy + inner,
                            r,
                            g,
                            b,
                            leading,
                            cx - outer,
                            cy + outer,
                            r,
                            g,
                            b,
                            0F,
                            cx - outer,
                            cy - outer,
                            r,
                            g,
                            b,
                            0F,
                            cx - middle,
                            cy - middle,
                            r,
                            g,
                            b,
                            trailing / 2F,
                            cx - middle,
                            cy + middle,
                            r,
                            g,
                            b,
                            trailing / 2F,
                            cx - middle,
                            cy + middle,
                            r,
                            g,
                            b,
                            trailing / 2F,
                            cx - middle,
                            cy - middle,
                            r,
                            g,
                            b,
                            trailing / 2F,
                            cx - inner,
                            cy - inner,
                            r,
                            g,
                            b,
                            trailing,
                            cx - inner,
                            cy + inner,
                            r,
                            g,
                            b,
                            trailing
                        }));
    }

    private static PlanetInfo planetInfo(
            float phase,
            float offset,
            float patternOffset,
            float atmosphereTime,
            int atmosphereStyle,
            float impactTime,
            List<ShockStatus> nukeShocks,
            double dayTicks) {
        PlanetInfo info = new PlanetInfo();
        info.phase = phase;
        info.offset = offset;
        info.patternOffset = patternOffset;
        info.atmosphereTime = atmosphereTime;
        info.atmosphereStyle = atmosphereStyle;
        info.impactTime = impactTime;
        info.useBodyAlphaMask = 1;
        AtmosphereRenderUtil.applyNukeShockUniforms(info, nukeShocks, dayTicks);
        return info;
    }

    private void overlays(
            CelestialBody body,
            float bodyScreenX,
            float bodyScreenY,
            float drawSize,
            float phase,
            boolean rotateBody,
            float bodyRotationAngle,
            double dayTicks,
            float textureUOffset,
            long gameTime) {
        CBT_Impact impact = body.getTrait(CBT_Impact.class);
        CBT_Lights light = body.getTrait(CBT_Lights.class);
        List<ShockStatus> nukeShocks = CelestialNukeShockHandler.getClientShocks(body, gameTime);
        double impactTime = impact != null ? dayTicks - impact.time : 0.0D;
        float impactAnimationTime = impact != null ? (float) impactTime : -1.0F;
        int lightIntensity =
                light != null && impactTime < 40.0D
                        ? Mth.clamp(light.getIntensity(), 0, CITY_LIGHTS.length - 1)
                        : 0;
        int activeBlackouts = Math.max(0, Math.min((int) (impactTime / 8.0D), 5));
        float atmosphereAlpha = AtmosphereRenderUtil.getAtmosphereSurfaceAlpha(body);
        float atmosphereDensity = AtmosphereRenderUtil.getAtmosphereDensity(body);
        Vec3 atmosphereColor = AtmosphereRenderUtil.getBodyAtmosphereColor(body);
        Vec3 cloudColor = AtmosphereRenderUtil.getBodyCloudColor(body);
        float cloudTintStrength = AtmosphereRenderUtil.getBodyCloudTintStrength(body);
        float weatherPartialTicks = (float) (dayTicks - Math.floor(dayTicks));
        float cloudStormDarkness =
                AtmosphereRenderUtil.getBodyCloudStormDarkness(body, weatherPartialTicks);
        float cloudLightningStrength =
                AtmosphereRenderUtil.getBodyCloudLightningStrength(body, weatherPartialTicks);
        int atmosphereStyle = AtmosphereRenderUtil.getAtmosphereStyle(body);
        float atmosphereTime = (float) (dayTicks / 20.0D);
        float atmospherePatternOffset = textureUOffset;
        if (!rotateBody) {
            double period = body.getRotationalPeriod();
            if (period > 0D && !Double.isNaN(period) && !Double.isInfinite(period)) {
                atmospherePatternOffset = (float) (dayTicks / period);
            }
        }
        float angle = rotateBody ? bodyRotationAngle : 0F;
        boolean lit =
                atmosphereStyle == AtmosphereRenderUtil.ATMOSPHERE_STYLE_CLOUDS
                        || atmosphereStyle == AtmosphereRenderUtil.ATMOSPHERE_STYLE_HAZE;
        Identifier cityMask = body.cityMask != null ? body.cityMask : DEFAULT_MASK;

        if (atmosphereAlpha > 0.001F) {
            PlanetInfo info =
                    planetInfo(
                            0F,
                            textureUOffset,
                            atmospherePatternOffset,
                            atmosphereTime,
                            atmosphereStyle,
                            impactAnimationTime,
                            nukeShocks,
                            dayTicks);
            info.atmosphereColorR = (float) atmosphereColor.x;
            info.atmosphereColorG = (float) atmosphereColor.y;
            info.atmosphereColorB = (float) atmosphereColor.z;
            info.cloudColorR = (float) cloudColor.x;
            info.cloudColorG = (float) cloudColor.y;
            info.cloudColorB = (float) cloudColor.z;
            info.cloudTintStrength = cloudTintStrength;
            info.cloudStormDarkness = cloudStormDarkness;
            info.atmosphereAlpha = atmosphereAlpha;
            texturedQuad(
                    SkyPipelines.ATMOSPHERE,
                    body.texture,
                    null,
                    null,
                    info,
                    1F,
                    bodyScreenX,
                    bodyScreenY,
                    drawSize,
                    0F,
                    angle);
        }

        PlanetInfo crescent = new PlanetInfo();
        crescent.phase = phase;
        crescent.offset = textureUOffset;
        crescent.useBodyAlphaMask = 1;
        texturedQuad(
                SkyPipelines.CRESCENT,
                body.texture,
                null,
                null,
                crescent,
                1F,
                bodyScreenX,
                bodyScreenY,
                drawSize,
                0F,
                angle);

        if (lightIntensity > 0 && atmosphereDensity > 0.001F && lit) {
            PlanetInfo info =
                    planetInfo(
                            phase,
                            textureUOffset,
                            atmospherePatternOffset,
                            atmosphereTime,
                            atmosphereStyle,
                            impactAnimationTime,
                            nukeShocks,
                            dayTicks);
            info.atmosphereDensity = atmosphereDensity;
            info.blackouts = activeBlackouts;
            texturedQuad(
                    StardarMapRenderer.ATMOSPHERE_EMISSIVE_MASKED,
                    CITY_LIGHTS[lightIntensity],
                    cityMask,
                    body.texture,
                    info,
                    1F,
                    bodyScreenX,
                    bodyScreenY,
                    drawSize,
                    0F,
                    angle);
        }

        if (lightIntensity > 0) {
            PlanetInfo info =
                    planetInfo(
                            phase,
                            textureUOffset,
                            atmospherePatternOffset,
                            atmosphereTime,
                            atmosphereStyle,
                            impactAnimationTime,
                            nukeShocks,
                            dayTicks);
            info.atmosphereDensity = atmosphereDensity;
            info.blackouts = activeBlackouts;
            texturedQuad(
                    StardarMapRenderer.NIGHT_LIGHTS_MASKED,
                    CITY_LIGHTS[lightIntensity],
                    cityMask,
                    body.texture,
                    info,
                    1F,
                    bodyScreenX,
                    bodyScreenY,
                    drawSize,
                    0F,
                    angle);
        }

        if (atmosphereAlpha > 0.001F && cloudLightningStrength > 0.001F && lit) {
            PlanetInfo info =
                    planetInfo(
                            phase,
                            textureUOffset,
                            atmospherePatternOffset,
                            atmosphereTime,
                            atmosphereStyle,
                            impactAnimationTime,
                            nukeShocks,
                            dayTicks);
            info.cloudTintStrength = cloudTintStrength;
            info.cloudLightningStrength = cloudLightningStrength;
            info.atmosphereAlpha = atmosphereAlpha;
            info.eveFlashStrength =
                    AtmosphereRenderUtil.getBodyEveFlashStrength(body, atmosphereTime);
            info.lightningMode = AtmosphereRenderUtil.getBodyLightningMode(body);
            texturedQuad(
                    SkyPipelines.LIGHTNING,
                    body.texture,
                    cityMask,
                    null,
                    info,
                    1F,
                    bodyScreenX,
                    bodyScreenY,
                    drawSize,
                    0F,
                    angle);
        }

        if (impact != null) {
            float lavaAlpha = (float) Math.min(impactTime * 0.1D, 1.0D);
            if (lavaAlpha > 0F) {
                texturedQuad(
                        SkyPipelines.TEXTURED_ADDITIVE_NO_CULL,
                        IMPACT,
                        null,
                        null,
                        null,
                        lavaAlpha,
                        bodyScreenX,
                        bodyScreenY,
                        drawSize,
                        rotateBody ? 0F : textureUOffset,
                        angle);
            }
        }

        if (!nukeShocks.isEmpty()) {
            PlanetInfo info = new PlanetInfo();
            info.phase = phase;
            AtmosphereRenderUtil.applyNukeShockUniforms(info, nukeShocks, dayTicks);
            float half = drawSize * 0.5F;
            float cx = bodyScreenX - left;
            float cy = bodyScreenY - top;
            draws.add(
                    new Textured(
                            SkyPipelines.NUKE,
                            null,
                            null,
                            null,
                            info,
                            1F,
                            1F,
                            1F,
                            1F,
                            cx - half,
                            cy - half,
                            cx + half,
                            cy + half,
                            0F,
                            0F,
                            1F,
                            1F,
                            angle,
                            cx,
                            cy));
        }
    }

    private boolean hasTransparentPixels(Identifier texture) {
        return textureAlphaCache.computeIfAbsent(
                texture,
                id -> {
                    Optional<Resource> resource = minecraft.getResourceManager().getResource(id);
                    if (resource.isEmpty()) return false;
                    try (InputStream stream = resource.get().open();
                            NativeImage image = NativeImage.read(stream)) {
                        for (int y = 0; y < image.getHeight(); y++) {
                            for (int x = 0; x < image.getWidth(); x++) {
                                if ((image.getPixel(x, y) >>> 24 & 255) < 255) return true;
                            }
                        }
                        return false;
                    } catch (IOException e) {
                        return false;
                    }
                });
    }

    static float rotationPhase(CelestialBody body, double dayTicks) {
        double period = body.getRotationalPeriod();
        if (period <= 0D || Double.isNaN(period) || Double.isInfinite(period)) return 0F;
        return (float) ((dayTicks % period) / period);
    }

    static float bodySizePxAt1x(CelestialBody body) {
        if (body.parent == null) return 36F * BODY_SCALE;
        if (isMoon(body)) return moonSizePxAt1x(body);
        float bodySizeAt1x = body.radiusKm * (36F / 261_600F) * 2.6F;
        bodySizeAt1x = Mth.clamp(bodySizeAt1x, 0.8F, 2.0F);
        return bodySizeAt1x * BODY_SCALE;
    }

    static float moonSizePxAt1x(CelestialBody moon) {
        float moonRadiusKm = Math.max(0F, moon.radiusKm);
        float radiusRangeKm = 500F - 65F;
        float t = (moonRadiusKm - 65F) / radiusRangeKm;
        t = Mth.clamp(t, 0F, 1F);
        return (0.2F + (0.5F - 0.2F) * t) * BODY_SCALE * MOON_VISUAL_SCALE;
    }

    static boolean isMoon(@Nullable CelestialBody body) {
        return body != null && body.parent != null && body.parent.parent != null;
    }
}
