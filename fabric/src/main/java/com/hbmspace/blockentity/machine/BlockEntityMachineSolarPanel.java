// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.WorldProviderCelestial;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityMachineSolarPanel extends BlockEntityMachineBase
        implements IEnergyHandlerMK2 {
    public static final long MAX_POWER = 1_000;

    public long power;

    public BlockEntityMachineSolarPanel(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.SOLAR_PANEL.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        int sun = level.getBrightness(LightLayer.SKY, worldPosition) - level.getSkyDarken() - 11;
        if (sun <= 0 || !seesSky(worldPosition.above())) return;

        long charged = Math.min(power + getOutput(sun), MAX_POWER);
        if (charged == power) return;
        power = charged;
        setChanged();
    }

    private boolean seesSky(BlockPos pos) {
        return pos.getY()
                >= level.getChunkAt(pos)
                        .getSkyLightSources()
                        .getLowestSourceY(pos.getX() & 15, pos.getZ() & 15);
    }

    public long getOutput(int sun) {
        return Mth.ceil(
                sun
                        * 25
                        * WorldProviderCelestial.getSunPower(
                                level, worldPosition.getX(), worldPosition.getZ()));
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        power = input.getLongOr("power", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
    }
}
