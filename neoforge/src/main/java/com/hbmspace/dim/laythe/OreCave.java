// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2022 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockSpike;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.generic.BlockFallingTint;
import com.hbmspace.dim.WorldGenWaterCoral;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.NoiseGeneratorPerlin;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class OreCave {

    private static final Direction[] VALID_DIRECTIONS = {
        Direction.DOWN,
        Direction.UP,
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST
    };
    private static final Direction[] HORIZONTALS = {
        Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private record Noise(long seed, NoiseGeneratorPerlin perlin) {}

    private final Supplier<BlockState> ore;
    private final int legacyHash;
    private double threshold = 2D;
    private int rangeMult = 3;
    private int maxRange = 4;
    private int yLevel = 30;
    private @Nullable Supplier<BlockState> fluid;
    private @Nullable Supplier<Block> override;
    private boolean ignoreWater;
    private @Nullable Supplier<BlockState> stalactite;
    private @Nullable Supplier<BlockState> stalagmite;
    private volatile @Nullable Noise noise;

    public OreCave(Supplier<BlockState> ore, String legacyName, int legacyMeta) {
        this.ore = ore;
        int result = 1;
        result = 31 * result + legacyName.hashCode();
        result = 31 * result + legacyMeta;
        this.legacyHash = result;
    }

    public static List<OreCave> laythe() {
        return List.of(
                stoneResource(() -> ModBlocks.STONE_RESOURCE_SULFUR.get(), 0)
                        .setThreshold(1.5D)
                        .setRangeMult(20)
                        .setYLevel(30)
                        .setMaxRange(20)
                        .withFluid(() -> ModBlocks.SULFURIC_ACID_BLOCK.get().defaultBlockState())
                        .setStalagmites(
                                () -> ModBlocks.STALACTITE_SULFUR.get().defaultBlockState(),
                                () -> ModBlocks.STALAGMITE_SULFUR.get().defaultBlockState()),
                stoneResource(() -> ModBlocks.STONE_RESOURCE_ASBESTOS.get(), 1)
                        .setThreshold(1.75D)
                        .setRangeMult(20)
                        .setYLevel(25)
                        .setMaxRange(20)
                        .setStalagmites(
                                () -> ModBlocks.STALACTITE_ASBESTOS.get().defaultBlockState(),
                                () -> ModBlocks.STALAGMITE_ASBESTOS.get().defaultBlockState()),
                new OreCave(
                                () -> TerrainBlocksSpace.TUMOR.get().defaultBlockState(),
                                "hbm:tile.tumor",
                                0)
                        .setThreshold(0.3D)
                        .setRangeMult(20)
                        .setYLevel(25)
                        .setMaxRange(70)
                        .setStalagmites(
                                () -> TerrainBlocksSpace.STALACTITE_FLESH.get().defaultBlockState(),
                                () ->
                                        TerrainBlocksSpace.STALAGMITE_FLESH
                                                .get()
                                                .defaultBlockState()),
                silt(2, 6).setThreshold(0.25D).setRangeMult(80).setYLevel(54).setMaxRange(24),
                silt(1, 3).setThreshold(0.2D).setRangeMult(60).setYLevel(58).setMaxRange(14));
    }

    private static OreCave stoneResource(Supplier<Block> block, int legacyMeta) {
        return new OreCave(
                () -> block.get().defaultBlockState(), "hbm:tile.stone_resource", legacyMeta);
    }

    private static OreCave silt(int shade, int legacyMeta) {
        return new OreCave(
                        () ->
                                TerrainBlocksSpace.LAYTHE_SILT
                                        .get()
                                        .defaultBlockState()
                                        .setValue(BlockFallingTint.SHADE, shade),
                        "hbm:tile.laythe_silt",
                        legacyMeta)
                .setBlockOverride(TerrainBlocksSpace.LAYTHE_SILT)
                .setIgnoreWater(true);
    }

    public OreCave setThreshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    public OreCave setRangeMult(int rangeMult) {
        this.rangeMult = rangeMult;
        return this;
    }

    public OreCave setMaxRange(int maxRange) {
        this.maxRange = maxRange;
        return this;
    }

    public OreCave setYLevel(int yLevel) {
        this.yLevel = yLevel;
        return this;
    }

    public OreCave withFluid(Supplier<BlockState> fluid) {
        this.fluid = fluid;
        return this;
    }

    public OreCave setBlockOverride(Supplier<? extends Block> override) {
        this.override = override::get;
        return this;
    }

    public OreCave setIgnoreWater(boolean ignoreWater) {
        this.ignoreWater = ignoreWater;
        return this;
    }

    public OreCave setStalagmites(
            Supplier<BlockState> stalactite, Supplier<BlockState> stalagmite) {
        this.stalactite = stalactite;
        this.stalagmite = stalagmite;
        return this;
    }

    private NoiseGeneratorPerlin noise(long seed) {
        Noise current = noise;
        if (current == null || current.seed() != seed) {
            current =
                    new Noise(
                            seed,
                            new NoiseGeneratorPerlin(
                                    new Random(seed + (legacyHash * 31) + yLevel), 2));
            noise = current;
        }
        return current.perlin();
    }

    public void generate(LegacyDecorWorld world, Random rand, int cX, int cZ, Block stone) {
        NoiseGeneratorPerlin perlin = noise(world.getSeed());
        Block replace = override != null ? override.get() : stone;
        BlockState oreState = ore.get();
        @Nullable BlockState fluidState = fluid == null ? null : fluid.get();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos near = new BlockPos.MutableBlockPos();

        double scale = 0.01D;

        for (int x = cX + 8; x < cX + 24; x++) {
            for (int z = cZ + 8; z < cZ + 24; z++) {
                double n = perlin.getValue(x * scale, z * scale);

                if (n > threshold) {
                    int range = (int) ((n - threshold) * rangeMult);

                    if (range > maxRange) range = (maxRange * 2) - range;

                    if (range < 0) continue;

                    for (int y = yLevel - range; y <= yLevel + range; y++) {
                        BlockState genTarget = world.getBlock(x, y, z);
                        pos.set(x, y, z);

                        if (normalCube(world, genTarget, pos) && genTarget.is(replace)) {
                            boolean shouldGen = false;
                            boolean canGenFluid = rand.nextBoolean();

                            for (Direction dir : VALID_DIRECTIONS) {
                                near.setWithOffset(pos, dir);
                                BlockState neighbor =
                                        world.getBlock(near.getX(), near.getY(), near.getZ());
                                if (neighbor.isAir()
                                        || (ignoreWater
                                                && WorldGenWaterCoral.isWaterMaterial(neighbor))
                                        || neighbor.getBlock() instanceof BlockSpike) {
                                    shouldGen = true;
                                }

                                if (shouldGen && (fluidState == null || !canGenFluid)) break;

                                if (fluidState != null) {
                                    switch (dir) {
                                        case UP -> {
                                            if (!neighbor.isAir()
                                                    && !(neighbor.getBlock()
                                                            instanceof BlockSpike)) {
                                                canGenFluid = false;
                                            }
                                        }
                                        case DOWN -> {
                                            if (!normalCube(world, neighbor, near))
                                                canGenFluid = false;
                                        }
                                        default -> {
                                            if (!normalCube(world, neighbor, near)
                                                    && !neighbor.is(fluidState.getBlock())) {
                                                canGenFluid = false;
                                            }
                                        }
                                    }
                                }
                            }

                            if (fluidState != null && canGenFluid) {
                                world.setBlock(x, y, z, fluidState, Block.UPDATE_CLIENTS);
                                world.setBlock(x, y - 1, z, oreState, Block.UPDATE_CLIENTS);

                                for (Direction dir : HORIZONTALS) {
                                    near.setWithOffset(pos, dir);
                                    BlockState neighbor =
                                            world.getBlock(near.getX(), y, near.getZ());

                                    if (normalCube(world, neighbor, near)) {
                                        world.setBlock(
                                                near.getX(),
                                                y,
                                                near.getZ(),
                                                oreState,
                                                Block.UPDATE_CLIENTS);
                                    }
                                }

                            } else if (shouldGen) {
                                world.setBlock(x, y, z, oreState, Block.UPDATE_CLIENTS);
                            }

                        } else if (stalactite != null) {
                            if ((genTarget.isAir() || !normalCube(world, genTarget, pos))
                                    && rand.nextInt(5) == 0
                                    && genTarget.getFluidState().isEmpty()) {
                                BlockState hanging = stalactite.get();
                                BlockState standing = stalagmite.get();
                                if (hanging.canSurvive(world.level(), pos)) {
                                    world.setBlock(x, y, z, hanging, Block.UPDATE_CLIENTS);
                                } else if (standing.canSurvive(world.level(), pos)) {
                                    world.setBlock(x, y, z, standing, Block.UPDATE_CLIENTS);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean normalCube(LegacyDecorWorld world, BlockState state, BlockPos pos) {
        return state.isRedstoneConductor(world.level(), pos);
    }
}
