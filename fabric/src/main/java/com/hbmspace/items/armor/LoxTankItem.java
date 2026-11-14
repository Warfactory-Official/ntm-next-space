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

package com.hbmspace.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.machine.FluidContainerItem;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import com.hbm.util.TooltipStyle;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class LoxTankItem extends Item {

    private static final int CHARGE = 1000;

    public LoxTankItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        for (Component line :
                TooltipStyle.lines(Component.translatable(getDescriptionId() + ".desc"))) {
            adder.accept(line);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!ArmorModHandler.hasMods(helmet)) return InteractionResult.PASS;
        ItemStack tank = ArmorModHandler.pryMod(helmet, ArmorModHandler.PLATE_ONLY);
        if (!(tank.getItem() instanceof FluidContainerItem container)
                || !container.machineFillable()) {
            return InteractionResult.PASS;
        }
        if (container.fill(tank, NTMFluids.OXYGEN, CHARGE, 0) <= 0) return InteractionResult.PASS;
        ArmorModHandler.applyMod(helmet, tank);

        player.getItemInHand(hand).consume(1, player);
        EntitySounds.play(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.JETPACK_TANK.get(),
                SoundSource.PLAYERS,
                1F,
                1F);
        return InteractionResult.SUCCESS;
    }
}
