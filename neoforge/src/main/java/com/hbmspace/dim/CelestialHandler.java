// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.api.entity.LivingHooks;
import com.hbm.api.entity.PlayerRespawns;
import com.hbm.packet.PacketWire;
import com.hbm.packet.PayloadHandlerThread;
import com.hbm.packet.toclient.PlayerInformPayload;
import com.hbm.platform.NeoForgeNetwork;
import com.hbm.platform.NeoForgeServerEvents;
import com.hbm.registration.NeoForgeRegistrar;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.dres.ChunkProviderDres;
import com.hbmspace.dim.dres.WorldGeneratorDres;
import com.hbmspace.dim.dres.WorldProviderDres;
import com.hbmspace.dim.duna.ChunkProviderDuna;
import com.hbmspace.dim.duna.WorldGeneratorDuna;
import com.hbmspace.dim.duna.WorldProviderDuna;
import com.hbmspace.dim.ike.ChunkProviderIke;
import com.hbmspace.dim.ike.WorldGeneratorIke;
import com.hbmspace.dim.ike.WorldProviderIke;
import com.hbmspace.dim.minmus.ChunkProviderMinmus;
import com.hbmspace.dim.minmus.WorldGeneratorMinmus;
import com.hbmspace.dim.minmus.WorldProviderMinmus;
import com.hbmspace.dim.moho.ChunkProviderMoho;
import com.hbmspace.dim.moho.WorldGeneratorMoho;
import com.hbmspace.dim.moho.WorldProviderMoho;
import com.hbmspace.dim.moon.ChunkProviderMoon;
import com.hbmspace.dim.moon.WorldGeneratorMoon;
import com.hbmspace.dim.moon.WorldProviderMoon;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.dim.trait.CBT_War;
import com.hbmspace.dim.trait.CBT_Weather;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.util.AstronomyUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class CelestialHandler {

    static {
        NeoForgeRegistrar.registerBiomeSource(
                "hbmspace:celestial", WorldChunkManagerCelestial.CODEC);

        WorldProviderCelestial.register(new WorldProviderEarth());
        WorldProviderCelestial.register(new WorldProviderMoon());
        SpaceChunkGenerators.register("hbmspace:mun", ChunkProviderMoon.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.MUN, new WorldGeneratorMoon());

        WorldProviderCelestial.register(new WorldProviderMinmus());
        WorldChunkManagerCelestial.register(
                SpaceDimensions.MINMUS.identifier(), WorldProviderMinmus::createBiomeGenerators);
        SpaceChunkGenerators.register("hbmspace:minmus", ChunkProviderMinmus.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.MINMUS, new WorldGeneratorMinmus());

        WorldProviderCelestial.register(new WorldProviderDres());
        WorldChunkManagerCelestial.register(
                SpaceDimensions.DRES.identifier(), WorldProviderDres::createBiomeGenerators);
        SpaceChunkGenerators.register("hbmspace:dres", ChunkProviderDres.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.DRES, new WorldGeneratorDres());

        WorldProviderCelestial.register(new WorldProviderMoho());
        WorldChunkManagerCelestial.register(
                SpaceDimensions.MOHO.identifier(), WorldProviderMoho::createBiomeGenerators);
        SpaceChunkGenerators.register("hbmspace:moho", ChunkProviderMoho.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.MOHO, new WorldGeneratorMoho());

        WorldProviderCelestial.register(new WorldProviderIke());
        SpaceChunkGenerators.register("hbmspace:ike", ChunkProviderIke.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.IKE, new WorldGeneratorIke());

        WorldProviderCelestial.register(new WorldProviderDuna());
        WorldChunkManagerCelestial.register(
                SpaceDimensions.DUNA.identifier(), WorldProviderDuna::createBiomeGenerators);
        SpaceChunkGenerators.register("hbmspace:duna", ChunkProviderDuna.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.DUNA, new WorldGeneratorDuna());

        NeoForgeServerEvents.onLevelLoad(
                level -> {
                    if (level.dimension() == Level.OVERWORLD)
                        SolarSystemWorldSavedData.publish(level.getServer());
                });
        NeoForgeServerEvents.onServerStopping(
                server -> {
                    SolarSystemWorldSavedData.onServerStopping();
                    CelestialTeleporter.onServerStopping();
                    AstronomyUtil.TIME_MULTIPLIER = 1;
                });
        NeoForgeServerEvents.onServerTickPre(CelestialHandler::onServerTickStart);
        NeoForgeServerEvents.onServerTickPost(CelestialHandler::onServerTickEnd);
        LivingHooks.onTick(SpaceGravity::onLivingUpdate);
        PlayerRespawns.register(WorldProviderCelestial::respawn);
        WorldTypeTeleport.register();
        CityLights.register();

        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        SolarSystemSyncPayload.TYPE,
                        SolarSystemSyncPayload.STREAM_CODEC,
                        SolarSystemSyncPayload::handle,
                        PayloadHandlerThread.MAIN));
    }

    private CelestialHandler() {}

    public static void onSimulationTickStart(MinecraftServer server) {
        if (!server.tickRateManager().runsNormally()) {
            for (CelestialBody body : CelestialBody.getAllBodies()) {
                CBT_Invasion invasion = body.getTrait(CBT_Invasion.class);
                if (invasion != null) invasion.updateBar(server, body);
            }
            return;
        }
        CBT_Weather.updateGlobalWeather(server);
        for (CelestialBody body : CelestialBody.getAllBodies()) {
            List<CelestialBodyTrait> traits = new ArrayList<>(body.currentTraits().values());
            for (CelestialBodyTrait trait : traits) {
                if (!(trait instanceof CBT_War)) trait.update(false, body);
            }
        }

        CelestialBody.updateSwarms();

        ServerLevel orbit = server.getLevel(SpaceDimensions.ORBIT);
        if (orbit != null) {
            for (Entity entity : orbit.getAllEntities()) {
                if (entity instanceof ItemEntity item) {
                    Vec3 motion = item.getDeltaMovement();
                    item.setDeltaMovement(motion.x * 0.9D, item.getGravity(), motion.z * 0.9D);
                }
            }
        }
    }

    private static void onServerTickStart(MinecraftServer server) {
        ServerLevel orbit = server.getLevel(SpaceDimensions.ORBIT);
        if (orbit != null) {
            for (ServerPlayer player : orbit.players()) {
                if (!(player.getVehicle() instanceof EntityRideableRocket))
                    checkOrbitalGrid(player);
            }
        }
    }

    private static void onServerTickEnd(MinecraftServer server) {
        CelestialTeleporter.runQueuedTeleports(server);

        if (server.tickRateManager().runsNormally() && server.overworld().getGameTime() % 20 == 0) {
            for (CelestialBody body : CelestialBody.getLandableBodies()) {
                var traits = body.getTraits();
                if (CelestialBody.updateChemistry(traits)) body.setTraits(traits);
            }
        }

        boolean warProjectiles = false;
        for (CelestialBody body : CelestialBody.getAllBodies()) {
            CBT_War war = body.getTrait(CBT_War.class);
            if (war != null) {
                warProjectiles |= !war.getProjectiles().isEmpty();
                war.update(false, body);
            }
        }
        if (warProjectiles || server.overworld().getGameTime() % 5 == 1) {
            SolarSystemWorldSavedData solarSystem = SolarSystemWorldSavedData.get(server);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PacketWire.sendTo(
                        SolarSystemSyncPayload.of(solarSystem, player.level().dimension()), player);
            }
        }
    }

    private static void checkOrbitalGrid(ServerPlayer player) {
        double rx = Math.abs(player.getX()) % OrbitalStation.STATION_SIZE;
        double rz = Math.abs(player.getZ()) % OrbitalStation.STATION_SIZE;

        int minBuffer = OrbitalStation.BUFFER_SIZE;
        int maxBuffer = OrbitalStation.STATION_SIZE - minBuffer;

        int minWarning = OrbitalStation.BUFFER_SIZE + OrbitalStation.WARNING_SIZE;
        int maxWarning = OrbitalStation.STATION_SIZE - minWarning;

        if (rx < minWarning || rx > maxWarning || rz < minWarning || rz > maxWarning) {
            PacketWire.sendTo(
                    new PlayerInformPayload(
                            Component.translatable("info.orbitfall").withStyle(ChatFormatting.RED),
                            PlayerInformPayload.ID_GAS_HAZARD,
                            3000),
                    player);
        }

        if (rx < minBuffer || rx > maxBuffer || rz < minBuffer || rz > maxBuffer) {
            OrbitalStation station =
                    OrbitalStation.getStationFromPosition(player.getBlockX(), player.getBlockZ());
            RandomSource rand = player.getRandom();
            int range = SpaceData.MAX_PROBE_DISTANCE.get();
            CelestialTeleporter.teleport(
                    player,
                    station.orbiting.dimension,
                    rand.nextInt(range * 2) - range,
                    800,
                    rand.nextInt(range * 2) - range,
                    false);
        }
    }
}
