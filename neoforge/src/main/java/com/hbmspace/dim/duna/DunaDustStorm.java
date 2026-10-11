// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna;

import com.hbm.packet.PacketWire;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.SpaceDimensions;
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

    public static void sync(ServerPlayer player, ServerLevel level) {
        if (level.dimension() == SpaceDimensions.DUNA) {
            PacketWire.sendTo(new DunaStormSyncPayload(get(level).intensity), player);
        }
    }

    static void broadcast(ServerLevel duna) {
        PacketWire.sendToDimension(new DunaStormSyncPayload(get(duna).intensity), duna);
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
