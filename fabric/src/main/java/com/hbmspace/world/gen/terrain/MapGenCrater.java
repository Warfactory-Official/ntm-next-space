// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.world.gen.terrain;

import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.MapGenBase;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MapGenCrater extends MapGenBase {

    private final int frequency;
    private int minSize = 8;
    private int maxSize = 64;

    public Block regolith;
    public Block rock;

    public MapGenCrater(int frequency) {
        this.frequency = frequency;
    }

    public void setSize(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.range = (maxSize / 8) + 1;
    }

    private double depthFunc(double x, double rad, double depth) {
        return -Math.pow(x, 2) / Math.pow(rad, 2) * depth + depth;
    }

    @Override
    protected void recursiveGenerate(
            LegacyWorld world,
            int offsetX,
            int offsetZ,
            int chunkX,
            int chunkZ,
            BlockState[] blocks) {
        if (rand.nextInt(frequency) == 0) {
            int xCoord = -offsetX + chunkX;
            int zCoord = -offsetZ + chunkZ;
            double radius = rand.nextInt(maxSize - minSize) + minSize;
            double depth = radius * 0.35D;
            BlockState regolithState = regolith.defaultBlockState();
            BlockState rockState = rock.defaultBlockState();

            for (int bx = 15; bx >= 0; bx--) {
                for (int bz = 15; bz >= 0; bz--) {
                    for (int y = 127; y >= 0; y--) {
                        int index = (bx * 16 + bz) * 256 + y;
                        BlockState state = blocks[index];

                        if (state != null
                                && (state.isSolidRender() || !state.getFluidState().isEmpty())) {
                            int x = xCoord * 16 + bx;
                            int z = zCoord * 16 + bz;

                            double r = Math.sqrt(x * x + z * z);
                            if (r - rand.nextInt(3) <= radius) {
                                int dep = (int) Mth.clamp(depthFunc(r, radius, depth), 0, y - 1);
                                for (int i = 0; i < dep; i++) {
                                    blocks[index - i] = null;
                                }
                                index -= dep;
                                y -= dep;
                                dep = Math.min(3, y - 1);

                                if (r + rand.nextInt(3) <= radius / 3D) {
                                    for (int i = 0; i < dep; i++) {
                                        blocks[index - i] = regolithState;
                                    }
                                } else {
                                    for (int i = 0; i < dep; i++) {
                                        blocks[index - i] = rockState;
                                    }
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
