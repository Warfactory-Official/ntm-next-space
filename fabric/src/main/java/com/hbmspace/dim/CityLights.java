// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.api.block.BlockPlacements;
import com.hbmspace.dim.trait.CBT_Lights;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;

public final class CityLights {

    private CityLights() {}

    static void register() {
        BlockPlacements.onPlaced(
                (level, pos) -> {
                    BlockState state = level.getBlockState(pos);
                    if (pos.getY() >= horizon(level) && state.getLightEmission() > 10)
                        count(level, state, true);
                });
        BlockPlacements.onRepaired((level, pos) -> count(level, level.getBlockState(pos), true));
    }

    public static void broken(ServerLevel level, BlockPos pos, BlockState state) {
        if (pos.getY() > horizon(level) && state.getLightEmission() > 10)
            count(level, state, false);
    }

    private static int horizon(ServerLevel level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider != null) return provider.getHorizon(level);
        return level.getServer().overworld().getChunkSource().getGenerator()
                        instanceof FlatLevelSource
                ? level.getMinY()
                : 63;
    }

    private static void count(ServerLevel level, BlockState state, boolean add) {
        CelestialBody body = CelestialBody.getBody(level);
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits = body.getTraits();
        CBT_Lights lights =
                (CBT_Lights) traits.computeIfAbsent(CBT_Lights.class, type -> new CBT_Lights());
        if (add) lights.addLight(state);
        else lights.removeLight(state);
        body.setTraits(traits);
    }
}
