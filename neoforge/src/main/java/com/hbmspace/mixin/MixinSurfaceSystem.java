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

import com.hbmspace.world.gen.surface.SolidSeaSurface;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurfaceSystem.class)
public abstract class MixinSurfaceSystem implements SolidSeaSurface {

    @Unique private @Nullable BlockState hbmspace$sea;

    @Override
    public void hbmspace$readSeaAsFluid(BlockState sea) {
        hbmspace$sea = sea;
    }

    @ModifyExpressionValue(
            method = "buildSurface",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/"
                                            + "chunk/BlockColumn;getBlock(I)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState hbmspace$seaAsFluid(BlockState state) {
        return state == hbmspace$sea ? Blocks.WATER.defaultBlockState() : state;
    }
}
