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
import com.hbm.items.armor.ArmorModItem;
import com.hbm.items.armor.ArmorSuitEffects;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ModFlippersItem extends ArmorModItem {

    private static final double PUSH = 0.08D;

    public ModFlippersItem(Properties properties) {
        super(properties, ArmorModHandler.BOOTS_ONLY, false, false, false, true);
    }

    @Override
    protected void addSpecificTooltip(ItemStack stack, Consumer<Component> adder) {
        adder.accept(
                Component.translatable("desc.hbmspace.armorMod.flippers")
                        .withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void addDescription(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(
                Component.translatable(
                                "desc.hbmspace.armorMod.flippers.installed", stack.getHoverName())
                        .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public boolean ticksClient() {
        return true;
    }

    @Override
    public void modUpdateClient(LivingEntity entity, ItemStack armor) {
        if (!(entity instanceof Player player) || !player.isInWater()) return;
        if (ArmorSuitEffects.hasEnvsuitSwim(player)) return;
        Vec3 push = player.getLookAngle().scale(PUSH * player.zza);
        player.setDeltaMovement(player.getDeltaMovement().add(push));
    }
}
