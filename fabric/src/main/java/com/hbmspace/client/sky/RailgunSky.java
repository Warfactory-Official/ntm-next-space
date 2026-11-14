/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionfc;

public final class RailgunSky {
    private static final Identifier TEXTURE =
            Identifier.parse("hbmspace:textures/models/railgun_main.png");
    private static final SkyMesh BODY =
            SkyMesh.obj(Identifier.parse("hbmspace:models/railgun_main.obj"));

    private RailgunSky() {}

    public static void render(SkyDraws draws, PoseStack pose, long frequency) {
        pose.pushPose();
        pose.scale(5F, 5F, 5F);

        double rounded = Math.round(frequency / 1000.0D);
        double x = ((frequency % 10) - 5) * 2;
        double y = (((frequency / 10) % 10) - 5) * 2;
        pose.translate(
                Math.min(Math.max(-rounded + 30 + x, -50), 50),
                Math.min(Math.max(-rounded - 20 + y, -50), 50),
                20);
        pose.mulPose((Quaternionfc) Axis.ZP.rotationDegrees(-90F));
        draws.mesh(BODY, SkyPipelines.RAILGUN_BODY, pose, SkyDraws.IDENTITY, 1, 1, 1, 1, TEXTURE);
        draws.clearDepth();
        pose.popPose();
    }
}
