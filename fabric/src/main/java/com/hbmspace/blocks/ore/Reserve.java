// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.ore;

import com.hbm.blocks.generic.FluidReserveBlock;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.blocks.ore.BlockOreFluid.ReserveType;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.world.feature.OilCrack;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public record Reserve(
        ReserveType type,
        Body body,
        @Nullable Supplier<? extends Block> empty,
        @Nullable CelestialOre emptyFamily)
        implements FluidReserveBlock {

    @Override
    public Fluid primaryFluid() {
        return switch (type) {
            case OIL ->
                    body == Body.LAYTHE
                            ? NTMFluids.OIL_DS
                            : body == Body.TEKTO ? FluidsSpace.tcrude() : NTMFluids.OIL;
            case GAS -> NTMFluids.GAS;
            case BRINE -> FluidsSpace.brine();
        };
    }

    @Override
    public @Nullable Fluid secondaryFluid() {
        return switch (type) {
            case OIL -> body == Body.TEKTO ? FluidsSpace.hgas() : NTMFluids.GAS;
            case GAS -> NTMFluids.PETROLEUM;
            case BRINE -> null;
        };
    }

    @Override
    public int primaryAmount() {
        if (empty == null) {
            return body == Body.TEKTO
                    ? SpaceData.TEKTO_BEDROCK_OIL_PER_DEPOSIT.get()
                    : SpaceData.BEDROCK_OIL_PER_DEPOSIT.get();
        }
        return switch (body) {
            case DUNA -> SpaceData.DUNA_OIL_PER_DEPOSIT.get();
            case LAYTHE -> SpaceData.LAYTHE_OIL_PER_DEPOSIT.get();
            case EVE -> SpaceData.EVE_GAS_PER_DEPOSIT.get();
            case MUN -> SpaceData.MUN_BRINE_PER_DEPOSIT.get();
            case MINMUS -> SpaceData.MINMUS_BRINE_PER_DEPOSIT.get();
            case IKE -> SpaceData.IKE_BRINE_PER_DEPOSIT.get();
            case TEKTO -> SpaceData.TEKTO_OIL_PER_DEPOSIT.get();
            default -> SpaceData.EARTH_OIL_PER_DEPOSIT.get();
        };
    }

    @Override
    public int secondaryAmount(RandomSource random) {
        if (empty == null) {
            return body == Body.TEKTO
                    ? between(
                            random,
                            SpaceData.TEKTO_BEDROCK_GAS_MIN.get(),
                            SpaceData.TEKTO_BEDROCK_GAS_MAX.get())
                    : between(
                            random,
                            SpaceData.BEDROCK_GAS_MIN.get(),
                            SpaceData.BEDROCK_GAS_MAX.get());
        }
        return switch (body) {
            case DUNA ->
                    between(random, SpaceData.DUNA_GAS_MIN.get(), SpaceData.DUNA_GAS_MAX.get());
            case LAYTHE ->
                    between(random, SpaceData.LAYTHE_GAS_MIN.get(), SpaceData.LAYTHE_GAS_MAX.get());
            case EVE -> between(random, SpaceData.EVE_PET_MIN.get(), SpaceData.EVE_PET_MAX.get());
            case TEKTO ->
                    between(random, SpaceData.TEKTO_GAS_MIN.get(), SpaceData.TEKTO_GAS_MAX.get());
            default ->
                    between(random, SpaceData.EARTH_GAS_MIN.get(), SpaceData.EARTH_GAS_MAX.get());
        };
    }

    private static int between(RandomSource random, int min, int max) {
        return min >= max ? min : min + random.nextInt(max - min);
    }

    private double drainChance() {
        if (empty == null) return 0.0D;
        return switch (body) {
            case DUNA -> SpaceData.DUNA_OIL_DRAIN.get();
            case LAYTHE -> SpaceData.LAYTHE_OIL_DRAIN.get();
            case EVE -> SpaceData.EVE_GAS_DRAIN.get();
            case MUN -> SpaceData.MUN_BRINE_DRAIN.get();
            case MINMUS -> SpaceData.MINMUS_BRINE_DRAIN.get();
            case IKE -> SpaceData.IKE_BRINE_DRAIN.get();
            case TEKTO -> SpaceData.TEKTO_OIL_DRAIN.get();
            default -> SpaceData.EARTH_OIL_DRAIN.get();
        };
    }

    @Override
    public void drain(Level level, BlockPos pos, RandomSource random, double chanceMultiplier) {
        if (empty == null) return;
        if (random.nextDouble() < drainChance() * chanceMultiplier) {
            level.setBlock(pos, empty.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean requiresFracking() {
        return empty == null;
    }

    @Override
    public void scarSurface(Level level, RandomSource random, int x, int z, int range) {
        if (body == Body.TEKTO)
            OilCrack.generateCrack((ServerLevel) level, random, x, z, range, 10);
        else FluidReserveBlock.super.scarSurface(level, random, x, z, range);
    }

    @Override
    public int blockFluidAmount() {
        return switch (type) {
            case OIL -> 250;
            case GAS -> 100;
            case BRINE -> 0;
        };
    }

    @Override
    public void settle(Level level, BlockPos pos, BlockState state) {
        if (empty == null) return;
        Block beneath = level.getBlockState(pos.below()).getBlock();
        for (Body variant : Body.values()) {
            if (beneath != emptyFamily.block(variant)) continue;
            level.setBlock(pos, empty.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos.below(), state, Block.UPDATE_ALL);
            return;
        }
    }

    String detectorSuffix() {
        return switch (type) {
            case OIL -> "";
            case GAS -> "_gas";
            case BRINE -> "_brine";
        };
    }
}
