// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockImpactDirt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockImpactDirt.class)
public abstract class MixinBlockImpactDirt {
    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void hbmspace$staysBesideGrass(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "randomTick",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
                            shift = At.Shift.AFTER))
    private void hbmspace$sinks(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.DIRT) || below.is(Blocks.COARSE_DIRT) || below.is(Blocks.PODZOL)) {
            level.setBlockAndUpdate(pos.below(), ModBlocks.IMPACT_DIRT.get().defaultBlockState());
        }
    }
}
