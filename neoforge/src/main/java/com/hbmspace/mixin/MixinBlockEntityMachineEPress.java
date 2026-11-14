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

import com.hbm.blockentity.PlushieBlockEntity;
import com.hbm.blockentity.processing.ElectricPressBlockEntity;
import com.hbmspace.blocks.machine.PressCrushSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElectricPressBlockEntity.class)
public abstract class MixinBlockEntityMachineEPress {
    @Inject(method = "tickClient", at = @At("TAIL"))
    private void hbmspace$plushieMisery(CallbackInfo ci) {
        ElectricPressBlockEntity press = (ElectricPressBlockEntity) (Object) this;
        if (press.getLevel().getBlockEntity(press.getBlockPos().above())
                instanceof PlushieBlockEntity plushie) {
            plushie.miseryFactor = Mth.clamp(((float) press.renderPress - 100F) / 100F, 0F, 1F);
        }
    }

    @Redirect(
            method = "syncPress",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/sound/WorldSounds;"
                                            + "machine(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DDD"
                                            + "Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void hbmspace$pressCrush(
            Level level,
            @Nullable Entity except,
            double x,
            double y,
            double z,
            SoundEvent sound,
            SoundSource source,
            float volume,
            float pitch) {
        PressCrushSpace.play(
                level, except, BlockPos.containing(x, y, z), sound, source, volume, pitch);
    }
}
