// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbm.items.ModItems;
import com.hbm.items.special.ItemSatellite;
import com.hbm.packet.PacketWire;
import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.NeoForgeNetwork;
import com.hbm.platform.NeoForgeServerEvents;
import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.items.ModDataComponentsSpace;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public final class SatellitesSpace {

    private static final Map<SatelliteType, float[]> COLORS = new IdentityHashMap<>();
    private static boolean changed;

    private SatellitesSpace() {}

    public static void install() {
        satellite(
                SatelliteType.MAPPER,
                0.538F,
                1.0F,
                0.523F,
                ModItems.SATELLITE_SPY,
                ModItems.SAT_MAPPER);
        satellite(
                SatelliteType.SCANNER,
                0.544F,
                0.680F,
                1.0F,
                ModItems.SATELLITE_SCANNER,
                ModItems.SAT_SCANNER);
        satellite(
                SatelliteType.RADAR,
                0.134F,
                1.0F,
                0.134F,
                ModItems.SATELLITE_RADAR,
                ModItems.SAT_RADAR);
        satellite(
                SatelliteType.DEATH_RAY,
                0.221F,
                0.663F,
                1.0F,
                ModItems.SATELLITE_DEATH_RAY,
                ModItems.SAT_LASER);
        satellite(
                SatelliteType.RESONATOR,
                1.0F,
                0.646F,
                0.181F,
                ModItems.SATELLITE_XENIUM_RESONATOR,
                ModItems.SAT_RESONATOR);
        satellite(SatelliteType.RELAY, 1.0F, 0.15F, 0.15F, ModItems.SATELLITE_RELAY);
        satellite(
                SatelliteType.MINER,
                0.46F,
                0.56F,
                0.68F,
                ModItems.SATELLITE_MINER_ASTRO,
                ModItems.SAT_MINER);
        satellite(
                SatelliteType.LUNAR_MINER,
                0.42F,
                0.54F,
                0.82F,
                ModItems.SATELLITE_MINER_LUNAR,
                ModItems.SAT_LUNAR_MINER);
        satellite(SatelliteType.HORIZONS, 0.0F, 0.0F, 0.0F, ModItems.SAT_GERALD);
        satellite(
                SatelliteType.PRECISION_LASER,
                0.221F,
                1.0F,
                0.663F,
                ModItems.SATELLITE_PRECISION_LASER);
        satellite(SatelliteType.DETECTOR, 0.1F, 0.2F, 0.3F, ModItems.SATELLITE_DETECTOR);
        satellite(SatelliteType.RAY_SCAN, 0.1F, 0.2F, 0.3F, ModItems.SATELLITE_RAY_SCAN);

        Satellite.CARRIED.install(HbmSpace.MOD_ID, List.of(ModDataComponentsSpace.SATELLITE_ORBIT));
        Satellite.ORBIT_LEVEL.install(HbmSpace.MOD_ID, SatellitesSpace::orbitLevel);
        SatelliteSavedData.AT.install(HbmSpace.MOD_ID, SatellitesSpace::at);
        SatelliteSavedData.CHANGED.install(HbmSpace.MOD_ID, () -> changed = true);
        ItemSatellite.DESCRIBE.install(HbmSpace.MOD_ID, SatelliteItems::describe);
        ItemSatellite.USE.install(HbmSpace.MOD_ID, SatelliteItems::use);

        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        SatelliteSyncPayload.TYPE,
                        SatelliteSyncPayload.STREAM_CODEC,
                        SatelliteSyncPayload::handle,
                        PayloadHandlerThread.MAIN));
        NeoForgeNetwork.serverbound.add(
                new NeoForgeNetwork.Registration<>(
                        SatelliteSettingsPayload.TYPE,
                        SatelliteSettingsPayload.STREAM_CODEC,
                        SatelliteSettingsPayload::handle,
                        PayloadHandlerThread.MAIN));
        NeoForgeServerEvents.onPlayerJoin(
                player ->
                        PacketWire.sendTo(
                                SatelliteSyncPayload.of(player.level().getServer()), player));
        NeoForgeServerEvents.onServerTickPost(SatellitesSpace::flush);
        NeoForgeServerEvents.onServerStopping(server -> changed = false);
    }

    @SafeVarargs
    public static void satellite(
            SatelliteType type,
            float red,
            float green,
            float blue,
            RegistryHandle<? extends Item>... items) {
        COLORS.put(type, new float[] {red, green, blue});
        for (RegistryHandle<? extends Item> item : items) {
            NeoForgeRegistrar.modifyDefaultComponents(
                    item,
                    components ->
                            components.set(
                                    ModDataComponentsSpace.SATELLITE_ORBIT.get(), defaults(type)));
        }
    }

    public static boolean isSpace(SatelliteType type) {
        return COLORS.containsKey(type);
    }

    public static SatelliteOrbit defaults(SatelliteType type) {
        float[] color = COLORS.get(type);
        return SatelliteOrbit.defaults(color[0], color[1], color[2]);
    }

    public static SatelliteOrbit orbit(Satellite satellite) {
        SatelliteOrbit orbit =
                satellite.components.get(ModDataComponentsSpace.SATELLITE_ORBIT.get());
        return orbit != null ? orbit : defaults(satellite.type());
    }

    public static SolarSystem.@Nullable Body home(@Nullable SatelliteType type) {
        if (type == SatelliteType.RELAY) return SolarSystem.Body.DUNA;
        if (type == SatelliteType.LUNAR_MINER) return SolarSystem.Body.MUN;
        if (type == SatelliteType.MINER) return SolarSystem.Body.DRES;
        return null;
    }

    private static ServerLevel orbitLevel(ServerLevel level, SatelliteType type) {
        SolarSystem.Body home = home(type);
        if (home == null) return level;
        ServerLevel target = level.getServer().getLevel(home.getDimension());
        return target != null ? target : level;
    }

    private static SatelliteSavedData at(ServerLevel level, int x, int z) {
        if (CelestialBody.inOrbit(level)) {
            ServerLevel body =
                    OrbitalStation.getStationFromPosition(x, z)
                            .orbiting
                            .getLevel(level.getServer());
            if (body != null) return SatelliteSavedData.get(body);
        }
        return SatelliteSavedData.get(level);
    }

    private static void flush(MinecraftServer server) {
        if (!changed) return;
        changed = false;
        PacketWire.sendToAll(SatelliteSyncPayload.of(server));
    }
}
