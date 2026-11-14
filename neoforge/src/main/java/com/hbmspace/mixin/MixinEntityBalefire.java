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

import com.hbm.entity.logic.BalefireEntity;
import com.hbm.explosion.spiral.ExplosionColumnProcessor;
import com.hbm.explosion.spiral.SpiralExplosion;
import com.hbmspace.entity.logic.AntimatterBlastEntity;
import com.hbmspace.explosion.ExplosionAntimatter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BalefireEntity.class)
public abstract class MixinEntityBalefire {
    @WrapOperation(
            method = {"tick", "readAdditionalSaveData"},
            at = @At(value = "NEW", target = "com/hbm/explosion/spiral/SpiralExplosion"))
    private SpiralExplosion hbmspace$antimatterCrater(
            Level level,
            int x,
            int y,
            int z,
            int range,
            ExplosionColumnProcessor action,
            Operation<SpiralExplosion> original) {
        return original.call(
                level,
                x,
                y,
                z,
                range,
                (Object) this instanceof AntimatterBlastEntity
                        ? ExplosionAntimatter.INSTANCE
                        : action);
    }

    @Inject(
            method = "tick",
            at = @At(value = "JUMP", opcode = Opcodes.IFNE, ordinal = 0),
            slice =
                    @Slice(
                            from =
                                    @At(
                                            value = "INVOKE",
                                            target =
                                                    "Lcom/hbm/explosion/spiral/SpiralExplosion;advanceColumn()Z")))
    private void hbmspace$stepEffects(CallbackInfo ci, @Local(ordinal = 0) boolean finished) {
        if ((Object) this instanceof AntimatterBlastEntity blast) blast.flash();
        AntimatterBlastEntity.stepEffects((BalefireEntity) (Object) this, finished);
    }
}
