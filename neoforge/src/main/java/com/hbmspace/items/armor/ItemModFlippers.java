// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ArmorSuitEffects;
import com.hbm.items.armor.ItemArmorMod;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemModFlippers extends ItemArmorMod {

    private static final double PUSH = 0.08D;

    public ItemModFlippers(Properties properties) {
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
