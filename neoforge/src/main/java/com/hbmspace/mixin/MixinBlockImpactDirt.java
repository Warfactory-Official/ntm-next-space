/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.mixin;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.ImpactDirtBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImpactDirtBlock.class)
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
