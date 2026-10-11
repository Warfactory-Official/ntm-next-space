// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.items.armor.ArmorDamageHandler;
import com.hbmspace.lib.ModDamageTypesSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorDamageHandler.class)
public abstract class MixinArmorDamageHandler {
    @WrapOperation(
            method = "filterAttack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/items/armor/ArmorDamageHandler;plink(Lnet/minecraft/world/entity/player/Player;)V",
                            ordinal = 1))
    private static void hbmspace$silentOxyprime(
            Player player, Operation<Void> original, @Local(argsOnly = true) DamageSource source) {
        if (!source.is(ModDamageTypesSpace.OXYPRIME)) original.call(player);
    }
}
