// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2021 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy.feature;

import com.hbmspace.dim.legacy.LegacyDecorWorld;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class DepthDeposit {

    private DepthDeposit() {}

    public static void generateCondition(
            LegacyDecorWorld world,
            int x,
            int yMin,
            int yDev,
            int z,
            int size,
            double fill,
            BlockState block,
            Random rand,
            int chance,
            Block genTarget,
            BlockState filler) {
        if (rand.nextInt(chance) == 0) {
            generate(
                    world,
                    x + rand.nextInt(16) + 8,
                    yMin + rand.nextInt(yDev),
                    z + rand.nextInt(16) + 8,
                    size,
                    fill,
                    block,
                    rand,
                    genTarget,
                    filler);
        }
    }

    public static void generate(
            LegacyDecorWorld world,
            int x,
            int y,
            int z,
            int size,
            double fill,
            BlockState block,
            Random rand,
            Block genTarget,
            BlockState filler) {
        for (int i = x - size; i <= x + size; i++) {
            for (int j = y - size; j <= y + size; j++) {
                for (int k = z - size; k <= z + size; k++) {
                    if (j < 1 || j > 126) continue;

                    double len =
                            (float)
                                    Math.sqrt(
                                            (double) (x - i) * (x - i)
                                                    + (double) (y - j) * (y - j)
                                                    + (double) (z - k) * (z - k));
                    if (world.is(i, j, k, genTarget) || world.is(i, j, k, Blocks.BEDROCK)) {
                        if (len + rand.nextInt(2) < size * fill) {
                            world.setBlock(i, j, k, block, Block.UPDATE_ALL);
                        } else if (len + rand.nextInt(2) <= size) {
                            world.setBlock(i, j, k, filler, Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }
    }
}
