// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MapGenBase {

    protected int range = 8;
    protected Random rand = new Random();
    protected LegacyWorld worldObj;

    public void generate(LegacyWorld world, int chunkX, int chunkZ, BlockState[] blocks) {
        int k = this.range;
        this.worldObj = world;
        this.rand.setSeed(world.getSeed());
        long l = this.rand.nextLong();
        long i1 = this.rand.nextLong();

        for (int j1 = chunkX - k; j1 <= chunkX + k; ++j1) {
            for (int k1 = chunkZ - k; k1 <= chunkZ + k; ++k1) {
                long l1 = (long) j1 * l;
                long i2 = (long) k1 * i1;
                this.rand.setSeed(l1 ^ i2 ^ world.getSeed());
                this.recursiveGenerate(world, j1, k1, chunkX, chunkZ, blocks);
            }
        }
    }

    protected void recursiveGenerate(
            LegacyWorld world,
            int offsetX,
            int offsetZ,
            int chunkX,
            int chunkZ,
            BlockState[] blocks) {}

    protected static boolean is(BlockState state, Block block) {
        return state != null && state.getBlock() == block;
    }
}
