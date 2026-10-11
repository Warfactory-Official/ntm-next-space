// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.render.util.BeamPronter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class SkyProviderThatmo extends SkyProviderCelestial {

    private static final Vec3 STREAK = new Vec3(0, 5, 0);

    @Override
    protected void renderSpecialEffects(SkyDraws draws) {
        Random random = new Random(42);
        VertexConsumer beams =
                draws.draw(SkyPipelines.BEAM_ADDITIVE, new PoseStack(), 1, 1, 1, 1, null);
        float alt = partialTicks;
        cluster(beams, random, null, 50.0F, alt, 0.0F, 20, 0.0F);
        cluster(beams, random, null, -40.0F, alt, 0.0F, 20, -1.0F);
        cluster(beams, random, null, 0.0F, alt - 110.0F, -80.0F, 20, -1.0F);
        cluster(
                beams,
                random,
                turn(-90.0F * 0.5961033F, -11.0F, 5.0F, -65.0F),
                0.0F,
                alt - 110.0F,
                55.0F,
                20,
                -1.0F);
        cluster(beams, random, turn(-25.0F, -16.0F, 5.0F, 0.0F), -60.0F, alt, 0.0F, 15, 0.0F);
        cluster(beams, random, turn(-65.0F, -12.0F, 5.0F, 0.0F), 50.0F, alt, 0.0F, 15, 0.0F);
    }

    private static Quaternionf turn(float degrees, float x, float y, float z) {
        return new Quaternionf()
                .rotationAxis(degrees * Mth.DEG_TO_RAD, new Vector3f(x, y, z).normalize());
    }

    private static void cluster(
            VertexConsumer beams,
            Random random,
            @Nullable Quaternionf turn,
            float x,
            float y,
            float z,
            int spread,
            float stepZ) {
        PoseStack pose = new PoseStack();
        if (turn != null) pose.mulPose(turn);
        pose.translate(x, y, z);
        for (int i = 0; i < 17; i++) {
            pose.translate(0.0F, -random.nextInt(spread), stepZ);
            BeamPronter.writeSolid(
                    pose,
                    beams,
                    STREAK,
                    EnumWaveType.SPIRAL,
                    0xFF9000,
                    0xFF9000,
                    0,
                    1,
                    0.0F,
                    6,
                    0.2F * 0.2F,
                    0.5F);
        }
    }
}
