// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class MobDrops {

    private MobDrops() {}

    public static int looting(ServerLevel level, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) return 0;
        Holder<Enchantment> looting =
                level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.LOOTING);
        return EnchantmentHelper.getEnchantmentLevel(looting, attacker);
    }

    public static void dropFew(ServerLevel level, Mob mob, DamageSource source, Item item) {
        int looting = looting(level, source);
        int count = mob.getRandom().nextInt(3);
        if (looting > 0) count += mob.getRandom().nextInt(looting + 1);
        for (int i = 0; i < count; i++) mob.spawnAtLocation(level, new ItemStack(item));
    }
}
