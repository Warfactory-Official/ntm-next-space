// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import com.hbmspace.dim.CelestialBody;
import io.netty.buffer.ByteBuf;
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
        buf.writeInt(swarms.size() * 2);
        for (Map.Entry<Integer, Swarm> entry : swarms.entrySet()) {
            buf.writeShort(entry.getKey());
            buf.writeInt(entry.getValue().members);
        }
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        int count = buf.readInt();
        swarms = new HashMap<>();
        for (int i = 0; i < count; i += 2) {
            int id = buf.readShort();
            int members = buf.readInt();
            swarms.put(id, new Swarm(members));
        }
    }
}
