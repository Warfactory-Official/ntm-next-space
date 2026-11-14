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

import com.hbm.blockentity.machine.DemonLampBlockEntity;
import com.hbm.util.ContaminationUtil;
import com.hbmspace.hazard.NeutronExposureSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DemonLampBlockEntity.class)
public abstract class MixinBlockEntityDemonLamp {
    @WrapOperation(
            method = "emitRadiation",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/util/ContaminationUtil;"
                                            + "contaminate(Lnet/minecraft/world/entity/LivingEntity;Lcom/hbm/util/ContaminationUtil$HazardType;"
                                            + "Lcom/hbm/util/ContaminationUtil$ContaminationType;D)Z"))
    private static boolean hbmspace$neutronLamp(
            LivingEntity entity,
            ContaminationUtil.HazardType hazard,
            ContaminationUtil.ContaminationType type,
            double dose,
            Operation<Boolean> original,
            @Local(argsOnly = true) BlockPos pos) {
        if (entity.getX() == pos.getX() + 0.5D
                && entity.getEyeY() == pos.getY() + 0.5D
                && entity.getZ() == pos.getZ() + 0.5D) dose = DemonLampBlockEntity.RAW_STRENGTH;
        boolean applied = original.call(entity, hazard, type, dose);
        NeutronExposureSpace.irradiate(entity, (float) dose);
        return applied;
    }
}
