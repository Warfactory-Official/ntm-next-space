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

package com.hbmspace.data;

import com.hbm.data.DataGroup;
import com.hbm.data.DataGroups;
import com.hbm.registration.Registrar;
import com.hbmspace.HbmSpace;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class SpaceData {

    public static final ResourceKey<Registry<DataGroup>> REGISTRY =
            ResourceKey.createRegistryKey(
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "space_config"));

    private static final List<DataGroups.Group> GROUPS = new ArrayList<>();

    private static final DataGroups.Group TRAVEL = group("travel");
    public static final DataGroups.Param<Boolean> ALLOW_NETHER_PORTALS =
            TRAVEL.bool("allow_nether_portals", false);
    public static final DataGroups.Param<Integer> MAX_PROBE_DISTANCE =
            TRAVEL.bounded("max_probe_distance", 32_000, 1, Integer.MAX_VALUE / 2);
    public static final DataGroups.Param<Integer> MAX_STATION_DISTANCE =
            TRAVEL.bounded("max_station_distance", 32_000, 1024, Integer.MAX_VALUE);
    public static final DataGroups.Param<Boolean> COMBAT_POD_DESPAWN =
            TRAVEL.bool("combat_pod_despawn", false);

    private static final DataGroups.Group WORLDGEN = group("worldgen");
    public static final DataGroups.Param<Boolean> ENABLE_VOLCANO_GEN =
            WORLDGEN.bool("enable_volcano_gen", true);

    private static final DataGroups.Group MOBS = group("mobs");
    public static final DataGroups.Param<Boolean> ENABLE_MUN_COWS =
            MOBS.bool("enable_mun_cows", true);
    public static final DataGroups.Param<Boolean> SERVER_SAFETY = MOBS.bool("server_safety", false);

    private static final DataGroups.Group RESERVES = group("reserves");
    public static final DataGroups.Param<Integer> BEDROCK_OIL_PER_DEPOSIT =
            RESERVES.integer("bedrock_oil_per_deposit", 100);
    public static final DataGroups.Param<Integer> BEDROCK_GAS_MIN =
            RESERVES.integer("bedrock_gas_per_deposit_min", 10);
    public static final DataGroups.Param<Integer> BEDROCK_GAS_MAX =
            RESERVES.integer("bedrock_gas_per_deposit_max", 50);
    public static final DataGroups.Param<Integer> EARTH_OIL_PER_DEPOSIT =
            RESERVES.integer("earth_oil_per_deposit", 500);
    public static final DataGroups.Param<Integer> EARTH_GAS_MIN =
            RESERVES.integer("earth_gas_per_deposit_min", 100);
    public static final DataGroups.Param<Integer> EARTH_GAS_MAX =
            RESERVES.integer("earth_gas_per_deposit_max", 500);
    public static final DataGroups.Param<Double> EARTH_OIL_DRAIN =
            RESERVES.chance("earth_oil_drain_chance", 0.05D);
    public static final DataGroups.Param<Integer> DUNA_OIL_PER_DEPOSIT =
            RESERVES.integer("duna_oil_per_deposit", 200);
    public static final DataGroups.Param<Integer> DUNA_GAS_MIN =
            RESERVES.integer("duna_gas_per_deposit_min", 100);
    public static final DataGroups.Param<Integer> DUNA_GAS_MAX =
            RESERVES.integer("duna_gas_per_deposit_max", 500);
    public static final DataGroups.Param<Double> DUNA_OIL_DRAIN =
            RESERVES.chance("duna_oil_drain_chance", 0.1D);
    public static final DataGroups.Param<Integer> LAYTHE_OIL_PER_DEPOSIT =
            RESERVES.integer("laythe_oil_per_deposit", 500);
    public static final DataGroups.Param<Integer> LAYTHE_GAS_MIN =
            RESERVES.integer("laythe_gas_per_deposit_min", 100);
    public static final DataGroups.Param<Integer> LAYTHE_GAS_MAX =
            RESERVES.integer("laythe_gas_per_deposit_max", 500);
    public static final DataGroups.Param<Double> LAYTHE_OIL_DRAIN =
            RESERVES.chance("laythe_oil_drain_chance", 0.05D);
    public static final DataGroups.Param<Integer> TEKTO_OIL_PER_DEPOSIT =
            RESERVES.integer("tekto_oil_per_deposit", 500);
    public static final DataGroups.Param<Integer> TEKTO_GAS_MIN =
            RESERVES.integer("tekto_gas_per_deposit_min", 100);
    public static final DataGroups.Param<Integer> TEKTO_GAS_MAX =
            RESERVES.integer("tekto_gas_per_deposit_max", 500);
    public static final DataGroups.Param<Double> TEKTO_OIL_DRAIN =
            RESERVES.chance("tekto_oil_drain_chance", 0.05D);
    public static final DataGroups.Param<Integer> TEKTO_BEDROCK_OIL_PER_DEPOSIT =
            RESERVES.integer("tekto_bedrock_oil_per_deposit", 100);
    public static final DataGroups.Param<Integer> TEKTO_BEDROCK_GAS_MIN =
            RESERVES.integer("tekto_bedrock_gas_per_deposit_min", 10);
    public static final DataGroups.Param<Integer> TEKTO_BEDROCK_GAS_MAX =
            RESERVES.integer("tekto_bedrock_gas_per_deposit_max", 50);
    public static final DataGroups.Param<Integer> EVE_GAS_PER_DEPOSIT =
            RESERVES.integer("eve_gas_per_deposit", 500);
    public static final DataGroups.Param<Integer> EVE_PET_MIN =
            RESERVES.integer("eve_petroleum_per_deposit_min", 20);
    public static final DataGroups.Param<Integer> EVE_PET_MAX =
            RESERVES.integer("eve_petroleum_per_deposit_max", 100);
    public static final DataGroups.Param<Double> EVE_GAS_DRAIN =
            RESERVES.chance("eve_gas_drain_chance", 0.05D);
    public static final DataGroups.Param<Integer> MUN_BRINE_PER_DEPOSIT =
            RESERVES.integer("mun_brine_per_deposit", 300);
    public static final DataGroups.Param<Double> MUN_BRINE_DRAIN =
            RESERVES.chance("mun_brine_drain_chance", 0.05D);
    public static final DataGroups.Param<Integer> MINMUS_BRINE_PER_DEPOSIT =
            RESERVES.integer("minmus_brine_per_deposit", 300);
    public static final DataGroups.Param<Double> MINMUS_BRINE_DRAIN =
            RESERVES.chance("minmus_brine_drain_chance", 0.05D);
    public static final DataGroups.Param<Integer> IKE_BRINE_PER_DEPOSIT =
            RESERVES.integer("ike_brine_per_deposit", 300);
    public static final DataGroups.Param<Double> IKE_BRINE_DRAIN =
            RESERVES.chance("ike_brine_drain_chance", 0.05D);

    private static final DataGroups.Group RADIATION = group("radiation");
    public static final DataGroups.Param<Double> CELESTIAL_RAD_MULTIPLIER =
            RADIATION.real("celestial_rad_multiplier", 0.25D);

    private static final DataGroups.Group RADIATOR = group("radiator");
    public static final DataGroups.Param<Integer> RADIATOR_INPUT_TANK_SIZE =
            RADIATOR.integer("input_tank_capacity", 500);
    public static final DataGroups.Param<Integer> RADIATOR_OUTPUT_TANK_SIZE =
            RADIATOR.integer("output_tank_capacity", 500);

    private static final DataGroups.Group MACHINES = group("machines");
    public static final DataGroups.Param<Boolean> UNAVOIDABLE_GRAVITY =
            MACHINES.bool("unavoidable_gravity", true);

    static {
        Registrar.registerSyncedDataPackRegistry(REGISTRY, DataGroup.CODEC);
        DataGroups.addon(SpaceData::applyDataPack);
    }

    private SpaceData() {}

    private static DataGroups.Group group(String name) {
        return DataGroups.group(GROUPS, Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name));
    }

    public static List<DataGroups.Group> groups() {
        return List.copyOf(GROUPS);
    }

    public static void applyDataPack(RegistryAccess registries) {
        DataGroups.apply(registries, REGISTRY, GROUPS);
    }
}
