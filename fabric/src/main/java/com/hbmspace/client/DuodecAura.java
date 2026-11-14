/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023-2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023-2024 JamesH_2. All rights reserved.
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

package com.hbmspace.client;

import com.hbmspace.particle.SpaceParticles;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class DuodecAura {
    private static final UUID DUODEC = UUID.fromString("85d54b8c-1862-4c86-b351-5ef1b998aa32");

    private DuodecAura() {}

    public static void playerTick(Player player) {
        if (!player.getUUID().equals(DUODEC) || player.isInvisible() || player.isShiftKeyDown())
            return;
        RandomSource random = player.getRandom();
        double radius = 3D * random.nextDouble();
        float zAngle = (float) (random.nextDouble() * Math.PI);
        float yAngle = (float) (random.nextDouble() * Math.PI * 2D);
        Vec3 at =
                new Vec3(radius, 0D, 0D)
                        .zRot(zAngle)
                        .yRot(yAngle)
                        .add(player.getX(), player.getY() + 1D, player.getZ());
        Minecraft mc = Minecraft.getInstance();
        emit(mc, SpaceParticles.DUOEWE.get(), at, random);
        emit(mc, SpaceParticles.DUODEC.get(), at, random);
    }

    private static void emit(Minecraft mc, SimpleParticleType type, Vec3 at, RandomSource random) {
        if (random.nextInt(12) != 0) return;
        ParticleStatus settings = mc.options.particles().get();
        if (settings == ParticleStatus.ALL
                || settings == ParticleStatus.DECREASED && random.nextBoolean()) {
            mc.particleEngine.createParticle(type, at.x, at.y, at.z, 0D, 0D, 0D);
        }
    }
}
