/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

public class SkyProviderLaytheSunset extends SkyProviderCelestial {

    private static final int SEGMENTS = 16;
    private static final SkyMesh HIGH = fan(150.0F, 160.0F, 1.0F);
    private static final SkyMesh LOW = fan(100.0F, 100.0F, 1.0F);
    private static final SkyMesh INNER = fan(80.0F, 100.0F, 0.2F);

    @Override
    protected void renderSunset(
            SkyRenderer vanilla, SkyRenderState state, SkyDraws draws, float pressure) {
        float[] color = calcSunriseSunsetColors(state, pressure);
        if (color == null) return;

        PoseStack pose = new PoseStack();
        pose.mulPose((Quaternionfc) Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(
                (Quaternionfc)
                        Axis.ZP.rotationDegrees(Mth.sin(state.sunAngle) < 0.0F ? 180.0F : 0.0F));
        pose.mulPose((Quaternionfc) Axis.ZP.rotationDegrees(90.0F));
        draw(draws, HIGH, pose, new Matrix4f().scaling(1.0F, 1.0F, color[3]), color);

        pose = new PoseStack();
        pose.mulPose((Quaternionfc) Axis.XP.rotationDegrees(135.0F));
        pose.translate(0.0F, -60.0F, 0.0F);
        draw(draws, LOW, pose, SkyDraws.IDENTITY, color);

        pose = new PoseStack();
        pose.mulPose((Quaternionfc) Axis.XP.rotationDegrees(135.0F));
        pose.translate(0.0F, -30.0F, 0.0F);
        draw(draws, INNER, pose, SkyDraws.IDENTITY, color);
    }

    private static void draw(
            SkyDraws draws, SkyMesh fan, PoseStack pose, Matrix4fc local, float[] color) {
        draws.mesh(
                fan,
                SkyPipelines.SUNSET_ADDITIVE,
                pose,
                local,
                color[0],
                color[1],
                color[2],
                color[3],
                null);
    }

    private static SkyMesh fan(float height, float radius, float rimGreen) {
        return new SkyMesh(
                "Laythe sunset",
                DefaultVertexFormat.POSITION_COLOR,
                PrimitiveTopology.TRIANGLE_FAN,
                buffer -> {
                    buffer.addVertex(0.0F, height, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    for (int j = 0; j <= SEGMENTS; j++) {
                        float angle = j * (float) Math.PI * 2.0F / SEGMENTS;
                        float sin = Mth.sin(angle);
                        float cos = Mth.cos(angle);
                        buffer.addVertex(sin * radius, cos * radius, -cos * 90.0F)
                                .setColor(1.0F, rimGreen, 1.0F, 0.0F);
                    }
                });
    }
}
