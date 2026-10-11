// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto;

import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TTree {

    private final int offset;
    private final int smallest;
    private final int tallest;
    private final int xz;
    private final int y;
    private final boolean vines;
    private final Supplier<? extends Block> log;
    private final Supplier<? extends Block> leaves;

    public TTree(
            int offset,
            int smallest,
            int tallest,
            int xz,
            int y,
            boolean vines,
            Supplier<? extends Block> log,
            Supplier<? extends Block> leaves) {
        this.offset = offset;
        this.smallest = smallest;
        this.tallest = tallest;
        this.xz = xz;
        this.y = y;
        this.vines = vines;
        this.log = log;
        this.leaves = leaves;
    }

    public boolean generate(LevelAccessor world, Random rand, int x, int y, int z) {
        int height = rand.nextInt(smallest) + tallest;

        if (y < world.getMinY() + 1 || y + height + 1 > world.getMaxY() + 1) {
            return false;
        }

        BlockState below = world.getBlockState(new BlockPos(x, y - 1, z));
        if (!below.is(FloraBlocksSpace.RUBBER_GRASS.get())
                && !below.is(TerrainBlocksSpace.RUBBER_SILT.get())) {
            return false;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState logState = log.get().defaultBlockState();
        for (int i = 0; i < height; i++) {
            world.setBlock(pos.set(x, y + i, z), logState, Block.UPDATE_CLIENTS);
        }

        BlockState leafState = leaves.get().defaultBlockState();
        int bulbStartY = y + height - offset;

        for (int dy = -this.y; dy <= this.y; dy++) {
            for (int dx = -xz; dx <= xz; dx++) {
                for (int dz = -xz; dz <= xz; dz++) {

                    if (Math.pow(dx / (double) xz, 2)
                                    + Math.pow(dy / (double) this.y, 2)
                                    + Math.pow(dz / (double) xz, 2)
                            <= 1) {
                        if (world.getBlockState(pos.set(x + dx, bulbStartY + dy, z + dz)).isAir()) {
                            world.setBlock(pos, leafState, Block.UPDATE_CLIENTS);
                            if (vines) hangVines(world, rand, x + dx, bulbStartY + dy, z + dz);
                        }
                    }
                }
            }
        }

        return true;
    }

    private void hangVines(LevelAccessor world, Random rand, int leafX, int leafY, int leafZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 4; i++) {
            int vineX = leafX + (i % 2 == 0 ? (i - 1) : 0);
            int vineZ = leafZ + (i % 2 == 1 ? (i - 2) : 0);

            if (rand.nextInt(12) == 0) {
                for (int j = 0; j < rand.nextInt(15); j++) {
                    int vineY = leafY - j;
                    if (!world.getBlockState(pos.set(vineX, vineY, vineZ)).isAir()) break;
                    BlockState vine = FloraBlocksSpace.VINYL_VINE.get().defaultBlockState();
                    if (vineX > leafX) vine = vine.setValue(VineBlock.WEST, true);
                    else if (vineX < leafX) vine = vine.setValue(VineBlock.EAST, true);
                    else if (vineZ > leafZ) vine = vine.setValue(VineBlock.NORTH, true);
                    else if (vineZ < leafZ) vine = vine.setValue(VineBlock.SOUTH, true);
                    world.setBlock(pos, vine, Block.UPDATE_CLIENTS);
                }
            }
        }
    }
}
