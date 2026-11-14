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

import com.hbm.blockentity.machine.reactor.ResearchReactorBlockEntity;
import com.hbmspace.hazard.NeutronExposureSpace;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResearchReactorBlockEntity.class)
public abstract class MixinResearchColumn {
    @Inject(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/handler/radiation/RadiationSystemNT;incrementRad(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;D)V",
                            shift = At.Shift.AFTER),
            order = 10000)
    private void hbmspace$researchNeutrons(CallbackInfo _) {
        var reactor = (ResearchReactorBlockEntity) (Object) this;
        NeutronExposureSpace.radiate(
                (ServerLevel) reactor.getLevel(),
                reactor.getBlockPos(),
                (float) reactor.heat / ResearchReactorBlockEntity.HEAT_LIMIT * 50F);
    }
}
