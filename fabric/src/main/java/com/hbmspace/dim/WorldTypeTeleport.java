// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.platform.FabricServerEvents;
import com.hbmspace.HbmSpace;
import com.hbmspace.extprop.SpacePlayerProps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class WorldTypeTeleport {

    public static final ResourceKey<NoiseGeneratorSettings> NOISE =
            ResourceKey.create(
                    Registries.NOISE_SETTINGS,
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "martian"));

    private static volatile boolean martian;

    private WorldTypeTeleport() {}

    static void register() {
        FabricServerEvents.onLevelLoad(
                level -> {
                    if (level.dimension() == Level.OVERWORLD)
                        martian = marks(level.getChunkSource().getGenerator());
                });
        FabricServerEvents.onPlayerJoin(WorldTypeTeleport::onPlayerJoin);
    }

    public static boolean marks(ChunkGenerator generator) {
        return generator instanceof NoiseBasedChunkGenerator noise
                && noise.generatorSettings().is(NOISE);
    }

    public static boolean isMartian() {
        return martian;
    }

    private static void onPlayerJoin(ServerPlayer player) {
        if (!martian || SpacePlayerProps.hasWarped(player)) return;
        CelestialTeleporter.teleport(player, SpaceDimensions.DUNA, 0, 0, 0, true);
        SpacePlayerProps.setWarped(player, true);
    }
}
