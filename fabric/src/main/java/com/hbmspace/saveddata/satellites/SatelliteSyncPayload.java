// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.HbmSpace;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class SatelliteSyncPayload extends ThreadedPayload {

    public static final Type<SatelliteSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "satellites"));
    public static final StreamCodec<ByteBuf, SatelliteSyncPayload> STREAM_CODEC =
            streamCodec(SatelliteSyncPayload::decode);

    private final byte[] data;

    private SatelliteSyncPayload(byte[] data) {
        this.data = data;
    }

    public static SatelliteSyncPayload of(MinecraftServer server) {
        List<ServerLevel> levels = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            if (!spaceSatellites(level).isEmpty()) levels.add(level);
        }

        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(levels.size());
        for (ServerLevel level : levels) {
            Utf8String.write(buf, level.dimension().identifier().toString(), Short.MAX_VALUE);
            List<Map.Entry<Integer, Satellite>> satellites = spaceSatellites(level);
            buf.writeInt(satellites.size());
            for (Map.Entry<Integer, Satellite> entry : satellites) {
                buf.writeInt(entry.getKey());
                Utf8String.write(buf, entry.getValue().type().getSerializedName(), Short.MAX_VALUE);
                SatelliteOrbit.STREAM_CODEC.encode(buf, SatellitesSpace.orbit(entry.getValue()));
            }
        }

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new SatelliteSyncPayload(data);
    }

    private static List<Map.Entry<Integer, Satellite>> spaceSatellites(ServerLevel level) {
        List<Map.Entry<Integer, Satellite>> satellites = new ArrayList<>();
        for (Map.Entry<Integer, Satellite> entry : SatelliteSavedData.get(level).sats.entrySet()) {
            if (SatellitesSpace.isSpace(entry.getValue().type())) satellites.add(entry);
        }
        return satellites;
    }

    private static SatelliteSyncPayload decode(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new SatelliteSyncPayload(data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(data);
    }

    @Override
    protected int bodyCapacity() {
        return data.length;
    }

    public static void handle(SatelliteSyncPayload payload, IPayloadHandlerContext context) {
        ClientSatellites.replace(read(payload));
    }

    public static Map<ResourceKey<Level>, Map<Integer, ClientSatellites.Entry>> read(
            SatelliteSyncPayload payload) {
        ByteBuf buf = Unpooled.wrappedBuffer(payload.data);
        Map<String, SatelliteType> types = new HashMap<>();
        for (SatelliteType type : SatelliteType.values()) types.put(type.getSerializedName(), type);

        Map<ResourceKey<Level>, Map<Integer, ClientSatellites.Entry>> byLevel = new HashMap<>();
        int levels = buf.readInt();
        for (int l = 0; l < levels; l++) {
            ResourceKey<Level> level =
                    ResourceKey.create(
                            Registries.DIMENSION,
                            Identifier.parse(Utf8String.read(buf, Short.MAX_VALUE)));
            Map<Integer, ClientSatellites.Entry> satellites = new HashMap<>();
            int count = buf.readInt();
            for (int i = 0; i < count; i++) {
                int freq = buf.readInt();
                SatelliteType type = types.get(Utf8String.read(buf, Short.MAX_VALUE));
                SatelliteOrbit orbit = SatelliteOrbit.STREAM_CODEC.decode(buf);
                if (type == null)
                    throw new IllegalStateException(
                            "the server sent a satellite kind this client lacks");
                satellites.put(freq, new ClientSatellites.Entry(type, orbit));
            }
            byLevel.put(level, Map.copyOf(satellites));
        }
        return Map.copyOf(byLevel);
    }

    @Override
    public Type<SatelliteSyncPayload> type() {
        return TYPE;
    }
}
