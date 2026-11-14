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
