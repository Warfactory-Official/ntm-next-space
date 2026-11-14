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

import com.hbm.blockentity.network.pneumatic.PneumaticTubeBlockEntity;
import com.hbm.blocks.network.pneumatic.PneumoTubeBlock;
import com.hbm.uninos.graph.PhysicalGraph;
import com.hbmspace.handler.PneumoIdentifierSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PneumoTubeBlock.class)
public abstract class MixinPneumoTubeBlock extends Block {
    protected MixinPneumoTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        return PneumoIdentifierSelection.handle(stack, level, pos, player)
                ? InteractionResult.SUCCESS
                : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Inject(method = "canConnectToAir", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$typedNozzle(
            LevelReader level, BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir) {
        if (!(level.getBlockEntity(pos) instanceof PneumaticTubeBlockEntity tube)
                || !tube.hasIntake()
                || tube.outletFace == dir
                || tube.intakeFace == dir
                || level.getBlockState(pos.relative(dir)).getBlock() instanceof PneumoTubeBlock) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(
                PhysicalGraph.fluidContact(
                        level, pos.relative(dir), dir.getOpposite(), tube.gasTank.getFluid()));
    }
}
