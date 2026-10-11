// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 detree05
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.packet.PacketWire;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.Target;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.network.Utf8String;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public final class CelestialNukeShockHandler {

    private static final long MAX_AGE = 220L;
    public static final int MAX_ACTIVE_SHOCKS = 4;
    private static final float FLASH_GRID = 16.0F;

    public static final class ShockStatus {
        public long time;
        public float centerX;
        public float centerY;
        public float strength;

        public ShockStatus(long time, float centerX, float centerY, float strength) {
            this.time = time;
            this.centerX = centerX;
            this.centerY = centerY;
            this.strength = strength;
        }
    }

    private static final Map<String, List<ShockStatus>> serverStatuses = new HashMap<>();
    private static final Map<String, List<ShockStatus>> clientStatuses = new HashMap<>();

    private CelestialNukeShockHandler() {}

    public static void trigger(ServerLevel level, double x, double z, float blastRadius) {
        Target target = CelestialBody.getTarget(level, Mth.floor(x), Mth.floor(z));
        CelestialBody body = target.body();

        long time = level.getGameTime();
        float rawX = 0.18F + hashToUnit(x * 0.03125D + z * 0.0125D + time * 0.017D) * 0.64F;
        float rawY = 0.22F + hashToUnit(x * -0.015D + z * 0.0275D + time * 0.011D + 19.0D) * 0.52F;
        float centerX = quantizeCenter(rawX);
        float centerY = quantizeCenter(rawY);
        float strength = Mth.clamp(blastRadius / 140.0F, 0.35F, 1.0F);

        List<ShockStatus> statuses =
                serverStatuses.computeIfAbsent(body.name, name -> new ArrayList<>());
        pruneStatuses(statuses, time);
        statuses.add(new ShockStatus(time, centerX, centerY, strength));
        while (statuses.size() > MAX_ACTIVE_SHOCKS) {
            statuses.removeFirst();
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) sync(player);
    }

    public static void sync(ServerPlayer player) {
        PacketWire.sendTo(NukeShockSyncPayload.of(player.level().getGameTime()), player);
    }

    public static List<ShockStatus> getServerShocks(CelestialBody body) {
        return serverStatuses.getOrDefault(body.name, Collections.emptyList());
    }

    public static List<ShockStatus> getClientShocks(CelestialBody body, long currentTime) {
        List<ShockStatus> statuses = clientStatuses.get(body.name);
        if (statuses == null) return Collections.emptyList();
        pruneStatuses(statuses, currentTime);
        return statuses;
    }

    public static void onServerStopping(MinecraftServer server) {
        serverStatuses.clear();
    }

    static void writeSync(ByteBuf buf, long currentTime) {
        cleanup(serverStatuses, currentTime);

        buf.writeShort(serverStatuses.size());
        for (Map.Entry<String, List<ShockStatus>> entry : serverStatuses.entrySet()) {
            List<ShockStatus> statuses = entry.getValue();
            Utf8String.write(buf, entry.getKey(), Short.MAX_VALUE);
            buf.writeByte(statuses.size());
            for (ShockStatus status : statuses) {
                buf.writeLong(status.time);
                buf.writeFloat(status.centerX);
                buf.writeFloat(status.centerY);
                buf.writeFloat(status.strength);
            }
        }
    }

    static void readSync(ByteBuf buf) {
        clientStatuses.clear();

        int count = buf.readShort();
        for (int i = 0; i < count; i++) {
            String body = Utf8String.read(buf, Short.MAX_VALUE);
            int shockCount = buf.readUnsignedByte();
            List<ShockStatus> statuses = new ArrayList<>(shockCount);
            for (int j = 0; j < shockCount; j++) {
                long time = buf.readLong();
                float centerX = buf.readFloat();
                float centerY = buf.readFloat();
                float strength = buf.readFloat();
                statuses.add(new ShockStatus(time, centerX, centerY, strength));
            }
            clientStatuses.put(body, statuses);
        }
    }

    private static void cleanup(Map<String, List<ShockStatus>> statuses, long currentTime) {
        Iterator<Map.Entry<String, List<ShockStatus>>> iterator = statuses.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<ShockStatus>> entry = iterator.next();
            pruneStatuses(entry.getValue(), currentTime);
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }
    }

    private static void pruneStatuses(List<ShockStatus> statuses, long currentTime) {
        statuses.removeIf(status -> currentTime - status.time > MAX_AGE);
    }

    private static float hashToUnit(double value) {
        return (float) (value - Math.floor(value));
    }

    private static float quantizeCenter(float value) {
        float clamped = Mth.clamp(value, 0.05F, 0.95F);
        return (Mth.floor(clamped * FLASH_GRID) + 0.5F) / FLASH_GRID;
    }
}
