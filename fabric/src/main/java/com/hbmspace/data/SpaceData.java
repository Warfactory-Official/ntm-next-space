// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.data;

import com.hbm.data.DataGroup;
import com.hbm.data.DataGroups;
import com.hbm.registration.FabricRegistrar;
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

    private static final DataGroups.Group OREGEN = group("oregen");
    public static final DataGroups.Param<Integer> DUNA_OIL_SPAWN =
            OREGEN.positive("duna_oil_spawn", 100);
    public static final DataGroups.Param<Integer> EVE_GAS_SPAWN =
            OREGEN.positive("eve_gas_spawn", 100);
    public static final DataGroups.Param<Integer> LAYTHE_OIL_SPAWN =
            OREGEN.positive("laythe_oil_spawn", 100);
    public static final DataGroups.Param<Integer> MUN_BRINE_SPAWN =
            OREGEN.positive("mun_brine_spawn", 100);
    public static final DataGroups.Param<Integer> MINMUS_BRINE_SPAWN =
            OREGEN.positive("minmus_brine_spawn", 100);
    public static final DataGroups.Param<Integer> IKE_BRINE_SPAWN =
            OREGEN.positive("ike_brine_spawn", 100);
    public static final DataGroups.Param<Integer> TEKTO_OIL_SPAWN =
            OREGEN.positive("tekto_oil_spawn", 100);
    public static final DataGroups.Param<Integer> TEKTO_BEDROCK_OIL_SPAWN =
            OREGEN.positive("tekto_bedrock_oil_spawn", 200);

    public static final DataGroups.Param<Integer> URANIUM_SPAWN =
            OREGEN.integer("uranium_spawn", 7);
    public static final DataGroups.Param<Integer> TITANIUM_SPAWN =
            OREGEN.integer("titanium_spawn", 8);
    public static final DataGroups.Param<Integer> SULFUR_SPAWN = OREGEN.integer("sulfur_spawn", 5);
    public static final DataGroups.Param<Integer> ALUMINIUM_SPAWN =
            OREGEN.integer("aluminium_spawn", 7);
    public static final DataGroups.Param<Integer> COPPER_SPAWN = OREGEN.integer("copper_spawn", 12);
    public static final DataGroups.Param<Integer> FLUORITE_SPAWN =
            OREGEN.integer("fluorite_spawn", 6);
    public static final DataGroups.Param<Integer> NITER_SPAWN = OREGEN.integer("niter_spawn", 6);
    public static final DataGroups.Param<Integer> TUNGSTEN_SPAWN =
            OREGEN.integer("tungsten_spawn", 10);
    public static final DataGroups.Param<Integer> LEAD_SPAWN = OREGEN.integer("lead_spawn", 6);
    public static final DataGroups.Param<Integer> BERYLLIUM_SPAWN =
            OREGEN.integer("beryllium_spawn", 6);
    public static final DataGroups.Param<Integer> THORIUM_SPAWN =
            OREGEN.integer("thorium_spawn", 7);
    public static final DataGroups.Param<Integer> ASBESTOS_SPAWN =
            OREGEN.integer("asbestos_spawn", 2);
    public static final DataGroups.Param<Integer> LITHIUM_SPAWN =
            OREGEN.integer("lithium_spawn", 6);
    public static final DataGroups.Param<Integer> RARE_SPAWN = OREGEN.integer("rare_spawn", 6);
    public static final DataGroups.Param<Integer> CINNEBAR_SPAWN =
            OREGEN.integer("cinnebar_spawn", 1);
    public static final DataGroups.Param<Integer> COBALT_SPAWN = OREGEN.integer("cobalt_spawn", 2);
    public static final DataGroups.Param<Integer> ALEXANDRITE_SPAWN =
            OREGEN.integer("alexandrite_spawn", 100);
    public static final DataGroups.Param<Integer> NICKEL_SPAWN = OREGEN.integer("nickel_spawn", 12);
    public static final DataGroups.Param<Integer> ZINC_SPAWN = OREGEN.integer("zinc_spawn", 8);
    public static final DataGroups.Param<Integer> MINERAL_SPAWN =
            OREGEN.integer("mineral_spawn", 4);
    public static final DataGroups.Param<Integer> IRON_CLUSTER_SPAWN =
            OREGEN.integer("iron_cluster_spawn", 4);
    public static final DataGroups.Param<Integer> TITANIUM_CLUSTER_SPAWN =
            OREGEN.integer("titanium_cluster_spawn", 2);
    public static final DataGroups.Param<Integer> ALUMINIUM_CLUSTER_SPAWN =
            OREGEN.integer("aluminium_cluster_spawn", 3);
    public static final DataGroups.Param<Integer> COPPER_CLUSTER_SPAWN =
            OREGEN.integer("copper_cluster_spawn", 4);
    public static final DataGroups.Param<Integer> LIMESTONE_SPAWN =
            OREGEN.integer("limestone_spawn", 1);
    public static final DataGroups.Param<Integer> NETHER_PHOSPHORUS_SPAWN =
            OREGEN.integer("nether_phosphorus_spawn", 24);
    public static final DataGroups.Param<Integer> COLTAN_RATE = OREGEN.integer("coltan_rate", 2);
    public static final DataGroups.Param<Integer> DRES_IRON_SPAWN =
            OREGEN.integer("dres_iron_spawn", 12);

    public static final int IRON_SPAWN = 9;

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

    static {
        FabricRegistrar.registerSyncedDataPackRegistry(REGISTRY, DataGroup.CODEC);
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
