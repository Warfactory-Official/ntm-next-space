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
import com.hbmspace.handler.SpaceMultiblockEscape;
import com.hbmspace.handler.SpaceMultiblockPlacement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {
    @WrapOperation(
            method = "place",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;"
                                            + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;"
                                            + "Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void hbmspace$clickedFace(
            Block block,
            Level level,
            BlockPos pos,
            BlockState state,
            LivingEntity placer,
            ItemStack stack,
            Operation<Void> original,
            @Local(ordinal = 1) BlockPlaceContext context) {
        if (!(block instanceof MultiblockCoreBlock machine)) {
            original.call(block, level, pos, state, placer, stack);
            return;
        }
        int previous = SpaceMultiblockPlacement.begin(context.getClickedFace());
        try {
            original.call(block, level, pos, state, placer, stack);
            if (SpaceMultiblockPlacement.didAssemble()) {
                BlockPos core =
                        SpaceMultiblockPlacement.core(
                                machine, pos, context.getClickedFace(), placer.getYRot());
                SpaceMultiblockEscape.placed(level, core, machine, placer);
            }
        } finally {
            SpaceMultiblockPlacement.end(previous);
        }
    }
}
