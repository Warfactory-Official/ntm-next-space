/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.saveddata.satellites;

import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.orbit.OrbitalStation;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ClientSatellites {

    private static Map<ResourceKey<Level>, Map<Integer, Entry>> byLevel = Map.of();
    private static final StreamCodec<ByteBuf, ResourceKey<Level>> DIMENSION_CODEC =
            ResourceKey.streamCodec(Registries.DIMENSION);
    private static final StreamCodec<ByteBuf, Map<Integer, Entry>> BY_FREQUENCY =
            new StreamCodec<>() {
                @Override
                public Map<Integer, Entry> decode(ByteBuf input) {
                    int count = readCount(input, 1);
                    Map<Integer, Entry> entries = new HashMap<>(Math.min(count, 65_536));
                    for (int i = 0; i < count; i++) {
                        int frequency = ByteBufCodecs.INT.decode(input);
                        if (entries.put(frequency, Entry.STREAM_CODEC.decode(input)) != null) {
                            throw new DecoderException(
                                    "Duplicate satellite frequency: " + frequency);
                        }
                    }
                    return entries;
                }

                @Override
                public void encode(ByteBuf output, Map<Integer, Entry> entries) {
                    ByteBufCodecs.VAR_INT.encode(output, entries.size());
                    entries.forEach(
                            (frequency, entry) -> {
                                ByteBufCodecs.INT.encode(output, frequency);
                                Entry.STREAM_CODEC.encode(output, entry);
                            });
                }
            };
    public static final StreamCodec<ByteBuf, Map<ResourceKey<Level>, Map<Integer, Entry>>>
            STREAM_CODEC =
                    new StreamCodec<>() {
                        @Override
                        public Map<ResourceKey<Level>, Map<Integer, Entry>> decode(ByteBuf input) {
                            int count = readCount(input, 1);
                            Map<ResourceKey<Level>, Map<Integer, Entry>> result =
                                    new HashMap<>(Math.min(count, 65_536));
                            for (int i = 0; i < count; i++) {
                                ResourceKey<Level> dimension = DIMENSION_CODEC.decode(input);
                                if (result.put(dimension, BY_FREQUENCY.decode(input)) != null) {
                                    throw new DecoderException(
                                            "Duplicate satellite dimension: " + dimension);
                                }
                            }
                            return result;
                        }

                        @Override
                        public void encode(
                                ByteBuf output,
                                Map<ResourceKey<Level>, Map<Integer, Entry>> satellites) {
                            ByteBufCodecs.VAR_INT.encode(output, satellites.size());
                            satellites.forEach(
                                    (dimension, entries) -> {
                                        DIMENSION_CODEC.encode(output, dimension);
                                        BY_FREQUENCY.encode(output, entries);
                                    });
                        }
                    };

    private ClientSatellites() {}

    public static Map<Integer, Entry> of(ResourceKey<Level> level) {
        return byLevel.getOrDefault(level, Map.of());
    }

    public static Map<Integer, Entry> current(Level level) {
        if (!CelestialBody.inOrbit(level)) return of(level.dimension());
        CelestialBody body = OrbitalStation.clientStation.orbiting;
        return body == null || body.dimension == null ? Map.of() : of(body.dimension);
    }

    public static void replace(Map<ResourceKey<Level>, Map<Integer, Entry>> satellites) {
        byLevel = satellites;
    }

    private static int readCount(ByteBuf input, int minimumBytesPerEntry) {
        int count = ByteBufCodecs.VAR_INT.decode(input);
        if (count < 0 || count > input.readableBytes() / minimumBytesPerEntry) {
            throw new DecoderException("Invalid satellite map count: " + count);
        }
        return count;
    }

    public record Entry(SatelliteType type, SatelliteOrbit orbit) {

        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC =
                StreamCodec.composite(
                        SatelliteType.STREAM_CODEC,
                        Entry::type,
                        SatelliteOrbit.STREAM_CODEC,
                        Entry::orbit,
                        Entry::new);
    }
}
