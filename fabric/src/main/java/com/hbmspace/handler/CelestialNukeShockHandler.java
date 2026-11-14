/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 detree05. All rights reserved.
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

package com.hbmspace.handler;

import com.hbm.packet.PacketWire;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.Target;
import com.hbmspace.packet.layout.SpaceNukeShocks;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public final class CelestialNukeShockHandler {

    private static final long MAX_AGE = 220L;
    public static final int MAX_ACTIVE_SHOCKS = 4;
    private static final float FLASH_GRID = 16.0F;

    public record ShockStatus(long time, float centerX, float centerY, float strength) {

        private static final int BYTES = Long.BYTES + Float.BYTES * 3;

        static final StreamCodec<ByteBuf, ShockStatus> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.LONG,
                        ShockStatus::time,
                        ByteBufCodecs.FLOAT,
                        ShockStatus::centerX,
                        ByteBufCodecs.FLOAT,
                        ShockStatus::centerY,
                        ByteBufCodecs.FLOAT,
                        ShockStatus::strength,
                        ShockStatus::new);
    }

    public static final StreamCodec<ByteBuf, Map<String, List<ShockStatus>>> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public Map<String, List<ShockStatus>> decode(ByteBuf input) {
                    int count = readCount(input, 2);
                    Map<String, List<ShockStatus>> result = new HashMap<>(Math.min(count, 65_536));
                    for (int i = 0; i < count; i++) {
                        String body = ByteBufCodecs.STRING_UTF8.decode(input);
                        int shocks = readCount(input, ShockStatus.BYTES);
                        if (shocks > MAX_ACTIVE_SHOCKS) {
                            throw new DecoderException(
                                    "Too many shocks for " + body + ": " + shocks);
                        }
                        List<ShockStatus> statuses = new ArrayList<>(shocks);
                        for (int j = 0; j < shocks; j++)
                            statuses.add(ShockStatus.STREAM_CODEC.decode(input));
                        if (result.put(body, List.copyOf(statuses)) != null) {
                            throw new DecoderException(
                                    "Duplicate celestial body shock entry: " + body);
                        }
                    }
                    return result;
                }

                @Override
                public void encode(ByteBuf output, Map<String, List<ShockStatus>> shocks) {
                    ByteBufCodecs.VAR_INT.encode(output, shocks.size());
                    shocks.forEach(
                            (body, statuses) -> {
                                if (statuses.size() > MAX_ACTIVE_SHOCKS) {
                                    throw new IllegalArgumentException(
                                            "Too many shocks for " + body + ": " + statuses.size());
                                }
                                ByteBufCodecs.STRING_UTF8.encode(output, body);
                                ByteBufCodecs.VAR_INT.encode(output, statuses.size());
                                for (ShockStatus status : statuses)
                                    ShockStatus.STREAM_CODEC.encode(output, status);
                            });
                }
            };

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
        PacketWire.sendTo(SpaceNukeShocks.encode(snapshot(player.level().getGameTime())), player);
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

    private static Map<String, List<ShockStatus>> snapshot(long currentTime) {
        cleanup(serverStatuses, currentTime);
        Map<String, List<ShockStatus>> copy = new HashMap<>();
        serverStatuses.forEach((body, statuses) -> copy.put(body, List.copyOf(statuses)));
        return copy;
    }

    public static void replaceClient(Map<String, List<ShockStatus>> shocks) {
        clientStatuses.clear();
        shocks.forEach((body, statuses) -> clientStatuses.put(body, new ArrayList<>(statuses)));
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
        statuses.removeIf(status -> currentTime - status.time() > MAX_AGE);
    }

    private static int readCount(ByteBuf input, int minimumBytesPerEntry) {
        int count = ByteBufCodecs.VAR_INT.decode(input);
        if (count < 0 || count > input.readableBytes() / minimumBytesPerEntry) {
            throw new DecoderException("Invalid shock count: " + count);
        }
        return count;
    }

    private static float hashToUnit(double value) {
        return (float) (value - Math.floor(value));
    }

    private static float quantizeCenter(float value) {
        float clamped = Mth.clamp(value, 0.05F, 0.95F);
        return (Mth.floor(clamped * FLASH_GRID) + 0.5F) / FLASH_GRID;
    }
}
