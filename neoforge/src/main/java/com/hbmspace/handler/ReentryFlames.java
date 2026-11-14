/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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

package com.hbmspace.handler;

import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.ClientEventConstants;
import com.hbm.packet.PacketWire;
import com.hbm.packet.layout.GasFlameLayout;
import com.hbm.particle.HbmParticles;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ReentryFlames {

    private static final Random RANDOM = new Random();

    private ReentryFlames() {}

    public static void playerTick(Player player) {
        if (player.isSpectator()) return;
        double y = player.getY();
        if (y <= 300D || y >= 1000D) return;
        double x = player.getX() - 1D + 3D * RANDOM.nextDouble();
        Level level = player.level();
        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        if (!eligible(
                y,
                atmosphere == null ? 0D : atmosphere.getPressure(),
                player.isPassenger(),
                player.getDeltaMovement())) return;

        double z = player.getZ();
        if (level instanceof ServerLevel server) {
            PacketWire.sendToAllAround(
                    GasFlameLayout.encode(
                            x, y, z, 0D, 0D, 0D, ClientEventConstants.GAS_FLAME_DEFAULT_SCALE),
                    new TargetPoint(server, x, y, z, 150D));
        } else {
            level.addParticle(HbmParticles.GAS_FLAME.get(), true, false, x, y, z, 0D, 0D, 0D);
        }
    }

    private static boolean eligible(double y, double pressure, boolean riding, Vec3 motion) {
        return y > 300D
                && y < 1000D
                && pressure > 0.05D
                && !riding
                && (Math.abs(motion.x) > 1D || Math.abs(motion.y) > 1D || Math.abs(motion.z) > 1D);
    }
}
