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

import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.duna.DunaDustStorm;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public abstract class MixinLevel {
    @ModifyReturnValue(method = "isBrightOutside", at = @At("RETURN"))
    private boolean hbmspace$stormNight(boolean bright) {
        Level level = (Level) (Object) this;
        if (!bright || level.dimension() != SpaceDimensions.DUNA) return bright;
        float intensity =
                level instanceof ServerLevel server
                        ? DunaDustStorm.get(server).intensity
                        : DunaDustStorm.clientIntensity;
        return !(intensity >= 0.2F);
    }
}
