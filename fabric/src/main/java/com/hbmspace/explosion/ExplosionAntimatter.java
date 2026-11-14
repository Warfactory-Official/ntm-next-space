/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-FileCopyrightText: 2020 HbmMods (The Bobcat)
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
 * Copyright (C) 2020 HbmMods (The Bobcat). All rights reserved.
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

package com.hbmspace.explosion;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.spiral.BalefireColumnProcessor;
import com.hbm.explosion.spiral.ExplosionColumnProcessor;
import com.hbm.explosion.spiral.SpiralExplosion;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.ClientEventConstants;
import com.hbm.packet.PacketWire;
import com.hbm.packet.layout.GasFlameLayout;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ExplosionAntimatter implements ExplosionColumnProcessor {

    public static final ExplosionAntimatter INSTANCE = new ExplosionAntimatter();

    private ExplosionAntimatter() {}

    @Override
    public void column(SpiralExplosion sweep, ServerLevel level, int x, int z) {
        int radius = sweep.getRadius();
        int dist = (int) (radius - Math.sqrt(x * x + z * z));

        if (dist <= 0) return;

        int pX = sweep.getOriginX() + x;
        int pZ = sweep.getOriginZ() + z;
        var pos = sweep.getCursor();

        int y = BalefireColumnProcessor.surface(level, pos, pX, pZ);
        int maxdepth = (int) (10 + radius * 0.25);
        int depth = (int) (maxdepth * dist / radius + (Math.sin(dist * 0.15 + 2) * 2));

        depth = Math.max(y - depth, level.getMinY());

        while (y > depth) {
            pos.set(pX, y, pZ);

            if (flames(level.getBlockState(pos))) {
                PacketWire.sendToAllAround(
                        GasFlameLayout.encode(
                                pX, y, pZ, x, y, z, ClientEventConstants.GAS_FLAME_DEFAULT_SCALE),
                        new TargetPoint(level, pX, y, pZ, 150));
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }

            y--;
        }

        for (int i = depth; i > depth - 5; i--) {
            if (level.getRandom().nextInt(dist) == 0) {
                level.setBlock(
                        pos.set(pX, depth, pZ),
                        ModBlocks.VOLCANIC_LAVA_BLOCK.get().defaultBlockState(),
                        Block.UPDATE_ALL);
            }

            pos.set(pX, i, pZ);
            if (BalefireColumnProcessor.slakes(level.getBlockState(pos))) {
                level.setBlock(
                        pos,
                        ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }
    }

    private static boolean flames(BlockState state) {
        return state.is(BlockTags.SNOW) || state.is(BlockTags.ICE);
    }
}
