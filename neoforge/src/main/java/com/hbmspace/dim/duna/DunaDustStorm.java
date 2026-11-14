/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

package com.hbmspace.dim.duna;

import com.hbm.packet.PacketWire;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.packet.layout.SpaceDunaStorm;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class DunaDustStorm extends SavedData {

    public static final Codec<DunaDustStorm> CODEC =
            RecordCodecBuilder.create(
                    i ->
                            i.group(
                                            Codec.INT
                                                    .fieldOf("dustStormTimer")
                                                    .forGetter(storm -> storm.timer),
                                            Codec.FLOAT
                                                    .fieldOf("dustStormIntensity")
                                                    .forGetter(storm -> storm.intensity))
                                    .apply(i, DunaDustStorm::new));
    public static final SavedDataType<DunaDustStorm> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "duna_dust_storm"),
                    DunaDustStorm::new,
                    CODEC,
                    null);

    public static float clientIntensity;

    public int timer;
    public float intensity = 1;

    public DunaDustStorm() {}

    private DunaDustStorm(int timer, float intensity) {
        this.timer = timer;
        this.intensity = intensity;
    }

    public static DunaDustStorm get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static void acceptSync(float intensity) {
        clientIntensity = intensity;
    }

    public static void sync(ServerPlayer player, ServerLevel level) {
        if (level.dimension() == SpaceDimensions.DUNA) {
            PacketWire.sendTo(SpaceDunaStorm.encode(get(level).intensity), player);
        }
    }

    static void broadcast(ServerLevel duna) {
        PacketWire.sendToDimension(SpaceDunaStorm.encode(get(duna).intensity), duna);
    }

    public boolean darkensDay() {
        return intensity >= 0.2F;
    }

    void tick(RandomSource rand) {
        if (timer <= 0) {
            if (intensity >= 0.05F) {
                intensity = 0;
                timer = rand.nextInt(168000) + 12000;
            } else {
                intensity = rand.nextFloat() * 0.75F + 0.25F;
                timer = rand.nextInt(12000) + 12000;
            }
        }

        timer--;
        setDirty();
    }

    void clear(RandomSource rand) {
        intensity = 0;
        timer = rand.nextInt(168000) + 12000;
        setDirty();
    }
}
