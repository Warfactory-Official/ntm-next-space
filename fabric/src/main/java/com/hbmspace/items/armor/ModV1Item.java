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

import com.hbm.items.armor.ModCloudItem;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class ModV1Item extends ModCloudItem {

    private static final double SPEED = 0.5D;

    public ModV1Item(Properties properties) {
        super(properties);
    }

    @Override
    protected void addSpecificTooltip(ItemStack stack, Consumer<Component> adder) {
        adder.accept(
                Component.translatable("desc.hbmspace.armorMod.v1").withStyle(ChatFormatting.RED));
    }

    @Override
    public void addDescription(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(
                Component.translatable("desc.hbmspace.armorMod.v1.installed", stack.getHoverName())
                        .withStyle(ChatFormatting.RED));
    }

    @Override
    public void collectModifiers(
            EquipmentSlot slot,
            Identifier id,
            BiConsumer<Holder<Attribute>, AttributeModifier> adder) {
        adder.accept(
                Attributes.MOVEMENT_SPEED,
                new AttributeModifier(
                        SPEED_ID, SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
