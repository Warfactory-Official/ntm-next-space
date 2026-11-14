/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.packet.PacketWire;
import com.hbm.packet.layout.BlockDustBurstLayout;
import com.hbm.particle.EffectKinds.GibletType;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.registration.Reg;
import com.hbm.sound.EntitySounds;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public final class ArmorModsSpace {
    public static final Reg.Handle<ModV1Item> V1 =
            HbmSpace.REG.item("v1", ModV1Item::new, Item.Properties::new);
    public static final Reg.Handle<ModFlippersItem> FLIPPERS =
            HbmSpace.REG.item("flippers", ModFlippersItem::new, Item.Properties::new);
    public static final Reg.Handle<ModHeavyBootsItem> HEAVY_BOOTS =
            HbmSpace.REG.item(
                    "heavy_boots",
                    ModHeavyBootsItem::new,
                    () ->
                            new Item.Properties()
                                    .component(
                                            DataComponents.EQUIPPABLE,
                                            Equippable.builder(EquipmentSlot.FEET)
                                                    .setAsset(
                                                            ResourceKey.create(
                                                                    EquipmentAssets.ROOT_ID,
                                                                    Identifier.fromNamespaceAndPath(
                                                                            HbmSpace.MOD_ID,
                                                                            "heavy_boots")))
                                                    .build()));

    private static final float KILL_HEAL = 0.25F;
    private static final float HIT_HEAL = 0.5F;

    static {
        after("medal_liquidator", V1);
        after("armor_battery_mk3", FLIPPERS, HEAVY_BOOTS);
    }

    private ArmorModsSpace() {}

    @SafeVarargs
    private static void after(String anchor, Reg.Handle<? extends Item>... entries) {
        for (Reg.Handle<? extends Item> entry : entries) {
            CreativeContents.insertAfter(
                    Tab.CONSUMABLES, RegistryUtil.id(anchor), () -> new ItemStack(entry.get()));
        }
    }

    private static @Nullable Player v1Attacker(DamageSource source) {
        if (!(source.getEntity() instanceof Player player)) return null;
        ItemStack plate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ArmorModHandler.isArmor(plate)) return null;
        return ArmorModHandler.pryMods(plate)[ArmorModHandler.PLATE_ONLY].is(V1.get())
                ? player
                : null;
    }

    public static void onHurt(LivingEntity victim, DamageSource source, float amount) {
        Player attacker = v1Attacker(source);
        if (attacker == null || !(victim.level() instanceof ServerLevel level)) return;
        double y = victim.getY() + victim.getBbHeight() * 0.5D;
        PacketWire.sendToAllAround(
                BlockDustBurstLayout.encode(
                        Block.BLOCK_STATE_REGISTRY.getId(Blocks.REDSTONE_BLOCK.defaultBlockState()),
                        victim.getX(),
                        y,
                        victim.getZ(),
                        (int) Math.min(victim.getMaxHealth() / 2F, 250),
                        0.1D),
                new TargetPoint(level, victim.getX(), victim.getY(), victim.getZ(), 50));
        if (attacker.distanceToSqr(victim) < 25) attacker.heal(amount * HIT_HEAL);
    }

    public static void onDeath(LivingEntity victim, DamageSource source) {
        Player attacker = v1Attacker(source);
        if (attacker == null || !(victim.level() instanceof ServerLevel level)) return;
        ParticleCreators.giblets(level, victim, GibletType.MEAT, 0);
        EntitySounds.play(
                victim,
                victim.getX(),
                victim.getY(),
                victim.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,
                SoundSource.HOSTILE,
                2.0F,
                0.95F + level.getRandom().nextFloat() * 0.2F);
        if (attacker.distanceToSqr(victim) < 100) attacker.heal(victim.getMaxHealth() * KILL_HEAL);
    }
}
