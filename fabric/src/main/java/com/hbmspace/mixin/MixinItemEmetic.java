// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.items.food.ItemEmetic;
import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.potion.HbmPotionSpace;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEmetic.class)
public abstract class MixinItemEmetic {
    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void hbmspace$purgeNitan(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir) {
        if (level instanceof ServerLevel
                && entity instanceof Player player
                && SpacePlayerProps.getNitanCount(player) > 0) {
            player.removeEffect(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HbmPotionSpace.NITAN.get()));
            SpacePlayerProps.setNitanCount(player, 0);
            SpacePlayerProps.clearNitanHealth(player);
        }
    }
}
