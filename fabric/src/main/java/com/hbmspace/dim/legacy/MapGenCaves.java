// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MapGenCaves extends MapGenBase {
    protected float branchWidth = 0.5F;
    protected boolean waterStopsTunnels = true;
    protected float pitchDivisor = 8.0F;
    protected float widthScale = 2.0F;
    protected float wideScale = 3.0F;
    protected double tunnelHeightFactor = 1.0D;

    protected void addRoom(
            long seed, int chunkX, int chunkZ, BlockState[] blocks, double x, double y, double z) {
        this.addTunnel(
                seed,
                chunkX,
                chunkZ,
                blocks,
                x,
                y,
                z,
                1.0F + this.rand.nextFloat() * 6.0F,
                0.0F,
                0.0F,
                -1,
                -1,
                0.5D);
    }

    protected void addTunnel(
            long seed,
            int chunkX,
            int chunkZ,
            BlockState[] blocks,
            double x,
            double y,
            double z,
            float width,
            float yaw,
            float pitch,
            int from,
            int to,
            double heightFactor) {
        double centerX = (double) (chunkX * 16 + 8);
        double centerZ = (double) (chunkZ * 16 + 8);
        float yawChange = 0.0F;
        float pitchChange = 0.0F;
        Random random = new Random(seed);

        if (to <= 0) {
            int j1 = this.range * 16 - 16;
            to = j1 - random.nextInt(j1 / 4);
        }

        boolean room = false;

        if (from == -1) {
            from = to / 2;
            room = true;
        }

        int branch = random.nextInt(to / 2) + to / 4;

        for (boolean steep = random.nextInt(6) == 0; from < to; ++from) {
            double radius =
                    1.5D
                            + (double)
                                    (LegacyMath.sin((float) from * (float) Math.PI / (float) to)
                                            * width
                                            * 1.0F);
            double height = radius * heightFactor;
            float cosPitch = LegacyMath.cos(pitch);
            float sinPitch = LegacyMath.sin(pitch);
            x += (double) (LegacyMath.cos(yaw) * cosPitch);
            y += (double) sinPitch;
            z += (double) (LegacyMath.sin(yaw) * cosPitch);

            if (steep) {
                pitch *= 0.92F;
            } else {
                pitch *= 0.7F;
            }

            pitch += pitchChange * 0.1F;
            yaw += yawChange * 0.1F;
            pitchChange *= 0.9F;
            yawChange *= 0.75F;
            pitchChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!room && from == branch && width > 1.0F && to > 0) {
                this.addTunnel(
                        random.nextLong(),
                        chunkX,
                        chunkZ,
                        blocks,
                        x,
                        y,
                        z,
                        random.nextFloat() * branchWidth + 0.5F,
                        yaw - ((float) Math.PI / 2F),
                        pitch / 3.0F,
                        from,
                        to,
                        1.0D);
                this.addTunnel(
                        random.nextLong(),
                        chunkX,
                        chunkZ,
                        blocks,
                        x,
                        y,
                        z,
                        random.nextFloat() * branchWidth + 0.5F,
                        yaw + ((float) Math.PI / 2F),
                        pitch / 3.0F,
                        from,
                        to,
                        1.0D);
                return;
            }

            if (room || random.nextInt(4) != 0) {
                double dx = x - centerX;
                double dz = z - centerZ;
                double remaining = (double) (to - from);
                double maxReach = (double) (width + 2.0F + 16.0F);

                if (dx * dx + dz * dz - remaining * remaining > maxReach * maxReach) {
                    return;
                }

                if (x >= centerX - 16.0D - radius * 2.0D
                        && z >= centerZ - 16.0D - radius * 2.0D
                        && x <= centerX + 16.0D + radius * 2.0D
                        && z <= centerZ + 16.0D + radius * 2.0D) {
                    int minX = Mth.floor(x - radius) - chunkX * 16 - 1;
                    int maxX = Mth.floor(x + radius) - chunkX * 16 + 1;
                    int minY = Mth.floor(y - height) - 1;
                    int maxY = Mth.floor(y + height) + 1;
                    int minZ = Mth.floor(z - radius) - chunkZ * 16 - 1;
                    int maxZ = Mth.floor(z + radius) - chunkZ * 16 + 1;

                    if (minX < 0) minX = 0;
                    if (maxX > 16) maxX = 16;
                    if (minY < 1) minY = 1;
                    if (maxY > 248) maxY = 248;
                    if (minZ < 0) minZ = 0;
                    if (maxZ > 16) maxZ = 16;

                    boolean water = false;
                    int bx;
                    int bz;

                    for (bx = minX; waterStopsTunnels && !water && bx < maxX; ++bx) {
                        for (bz = minZ; !water && bz < maxZ; ++bz) {
                            for (int by = maxY + 1; !water && by >= minY - 1; --by) {
                                int index = (bx * 16 + bz) * 256 + by;

                                if (by >= 0 && by < 256) {
                                    if (isOceanBlock(blocks, index, bx, by, bz, chunkX, chunkZ)) {
                                        water = true;
                                    }

                                    if (by != minY - 1
                                            && bx != minX
                                            && bx != maxX - 1
                                            && bz != minZ
                                            && bz != maxZ - 1) {
                                        by = minY;
                                    }
                                }
                            }
                        }
                    }

                    if (!water) {
                        for (bx = minX; bx < maxX; ++bx) {
                            double nx = ((double) (bx + chunkX * 16) + 0.5D - x) / radius;

                            for (bz = minZ; bz < maxZ; ++bz) {
                                double nz = ((double) (bz + chunkZ * 16) + 0.5D - z) / radius;
                                int index = (bx * 16 + bz) * 256 + maxY;
                                boolean foundTop = false;

                                if (nx * nx + nz * nz < 1.0D) {
                                    for (int by = maxY - 1; by >= minY; --by) {
                                        double ny = ((double) by + 0.5D - y) / height;

                                        if (ny > -0.7D && nx * nx + ny * ny + nz * nz < 1.0D) {
                                            if (isTopBlock(
                                                    blocks, index, bx, by, bz, chunkX, chunkZ)) {
                                                foundTop = true;
                                            }
                                            digBlock(
                                                    blocks, index, bx, by, bz, chunkX, chunkZ,
                                                    foundTop);
                                        }

                                        --index;
                                    }
                                }
                            }
                        }

                        if (room) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void recursiveGenerate(
            LegacyWorld world,
            int offsetX,
            int offsetZ,
            int chunkX,
            int chunkZ,
            BlockState[] blocks) {
        int count = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);

        if (this.rand.nextInt(7) != 0) {
            count = 0;
        }

        for (int j1 = 0; j1 < count; ++j1) {
            double x = (double) (offsetX * 16 + this.rand.nextInt(16));
            double y = (double) this.rand.nextInt(this.rand.nextInt(120) + 8);
            double z = (double) (offsetZ * 16 + this.rand.nextInt(16));
            int tunnels = 1;

            if (this.rand.nextInt(4) == 0) {
                this.addRoom(this.rand.nextLong(), chunkX, chunkZ, blocks, x, y, z);
                tunnels += this.rand.nextInt(4);
            }

            for (int l1 = 0; l1 < tunnels; ++l1) {
                float yaw = this.rand.nextFloat() * (float) Math.PI * 2.0F;
                float pitch = (this.rand.nextFloat() - 0.5F) * 2.0F / pitchDivisor;
                float width = this.rand.nextFloat() * widthScale + this.rand.nextFloat();

                if (this.rand.nextInt(10) == 0) {
                    width *= this.rand.nextFloat() * this.rand.nextFloat() * wideScale + 1.0F;
                }

                this.addTunnel(
                        this.rand.nextLong(),
                        chunkX,
                        chunkZ,
                        blocks,
                        x,
                        y,
                        z,
                        width,
                        yaw,
                        pitch,
                        0,
                        0,
                        tunnelHeightFactor);
            }
        }
    }

    protected boolean isOceanBlock(
            BlockState[] data, int index, int x, int y, int z, int chunkX, int chunkZ) {
        return is(data[index], Blocks.WATER);
    }

    private boolean isTopBlock(
            BlockState[] data, int index, int x, int y, int z, int chunkX, int chunkZ) {
        BiomeGenBaseCelestial biome =
                worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
        return is(data[index], biome.topBlock);
    }

    protected void digBlock(
            BlockState[] data,
            int index,
            int x,
            int y,
            int z,
            int chunkX,
            int chunkZ,
            boolean foundTop) {
        BiomeGenBaseCelestial biome =
                worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
        BlockState block = data[index];

        if (is(block, Blocks.STONE) || is(block, biome.fillerBlock) || is(block, biome.topBlock)) {
            if (y < 10) {
                data[index] = Blocks.LAVA.defaultBlockState();
            } else {
                data[index] = null;

                if (foundTop && is(data[index - 1], biome.fillerBlock)) {
                    data[index - 1] = biome.topBlock.defaultBlockState();
                }
            }
        }
    }
}
