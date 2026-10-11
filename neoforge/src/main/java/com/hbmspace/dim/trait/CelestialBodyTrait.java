// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2026 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hbmspace.dim.CelestialBody;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;

public abstract class CelestialBodyTrait {

    public static class CBT_BATTLEFIELD extends CelestialBodyTrait {}

    public static final CBT_BATTLEFIELD BATTLE = new CBT_BATTLEFIELD();

    public static class CBT_COMPROMISED extends CelestialBodyTrait {}

    public static final CBT_COMPROMISED COMP = new CBT_COMPROMISED();

    public static final List<Class<? extends CelestialBodyTrait>> traitList = new ArrayList<>();
    public static final BiMap<String, Class<? extends CelestialBodyTrait>> traitMap =
            HashBiMap.create();
    private static final Map<
                    Class<? extends CelestialBodyTrait>, Supplier<? extends CelestialBodyTrait>>
            FACTORIES = new HashMap<>();

    static {
        registerTrait("atmosphere", CBT_Atmosphere.class, CBT_Atmosphere::new);
        registerTrait("temperature", CBT_Temperature.class, CBT_Temperature::new);
        registerTrait("bees", CBT_Bees.class, CBT_Bees::new);
        registerTrait("war", CBT_War.class, CBT_War::new);
        registerTrait("destroyed", CBT_Destroyed.class, CBT_Destroyed::new);
        registerTrait("water", CBT_Water.class, CBT_Water::new);
        registerTrait("weather", CBT_Weather.class, CBT_Weather::new);
        registerTrait("battle", CBT_BATTLEFIELD.class, CBT_BATTLEFIELD::new);
        registerTrait("infected", CBT_COMPROMISED.class, CBT_COMPROMISED::new);
        registerTrait("dyson", CBT_Dyson.class, CBT_Dyson::new);
        registerTrait("impact", CBT_Impact.class, CBT_Impact::new);
        registerTrait("lights", CBT_Lights.class, CBT_Lights::new);
        registerTrait("invasion", CBT_Invasion.class, CBT_Invasion::new);
    }

    private static <T extends CelestialBodyTrait> void registerTrait(
            String name, Class<T> clazz, Supplier<T> factory) {
        traitList.add(clazz);
        traitMap.put(name, clazz);
        FACTORIES.put(clazz, factory);
    }

    public static CelestialBodyTrait create(Class<? extends CelestialBodyTrait> clazz) {
        return FACTORIES.get(clazz).get();
    }

    public void readFromNBT(CompoundTag nbt) {}

    public void writeToNBT(CompoundTag nbt) {}

    public void readFromBytes(ByteBuf buf) {}

    public void writeToBytes(ByteBuf buf) {}

    public void update(boolean isRemote, CelestialBody body) {}
}
