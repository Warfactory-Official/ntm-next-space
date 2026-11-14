/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
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

import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.render.util.BeamPronter;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class SkyProviderThatmo extends SkyProviderCelestial {

    private static final Vec3 STREAK = new Vec3(0, 5, 0);
    private static final Cluster[] CLUSTERS = clusters();

    private record Cluster(@Nullable Quaternionf turn, SkyMesh streaks) {}

    private static Cluster[] clusters() {
        Random random = new Random(42);
        return new Cluster[] {
            cluster(random, null, 50.0F, 0.0F, 0.0F, 20, 0.0F),
            cluster(random, null, -40.0F, 0.0F, 0.0F, 20, -1.0F),
            cluster(random, null, 0.0F, -110.0F, -80.0F, 20, -1.0F),
            cluster(
                    random,
                    turn(-90.0F * 0.5961033F, -11.0F, 5.0F, -65.0F),
                    0.0F,
                    -110.0F,
                    55.0F,
                    20,
                    -1.0F),
            cluster(random, turn(-25.0F, -16.0F, 5.0F, 0.0F), -60.0F, 0.0F, 0.0F, 15, 0.0F),
            cluster(random, turn(-65.0F, -12.0F, 5.0F, 0.0F), 50.0F, 0.0F, 0.0F, 15, 0.0F)
        };
    }

    @Override
    protected void renderSpecialEffects(SkyDraws draws) {
        for (Cluster cluster : CLUSTERS) {
            PoseStack pose = new PoseStack();
            if (cluster.turn != null) pose.mulPose((Quaternionfc) cluster.turn);
            pose.translate(0.0F, partialTicks, 0.0F);
            draws.mesh(
                    cluster.streaks,
                    SkyPipelines.BEAM_ADDITIVE,
                    pose,
                    SkyDraws.IDENTITY,
                    1,
                    1,
                    1,
                    1,
                    null);
        }
    }

    private static Quaternionf turn(float degrees, float x, float y, float z) {
        return new Quaternionf()
                .rotationAxis(degrees * Mth.DEG_TO_RAD, new Vector3f(x, y, z).normalize());
    }

    private static Cluster cluster(
            Random random,
            @Nullable Quaternionf turn,
            float x,
            float y,
            float z,
            int spread,
            float stepZ) {
        int[] drops = new int[17];
        for (int i = 0; i < drops.length; i++) drops[i] = random.nextInt(spread);
        return new Cluster(
                turn,
                new SkyMesh(
                        "Thatmo streaks",
                        DefaultVertexFormat.POSITION_COLOR,
                        PrimitiveTopology.QUADS,
                        buffer -> {
                            PoseStack pose = new PoseStack();
                            pose.translate(x, y, z);
                            for (int drop : drops) {
                                pose.translate(0.0F, -drop, stepZ);
                                BeamPronter.writeSolid(
                                        pose,
                                        buffer,
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
                        }));
    }
}
