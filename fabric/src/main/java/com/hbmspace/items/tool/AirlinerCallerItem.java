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

package com.hbmspace.items.tool;

import com.hbm.entity.logic.BomberEntity;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AirlinerCallerItem extends Item {

    public AirlinerCallerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = player.pick(500D, 1F, false);
        if (!(hit instanceof BlockHitResult blockHit)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            double x = blockHit.getBlockPos().getX();
            double y = blockHit.getBlockPos().getY();
            double z = blockHit.getBlockPos().getZ();
            level.addFreshEntity(BomberEntity.variant(level, x, y + 16D, z, 75, 125, 90000, 8, 9));
            player.sendSystemMessage(
                    Component.translatable("desc.item.bombCaller.reroutedCivilianTraffic"));
            EntitySounds.play(
                    player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.TECH_BLEEP.get(),
                    SoundSource.PLAYERS,
                    1F,
                    1F);
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        adder.accept(Component.translatable("desc.item.bombCaller.aimClickToCall"));
        adder.accept(Component.translatable("desc.item.bombCaller.civilianAirliner"));
    }
}
