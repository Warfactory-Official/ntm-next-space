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

import com.hbm.entity.mob.DuckEntity;
import com.hbm.entity.mob.QuackosEntity;
import com.hbmspace.data.SpaceData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuackosEntity.class)
public abstract class MixinEntityQuackos extends DuckEntity {
    @Unique private int hbmspace$killCounter;

    private MixinEntityQuackos(EntityType<? extends DuckEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void hbmspace$cullable(RemovalReason reason, CallbackInfo ci) {
        if (level().isClientSide()
                || reason != RemovalReason.KILLED && reason != RemovalReason.DISCARDED
                || !SpaceData.SERVER_SAFETY.get()) return;
        hbmspace$killCounter += 2;
        if (hbmspace$killCounter > 20) super.remove(reason);
        ci.cancel();
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void hbmspace$cool(CallbackInfo ci) {
        if (hbmspace$killCounter > 0) hbmspace$killCounter--;
    }
}
