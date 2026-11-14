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
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ModHeavyBootsItem extends ArmorModItem {

    public ModHeavyBootsItem(Properties properties) {
        super(properties, ArmorModHandler.BOOTS_ONLY, false, false, false, true);
    }

    public static boolean pulls(Player player) {
        if (!player.isShiftKeyDown() || player.onGround()) return false;
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        return feet.getItem() instanceof ModHeavyBootsItem
                || ArmorModHandler.modItem(feet, ArmorModHandler.BOOTS_ONLY)
                        instanceof ModHeavyBootsItem;
    }

    @Override
    protected void addSpecificTooltip(ItemStack stack, Consumer<Component> adder) {
        adder.accept(
                Component.translatable("desc.hbmspace.armorMod.heavy_boots")
                        .withStyle(ChatFormatting.BLUE));
        adder.accept(
                Component.translatable("desc.hbmspace.armorMod.heavy_boots.crouch")
                        .withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, display, adder, flag);
        adder.accept(
                Component.translatable("desc.hbmspace.armorMod.heavy_boots.alone")
                        .withStyle(ChatFormatting.GOLD));
        adder.accept(
                Component.translatable("desc.hbmspace.armorMod.heavy_boots.warning1")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        adder.accept(
                Component.translatable("desc.hbmspace.armorMod.heavy_boots.warning2")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public void addDescription(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(
                Component.translatable(
                                "desc.hbmspace.armorMod.heavy_boots.installed",
                                stack.getHoverName(),
                                Component.translatable("armor.fastFall"))
                        .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
