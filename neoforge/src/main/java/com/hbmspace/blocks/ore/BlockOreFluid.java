// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.ore;

import com.hbm.blocks.generic.FluidReserveBlock;
import com.hbmspace.dim.SolarSystem.Body;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class BlockOreFluid extends Block implements FluidReserveBlock {

    public enum ReserveType {
        OIL,
        GAS,
        BRINE
    }

    public final Reserve reserve;

    public BlockOreFluid(
            Properties properties,
            @Nullable Supplier<? extends Block> empty,
            ReserveType type,
            Body body,
            @Nullable CelestialOre emptyFamily) {
        super(properties);
        this.reserve = new Reserve(type, body, empty, emptyFamily);
    }

    @Override
    public Fluid primaryFluid() {
        return reserve.primaryFluid();
    }

    @Override
    public @Nullable Fluid secondaryFluid() {
        return reserve.secondaryFluid();
    }

    @Override
    public int primaryAmount() {
        return reserve.primaryAmount();
    }

    @Override
    public int secondaryAmount(RandomSource random) {
        return reserve.secondaryAmount(random);
    }

    @Override
    public void drain(Level level, BlockPos pos, RandomSource random, double chanceMultiplier) {
        reserve.drain(level, pos, random, chanceMultiplier);
    }

    @Override
    public boolean requiresFracking() {
        return reserve.requiresFracking();
    }

    @Override
    public void scarSurface(Level level, RandomSource random, int x, int z, int range) {
        reserve.scarSurface(level, random, x, z, range);
    }

    @Override
    public int blockFluidAmount() {
        return reserve.blockFluidAmount();
    }

    @Override
    public void settle(Level level, BlockPos pos, BlockState state) {
        reserve.settle(level, pos, state);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighbor,
            @Nullable Orientation orientation,
            boolean movedByPiston) {
        settle(level, pos, state);
    }

    public static class Drained extends Block implements FluidReserveBlock.Drained {

        public Drained(Properties properties) {
            super(properties);
        }
    }
}
