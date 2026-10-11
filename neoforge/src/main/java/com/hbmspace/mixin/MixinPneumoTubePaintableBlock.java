// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.network.pneumatic.PneumoTubePaintableBlock;
import com.hbmspace.handler.PneumoIdentifierSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PneumoTubePaintableBlock.class)
public abstract class MixinPneumoTubePaintableBlock {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void hbmspace$selectGas(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (PneumoIdentifierSelection.handle(stack, level, pos, player)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
