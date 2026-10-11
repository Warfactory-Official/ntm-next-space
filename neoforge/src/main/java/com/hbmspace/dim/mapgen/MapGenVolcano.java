// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.mapgen;

import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.MapGenBase;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MapGenVolcano extends MapGenBase {

    private int chancePerChunk = 100;
    private int minSize = 32;
    private int maxSize = 64;

    private Block coreBlock;
    private Block materialBlock;

    public MapGenVolcano(int chancePerChunk) {
        this.chancePerChunk = chancePerChunk;
    }

    public void setSize(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;

        this.range = (maxSize / 8) + 1;
    }

    public void setMaterial(Block coreBlock, Block materialBlock) {
        this.coreBlock = coreBlock;
        this.materialBlock = materialBlock;
    }

    private double heightFunc(double x, double rad, double depth) {
        double xs = x / (rad * 2);
        double inner = (x * x * x) / (rad / 4) + 32;
        double outer = 1 / (xs * xs);
        return Math.min(inner, outer) * depth;
    }

    @Override
    protected void recursiveGenerate(
            LegacyWorld world,
            int offsetX,
            int offsetZ,
            int chunkX,
            int chunkZ,
            BlockState[] blocks) {
        if (rand.nextInt(chancePerChunk) == Math.abs(offsetX) % chancePerChunk
                && rand.nextInt(chancePerChunk) == Math.abs(offsetZ) % chancePerChunk) {

            double radius = rand.nextInt(maxSize - minSize) + minSize;
            double depth = 0.75D;

            int xCoord = -offsetX + chunkX;
            int zCoord = -offsetZ + chunkZ;

            for (int bx = 15; bx >= 0; bx--) {
                for (int bz = 15; bz >= 0; bz--) {
                    for (int y = 254; y >= 0; y--) {
                        int index = (bx * 16 + bz) * 256 + y;

                        if (blocks[index] != null && blocks[index].isSolidRender()) {
                            int x = xCoord * 16 + bx;
                            int z = zCoord * 16 + bz;

                            double r = Math.sqrt(x * x + z * z);

                            if (r - rand.nextInt(16) <= radius) {
                                int height =
                                        (int) Mth.clamp(heightFunc(r, radius, depth), 0, y - 1);
                                if (height > 0) {
                                    for (int i = 0; i < height && i + y < 255; i++) {
                                        blocks[index + i] = materialBlock.defaultBlockState();
                                    }
                                } else {
                                    for (int i = 0; i > height && i + y > 1; i--) {
                                        blocks[index + i + 1] = null;
                                    }
                                }

                                index += height;
                                y += height;

                                if (x == 0 && z == 0 && SpaceData.ENABLE_VOLCANO_GEN.get()) {
                                    blocks[index + 1] = coreBlock.defaultBlockState();
                                }
                            }

                            break;
                        }
                    }
                }
            }
        }
    }
}
