// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Unique
    private static final Identifier hbmspace$RARE =
            Identifier.fromNamespaceAndPath("hbm", "ore_rare");

    @Unique
    private static final Identifier hbmspace$AUSTRALIUM =
            Identifier.fromNamespaceAndPath("hbm", "ore_australium");

    @ModifyVariable(method = "getRarity", at = @At("STORE"), ordinal = 0, require = 1)
    private Rarity hbmspace$oreRarity(Rarity base) {
        if (base != Rarity.COMMON) return base;
        Identifier id = BuiltInRegistries.ITEM.getKey(((ItemStack) (Object) this).getItem());
        return hbmspace$RARE.equals(id) || hbmspace$AUSTRALIUM.equals(id) ? Rarity.UNCOMMON : base;
    }
}
