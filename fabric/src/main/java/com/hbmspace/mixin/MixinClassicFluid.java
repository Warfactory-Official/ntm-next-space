// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.fluid.ClassicFluid;
import com.hbmspace.blocks.fluid.SchrabidicReactions;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClassicFluid.class)
public abstract class MixinClassicFluid {
    @Inject(method = "tick", at = @At("TAIL"))
    private void hbmspace$reactAfterDissipation(
            ServerLevel level,
            BlockPos pos,
            BlockState before,
            FluidState fluid,
            CallbackInfo ci,
            @Local(name = "after") BlockState after) {
        ClassicFluid self = (ClassicFluid) (Object) this;
        if (self.spec().block().get() != ModBlocks.SCHRABIDIC_BLOCK.get()
                || after.is(ModBlocks.SCHRABIDIC_BLOCK.get())) return;
        for (Direction direction : Direction.VALUES) {
            SchrabidicReactions.react(level, pos.relative(direction), level.getRandom());
        }
    }
}
