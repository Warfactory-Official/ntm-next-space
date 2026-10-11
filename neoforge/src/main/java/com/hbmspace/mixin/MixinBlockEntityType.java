// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.ModBlockEntities;
import com.hbm.blocks.machine.storage.MachineCapacitor;
import com.hbmspace.blockentity.machine.BlockEntityChungusSpace;
import com.hbmspace.blockentity.machine.BlockEntityCondenserPoweredSpace;
import com.hbmspace.blockentity.machine.BlockEntityMachineLPW2Space;
import com.hbmspace.blocks.CapacitorBlocksSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class MixinBlockEntityType {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void hbmspace$complexCapacitor(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof MachineCapacitor
                && (Object) this == ModBlockEntities.CAPACITOR.get()
                && state.getBlock() == CapacitorBlocksSpace.CAPACITOR_COMPLEX.get()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void hbmspace$lpw2Engine(
            BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        if ((Object) this == ModBlockEntities.LPW2.get()) {
            cir.setReturnValue(new BlockEntityMachineLPW2Space(pos, state));
        }
    }

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void hbmspace$thermalDamage(
            BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        if ((Object) this == ModBlockEntities.CHUNGUS.get()) {
            cir.setReturnValue(new BlockEntityChungusSpace(pos, state));
        } else if ((Object) this == ModBlockEntities.CONDENSER_POWERED.get()) {
            cir.setReturnValue(new BlockEntityCondenserPoweredSpace(pos, state));
        }
    }
}
