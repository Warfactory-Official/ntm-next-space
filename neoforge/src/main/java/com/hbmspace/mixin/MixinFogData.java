// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

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
