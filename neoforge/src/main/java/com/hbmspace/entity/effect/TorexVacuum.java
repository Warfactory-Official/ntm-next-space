// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2022-2023 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.effect;

import com.hbm.client.TorexClientFX;
import com.hbm.entity.effect.EntityNukeTorex.Cloudlet;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TorexVacuum implements EntityNukeTorex.Simulation {
    private static final double LEGACY_SCALE = 1.5;
    private static final double LEGACY_CS = 1.5;

    public static boolean airless(boolean orbit, @Nullable CBT_Atmosphere atmosphere) {
        return orbit || atmosphere == null || atmosphere.getPressure() < 0.01;
    }

    public static boolean airless(ResourceKey<Level> dimension) {
        return airless(
                dimension == SpaceDimensions.ORBIT,
                CelestialBody.getBody(dimension).getTrait(CBT_Atmosphere.class));
    }

    @Override
    public boolean claims(EntityNukeTorex torex) {
        return airless(torex.level().dimension());
    }

    @Override
    public int tick(EntityNukeTorex torex, int age, boolean live) {
        double scale = torex.getScaleVal();
        if (live) TorexClientFX.hudFlash(torex);
        if (live && age < 120 * scale) TorexClientFX.skyFlash(torex.level());
        if (age == 1) torex.heat = scale * scale;

        if (age < 40) {
            RandomSource rand = torex.getRandom();
            double range = (age + rand.nextDouble()) * 0.6 * scale;
            int toSpawn = age * 5;
            int lifetime = Math.max(800 - age * 10, 40);
            for (int i = 0; i < toSpawn; i++) {
                double x = torex.getX() + rand.nextGaussian() * range;
                double y = torex.getY() + rand.nextGaussian() * range;
                double z = torex.getZ() + rand.nextGaussian() * range;
                Cloudlet cloud =
                        torex
                        .new Cloudlet(
                                x,
                                y,
                                z,
                                (float) (rand.nextDouble() * 2D * Math.PI),
                                0,
                                lifetime,
                                TorexTypesSpace.STARFISH);
                cloud.setScale(4F * (float) scale, 5F * (float) scale);
                cloud.setMotion((age < 25 && rand.nextBoolean() ? 0.5 : 0.05) * scale);
                torex.cloudlets.add(cloud);
            }
        }

        if (live && age < 150) TorexClientFX.shockwaveArrival(torex);

        for (Cloudlet cloud : torex.cloudlets) cloud.update();
        int updated = torex.cloudlets.size();
        torex.coreHeight += 0.15 / LEGACY_SCALE * scale;
        torex.torusWidth += 0.05 / LEGACY_SCALE * scale;
        torex.rollerSize = torex.torusWidth * 0.8;
        torex.convectionHeight = torex.coreHeight + torex.rollerSize;

        int maxHeat = (int) (50 * LEGACY_CS);
        torex.heat = (maxHeat - (maxHeat * age) / torex.maxAge) * scale * scale;

        torex.cloudlets.removeIf(cloud -> cloud.isDead);
        return updated;
    }
}
