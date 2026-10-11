// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.items.armor.ModArmorItem.Suit;
import com.hbm.items.armor.ModArmorItem;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toclient.PlayerInformPayload;
import com.hbm.sound.EntitySounds;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ArmorUtilSpace {
    private static final Set<Suit> SEALED =
            EnumSet.of(
                    Suit.T51,
                    Suit.DESH,
                    Suit.AJR,
                    Suit.AJRO,
                    Suit.RPA,
                    Suit.NCRPA,
                    Suit.ENVSUIT,
                    Suit.HEV,
                    Suit.DIGAMMA,
                    Suit.DNS,
                    Suit.TAURUN,
                    Suit.TRENCHMASTER);

    private ArmorUtilSpace() {}

    public static boolean canSeal(ItemStack stack) {
        return stack.getItem() instanceof ModArmorItem armor && SEALED.contains(armor.suit());
    }

    public static boolean checkForOxy(LivingEntity entity, @Nullable CBT_Atmosphere atmosphere) {
        if (!(entity instanceof Player player))
            return ChunkAtmosphereManager.proxy.canBreathe(atmosphere);

        if (checkSpecialBreathing(player)) {
            SpaceLivingProps.setTankInUse(player, false);
            return true;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack tank = getOxygenTank(player);
        if (tank.isEmpty()) {
            SpaceLivingProps.setTankInUse(player, false);
            return ChunkAtmosphereManager.proxy.canBreathe(atmosphere);
        }

        boolean isInWater = entity.getAirSupply() < entity.getMaxAirSupply();
        ItemModOxy oxy = (ItemModOxy) tank.getItem();
        int fuel = oxy.getFuel(tank);
        boolean canBreatheTank = oxy.attemptBreathing(entity, tank, atmosphere, isInWater);
        if (oxy.getFuel(tank) != fuel) ArmorModHandler.applyMod(helmet, tank);

        if (isInWater && canBreatheTank) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }

        return canBreatheTank;
    }

    public static ItemStack getOxygenTank(Player player) {
        for (EquipmentSlot slot : ArmorUtil.ARMOR_SLOTS) {
            if (!canSeal(player.getItemBySlot(slot))) return ItemStack.EMPTY;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (ArmorModHandler.hasMods(helmet)) {
            ItemStack tankMod = ArmorModHandler.pryMod(helmet, ArmorModHandler.PLATE_ONLY);
            if (!(tankMod.getItem() instanceof ItemModOxy)) return ItemStack.EMPTY;

            return tankMod;
        }

        return ItemStack.EMPTY;
    }

    private static boolean checkSpecialBreathing(Player player) {
        if (player.isCreative()) return true;

        for (EquipmentSlot slot : ArmorUtil.ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof ModArmorItem armor) || armor.suit() != Suit.DNS)
                return false;
        }

        return true;
    }

    public static boolean checkForCorrosion(
            LivingEntity entity, @Nullable CBT_Atmosphere atmosphere) {
        if (!ChunkAtmosphereManager.proxy.willCorrode(atmosphere)) return false;

        if (!(entity instanceof Player player)) return true;

        ItemStack insert = getCorrosionProtection(player);
        if (insert.isEmpty()) {
            boolean isSealed = true;
            RandomSource rand = entity.getRandom();

            for (EquipmentSlot slot : ArmorUtil.ARMOR_SLOTS) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!canSeal(stack)) isSealed = false;

                if (stack.isEmpty()) continue;

                if (rand.nextInt(2) == 0) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                }

                if (stack.getDamageValue() >= stack.getMaxDamage()
                        || !(stack.getItem() instanceof ModArmorItem armor)
                        || !armor.isEnabled(stack)) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    EntitySounds.play(
                            entity,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            SoundEvents.FIRE_EXTINGUISH,
                            entity.getSoundSource(),
                            0.2F,
                            1F);
                }
            }

            if (isSealed && entity instanceof ServerPlayer serverPlayer) {
                PacketWire.sendTo(
                        new PlayerInformPayload(
                                Component.translatable("info.corrosion")
                                        .withStyle(ChatFormatting.RED),
                                PlayerInformPayload.ID_GAS_HAZARD,
                                3_000),
                        serverPlayer);
            }

            return !isSealed;
        }

        return false;
    }

    public static ItemStack getCorrosionProtection(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (ArmorModHandler.hasMods(chest)) {
            ItemStack insertMod = ArmorModHandler.pryMod(chest, ArmorModHandler.KEVLAR);
            if (!isCorrosionProtection(insertMod)) return ItemStack.EMPTY;

            return insertMod;
        }

        return ItemStack.EMPTY;
    }

    public static boolean isCorrosionProtection(ItemStack stack) {
        return stack.is(ModArmorItemsSpace.INSERT_CMB.get())
                || stack.is(ModItems.INSERT_YHARONITE.get());
    }
}
