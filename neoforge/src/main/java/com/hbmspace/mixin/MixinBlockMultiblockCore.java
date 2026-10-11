// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbmspace.handler.SpaceMultiblockPlacement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockMultiblockCore.class)
public abstract class MixinBlockMultiblockCore {
    @Inject(
            method = "setPlacedBy",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blocks/multiblock/BlockMultiblockCore;fillSpace(Lnet/minecraft/world/level/LevelAccessor;"
                                            + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V",
                            shift = At.Shift.AFTER))
    private void hbmspace$assembled(
            Level level,
            BlockPos clicked,
            BlockState state,
            LivingEntity placer,
            ItemStack stack,
            CallbackInfo ci) {
        SpaceMultiblockPlacement.assembled();
    }

    @ModifyVariable(method = "setPlacedBy", at = @At("STORE"), ordinal = 1)
    private BlockPos hbmspace$placementAnchor(
            BlockPos original,
            Level level,
            BlockPos clicked,
            BlockState state,
            LivingEntity placer,
            ItemStack stack) {
        Direction face = SpaceMultiblockPlacement.clickedFace();
        return face == null
                ? original
                : SpaceMultiblockPlacement.requirementAnchor(
                        (BlockMultiblockCore) (Object) this, clicked, face, placer.getYRot());
    }

    @ModifyExpressionValue(
            method = "setPlacedBy",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blocks/multiblock/BlockMultiblockCore;getDirModified(Lnet/minecraft/core/Direction;)"
                                            + "Lnet/minecraft/core/Direction;"))
    private Direction hbmspace$wallDirection(
            Direction original,
            Level level,
            BlockPos clicked,
            BlockState state,
            LivingEntity placer,
            ItemStack stack) {
        Direction face = SpaceMultiblockPlacement.clickedFace();
        return face == null
                ? original
                : SpaceMultiblockPlacement.direction(
                        (BlockMultiblockCore) (Object) this, face, placer.getYRot());
    }
}
