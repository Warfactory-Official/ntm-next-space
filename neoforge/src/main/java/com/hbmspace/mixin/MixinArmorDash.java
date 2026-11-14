/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

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
