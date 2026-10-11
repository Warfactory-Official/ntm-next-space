// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.items.armor.ArmorDash;
import com.hbmspace.sound.ModSoundsSpace;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ArmorDash.class)
public abstract class MixinArmorDash {

    private static final String PLAY =
            "Lcom/hbm/sound/EntitySounds;play("
                    + "Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;"
                    + "Lnet/minecraft/sounds/SoundSource;FF)V";

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = PLAY), index = 4)
    private static SoundEvent hbmspace$dashRecharge(SoundEvent original) {
        return ModSoundsSpace.PLAYER_DASH_RECHARGE.get();
    }

    @ModifyArg(method = "dash", at = @At(value = "INVOKE", target = PLAY), index = 4)
    private static SoundEvent hbmspace$dash(SoundEvent original) {
        return ModSoundsSpace.PLAYER_DASH.get();
    }
}
