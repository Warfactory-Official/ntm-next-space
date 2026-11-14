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

import com.hbm.entity.effect.NukeTorexEntity;
import com.hbm.entity.logic.BalefireEntity;
import com.hbm.explosion.ExplosionNukeCustom;
import com.hbmspace.entity.BombEntitiesSpace;
import com.hbmspace.entity.effect.TorexAntimatter;
import com.hbmspace.entity.logic.AntimatterBlastEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExplosionNukeCustom.class)
public abstract class MixinExplosionNukeCustom {

    @WrapOperation(
            method = "explodeCustom",
            at = @At(value = "NEW", target = "com/hbm/entity/logic/BalefireEntity"))
    private static BalefireEntity hbmspace$antimatterBlast(
            EntityType<? extends BalefireEntity> type,
            Level level,
            Operation<BalefireEntity> original) {
        return new AntimatterBlastEntity(BombEntitiesSpace.ANTIMATTER_BLAST.get(), level);
    }

    @WrapOperation(
            method = "explodeCustom",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/entity/effect/NukeTorexEntity;statFacBale(Lnet/minecraft/world/level/Level;DDDF)Lcom/hbm/entity/effect/NukeTorexEntity;"))
    private static NukeTorexEntity hbmspace$antimatterTorex(
            Level level,
            double x,
            double y,
            double z,
            float scale,
            Operation<NukeTorexEntity> original) {
        return TorexAntimatter.startFacAnti(level, x, y, z, scale);
    }
}
