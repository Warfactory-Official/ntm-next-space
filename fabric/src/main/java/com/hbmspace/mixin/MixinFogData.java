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

import com.hbmspace.client.fog.TerrainFogPayload;
import net.minecraft.client.renderer.fog.FogData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FogData.class)
public abstract class MixinFogData implements TerrainFogPayload {
    @Unique private float hbmspace$density;
    @Unique private int hbmspace$flags;

    @Override
    public float hbmspace$density() {
        return hbmspace$density;
    }

    @Override
    public int hbmspace$flags() {
        return hbmspace$flags;
    }

    @Override
    public void hbmspace$payload(float density, int flags) {
        hbmspace$density = density;
        hbmspace$flags = flags;
    }
}
