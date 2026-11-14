/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2022-2023 HbmMods (The Bobcat)
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2022-2023 HbmMods (The Bobcat). All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.entity.effect;

import com.hbm.client.TorexClientFX;
import com.hbm.entity.effect.NukeTorexEntity.Cloudlet;
import com.hbm.entity.effect.NukeTorexEntity;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TorexVacuum implements NukeTorexEntity.Simulation {
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
    public boolean claims(NukeTorexEntity torex) {
        return airless(torex.level().dimension());
    }

    @Override
    public int tick(NukeTorexEntity torex, int age, boolean live) {
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
