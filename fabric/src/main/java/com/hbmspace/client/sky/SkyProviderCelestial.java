/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-FileCopyrightText: 2025-2026 JamesH_2
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * Copyright (C) 2025-2026 JamesH_2. All rights reserved.
 * Copyright (C) 2026 detree05. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.client.sky;

import com.hbm.api.client.ShaderPacks;
import com.hbm.client.render.NTMSkybox;
import com.hbm.extprop.LivingHazards;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbm.util.GameTime;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem.AstroMetric;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.dim.trait.CBT_Destroyed;
import com.hbmspace.dim.trait.CBT_Dyson;
import com.hbmspace.dim.trait.CBT_Impact;
import com.hbmspace.dim.trait.CBT_Lights;
import com.hbmspace.dim.trait.CBT_War;
import com.hbmspace.dim.trait.CelestialBodyTrait.CBT_COMPROMISED;
import com.hbmspace.handler.CelestialNukeShockHandler.ShockStatus;
import com.hbmspace.handler.CelestialNukeShockHandler;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.WarItemsSpace;
import com.hbmspace.render.util.AtmosphereRenderUtil;
import com.hbmspace.saveddata.satellites.ClientSatellites;
import com.hbmspace.saveddata.satellites.SatelliteOrbit;
import com.hbmspace.saveddata.satellites.SatellitesSpace;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public class SkyProviderCelestial {

    private static final Identifier PLANET = space("planet.png");
    private static final Identifier FLARE = space("sunspike.png");
    private static final Identifier NIGHT = space("night.png");
    private static final Identifier DIGAMMA_STAR = space("star_digamma.png");
    private static final Identifier LODE_STAR = ntm("textures/misc/star_lode.png");
    private static final Identifier STATION = space("station.png");
    private static final Identifier SATELLITE = space("satellite.png");
    private static final Identifier IMPACT = space("impact.png");
    private static final Identifier SHOCKWAVE = ntm("textures/particle/shockwave.png");
    private static final Identifier SHOCK_FLARE = ntm("textures/particle/flare.png");
    private static final Identifier SHOCK_SHIELD = space("cens.png");
    private static final SkyMesh SHOCK_PLANE =
            SkyMesh.obj(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "models/plane.obj"));
    private static final SkyMesh FLESH_SPHERE =
            SkyMesh.obj(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "models/spherev2.obj"));
    private static final Identifier RINGS = space("rings.png");
    private static final Identifier DESTROYED = space("destroyed.png");
    private static final Identifier NOISE = space("ichannel1.png");
    private static final Identifier PARTICLE_BASE = ntm("textures/particle/particle_base.png");
    private static final Identifier DEFAULT_MASK = space("default_mask.png");
    private static final Identifier[] CITY_LIGHTS = {
        space("citylights_0.png"),
        space("citylights_1.png"),
        space("citylights_2.png"),
        space("citylights_3.png"),
    };

    private static final float BOBMAZON_R = 1.0F;
    private static final float BOBMAZON_G = 0.534F;
    private static final float BOBMAZON_B = 0.385F;
    private static final float DEFAULT_ALTITUDE_KM = 100;

    private static final SkyMesh DISC =
            new SkyMesh(
                    "Space sky disc",
                    DefaultVertexFormat.POSITION,
                    PrimitiveTopology.TRIANGLE_FAN,
                    buffer -> {
                        buffer.addVertex(0.0F, 16.0F, 0.0F);
                        for (int i = -180; i <= 180; i += 45) {
                            buffer.addVertex(
                                    512.0F * Mth.cos(i * (float) (Math.PI / 180.0)),
                                    16.0F,
                                    512.0F * Mth.sin(i * (float) (Math.PI / 180.0)));
                        }
                    });
    private static final SkyMesh NIGHT_CUBE =
            new SkyMesh(
                    "Space night sky",
                    DefaultVertexFormat.POSITION_TEX,
                    PrimitiveTopology.QUADS,
                    buffer -> {
                        Matrix4f face =
                                new Matrix4f()
                                        .rotate(Axis.XP.rotationDegrees(90.0F))
                                        .rotate(Axis.ZP.rotationDegrees(-90.0F));
                        nightSide(buffer, face, 4);
                        nightSide(
                                buffer,
                                new Matrix4f(face).rotate(Axis.XP.rotationDegrees(90.0F)),
                                1);
                        nightSide(
                                buffer,
                                new Matrix4f(face).rotate(Axis.XP.rotationDegrees(-90.0F)),
                                0);
                        for (int side : new int[] {5, 2, 3})
                            nightSide(buffer, face.rotate(Axis.ZP.rotationDegrees(90.0F)), side);
                    });
    private static final SkyMesh RING_BACK =
            new SkyMesh(
                    "Space ring back",
                    DefaultVertexFormat.POSITION_TEX,
                    PrimitiveTopology.QUADS,
                    buffer -> {
                        buffer.addVertex(-1.0F, 0.0F, -1.0F).setUv(0.0F, 0.0F);
                        buffer.addVertex(1.0F, 0.0F, -1.0F).setUv(1.0F, 0.0F);
                        buffer.addVertex(1.0F, 0.0F, 0.0F).setUv(1.0F, 0.5F);
                        buffer.addVertex(-1.0F, 0.0F, 0.0F).setUv(0.0F, 0.5F);
                    });
    private static final SkyMesh RING_FRONT =
            new SkyMesh(
                    "Space ring front",
                    DefaultVertexFormat.POSITION_TEX,
                    PrimitiveTopology.QUADS,
                    buffer -> {
                        buffer.addVertex(-1.0F, 0.0F, 0.0F).setUv(0.0F, 0.5F);
                        buffer.addVertex(1.0F, 0.0F, 0.0F).setUv(1.0F, 0.5F);
                        buffer.addVertex(1.0F, 0.0F, 1.0F).setUv(1.0F, 1.0F);
                        buffer.addVertex(-1.0F, 0.0F, 1.0F).setUv(0.0F, 1.0F);
                    });
    private static final SkyMesh RING_WALL =
            new SkyMesh(
                    "Space ring wall",
                    DefaultVertexFormat.POSITION_TEX,
                    PrimitiveTopology.QUADS,
                    buffer -> {
                        buffer.addVertex(0.0F, -1.0F, -1.0F).setUv(0.0F, 0.0F);
                        buffer.addVertex(0.0F, 1.0F, -1.0F).setUv(1.0F, 0.0F);
                        buffer.addVertex(0.0F, 1.0F, 1.0F).setUv(1.0F, 1.0F);
                        buffer.addVertex(0.0F, -1.0F, 1.0F).setUv(0.0F, 1.0F);
                    });
    private static final SkyMesh ORBIT =
            new SkyMesh(
                    "Space satellite orbit",
                    DefaultVertexFormat.POSITION_COLOR,
                    PrimitiveTopology.DEBUG_LINE_STRIP,
                    buffer -> {
                        for (int i = 0; i <= 72; i++) {
                            double angle = Math.PI * 2.0D * (i % 72) / 72.0D;
                            buffer.addVertex(0.0F, (float) Math.cos(angle), (float) Math.sin(angle))
                                    .setColor(1.0F, 1.0F, 1.0F, 1.0F);
                        }
                    });
    private static final float[] BILLBOARD = {
        -0.5F, -0.25F, 1.0F, 0.0F,
        0.5F, -0.25F, 0.0F, 0.0F,
        0.5F, 0.75F, 0.0F, 1.0F,
        -0.5F, 0.75F, 1.0F, 1.0F,
    };
    private static final float SMOKE_FADE = 100f * 0.35F;
    private static final int SMOKE_SHADES = (int) SMOKE_FADE + 1;

    protected ClientLevel level;
    protected float partialTicks;
    protected CelestialBody body;
    protected List<AstroMetric> metrics = List.of();
    protected float solarAngle;
    protected float sunBrightness;
    protected Vec3 feet = Vec3.ZERO;
    protected double feetY;
    protected float cameraYaw;
    protected float cameraPitch;
    protected float fov;
    protected float digamma;
    private float celestialFogDensity;
    private int celestialFogColor;
    protected boolean belowHorizon;
    private final Map<Identifier, Integer> sheetTexels = new HashMap<>();
    private final List<List<Meteors.Meteor>> meteorShades =
            Stream.<List<Meteors.Meteor>>generate(ArrayList::new).limit(SMOKE_SHADES + 1).toList();

    private static Identifier space(String name) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/misc/space/" + name);
    }

    private static Identifier ntm(String path) {
        return Identifier.fromNamespaceAndPath("hbm", path);
    }

    public void extract(
            ClientLevel level, float partialTicks, Camera camera, SkyRenderState state) {
        this.level = level;
        this.partialTicks = partialTicks;
        body = CelestialBody.getBody(level);
        solarAngle = state.sunAngle / ((float) Math.PI * 2.0F);
        metrics = SolarSystem.calculateMetricsFromBody(level, partialTicks, body, solarAngle);
        sunBrightness =
                camera.attributeProbe()
                        .getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, partialTicks);
        extractViewer(camera, partialTicks);
        belowHorizon = belowHorizon(state, partialTicks);
        celestialFogDensity = CelestialFogTint.density(level, camera);
        celestialFogColor =
                ARGB.colorFromVector3f(
                        camera.attributeProbe()
                                .getValue(EnvironmentAttributes.FOG_COLOR, partialTicks));
    }

    private boolean belowHorizon(SkyRenderState state, float partialTicks) {
        if (level.dimension() == Level.OVERWORLD) return state.shouldRenderDarkDisc;
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        double horizon = Objects.requireNonNull(WorldProviderCelestial.of(level)).getHorizon(level);
        return player.getEyePosition(partialTicks).y < horizon && !player.isUnderWater();
    }

    protected void extractViewer(Camera camera, float partialTicks) {
        Entity viewer = Objects.requireNonNull(Minecraft.getInstance().getCameraEntity());
        feet = viewer.getPosition(partialTicks);
        feetY = viewer.getY();
        cameraYaw = camera.yRot();
        cameraPitch = camera.xRot();
        fov = camera.getFov();
        digamma =
                Minecraft.getInstance().player == null
                        ? 0
                        : (float) LivingHazards.digamma(Minecraft.getInstance().player);
    }

    public void render(SkyRenderer vanilla, SkyRenderState state, SkyDraws draws) {
        CelestialBody sun = body.getStar();
        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);

        boolean hasAtmosphere = atmosphere != null;
        float pressure = hasAtmosphere ? (float) atmosphere.getPressure() : 0.0F;
        float visibility = hasAtmosphere ? Mth.clamp(2.0F - pressure, 0.1F, 1.0F) : 1.0F;

        float skyR = state.skyColor.x();
        float skyG = state.skyColor.y();
        float skyB = state.skyColor.z();

        if (feetY > 300) {
            float curvature = Mth.clamp((800.0F - (float) feetY) / 500.0F, 0.0F, 1.0F);
            skyR *= curvature;
            skyG *= curvature;
            skyB *= curvature;
        }

        Vec3 planetTint =
                CelestialFogTint.tint(skyR, skyG, skyB, celestialFogColor, celestialFogDensity);

        renderSkyDisc(vanilla, draws, skyR, skyG, skyB);

        float starBrightness = state.starBrightness * visibility;
        float siderealAngle = (float) SolarSystem.calculateSiderealAngle(level, partialTicks, body);

        renderSunset(vanilla, state, draws, pressure);

        renderStars(draws, starBrightness, solarAngle + siderealAngle, body.axialTilt);

        PoseStack pose = new PoseStack();
        pose.pushPose();
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(body.axialTilt));
        pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(-90.0F));
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(solarAngle * 360.0F));

        double sunSize = SolarSystem.calculateSunSize(body) * SolarSystem.SUN_RENDER_SCALE;
        double coronaSize = sunSize * (3 - Mth.clamp(pressure, 0.0F, 1.0F));

        renderSun(draws, pose, sun, sunSize, coronaSize, visibility, pressure);

        float blendAmount =
                hasAtmosphere
                        ? Mth.clamp(
                                1
                                        - WorldProviderCelestial.getSunBrightnessFactor(
                                                level, solarAngle),
                                0.25F,
                                1F)
                        : 1F;

        renderCelestials(
                draws,
                pose,
                metrics,
                planetTint,
                visibility,
                blendAmount,
                null,
                SolarSystem.MAX_APPARENT_SIZE_SURFACE);

        if (visibility > 0.2F) {
            if (level.dimension() == Level.OVERWORLD) {
                renderSatellite(
                        draws, pose, BOBMAZON_R, BOBMAZON_G, BOBMAZON_B, 0, DEFAULT_ALTITUDE_KM, 0);
            }

            renderPlayerSatellites(draws, pose);
            renderHeldSatellitePreview(draws, pose);

            for (OrbitalStation station : OrbitalStation.orbitingStations) {
                renderStation(draws, pose, station);
            }
        }
        pose.popPose();

        render3DModel(draws);

        renderWarShocks(draws);

        renderMeteors(draws);

        if (body.hasRings) {
            pose.pushPose();
            pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(body.axialTilt - body.ringTilt));
            pose.translate(0, -100, 0);
            pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(-90.0F));

            renderRings(draws, pose, body.ringColor, 200, visibility);
            pose.popPose();
        }

        renderSpecialEffects(draws);

        renderFleshShell(draws);

        draws.flush();

        if (belowHorizon) {
            try (RenderPass pass = draws.beginPass()) {
                vanilla.renderDarkDisc(pass);
            }
        }

        renderSurfaceFromAbove(draws);
        draws.flush();
    }

    protected static void renderSkyDisc(
            SkyRenderer vanilla, SkyDraws draws, float r, float g, float b) {
        if (ShaderPacks.inUse()) {
            try (RenderPass pass = draws.beginPass()) {
                vanilla.renderSkyDisc(pass, new Vector3f(r, g, b));
            }
            return;
        }
        draws.mesh(
                DISC, SkyPipelines.SKY_DISC, new PoseStack(), SkyDraws.IDENTITY, r, g, b, 1, null);
        draws.flush();
    }

    private void renderSurfaceFromAbove(SkyDraws draws) {
        float alpha = ((float) feet.y - 200.0F) / 300.0F;
        if (alpha <= 0) return;

        double sc = 1 / (feet.y / 1000);
        double uvOffset = (feet.x / 1024) % 1;

        PoseStack pose = new PoseStack();
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(180));

        draws.mesh(
                SkyMesh.QUAD,
                SkyPipelines.TEXTURED_TRANSLUCENT,
                pose,
                SkyDraws.lifted(100.0F, (float) (115 * sc)),
                sunBrightness,
                sunBrightness,
                sunBrightness,
                alpha,
                body.texture,
                null,
                null,
                0,
                (float) uvOffset);
    }

    protected void renderSunset(
            SkyRenderer vanilla, SkyRenderState state, SkyDraws draws, float pressure) {
        float[] sunsetColor = calcSunriseSunsetColors(state, pressure);
        if (sunsetColor == null) return;

        try (RenderPass pass = draws.beginPass()) {
            vanilla.renderSunriseAndSunset(
                    pass,
                    new PoseStack(),
                    state.sunAngle,
                    new Vector4f(sunsetColor[0], sunsetColor[1], sunsetColor[2], sunsetColor[3]));
        }

        if (pressure < 0.05F) {
            Random rand = new Random(0);
            float sunsetDirection = Mth.sin(state.sunAngle) < 0.0F ? 180.0F : 0.0F;
            double time = ((double) level.getDefaultClockTime() + partialTicks) * 0.002;

            PoseStack pose = new PoseStack();
            pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(90.0F));
            pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees(sunsetDirection));
            pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees(90.0F));

            int texel =
                    sheetTexels.computeIfAbsent(
                            Objects.requireNonNull(body.surfaceTexture),
                            SkyProviderCelestial::firstTexel);
            float r = (0.5F + rand.nextFloat() * 0.5F) * ARGB.redFloat(texel);
            float g = (0.5F + rand.nextFloat() * 0.5F) * ARGB.greenFloat(texel);
            float b = (0.5F + rand.nextFloat() * 0.5F) * ARGB.blueFloat(texel);
            float a = rand.nextFloat() * sunsetColor[3] * 4.0F * ARGB.alphaFloat(texel);

            VertexConsumer points =
                    draws.draw(SkyPipelines.POINTS_ADDITIVE, pose, r, g, b, a, null);
            for (int i = 0; i < 1024; i++) {
                double x = rand.nextGaussian() * 50;
                double z =
                        -((Math.abs(rand.nextGaussian() * 20) + time)
                                % Math.abs(rand.nextGaussian())
                                * 20);
                points.addVertex((float) x, 100.0F, (float) z);
            }
        }
    }

    private static int firstTexel(Identifier sheet) {
        try (InputStream stream =
                        Minecraft.getInstance()
                                .getResourceManager()
                                .getResourceOrThrow(sheet)
                                .open();
                NativeImage image = NativeImage.read(stream)) {
            return image.getPixel(0, 0);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    void close() {
        sheetTexels.clear();
    }

    protected float @Nullable [] calcSunriseSunsetColors(SkyRenderState state, float pressure) {
        if (pressure < 0.05F) {
            float cutoff = 0.4F;
            float angle = Mth.cos(solarAngle * (float) Math.PI * 2.0F);

            if (angle < -cutoff || angle > cutoff) return null;

            float colorIntensity = angle / cutoff * 0.5F + 0.5F;
            float alpha = 1.0F - (1.0F - Mth.sin(colorIntensity * (float) Math.PI)) * 0.99F;
            alpha *= alpha;
            return new float[] {0.9F, 1.0F, 1.0F, alpha * 0.2F};
        }

        var color = state.sunriseAndSunsetColor;
        if (color.w() == 0) return null;
        return new float[] {color.x(), color.y(), color.z(), color.w()};
    }

    protected void renderStars(
            SkyDraws draws, float starBrightness, float siderealAngle, float axialTilt) {
        if (NTMSkybox.lodeStarVisible()) {
            float lodeSize = 1F + level.getRandom().nextFloat() * 0.5F;
            float lodeDist = 100F;

            PoseStack pose = new PoseStack();
            pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(-75.0F));
            pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(10.0F));
            draws.mesh(
                    SkyMesh.QUAD_TRANSPOSED,
                    SkyPipelines.TEXTURED_ADDITIVE,
                    pose,
                    SkyDraws.lifted(lodeDist, lodeSize),
                    1,
                    1,
                    1,
                    1,
                    LODE_STAR);
        }

        PoseStack pose = new PoseStack();
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(axialTilt));
        pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(-90.0F));
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(siderealAngle * 360.0F));
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(-90.0F));

        if (starBrightness > 0.0F) {
            draws.mesh(
                    NIGHT_CUBE,
                    SkyPipelines.TEXTURED_ADDITIVE_ALPHA,
                    pose,
                    SkyDraws.IDENTITY,
                    1,
                    1,
                    1,
                    starBrightness * 0.6F,
                    NIGHT);
        }

        float brightness = Math.max(0.5F, starBrightness * 1.6F);
        float digmaSize = 1 + digamma * 0.25F;
        float digmaDist = 100F - digamma * 2.5F;

        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(140.0F));
        pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees(-40.0F));

        draws.mesh(
                SkyMesh.QUAD_TRANSPOSED,
                SkyPipelines.TEXTURED_ADDITIVE,
                pose,
                SkyDraws.lifted(digmaDist, digmaSize),
                brightness,
                brightness,
                brightness,
                brightness,
                DIGAMMA_STAR);
    }

    private static void nightSide(VertexConsumer buffer, Matrix4f face, int side) {
        float u = side % 3 / 3.0F;
        float v = side / 3 / 2.0F;
        buffer.addVertex(face, -100.0F, -100.0F, -100.0F).setUv(u, v);
        buffer.addVertex(face, -100.0F, -100.0F, 100.0F).setUv(u, v + 0.5F);
        buffer.addVertex(face, 100.0F, -100.0F, 100.0F).setUv(u + 0.33333334F, v + 0.5F);
        buffer.addVertex(face, 100.0F, -100.0F, -100.0F).setUv(u + 0.33333334F, v);
    }

    protected void renderSun(
            SkyDraws draws,
            PoseStack pose,
            CelestialBody sun,
            double sunSize,
            double coronaSize,
            float visibility,
            float pressure) {
        CBT_Dyson dyson = sun.getTrait(CBT_Dyson.class);
        int swarmCount = dyson != null ? dyson.size() : 0;

        if (sun.shader != null && sun.hasTrait(CBT_Destroyed.class)) {
            float shaderSize = (float) (sunSize * sun.shaderScale);
            float time = ((float) level.getDefaultClockTime() + partialTicks) / 20.0F;

            pose.pushPose();
            pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(-90.0F));
            draws.mesh(
                    SkyMesh.QUAD,
                    SkyPipelines.BLACK_HOLE,
                    pose,
                    SkyDraws.lifted(100.0F, shaderSize),
                    1,
                    1,
                    1,
                    1,
                    NOISE,
                    null,
                    null,
                    time,
                    0);
            pose.popPose();
            return;
        }

        draws.mesh(
                SkyMesh.QUAD_FLAT,
                SkyPipelines.FLAT_ADDITIVE_DEPTH_WRITE,
                pose,
                SkyDraws.lifted(100.1F, (float) (sunSize * 0.25D)),
                0,
                0,
                0,
                0,
                null);

        float[] sunColor =
                WorldProviderCelestial.of(level) != null
                        ? Objects.requireNonNull(WorldProviderCelestial.of(level)).getSunColor()
                        : new float[] {1.0F, 1.0F, 1.0F};

        draws.mesh(
                SkyMesh.QUAD,
                SkyPipelines.TEXTURED_ADDITIVE,
                pose,
                SkyDraws.lifted(100.0F, (float) sunSize),
                sunColor[0],
                sunColor[1],
                sunColor[2],
                visibility,
                SolarSystem.kerbol.texture);

        draws.mesh(
                SkyMesh.QUAD,
                SkyPipelines.TEXTURED_ADDITIVE,
                pose,
                SkyDraws.lifted(99.9F, (float) coronaSize),
                sunColor[0],
                sunColor[1],
                sunColor[2],
                1 - Mth.clamp(pressure, 0.0F, 1.0F) * 0.75F,
                FLARE);

        renderSwarm(draws, pose, sunSize * 0.5, swarmCount);

        draws.clearDepth();
    }

    private void renderSwarm(SkyDraws draws, PoseStack pose, double swarmRadius, int swarmCount) {
        if (swarmCount <= 0) return;

        float swarmScreenSize =
                (float)
                        ((Minecraft.getInstance().getWindow().getHeight() / fov)
                                * swarmRadius
                                * 0.002);
        float time = ((float) level.getDefaultClockTime() + partialTicks) / 800.0F;
        float alpha = Mth.clamp(swarmScreenSize, 0, 1);

        pose.pushPose();
        pose.translate(0.0F, 100.0F, 0.0F);
        pose.scale((float) swarmRadius, (float) swarmRadius, (float) swarmRadius);

        for (int ring = 0; ring < 3; ring++) {
            pose.pushPose();
            if (ring == 1) pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(60.0F));
            if (ring == 2) pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(-60.0F));
            pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(80.0F));

            VertexConsumer points =
                    draws.draw(SkyPipelines.SWARM, pose, 0, 0, 0, alpha, null, null, null, time);
            for (int i = ring; i < swarmCount; i += 3) {
                float t = i + time;
                points.addVertex((float) Math.cos(t), 0, (float) Math.sin(t));
            }
            pose.popPose();
        }
        pose.popPose();
    }

    protected void renderCelestials(
            SkyDraws draws,
            PoseStack pose,
            List<AstroMetric> metrics,
            Vec3 planetTint,
            float visibility,
            float blendAmount,
            @Nullable CelestialBody orbiting,
            float maxSize) {
        float blendDarken = 0.1F;

        double transitionMinSize = 0.01D;
        double transitionMaxSize = 0.5D;

        double worldTime = (double) level.getDefaultClockTime() + partialTicks;
        double totalTime = (double) level.getGameTime() + partialTicks;

        for (AstroMetric metric : metrics) {
            if (metric.distance == 0) continue;

            CelestialBody other = metric.body;
            boolean orbitingThis = other == orbiting;

            double uvOffset = orbitingThis ? 1 - ((worldTime / 1024) % 1) : 0;
            double atmospherePatternOffset = orbitingThis ? -(worldTime / 1024.0D) : 0.0D;
            float axialTilt = orbitingThis ? 0 : other.axialTilt;

            pose.pushPose();

            double size = Mth.clamp(metric.apparentSize, 0, maxSize);
            boolean renderPoint = size < transitionMaxSize;
            boolean renderBody = size > transitionMinSize;

            pose.rotate((Quaternionfc) Axis.XP.rotationDegrees((float) metric.angle));
            pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees((float) metric.inclination));
            pose.rotate((Quaternionfc) Axis.YP.rotationDegrees(axialTilt + 90.0F));

            float s = (float) size;
            Matrix4f square = SkyDraws.lifted(100.0F, s);
            if (renderBody) {
                if (other.hasRings) {
                    renderRingHalf(draws, pose, metric, s, visibility, true);
                }

                CBT_Destroyed destroyed = other.getTrait(CBT_Destroyed.class);

                if (destroyed != null) {
                    renderDestroyed(draws, pose, other, destroyed, s, (float) uvOffset);
                } else {
                    renderAtmosphereGlow(draws, pose, other, size, 1.0F);

                    draws.mesh(
                            SkyMesh.QUAD,
                            SkyPipelines.TEXTURED_OPAQUE,
                            pose,
                            square,
                            1,
                            1,
                            1,
                            visibility,
                            other.texture,
                            null,
                            null,
                            0,
                            (float) uvOffset);

                    CBT_Impact impact = other.getTrait(CBT_Impact.class);
                    CBT_Lights light = other.getTrait(CBT_Lights.class);
                    List<ShockStatus> nukeShocks =
                            CelestialNukeShockHandler.getClientShocks(other, level.getGameTime());

                    double impactTime =
                            impact != null ? (level.getGameTime() - impact.time) + partialTicks : 0;
                    float impactAnimationTime = impact != null ? (float) impactTime : -1.0F;
                    int lightIntensity =
                            light != null && impactTime < 40
                                    ? Mth.clamp(light.getIntensity(), 0, CITY_LIGHTS.length - 1)
                                    : 0;

                    int blackoutInterval = 8;
                    int maxBlackouts = 5;
                    int activeBlackouts =
                            Math.min((int) (impactTime / blackoutInterval), maxBlackouts);

                    Vec3 atmosphereColor = AtmosphereRenderUtil.getBodyAtmosphereColor(other);
                    Vec3 cloudColor = AtmosphereRenderUtil.getBodyCloudColor(other);
                    float cloudTintStrength = AtmosphereRenderUtil.getBodyCloudTintStrength(other);
                    float cloudStormDarkness =
                            AtmosphereRenderUtil.getBodyCloudStormDarkness(other, partialTicks);
                    float cloudLightningStrength =
                            AtmosphereRenderUtil.getBodyCloudLightningStrength(other, partialTicks);
                    float atmosphereOverlayAlpha =
                            AtmosphereRenderUtil.getAtmosphereSurfaceAlpha(other);
                    float atmosphereDensity = AtmosphereRenderUtil.getAtmosphereDensity(other);
                    int atmosphereStyle = AtmosphereRenderUtil.getAtmosphereStyle(other);

                    float atmosphereTime = (float) (totalTime / 20.0F);
                    float phase = (float) -metric.phase;

                    PlanetInfo shared = new PlanetInfo();
                    shared.offset = (float) uvOffset;
                    shared.patternOffset = (float) atmospherePatternOffset;
                    shared.atmosphereTime = atmosphereTime;
                    shared.atmosphereStyle = atmosphereStyle;
                    shared.impactTime = impactAnimationTime;
                    AtmosphereRenderUtil.applyNukeShockUniforms(shared, nukeShocks, totalTime);

                    if (atmosphereOverlayAlpha > 0.001F) {
                        PlanetInfo info = copy(shared);
                        info.atmosphereColorR = (float) atmosphereColor.x;
                        info.atmosphereColorG = (float) atmosphereColor.y;
                        info.atmosphereColorB = (float) atmosphereColor.z;
                        info.cloudColorR = (float) cloudColor.x;
                        info.cloudColorG = (float) cloudColor.y;
                        info.cloudColorB = (float) cloudColor.z;
                        info.cloudTintStrength = cloudTintStrength;
                        info.cloudStormDarkness = cloudStormDarkness;
                        info.atmosphereAlpha = atmosphereOverlayAlpha;
                        draws.mesh(
                                SkyMesh.QUAD,
                                SkyPipelines.ATMOSPHERE,
                                pose,
                                square,
                                1,
                                1,
                                1,
                                1,
                                other.texture,
                                null,
                                info,
                                0,
                                0);
                    }

                    PlanetInfo crescent = copy(shared);
                    crescent.phase = phase;
                    draws.mesh(
                            SkyMesh.QUAD,
                            SkyPipelines.CRESCENT,
                            pose,
                            square,
                            1,
                            1,
                            1,
                            1,
                            other.texture,
                            null,
                            crescent,
                            0,
                            0);

                    Identifier cityMask = other.cityMask != null ? other.cityMask : DEFAULT_MASK;
                    boolean lit =
                            atmosphereStyle == AtmosphereRenderUtil.ATMOSPHERE_STYLE_CLOUDS
                                    || atmosphereStyle
                                            == AtmosphereRenderUtil.ATMOSPHERE_STYLE_HAZE;

                    if (lightIntensity > 0 && atmosphereDensity > 0.001F && lit) {
                        PlanetInfo info = copy(shared);
                        info.phase = phase;
                        info.atmosphereDensity = atmosphereDensity;
                        info.blackouts = activeBlackouts;
                        draws.mesh(
                                SkyMesh.QUAD,
                                SkyPipelines.ATMOSPHERE_EMISSIVE,
                                pose,
                                square,
                                1,
                                1,
                                1,
                                1,
                                CITY_LIGHTS[lightIntensity],
                                cityMask,
                                info,
                                0,
                                0);
                    }

                    if (lightIntensity > 0) {
                        PlanetInfo info = copy(shared);
                        info.phase = phase;
                        info.atmosphereDensity = atmosphereDensity;
                        info.blackouts = activeBlackouts;
                        draws.mesh(
                                SkyMesh.QUAD,
                                SkyPipelines.NIGHT_LIGHTS,
                                pose,
                                square,
                                1,
                                1,
                                1,
                                1,
                                CITY_LIGHTS[lightIntensity],
                                cityMask,
                                info,
                                0,
                                0);
                    }

                    if (atmosphereOverlayAlpha > 0.001F && cloudLightningStrength > 0.001F && lit) {
                        PlanetInfo info = copy(shared);
                        info.phase = phase;
                        info.cloudTintStrength = cloudTintStrength;
                        info.cloudLightningStrength = cloudLightningStrength;
                        info.atmosphereAlpha = atmosphereOverlayAlpha;
                        info.eveFlashStrength =
                                AtmosphereRenderUtil.getBodyEveFlashStrength(other, atmosphereTime);
                        info.lightningMode = AtmosphereRenderUtil.getBodyLightningMode(other);
                        draws.mesh(
                                SkyMesh.QUAD,
                                SkyPipelines.LIGHTNING,
                                pose,
                                square,
                                1,
                                1,
                                1,
                                1,
                                other.texture,
                                cityMask,
                                info,
                                0,
                                0);
                    }

                    if (impact != null) {
                        renderImpact(draws, pose, s, (float) uvOffset, impactTime);
                    }

                    draws.mesh(
                            SkyMesh.QUAD_FLAT,
                            SkyPipelines.FLAT_ADDITIVE,
                            pose,
                            square,
                            (float) planetTint.x - blendDarken,
                            (float) planetTint.y - blendDarken,
                            (float) planetTint.z - blendDarken,
                            1 - blendAmount * visibility,
                            null);
                }

                if (other.hasRings) {
                    renderRingHalf(draws, pose, metric, s, visibility, false);
                }

                List<ShockStatus> flashShocks =
                        CelestialNukeShockHandler.getClientShocks(other, level.getGameTime());
                if (!flashShocks.isEmpty()) {
                    PlanetInfo info = new PlanetInfo();
                    info.phase = (float) -metric.phase;
                    AtmosphereRenderUtil.applyNukeShockUniforms(info, flashShocks, totalTime);
                    draws.mesh(
                            SkyMesh.QUAD,
                            SkyPipelines.NUKE,
                            pose,
                            square,
                            1,
                            1,
                            1,
                            1,
                            null,
                            null,
                            info,
                            0,
                            0);
                }
            }

            if (renderPoint) {
                float alpha = Mth.clamp((float) size * 100.0F, 0.0F, 1.0F);
                alpha *=
                        1
                                - Mth.clampedMap(
                                        (float) size,
                                        (float) transitionMinSize,
                                        (float) transitionMaxSize,
                                        0,
                                        1);
                draws.mesh(
                        SkyMesh.QUAD,
                        SkyPipelines.TEXTURED_ADDITIVE,
                        pose,
                        SkyDraws.lifted(100.0F, 1.0F),
                        other.color[0],
                        other.color[1],
                        other.color[2],
                        alpha * visibility,
                        PLANET);
            }

            pose.popPose();
        }
    }

    private static PlanetInfo copy(PlanetInfo shared) {
        PlanetInfo info = new PlanetInfo();
        System.arraycopy(shared.nukeShockTime, 0, info.nukeShockTime, 0, PlanetInfo.MAX_SHOCKS);
        System.arraycopy(
                shared.nukeShockCenterX, 0, info.nukeShockCenterX, 0, PlanetInfo.MAX_SHOCKS);
        System.arraycopy(
                shared.nukeShockCenterY, 0, info.nukeShockCenterY, 0, PlanetInfo.MAX_SHOCKS);
        System.arraycopy(
                shared.nukeShockStrength, 0, info.nukeShockStrength, 0, PlanetInfo.MAX_SHOCKS);
        info.nukeShockCount = shared.nukeShockCount;
        info.offset = shared.offset;
        info.patternOffset = shared.patternOffset;
        info.atmosphereTime = shared.atmosphereTime;
        info.atmosphereStyle = shared.atmosphereStyle;
        info.impactTime = shared.impactTime;
        return info;
    }

    private static void additiveSquare(
            SkyDraws draws,
            PoseStack pose,
            float size,
            float uvOffset,
            float alpha,
            Identifier texture) {
        if (uvOffset == 0) {
            draws.mesh(
                    SkyMesh.QUAD,
                    SkyPipelines.TEXTURED_ADDITIVE,
                    pose,
                    SkyDraws.lifted(100.0F, size),
                    1,
                    1,
                    1,
                    alpha,
                    texture);
            return;
        }
        VertexConsumer quad =
                draws.draw(SkyPipelines.TEXTURED_ADDITIVE, pose, 1, 1, 1, alpha, texture);
        quad.addVertex(-size, 100.0F, -size).setUv(uvOffset, 0.0F);
        quad.addVertex(size, 100.0F, -size).setUv(1.0F + uvOffset, 0.0F);
        quad.addVertex(size, 100.0F, size).setUv(1.0F + uvOffset, 1.0F);
        quad.addVertex(-size, 100.0F, size).setUv(uvOffset, 1.0F);
    }

    private static void renderRingHalf(
            SkyDraws draws,
            PoseStack pose,
            AstroMetric metric,
            float size,
            float visibility,
            boolean back) {
        CelestialBody other = metric.body;
        float ringSize = size * other.ringSize;

        pose.pushPose();
        pose.translate(0.0F, 100.0F, 0.0F);
        pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees((float) -metric.angle));
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(90.0F - other.ringTilt));
        pose.rotate((Quaternionfc) Axis.YP.rotationDegrees((float) metric.angle));

        draws.mesh(
                back ? RING_BACK : RING_FRONT,
                SkyPipelines.TEXTURED_ADDITIVE_NO_CULL,
                pose,
                SkyDraws.lifted(0.0F, ringSize),
                other.ringColor[0],
                other.ringColor[1],
                other.ringColor[2],
                visibility,
                RINGS);
        pose.popPose();
    }

    private void renderDestroyed(
            SkyDraws draws,
            PoseStack pose,
            CelestialBody other,
            CBT_Destroyed destroyed,
            float size,
            float uvOffset) {
        float destProgress = destroyed.progress(GameTime.ticks());
        double progress = destProgress + size * 0.5;

        float alpha = (float) (1.0F - Math.min(1.0F, progress / 100));
        Random random = new Random(12);

        int numQuads = 30;
        for (int i = 0; i < numQuads; i++) {
            double radius = (random.nextDouble() * size) * destProgress;

            double randomTheta = random.nextDouble() * Math.PI * 2;
            double randomPhi = random.nextDouble() * Math.PI;

            double randomX = radius * Math.sin(randomPhi) * Math.cos(randomTheta) * 0.7;
            double randomY = radius * Math.sin(randomPhi) * Math.sin(randomTheta);
            double randomZ = radius * Math.cos(randomPhi) * 0.7;

            float randomRotation = random.nextFloat() * 360.0F;

            float uMin = (float) random.nextDouble();
            float vMin = (float) random.nextDouble();
            float uMax = (float) Math.min(uMin + (random.nextDouble() * 0.2), 1.0);
            float vMax = (float) Math.min(vMin + (random.nextDouble() * 0.2), 1.0);

            pose.pushPose();
            pose.translate(
                    (float) (randomX * -0.05), (float) (randomY * 0.00), (float) (randomZ * -0.05));
            pose.rotate(
                    (Quaternionfc) Axis.YP.rotationDegrees(randomRotation * destProgress * 0.05F));
            float qsize = (float) (size * random.nextDouble() * 0.1);
            VertexConsumer quad =
                    draws.draw(SkyPipelines.TEXTURED_ADDITIVE, pose, 1, 1, 1, 1, other.texture);
            quad.addVertex(-qsize, 100.0F, -qsize).setUv(uMin, vMin);
            quad.addVertex(qsize, 100.0F, -qsize).setUv(uMax, vMin);
            quad.addVertex(qsize, 100.0F, qsize).setUv(uMax, vMax);
            quad.addVertex(-qsize, 100.0F, qsize).setUv(uMin, vMax);
            pose.popPose();

            pose.pushPose();
            pose.translate(
                    (float) (randomX * 0.04), (float) (randomY * 0.00), (float) (randomZ * 0.04));
            pose.rotate(
                    (Quaternionfc) Axis.YP.rotationDegrees(randomRotation * destProgress * 0.05F));
            qsize = (float) (size * random.nextDouble() * 0.07);
            quad = draws.draw(SkyPipelines.TEXTURED_ADDITIVE, pose, 1, 1, 1, 1, DESTROYED);
            quad.addVertex(-qsize, 100.0F, -qsize).setUv(uMin, vMin);
            quad.addVertex(qsize, 100.0F, -qsize).setUv(uMax, vMin);
            quad.addVertex(qsize, 100.0F, qsize).setUv(uMax, vMax);
            quad.addVertex(-qsize, 100.0F, qsize).setUv(uMin, vMax);
            pose.popPose();
        }

        additiveSquare(
                draws,
                pose,
                (float) ((destProgress * 0.5) * size * 0.1),
                uvOffset,
                alpha,
                SHOCKWAVE);
        additiveSquare(draws, pose, size * 3, uvOffset, alpha * 2, SHOCK_FLARE);
    }

    private static void renderImpact(
            SkyDraws draws, PoseStack pose, float size, float uvOffset, double impactTime) {
        double lavaAlpha = Math.min(impactTime * 0.1, 1.0);

        float impactSize = (float) ((impactTime * 0.1) * size * 0.035);
        double impactAlpha = 1.0 - Math.min(1.0, impactTime * 0.0015);
        float flareSize = size * 1.5F;
        double flareAlpha = 1.0 - Math.min(1.0, impactTime * 0.002);

        if (lavaAlpha > 0) additiveSquare(draws, pose, size, uvOffset, (float) lavaAlpha, IMPACT);

        pose.pushPose();
        pose.translate(-size * 0.5F, 0, size * 0.4F);

        if (impactAlpha > 0)
            additiveSquare(draws, pose, impactSize, 0, (float) impactAlpha, SHOCKWAVE);

        if (flareAlpha > 0)
            additiveSquare(draws, pose, flareSize, 0, (float) flareAlpha, SHOCK_FLARE);
        pose.popPose();
    }

    private static void renderAtmosphereGlow(
            SkyDraws draws, PoseStack pose, CelestialBody body, double size, float visibility) {
        float glowAlpha = AtmosphereRenderUtil.getAtmosphereGlowAlpha(body) * visibility;
        if (glowAlpha <= 0.001F) return;

        float leadingGlow = glowAlpha;
        float trailingGlow = glowAlpha;

        Vec3 atmo = AtmosphereRenderUtil.getBodyAtmosphereColor(body);
        float r = Mth.clamp((float) atmo.x * 1.15F, 0.0F, 1.0F);
        float g = Mth.clamp((float) atmo.y * 1.15F, 0.0F, 1.0F);
        float b = Mth.clamp((float) atmo.z * 1.15F, 0.0F, 1.0F);

        float innerSize = (float) (size * 0.98D);
        float middleSize = (float) (size * 1.075D);
        float outerSize = (float) (size * 1.15D * (1.0D + glowAlpha * 0.25D));
        float y = 100.0F;

        VertexConsumer bands =
                draws.draw(SkyPipelines.COLORED_TRANSLUCENT_NO_CULL, pose, 1, 1, 1, 1, null);

        bands.addVertex(-outerSize, y, -outerSize).setColor(r, g, b, 0.0F);
        bands.addVertex(outerSize, y, -outerSize).setColor(r, g, b, 0.0F);
        bands.addVertex(middleSize, y, -middleSize).setColor(r, g, b, leadingGlow / 2);
        bands.addVertex(-middleSize, y, -middleSize).setColor(r, g, b, trailingGlow / 2);

        bands.addVertex(-middleSize, y, -middleSize).setColor(r, g, b, trailingGlow / 2);
        bands.addVertex(middleSize, y, -middleSize).setColor(r, g, b, leadingGlow / 2);
        bands.addVertex(innerSize, y, -innerSize).setColor(r, g, b, leadingGlow);
        bands.addVertex(-innerSize, y, -innerSize).setColor(r, g, b, trailingGlow);

        bands.addVertex(outerSize, y, -outerSize).setColor(r, g, b, 0.0F);
        bands.addVertex(outerSize, y, outerSize).setColor(r, g, b, 0.0F);
        bands.addVertex(middleSize, y, middleSize).setColor(r, g, b, leadingGlow / 2);
        bands.addVertex(middleSize, y, -middleSize).setColor(r, g, b, leadingGlow / 2);

        bands.addVertex(middleSize, y, -middleSize).setColor(r, g, b, leadingGlow / 2);
        bands.addVertex(middleSize, y, middleSize).setColor(r, g, b, leadingGlow / 2);
        bands.addVertex(innerSize, y, innerSize).setColor(r, g, b, leadingGlow);
        bands.addVertex(innerSize, y, -innerSize).setColor(r, g, b, leadingGlow);

        bands.addVertex(outerSize, y, outerSize).setColor(r, g, b, 0.0F);
        bands.addVertex(-outerSize, y, outerSize).setColor(r, g, b, 0.0F);
        bands.addVertex(-middleSize, y, middleSize).setColor(r, g, b, trailingGlow / 2);
        bands.addVertex(middleSize, y, middleSize).setColor(r, g, b, leadingGlow / 2);

        bands.addVertex(middleSize, y, middleSize).setColor(r, g, b, leadingGlow / 2);
        bands.addVertex(-middleSize, y, middleSize).setColor(r, g, b, trailingGlow / 2);
        bands.addVertex(-innerSize, y, innerSize).setColor(r, g, b, trailingGlow);
        bands.addVertex(innerSize, y, innerSize).setColor(r, g, b, leadingGlow);

        bands.addVertex(-outerSize, y, outerSize).setColor(r, g, b, 0.0F);
        bands.addVertex(-outerSize, y, -outerSize).setColor(r, g, b, 0.0F);
        bands.addVertex(-middleSize, y, -middleSize).setColor(r, g, b, trailingGlow / 2);
        bands.addVertex(-middleSize, y, middleSize).setColor(r, g, b, trailingGlow / 2);

        bands.addVertex(-middleSize, y, middleSize).setColor(r, g, b, trailingGlow / 2);
        bands.addVertex(-middleSize, y, -middleSize).setColor(r, g, b, trailingGlow / 2);
        bands.addVertex(-innerSize, y, -innerSize).setColor(r, g, b, trailingGlow);
        bands.addVertex(-innerSize, y, innerSize).setColor(r, g, b, trailingGlow);
    }

    protected static void renderRings(
            SkyDraws draws, PoseStack pose, float[] ringColor, float ringSize, float visibility) {
        draws.mesh(
                RING_WALL,
                SkyPipelines.TEXTURED_ADDITIVE,
                pose,
                new Matrix4f().translation(-20.0F, 0.0F, 0.0F).scale(1.0F, ringSize, ringSize),
                ringColor[0],
                ringColor[1],
                ringColor[2],
                visibility,
                RINGS);
    }

    protected void renderSpecialEffects(SkyDraws draws) {}

    private void renderWarShocks(SkyDraws draws) {
        CBT_War war = body.getTrait(CBT_War.class);
        if (war == null) return;
        for (CBT_War.Projectile projectile : war.getProjectiles()) {
            if (projectile.getTravel() > 0) continue;
            render3DModel(draws);
            float age = projectile.getFlashtime() + partialTicks;
            float alpha = 1F - Math.min(1F, age / 100F);
            PoseStack pose = new PoseStack();
            pose.translate(
                    projectile.getTranslateX() + 70D,
                    projectile.getTranslateY(),
                    projectile.getTranslateZ() + 50D);
            pose.scale(age, age, age);
            pose.rotate(
                    (Quaternionfc)
                            new Quaternionf().rotationAxis((float) (Math.PI / 2D), -10F, -1F, 50F));
            pose.rotate(
                    (Quaternionfc)
                            new Quaternionf()
                                    .rotationAxis((float) Math.toRadians(20D), 0F, -1F, 1F));
            draws.mesh(
                    SHOCK_PLANE,
                    SkyPipelines.TEXTURED_ADDITIVE,
                    pose,
                    SkyDraws.IDENTITY,
                    1F,
                    1F,
                    1F,
                    alpha,
                    SHOCKWAVE);
            pose = new PoseStack();
            pose.translate(
                    projectile.getTranslateX() + 70D,
                    projectile.getTranslateY(),
                    projectile.getTranslateZ() + 50D);
            float inner = age * 0.4F;
            pose.scale(inner, inner, inner);
            pose.rotate(
                    (Quaternionfc)
                            new Quaternionf().rotationAxis((float) (Math.PI / 2D), -10F, -1F, 50F));
            pose.rotate(
                    (Quaternionfc)
                            new Quaternionf()
                                    .rotationAxis((float) Math.toRadians(20D), 0F, -1F, 1F));
            draws.mesh(
                    SHOCK_PLANE,
                    SkyPipelines.TEXTURED_ADDITIVE,
                    pose,
                    SkyDraws.IDENTITY,
                    1F,
                    1F,
                    1F,
                    alpha,
                    SHOCK_SHIELD);
        }
    }

    private void renderFleshShell(SkyDraws draws) {
        if (body.getTrait(CBT_COMPROMISED.class) == null) return;
        PoseStack pose = new PoseStack();
        pose.scale(194.5F, 70.5F, 94.5F);
        pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees(90F));
        float time = ((float) level.getDefaultClockTime() + partialTicks) * 0.2F;
        draws.mesh(
                FLESH_SPHERE,
                SkyPipelines.FLESH,
                pose,
                SkyDraws.IDENTITY,
                1F,
                1F,
                1F,
                1F,
                NOISE,
                null,
                null,
                time * 0.05F,
                0);
    }

    protected void render3DModel(SkyDraws draws) {}

    private void renderSatellite(
            SkyDraws draws,
            PoseStack pose,
            float r,
            float g,
            float b,
            float inclination,
            float altitude,
            float phaseOffset) {
        renderSatellite(
                draws,
                pose,
                r,
                g,
                b,
                inclination,
                altitude,
                phaseOffset,
                false,
                SatelliteOrbit.MIN_BLINK_PERIOD);
    }

    private void renderPlayerSatellites(SkyDraws draws, PoseStack pose) {
        for (var satellite : ClientSatellites.current(level).entrySet()) {
            ClientSatellites.Entry entry = satellite.getValue();
            if (entry.type() == WarItemsSpace.WAR) {
                RailgunSky.render(draws, pose, satellite.getKey());
                continue;
            }
            SatelliteOrbit orbit = entry.orbit();
            renderSatellite(
                    draws,
                    pose,
                    orbit.red(),
                    orbit.green(),
                    orbit.blue(),
                    orbit.inclination(),
                    orbit.altitude(),
                    orbit.phaseOffset(),
                    orbit.blinking(),
                    orbit.blinkPeriod());
        }
    }

    private void renderHeldSatellitePreview(SkyDraws draws, PoseStack pose) {
        ItemStack held = Minecraft.getInstance().player.getMainHandItem();
        SatelliteType type = SatelliteType.fromStack(held);
        if (!SatellitesSpace.isSpace(type)) return;
        var home = SatellitesSpace.home(type);
        if (home != null && home.getDimension() != body.dimension) return;
        SatelliteOrbit orbit =
                held.getOrDefault(
                        ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                        SatellitesSpace.defaults(type));
        renderOrbitLine(draws, pose, orbit);
        renderSatellite(
                draws,
                pose,
                orbit.red(),
                orbit.green(),
                orbit.blue(),
                orbit.inclination(),
                orbit.altitude(),
                orbit.phaseOffset(),
                orbit.blinking(),
                orbit.blinkPeriod());
    }

    private void renderSatellite(
            SkyDraws draws,
            PoseStack pose,
            float r,
            float g,
            float b,
            float inclination,
            float altitude,
            float phaseOffset,
            boolean blinking,
            float blinkPeriod) {
        double ticks = (double) GameTime.now() / 50.0D;
        float orbitAngle =
                SatelliteOrbit.applyPhaseOffsetToOrbitAngle(
                        phaseOffset, altitude, (ticks / 600.0D) * -360.0D, 360.0F);
        float renderAltitude = Math.max(1.0F, altitude);

        pose.pushPose();
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(solarAngle * -360.0F));
        pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees(inclination));
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(orbitAngle));

        draws.mesh(
                SkyMesh.QUAD_TRANSPOSED,
                SkyPipelines.TEXTURED_ADDITIVE,
                pose,
                SkyDraws.lifted(renderAltitude, 0.5F),
                r,
                g,
                b,
                SatelliteOrbit.blinkAlpha(blinking, blinkPeriod, ticks),
                SATELLITE);
        pose.popPose();
    }

    private void renderOrbitLine(SkyDraws draws, PoseStack pose, SatelliteOrbit orbit) {
        float altitude = Math.max(1.0F, orbit.altitude());
        float alpha = 0.35F * orbit.blinkAlpha(GameTime.now() / 50.0D);
        pose.pushPose();
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(solarAngle * -360.0F));
        pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees(orbit.inclination()));
        draws.mesh(
                ORBIT,
                SkyPipelines.SATELLITE_ORBIT_LINE,
                pose,
                new Matrix4f().scaling(1.0F, altitude, altitude),
                orbit.red(),
                orbit.green(),
                orbit.blue(),
                alpha,
                null);
        pose.popPose();
    }

    protected void renderStation(SkyDraws draws, PoseStack pose, OrbitalStation station) {
        long seed = station.dX * 1024L + station.dZ;
        double ticks = (double) (GameTime.now() % (1600 * 50)) / 50;

        pose.pushPose();
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees(solarAngle * -360.0F));
        pose.rotate(
                (Quaternionfc)
                        Axis.XP.rotationDegrees(-40.0F + (float) (seed % 800) * 0.1F - 5.0F));
        pose.rotate((Quaternionfc) Axis.YP.rotationDegrees((float) (seed % 50) * 0.1F - 20.0F));
        pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees((float) (seed % 80) * 0.1F - 2.5F));
        pose.rotate((Quaternionfc) Axis.XP.rotationDegrees((float) ((ticks / 1600.0D) * -360.0D)));

        draws.mesh(
                SkyMesh.QUAD_TRANSPOSED,
                SkyPipelines.TEXTURED_ADDITIVE,
                pose,
                SkyDraws.lifted(100.0F, 0.8F),
                0.8F,
                1,
                1,
                1,
                STATION);
        pose.popPose();
    }

    private void renderMeteors(SkyDraws draws) {
        if (Meteors.meteors.isEmpty()) return;
        for (Meteors.Meteor meteor : Meteors.meteors) {
            meteorShades
                    .get(
                            meteor.type == Meteors.MeteorType.SMOKE
                                    ? (int) Math.min(meteor.age, SMOKE_SHADES - 1)
                                    : SMOKE_SHADES)
                    .add(meteor);
        }

        Matrix4f facing =
                new Matrix4f()
                        .rotate(Axis.YP.rotationDegrees(180.0F - cameraYaw))
                        .rotate(Axis.XP.rotationDegrees(-cameraPitch));
        Vector3f[] corners = new Vector3f[4];
        for (int i = 0; i < corners.length; i++) {
            corners[i] =
                    facing.transformPosition(
                            BILLBOARD[i * 4], BILLBOARD[i * 4 + 1], 0.0F, new Vector3f());
        }
        int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();

        renderMeteorShade(draws, SMOKE_SHADES, corners, renderDistance, SHOCK_FLARE, 1, 1, 1);
        for (int age = 0; age < SMOKE_SHADES; age++) {
            float dark = 1f - Math.min(((float) (age) / SMOKE_FADE), 1f);
            renderMeteorShade(
                    draws,
                    age,
                    corners,
                    renderDistance,
                    PARTICLE_BASE,
                    0.6F * dark,
                    0.6F * dark,
                    dark);
        }
    }

    private void renderMeteorShade(
            SkyDraws draws,
            int shade,
            Vector3f[] corners,
            int renderDistance,
            Identifier texture,
            float r,
            float g,
            float b) {
        List<Meteors.Meteor> meteors = meteorShades.get(shade);
        if (meteors.isEmpty()) return;

        VertexConsumer quads =
                draws.draw(SkyPipelines.TEXTURED_ADDITIVE, new PoseStack(), r, g, b, 1, texture);
        for (Meteors.Meteor meteor : meteors) {
            Vec3 offset =
                    new Vec3(meteor.posX - feet.x, meteor.posY - feet.y, meteor.posZ - feet.z);
            double offsetLength = offset.length();
            double distance = Math.min(renderDistance * 16, offsetLength);
            Vec3 offsetNormal = offsetLength >= 1.0E-4D ? offset.scale(1 / offsetLength) : offset;
            float x = (float) (offsetNormal.x * distance);
            float y = (float) (offsetNormal.y * distance);
            float z = (float) (offsetNormal.z * distance);

            double descent = 2017d - meteor.posY;
            double quadratic = (-(descent * descent) + (1517 * descent)) / 41;
            float scalar = (float) (quadratic / offsetLength);

            for (int i = 0; i < corners.length; i++) {
                Vector3f corner = corners[i];
                quads.addVertex(x + corner.x * scalar, y + corner.y * scalar, z + corner.z * scalar)
                        .setUv(BILLBOARD[i * 4 + 2], BILLBOARD[i * 4 + 3]);
            }
        }
        meteors.clear();
    }
}
