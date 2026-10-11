// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import com.hbmspace.util.AstronomyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class SolarSystemSyncPayload extends ThreadedPayload {

    public static final Type<SolarSystemSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "solar_system"));
    public static final StreamCodec<ByteBuf, SolarSystemSyncPayload> STREAM_CODEC =
            streamCodec(SolarSystemSyncPayload::decode);

    private final byte[] data;

    private SolarSystemSyncPayload(byte[] data) {
        this.data = data;
    }

    public static SolarSystemSyncPayload of(
            SolarSystemWorldSavedData solarSystem, ResourceKey<Level> dimension) {
        ByteBuf buf = Unpooled.buffer();

        List<CelestialBody> bodies = new ArrayList<>(CelestialBody.getAllBodies());
        buf.writeInt(bodies.size());
        for (CelestialBody body : bodies) {
            Utf8String.write(buf, body.name, Short.MAX_VALUE);
            Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
                    solarSystem.getTraits(body.name);
            if (traits != null) {
                buf.writeBoolean(true);
                buf.writeInt(traits.size());

                for (int i = 0; i < CelestialBodyTrait.traitList.size(); i++) {
                    CelestialBodyTrait trait = traits.get(CelestialBodyTrait.traitList.get(i));

                    if (trait != null) {
                        buf.writeInt(i);
                        trait.writeToBytes(buf);
                    }
                }
            } else {
                buf.writeBoolean(false);
            }
        }

        List<OrbitalStation> stations = new ArrayList<>();
        for (OrbitalStation station : solarSystem.getStations().values()) {
            if (station.hasStation && station.orbiting.dimension == dimension)
                stations.add(station);
        }

        buf.writeInt(stations.size());
        for (OrbitalStation station : stations) {
            buf.writeInt(station.dX);
            buf.writeInt(station.dZ);
        }
        buf.writeLong(AstronomyUtil.TIME_MULTIPLIER);

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new SolarSystemSyncPayload(data);
    }

    private static SolarSystemSyncPayload decode(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new SolarSystemSyncPayload(data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(data);
    }

    @Override
    protected int bodyCapacity() {
        return data.length;
    }

    public static void handle(SolarSystemSyncPayload payload, IPayloadHandlerContext context) {
        Map<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>> traitMap =
                new HashMap<>(SolarSystemWorldSavedData.clientTraits());
        List<OrbitalStation> stations = new ArrayList<>();

        read(payload, traitMap, stations);

        SolarSystemWorldSavedData.updateClientTraits(traitMap);
        OrbitalStation.orbitingStations = stations;
    }

    public static void read(
            SolarSystemSyncPayload payload,
            Map<String, HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait>> traitMap,
            List<OrbitalStation> stations) {
        ByteBuf buf = Unpooled.wrappedBuffer(payload.data);

        int bodies = buf.readInt();
        for (int b = 0; b < bodies; b++) {
            String name = Utf8String.read(buf, Short.MAX_VALUE);
            if (buf.readBoolean()) {
                HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
                        traitMap.computeIfAbsent(name, k -> new HashMap<>());
                List<Class<? extends CelestialBodyTrait>> sentTraits = new ArrayList<>();

                int cbtSize = buf.readInt();
                for (int i = 0; i < cbtSize; i++) {
                    Class<? extends CelestialBodyTrait> clazz =
                            CelestialBodyTrait.traitList.get(buf.readInt());
                    sentTraits.add(clazz);

                    CelestialBodyTrait trait = traits.get(clazz);
                    if (trait == null) trait = CelestialBodyTrait.create(clazz);
                    trait.readFromBytes(buf);

                    traits.put(clazz, trait);
                }

                traits.keySet().removeIf(traitClass -> !sentTraits.contains(traitClass));
            } else {
                traitMap.remove(name);
            }
        }

        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            stations.add(new OrbitalStation(null, buf.readInt(), buf.readInt()));
        }
        long multiplier = buf.readLong();
        if (multiplier < 1)
            throw new IllegalStateException("invalid celestial time multiplier " + multiplier);
        AstronomyUtil.TIME_MULTIPLIER = multiplier;
    }

    @Override
    public Type<SolarSystemSyncPayload> type() {
        return TYPE;
    }
}
