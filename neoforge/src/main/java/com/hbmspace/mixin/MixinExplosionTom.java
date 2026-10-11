// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.explosion.ExplosionTom;
import com.hbmspace.explosion.TomLavaReach;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ExplosionTom.class)
public abstract class MixinExplosionTom implements TomLavaReach {
    @Shadow @Final private int radius2;

    @Unique private float hbmspace$lavaReach = 500F;

    @Shadow
    private int floor(int x, int z) {
        throw new AssertionError();
    }

    @Override
    public void hbmspace$setLavaReach(float reach) {
        hbmspace$lavaReach = reach;
    }

    @Override
    public boolean hbmspace$floods(int x, int z) {
        return (float) Math.sqrt((double) x * x + (double) z * z) < hbmspace$lavaReach;
    }

    @Override
    public boolean hbmspace$inside(int x, int z) {
        return radius2 - (x * x + z * z) > 0;
    }

    @Override
    public int hbmspace$floor(int x, int z) {
        return floor(x, z);
    }
}
