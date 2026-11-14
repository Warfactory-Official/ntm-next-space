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

import com.hbm.blockentity.machine.thermal.PoweredCondenserBlockEntity;
import com.hbm.blockentity.machine.thermal.PoweredCondenserBlockEntity;
import com.hbm.client.render.flywheel.CondenserPoweredVisual;
import com.hbmspace.blockentity.machine.ThermalMachineDamage;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBoundedBlockEntityVisual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CondenserPoweredVisual.class)
public abstract class MixinCondenserPoweredVisual
        extends AbstractBoundedBlockEntityVisual<PoweredCondenserBlockEntity> {

    protected MixinCondenserPoweredVisual(
            VisualizationContext context, PoweredCondenserBlockEntity be, float partialTicks) {
        super(context, be, partialTicks);
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void hbmspace$damagedParts(CallbackInfo ci) {
        boolean damaged = ThermalMachineDamage.damaged(blockEntity);
        collectCrumblingInstances(instance -> instance.setVisible(!damaged));
        if (damaged) ci.cancel();
    }
}
