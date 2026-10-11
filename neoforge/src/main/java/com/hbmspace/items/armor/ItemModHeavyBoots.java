// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemArmorMod;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.util.AstronomyUtil;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;

public class ItemModHeavyBoots extends ItemArmorMod {

    public ItemModHeavyBoots(Properties properties) {
        super(properties, ArmorModHandler.BOOTS_ONLY, false, false, false, true);
    }

    public static void tick(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        boolean worn = feet.getItem() instanceof ItemModHeavyBoots;
        boolean socketed =
                ArmorModHandler.isArmor(feet)
                        && ArmorModHandler.pryMods(feet)[ArmorModHandler.BOOTS_ONLY].getItem()
                                instanceof ItemModHeavyBoots;
        if (!worn && !socketed) return;
        if (!player.isShiftKeyDown() || player.onGround() || player.isInWater()) return;
        float gravity = CelestialBody.getGravity(player);
        if (gravity > 1.5F || gravity == 0) return;
        if (gravity < 0.2F) gravity = 0.2F;
        Vec3 motion = player.getDeltaMovement();
        double motionY = motion.y / 0.98F;
        motionY += gravity / 20F;
        motionY -= AstronomyUtil.STANDARD_GRAVITY / 20F;
        player.setDeltaMovement(motion.x, motionY * 0.98F, motion.z);
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
