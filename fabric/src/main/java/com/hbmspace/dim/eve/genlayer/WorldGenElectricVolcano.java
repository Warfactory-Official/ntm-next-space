// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.genlayer;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class WorldGenElectricVolcano {
    public static final int HEIGHT = 30;
    public static final int WIDTH = 22;

    public interface Terrain {
        BlockState get(int x, int y, int z);

        void set(int x, int y, int z, BlockState state);
    }

    public record Shape(int y, int l, int i1) {}

    private WorldGenElectricVolcano() {}

    public static Shape shape(Random rand, int y) {
        y += rand.nextInt(1);
        int l = rand.nextInt(4) + HEIGHT;
        int i1 = l / 4 + rand.nextInt(WIDTH);

        if (i1 > 1 && rand.nextInt(5) == 0) {
            rand.nextInt(30);
        }

        return new Shape(y, l, i1);
    }

    public static void build(
            Terrain terrain,
            Random rand,
            int x,
            int z,
            Shape shape,
            Block surfaceBlock,
            Block stoneBlock,
            BlockState basalt,
            BlockState neodymium,
            BlockState geysir,
            BlockState air) {
        int y = shape.y();
        int l = shape.l();
        int i1 = shape.i1();

        for (int j1 = 0; j1 < l; ++j1) {
            float f = (1.0F - (float) j1 / (float) l) * (float) i1;
            int k1 = Mth.ceil(f);

            for (int l1 = -k1; l1 <= k1; ++l1) {
                float f1 = (float) Mth.abs(l1) - 0.25F;

                for (int i2 = -k1; i2 <= k1; ++i2) {
                    float f2 = (float) Mth.abs(i2) - 0.75F;

                    if ((l1 == 0 && i2 == 0 || f1 * f1 + f2 * f2 <= f * f)
                            && (l1 != -k1 && l1 != k1 && i2 != -k1 && i2 != k1
                                    || rand.nextFloat() <= 0.75F)) {
                        if (replaceable(
                                terrain.get(x + l1, y + j1, z + i2), surfaceBlock, stoneBlock)) {
                            terrain.set(x + l1, y + j1, z + i2, basalt);
                        }

                        if (j1 != 0
                                && k1 > 1
                                && replaceable(
                                        terrain.get(x + l1, y - j1, z + i2),
                                        surfaceBlock,
                                        stoneBlock)) {
                            terrain.set(x + l1, y - j1, z + i2, neodymium);
                        }
                    }
                    if ((l1 == 0 && j1 == 0) || f1 * f1 + f2 * f2 > f * f) {
                        if (replaceable(
                                terrain.get(x + l1, y + j1, z + i2), surfaceBlock, stoneBlock)) {
                            terrain.set(x, y + j1, z, air);
                        }
                    }
                    if ((l1 == 0 && j1 == 0) || f1 * f1 + f2 * f2 > f * f) {
                        if (replaceable(
                                terrain.get(x + l1, y + j1, z + i2), surfaceBlock, stoneBlock)) {
                            terrain.set(x, y + 5, z, geysir);
                        }
                    }
                }
            }
        }
    }

    private static boolean replaceable(BlockState state, Block surfaceBlock, Block stoneBlock) {
        return state.isAir() || state.is(surfaceBlock) || state.is(stoneBlock);
    }
}
