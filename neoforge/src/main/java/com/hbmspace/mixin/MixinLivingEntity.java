// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.dim.SpaceGravity;
import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.potion.HbmPotionSpace;
import java.util.Collection;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(method = "onEffectsRemoved", at = @At("TAIL"))
    private void hbmspace$clearNitanBuffer(Collection<MobEffectInstance> effects, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || player.level().isClientSide()) return;
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect().value() == HbmPotionSpace.NITAN.get()) {
                SpacePlayerProps.clearNitanHealth(player);
                return;
            }
        }
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true)
    private double hbmspace$fallDistance(double fallDistance) {
        return SpaceGravity.scaleFall((LivingEntity) (Object) this, fallDistance);
    }
}
