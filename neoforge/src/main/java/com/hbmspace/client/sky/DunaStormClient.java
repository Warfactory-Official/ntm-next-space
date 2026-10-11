// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.duna.DunaDustStorm;
import com.hbmspace.dim.duna.WorldProviderDuna;
import com.hbmspace.particle.SpaceParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class DunaStormClient {

    private static @Nullable ClientLevel level;
    private static float smoothed;

    private DunaStormClient() {}

    static void tick(Minecraft mc) {
        ClientLevel current = mc.level;
        if (current != level) {
            level = current;
            smoothed = 0;
        }
        if (current == null || !(WorldProviderCelestial.of(current) instanceof WorldProviderDuna))
            return;

        smoothed = (float) Mth.lerp(0.008D, smoothed, DunaDustStorm.clientIntensity);
        RandomSource rand = current.getRandom();
        Entity view = mc.getCameraEntity();
        if (view != null && smoothed >= 0.05F && rand.nextFloat() < smoothed) {
            Vec3 offset =
                    new Vec3(20, 0, 50)
                            .zRot((float) (rand.nextDouble() * Math.PI * 10))
                            .yRot((float) (rand.nextDouble() * Math.PI * 2 * 5));
            current.addParticle(
                    SpaceParticles.DUST_STORM.get(),
                    view.getX() + offset.x,
                    view.getY(),
                    view.getZ() + offset.z,
                    -4,
                    0,
                    0);
        }
    }

    public static float smoothed() {
        return smoothed;
    }
}
