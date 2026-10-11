// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.CoolingTowerParticleOptions;
import com.hbmspace.blockentity.IDysonConverter;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.GasExchange;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityDysonConverterAnatmogenesis extends BlockEntityMachineBase
        implements IDysonConverter, SyncUnitSchema {
    private static final long HE_TO_MB = 28_800_000;

    @SyncField(units = 1L)
    public Fluid fluid = NTMFluids.OXYGEN;

    @SyncField(units = 1L)
    public boolean isEmitting = true;

    @SyncField(units = 1L)
    public long gasProduced;

    @SyncField(units = 1L)
    public boolean isConverting;

    public BlockEntityDysonConverterAnatmogenesis(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_CONVERTER_ANATMOGENESIS.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        isConverting = gasProduced > 0;

        networkPackNT(250);
        gasProduced = 0;
    }

    @Override
    public void tickClient() {
        if (!isConverting) return;
        RandomSource random = level.getRandom();
        CoolingTowerParticleOptions plume =
                new CoolingTowerParticleOptions.Builder()
                        .setLift(0.5F)
                        .setBaseScale(0.8F)
                        .setMaxScale(4F)
                        .setLife(100 + random.nextInt(50))
                        .setColor(BlockEntityAirPump.color(fluid))
                        .build();
        level.addParticle(
                plume,
                true,
                false,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 3.25,
                worldPosition.getZ() + 0.5,
                random.nextDouble() - 0.5,
                random.nextDouble() * 2,
                random.nextDouble() - 0.5);
    }

    @Override
    public boolean provideEnergy(BlockPos pos, long energy) {
        if (!pos.equals(
                worldPosition
                        .relative(BlockMultiblockCore.coreFacing(getBlockState()), 5)
                        .above())) {
            return false;
        }

        long volume = energy / HE_TO_MB;
        gasProduced += volume;

        if (isEmitting) GasExchange.release((ServerLevel) level, fluid, volume);
        else GasExchange.capture((ServerLevel) level, fluid, volume);

        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getString("fluid")
                .map(Identifier::tryParse)
                .flatMap(BuiltInRegistries.FLUID::getOptional)
                .ifPresent(read -> fluid = read);
        isEmitting = input.getBooleanOr("emit", true);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        output.putBoolean("emit", isEmitting);
    }

    @Override
    public long syncUnitMask() {
        return 1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(isConverting);
        output.writeLong(gasProduced);
        output.writeInt(BuiltInRegistries.FLUID.getId(fluid));
        output.writeBoolean(isEmitting);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        isConverting = input.readBoolean();
        gasProduced = input.readLong();
        fluid = BuiltInRegistries.FLUID.byId(input.readInt());
        isEmitting = input.readBoolean();
    }
}
