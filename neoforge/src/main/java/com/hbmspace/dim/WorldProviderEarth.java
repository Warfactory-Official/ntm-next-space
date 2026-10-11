// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public class WorldProviderEarth extends WorldProviderCelestial {

    public WorldProviderEarth() {
        super(Level.OVERWORLD);
    }

    @Override
    public boolean hasLife() {
        return true;
    }

    @Override
    public double getDayLength() {
        return 24_000D;
    }

    @Override
    public float getCelestialAngle(Level level, float partialTicks) {
        return level.environmentAttributes()
                        .getValue(EnvironmentAttributes.SUN_ANGLE, BlockPos.ZERO)
                / 360F;
    }

    @Override
    public int getHorizon(Level level) {
        return level instanceof ServerLevel server
                        && server.getChunkSource().getGenerator() instanceof FlatLevelSource
                ? level.getMinY()
                : 63;
    }
}
