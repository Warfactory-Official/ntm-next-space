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

package com.hbmspace.blocks.machine;

import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.inventory.BlockMenus;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.ReactorSensorItem;
import com.hbmspace.blockentity.machine.DishControlBlockEntity;
import com.hbmspace.blockentity.machine.StardarBlockEntity;
import com.hbmspace.blocks.IndustryBlocksSpace;
import com.hbmspace.blocks.RocketryBlocksSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class DishControlBlock extends MultiblockCoreBlock implements TickingEntityBlock {
    public DishControlBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DishControlBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(core) instanceof DishControlBlockEntity console))
            return InteractionResult.PASS;

        if (!console.isLinked) {
            complain(player, "chat.hbmspace.dishControl.notLinked");
            return InteractionResult.PASS;
        }
        StardarBlockEntity stardar = console.linkedStardar();
        if (stardar == null) {
            complain(player, "chat.hbmspace.dishControl.missingTarget");
            return InteractionResult.PASS;
        }
        BlockMenus.open(player, stardar);
        return InteractionResult.SUCCESS;
    }

    private void complain(Player player, String key) {
        player.sendSystemMessage(
                Component.literal("[")
                        .append(Component.translatable(getDescriptionId()))
                        .append("] ")
                        .withStyle(ChatFormatting.DARK_AQUA)
                        .append(Component.translatable(key).withStyle(ChatFormatting.RED)));
    }

    public static InteractionResult useSensor(UseOnContext context, @Nullable BlockPos core) {
        if (core == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockState state = level.getBlockState(core);
        if (state.is(RocketryBlocksSpace.MACHINE_STARDAR.get())) {
            return ReactorSensorItem.recordPosition(
                    context, Component.translatable("chat.hbmspace.reactorSensor.positionSet"));
        }
        if (!state.is(IndustryBlocksSpace.DISH_CONTROL.get())) return InteractionResult.PASS;

        ItemStack sensor = context.getItemInHand();
        if (sensor.get(ModDataComponents.REACTOR_POS.get()) == null) return InteractionResult.PASS;
        if (!(level.getBlockEntity(core) instanceof DishControlBlockEntity console))
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            Player player = context.getPlayer();
            if (player != null) {
                ReactorSensorItem.announce(
                        player,
                        Component.translatable("chat.hbmspace.reactorSensor.stardarLinked"));
            }
            console.tryLink(sensor);
        }
        ReactorSensorItem.confirm(context);
        return InteractionResult.SUCCESS;
    }
}
