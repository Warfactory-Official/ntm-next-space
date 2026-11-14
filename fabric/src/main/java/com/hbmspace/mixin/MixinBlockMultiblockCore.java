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

import com.hbm.blocks.multiblock.MultiblockCoreBlock;
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

@Mixin(MultiblockCoreBlock.class)
public abstract class MixinBlockMultiblockCore {
    @Inject(
            method = "setPlacedBy",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blocks/multiblock/MultiblockCoreBlock;fillSpace(Lnet/minecraft/world/level/LevelAccessor;"
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
                        (MultiblockCoreBlock) (Object) this, clicked, face, placer.getYRot());
    }

    @ModifyExpressionValue(
            method = "setPlacedBy",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blocks/multiblock/MultiblockCoreBlock;getDirModified(Lnet/minecraft/core/Direction;)"
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
                        (MultiblockCoreBlock) (Object) this, face, placer.getYRot());
    }
}
