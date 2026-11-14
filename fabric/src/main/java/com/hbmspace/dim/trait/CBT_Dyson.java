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

package com.hbmspace.dim.trait;

import com.hbmspace.dim.CelestialBody;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class CBT_Dyson extends CelestialBodyTrait {
    private HashMap<Integer, Swarm> swarms = new HashMap<>();

    private static class Swarm {

        int members;
        int consumers;

        private int addedConsumers;

        Swarm(int members) {
            this.members = members;
        }
    }

    public static void launch(Level level, int id) {
        launch(level, id, 1);
    }

    public static void launch(Level level, int id, int amount) {
        CelestialBody star = CelestialBody.getStar(level);
        CBT_Dyson dyson = star.getTrait(CBT_Dyson.class);
        if (dyson == null) dyson = new CBT_Dyson();

        Swarm swarm = dyson.swarms.get(id);
        if (swarm == null) {
            swarm = new Swarm(0);
            dyson.swarms.put(id, swarm);
        }

        swarm.members += amount;

        star.modifyTraits(dyson);
    }

    public static int count(Level level, int id) {
        CelestialBody star = CelestialBody.getStar(level);
        CBT_Dyson dyson = star.getTrait(CBT_Dyson.class);
        if (dyson == null) return 0;

        Swarm swarm = dyson.swarms.get(id);
        if (swarm == null) return 0;

        return swarm.members;
    }

    public static int consumers(Level level, int id) {
        CelestialBody star = CelestialBody.getStar(level);
        CBT_Dyson dyson = star.getTrait(CBT_Dyson.class);
        if (dyson == null) return 0;

        Swarm swarm = dyson.swarms.get(id);
        if (swarm == null) return 0;

        swarm.addedConsumers++;

        return swarm.consumers;
    }

    public int size() {
        int size = 0;
        for (Swarm swarm : swarms.values()) {
            size += swarm.members;
        }
        return size;
    }

    public void attenuate() {
        for (Swarm swarm : swarms.values()) {
            swarm.consumers = swarm.addedConsumers;
            swarm.addedConsumers = 0;

            if (swarm.members <= 0) continue;

            double decayChance = (double) size() / (1024 * 5 * 20);
            if (Math.random() < decayChance) swarm.members--;
        }
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        int[] swarmData = new int[swarms.size() * 2];
        int i = 0;
        for (Map.Entry<Integer, Swarm> entry : swarms.entrySet()) {
            swarmData[i] = entry.getKey();
            swarmData[i + 1] = entry.getValue().members;
            i += 2;
        }

        nbt.putIntArray("swarm", swarmData);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        int[] swarmData = nbt.getIntArray("swarm").orElse(new int[0]);
        swarms = new HashMap<>();
        for (int i = 0; i < swarmData.length; i += 2) {
            swarms.put(swarmData[i], new Swarm(swarmData[i + 1]));
        }
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeInt(swarms.size());
        for (Map.Entry<Integer, Swarm> entry : swarms.entrySet()) {
            buf.writeInt(entry.getKey());
            buf.writeInt(entry.getValue().members);
        }
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        int count = buf.readInt();
        if (count < 0 || count > buf.readableBytes() / (Integer.BYTES * 2)) {
            throw new DecoderException("Invalid Dyson swarm count: " + count);
        }
        swarms = new HashMap<>(Math.min(count, 65_536));
        for (int i = 0; i < count; i++) {
            int id = buf.readInt();
            int members = buf.readInt();
            swarms.put(id, new Swarm(members));
        }
    }
}
