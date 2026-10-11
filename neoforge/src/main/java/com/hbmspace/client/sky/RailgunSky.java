// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.client.model.Meshes;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.util.BeamPronter;
import com.hbm.util.GameTime;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class RailgunSky {
    private static final Identifier BODY_TEXTURE =
            Identifier.parse("hbmspace:textures/models/railgun_main.png");
    private static final Identifier FLASH =
            Identifier.parse("hbmspace:textures/misc/space/flare.png");
    private static final Identifier SHOCKWAVE =
            Identifier.parse("hbm:textures/particle/shockwave.png");
    private static final Identifier BODY = Identifier.parse("hbmspace:models/railgun_main.obj");
    private static final Identifier PLANE = Identifier.parse("hbmspace:models/plane.obj");
    private static final RendererReloadCache<Identifier, HFRWavefrontObject> MESHES =
            new RendererReloadCache<>(id -> Meshes.load(id).insetUv(0.0005F));
    private static final int COLOR = 0x202060;

    private RailgunSky() {}

    public static void render(SkyDraws draws, PoseStack pose, long frequency) {
        renderInterpolated(draws, pose, frequency, 0F, GameTime.ticks());
    }

    private static void renderInterpolated(
            SkyDraws draws, PoseStack pose, long frequency, float interpolation, long ticks) {
        pose.pushPose();
        pose.scale(5F, 5F, 5F);

        double rounded = Math.round(frequency / 1000.0D);
        double x = ((frequency % 10) - 5) * 2;
        double y = (((frequency / 10) % 10) - 5) * 2;
        pose.translate(
                Math.min(Math.max(-rounded + 30 + x, -50), 50),
                Math.min(Math.max(-rounded - 20 + y, -50), 50),
                20);
        float alpha = 1.0F - Math.min(1.0F, interpolation / 100);

        pose.pushPose();
        pose.translate(1, 5.5, 0);
        float scale = (float) (interpolation * 0.2D);
        pose.scale(scale, scale, scale);
        draws.obj(
                SkyPipelines.TEXTURED_ADDITIVE, pose, 1, 1, 1, alpha, FLASH, MESHES.get(PLANE), 0);
        draws.obj(
                SkyPipelines.TEXTURED_ADDITIVE,
                pose,
                1,
                1,
                1,
                alpha,
                SHOCKWAVE,
                MESHES.get(PLANE),
                0);
        pose.popPose();

        pose.pushPose();
        pose.translate(1, 5.5, 0);
        Vec3 skeleton = new Vec3(0, interpolation * 2, 0);
        beam(
                draws,
                pose,
                skeleton,
                BeamPronter.EnumWaveType.SPIRAL,
                0,
                1,
                0F,
                (float) 1.6 * 1.2F * alpha,
                alpha * 0.2F);
        beam(
                draws,
                pose,
                skeleton,
                BeamPronter.EnumWaveType.SPIRAL,
                0,
                1,
                0F,
                (float) 0.7 * 0.6F,
                alpha * 0.6F);
        beam(
                draws,
                pose,
                skeleton,
                BeamPronter.EnumWaveType.RANDOM,
                (int) (ticks / 5L) % 1000,
                35,
                0.2F,
                (float) 0.2 * 0.1F,
                alpha);
        draws.clearDepth();
        pose.popPose();

        pose.mulPose(Axis.ZP.rotationDegrees(-90F));
        draws.obj(
                SkyPipelines.RAILGUN_BODY,
                pose,
                32F / 255F,
                32F / 255F,
                96F / 255F,
                alpha,
                BODY_TEXTURE,
                MESHES.get(BODY),
                0);
        draws.clearDepth();
        pose.popPose();
    }

    private static void beam(
            SkyDraws draws,
            PoseStack pose,
            Vec3 skeleton,
            BeamPronter.EnumWaveType wave,
            int start,
            int segments,
            float size,
            float thickness,
            float alpha) {
        VertexConsumer vertices = draws.draw(SkyPipelines.RAILGUN_BEAM, pose, 1, 1, 1, alpha, null);
        BeamPronter.writeSolid(
                new PoseStack(),
                vertices,
                skeleton,
                wave,
                COLOR,
                COLOR,
                start,
                segments,
                size,
                6,
                thickness,
                1F);
    }
}
