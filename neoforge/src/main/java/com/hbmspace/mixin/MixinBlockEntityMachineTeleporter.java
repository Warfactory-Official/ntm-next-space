// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.Synced;
import com.hbm.blockentity.machine.BlockEntityMachineTeleporter;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbmspace.blockentity.machine.SpaceTeleporterTank;
import com.hbmspace.inventory.fluid.FluidsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityMachineTeleporter.class)
public abstract class MixinBlockEntityMachineTeleporter extends BlockEntity
        implements FluidTankEndpoint, SpaceTeleporterTank {

    @Shadow public @Nullable GlobalPos target;
    @Shadow public long power;

    @Unique private FluidTankNTM hbmspace$nmass;
    @Unique private FluidTankNTM[] hbmspace$receiving;

    protected MixinBlockEntityMachineTeleporter(
            BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hbmspace$createTank(BlockPos pos, BlockState state, CallbackInfo ci) {
        hbmspace$nmass = new FluidTankNTM(FluidsSpace.nmass(), 16_000);
        hbmspace$receiving = new FluidTankNTM[] {hbmspace$nmass};
    }

    @Override
    public FluidTankNTM hbmspace$tank() {
        return hbmspace$nmass;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return hbmspace$receiving;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return hbmspace$receiving;
    }

    @Override
    public long transferFluid(Fluid type, int pressure, long amount) {
        long refused = FluidTankEndpoint.super.transferFluid(type, pressure, amount);
        if (refused != amount) ((Synced) (Object) this).markChanged();
        return refused;
    }

    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void hbmspace$chargeCrossing(Entity entity, CallbackInfo ci) {
        if (target == null || power < BlockEntityMachineTeleporter.CONSUMPTION) return;
        if (entity.level().dimension().equals(target.dimension())) return;
        if (hbmspace$nmass.getFill() < 100) {
            ci.cancel();
            return;
        }
        hbmspace$nmass.setFill(hbmspace$nmass.getFill() - 100);
        setChanged();
        ((Synced) (Object) this).markChanged();
    }

    @Inject(method = "tickClient", at = @At("HEAD"), cancellable = true)
    private void hbmspace$chargedMotes(CallbackInfo ci) {
        if (hbmspace$nmass.getFill() < 100) ci.cancel();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void hbmspace$loadTank(ValueInput input, CallbackInfo ci) {
        input.child("tt").ifPresent(hbmspace$nmass::deserialize);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void hbmspace$saveTank(ValueOutput output, CallbackInfo ci) {
        hbmspace$nmass.serialize(output.child("tt"));
    }

    @Inject(method = "writeSyncUnit", at = @At("TAIL"))
    private void hbmspace$writeTank(int unit, ByteBuf output, CallbackInfo ci) {
        if (unit == 1) hbmspace$nmass.packetSerialize(output);
    }

    @Inject(method = "readSyncUnit", at = @At("TAIL"))
    private void hbmspace$readTank(int unit, ByteBuf input, CallbackInfo ci) {
        if (unit == 1) hbmspace$nmass.packetDeserialize(input);
    }
}
