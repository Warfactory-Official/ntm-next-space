// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class SkyProviderLaytheSunset extends SkyProviderCelestial {

    private static final int SEGMENTS = 16;

    @Override
    protected void renderSunset(
            SkyRenderer vanilla,
            SkyRenderState state,
            SkyDraws draws,
            float pressure,
            @Nullable Identifier surfaceTexture) {
        float[] color = calcSunriseSunsetColors(state, pressure);
        if (color == null) return;

        PoseStack pose = new PoseStack();
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(state.sunAngle) < 0.0F ? 180.0F : 0.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
        fan(draws, pose, color, 150.0F, 160.0F, 90.0F * color[3], 1.0F);

        pose = new PoseStack();
        pose.mulPose(Axis.XP.rotationDegrees(135.0F));
        pose.translate(0.0F, -60.0F, 0.0F);
        fan(draws, pose, color, 100.0F, 100.0F, 90.0F, 1.0F);

        pose = new PoseStack();
        pose.mulPose(Axis.XP.rotationDegrees(135.0F));
        pose.translate(0.0F, -30.0F, 0.0F);
        fan(draws, pose, color, 80.0F, 100.0F, 90.0F, 0.2F);
    }

    private static void fan(
            SkyDraws draws,
            PoseStack pose,
            float[] color,
            float height,
            float radius,
            float depth,
            float rimGreen) {
        VertexConsumer fan = draws.draw(SkyPipelines.SUNSET_ADDITIVE, pose, 1, 1, 1, 1, null);
        fan.addVertex(0.0F, height, 0.0F).setColor(color[0], color[1], color[2], color[3]);
        for (int j = 0; j <= SEGMENTS; j++) {
            float angle = j * (float) Math.PI * 2.0F / SEGMENTS;
            float sin = Mth.sin(angle);
            float cos = Mth.cos(angle);
            fan.addVertex(sin * radius, cos * radius, -cos * depth)
                    .setColor(color[0], color[1] * rimGreen, color[2], 0.0F);
        }
    }
}
