// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.armor;

import com.hbm.items.armor.ItemModCloud;
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

public class ItemModV1 extends ItemModCloud {

    private static final double SPEED = 0.5D;

    public ItemModV1(Properties properties) {
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
