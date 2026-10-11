// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.mapgen;

import com.hbmspace.dim.legacy.LegacyWorld;
import com.hbmspace.dim.legacy.MapGenBase;
import com.hbmspace.dim.noise.DoublePerlinNoiseSampler;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MapGenTiltedSpires extends MapGenBase {
    public static DoublePerlinNoiseSampler noise(long seed) {
        Random rand = new Random(seed + 69);
        return DoublePerlinNoiseSampler.create(new Random(rand.nextLong()), -8, 1.0D, 2.0D);
    }

    private final DoublePerlinNoiseSampler perlin;
    private final int chanceHigh;
    private final int chanceLow;
    private final float threshold;

    public int minSize = 6;
    public int maxSize = 16;
    public float minPoint = 0.5F;
    public float maxPoint = 4.0F;
    public float minTilt = 0.2F;
    public float maxTilt = 2.5F;
    public boolean curve = false;
    public int mid = 64;

    public Block regolith;
    public Block rock;

    public MapGenTiltedSpires(
            DoublePerlinNoiseSampler perlin, int chanceHigh, int chanceLow, float threshold) {
        this.perlin = perlin;
        this.chanceHigh = chanceHigh;
        this.chanceLow = chanceLow;
        this.threshold = threshold * 2F - 1F;
        range = 4;
    }

    public void setRange(int range) {
        this.range = range;
    }

    @Override
    protected void recursiveGenerate(
            LegacyWorld world,
            int offsetX,
            int offsetZ,
            int chunkX,
            int chunkZ,
            BlockState[] blocks) {
        int chance =
                perlin.sample(offsetX * 16, 0, offsetZ * 16) > threshold ? chanceHigh : chanceLow;

        if (rand.nextInt(chance) == Math.abs(offsetX) % chance
                && rand.nextInt(chance) == Math.abs(offsetZ) % chance) {

            float coneRadius = rand.nextInt(maxSize - minSize) + minSize;
            float stretch =
                    1F / (rand.nextFloat() * rand.nextFloat() * (maxPoint - minPoint) + minPoint);
            float direction = rand.nextFloat() * (float) Math.PI * 2F;
            float tilt = rand.nextFloat() * (maxTilt - minTilt) + minTilt;

            if (curve) tilt *= 0.01;

            int xCoord = -offsetX + chunkX;
            int zCoord = -offsetZ + chunkZ;

            float tx = (float) Math.cos(direction) * tilt;
            float tz = (float) Math.sin(direction) * tilt;

            int ox = (int) (tx * (curve ? 800 : 8));
            int oz = (int) (tz * (curve ? 800 : 8));

            BlockState rockState = rock.defaultBlockState();
            BlockState regolithState = regolith.defaultBlockState();

            for (int bx = 15; bx >= 0; bx--) {
                for (int bz = 15; bz >= 0; bz--) {
                    int d = 0;

                    for (int y = mid + 63; y >= 0; y--) {
                        int index = (bx * 16 + bz) * 256 + y;

                        if (blocks[index] == null || !blocks[index].isSolidRender()) {
                            int x = xCoord * 16 + bx - ox;
                            int z = zCoord * 16 + bz - oz;
                            int oy = y - mid;

                            float factor = (float) oy;
                            if (curve) factor *= factor;

                            x += tx * factor;
                            z += tz * factor;

                            float rs = x * x + z * z;
                            float radiusSqr = coneRadius - oy * stretch;
                            if (radiusSqr > 0) radiusSqr *= radiusSqr;

                            if (rs < radiusSqr) {
                                blocks[index] = rockState;
                                if (d == 1) blocks[index + 1] = regolithState;
                                d++;
                            } else if (d > 0) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
}
