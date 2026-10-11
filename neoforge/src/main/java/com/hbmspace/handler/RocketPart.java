// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.google.common.base.Suppliers;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbm.items.weapon.ItemCustomMissilePart.WarheadType;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.registration.RegistryHandle;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.items.SkyItemsSpace;
import com.hbmspace.items.WarItemsSpace;
import com.hbmspace.items.weapon.MissileKindsSpace;
import com.hbmspace.items.weapon.MissilePartsSpace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class RocketPart {

    public static final Codec<RocketPart> CODEC =
            BuiltInRegistries.ITEM
                    .byNameCodec()
                    .comapFlatMap(
                            item -> {
                                RocketPart part = of(item);
                                return part != null
                                        ? DataResult.success(part)
                                        : DataResult.error(
                                                () ->
                                                        BuiltInRegistries.ITEM.getKey(item)
                                                                + " is no rocket part");
                            },
                            part -> part.item);
    public static final StreamCodec<RegistryFriendlyByteBuf, RocketPart> STREAM_CODEC =
            ByteBufCodecs.registry(Registries.ITEM).map(RocketPart::require, part -> part.item);

    private static final Supplier<Map<Item, RocketPart>> PARTS =
            Suppliers.memoize(RocketPart::build);

    public final Item item;
    public final PartType type;
    public final double height;
    public final int mass;
    public final PartSize top;
    public final PartSize bottom;
    public final @Nullable WarheadType warhead;
    public final @Nullable FuelType fuel;
    public final int tank;
    public final int thrust;
    public final int isp;

    private RocketPart(
            Item item,
            PartType type,
            double height,
            int mass,
            PartSize top,
            PartSize bottom,
            @Nullable WarheadType warhead,
            @Nullable FuelType fuel,
            int tank,
            int thrust,
            int isp) {
        this.item = item;
        this.type = type;
        this.height = height;
        this.mass = mass;
        this.top = top;
        this.bottom = bottom;
        this.warhead = warhead;
        this.fuel = fuel;
        this.tank = tank;
        this.thrust = thrust;
        this.isp = isp;
    }

    public static @Nullable RocketPart of(Item item) {
        return PARTS.get().get(item);
    }

    public static @Nullable RocketPart of(ItemStack stack) {
        return stack.isEmpty() ? null : of(stack.getItem());
    }

    private static RocketPart require(Item item) {
        RocketPart part = of(item);
        if (part == null)
            throw new IllegalArgumentException(
                    BuiltInRegistries.ITEM.getKey(item) + " is no rocket part");
        return part;
    }

    public boolean isPayload() {
        return warhead == MissileKindsSpace.APOLLO || warhead == MissileKindsSpace.SATELLITE;
    }

    public @Nullable Fluid getFuel() {
        if (fuel == FuelType.KEROSENE || fuel == MissileKindsSpace.KEROLOX)
            return NTMFluids.KEROSENE;
        if (fuel == MissileKindsSpace.METHALOX) return NTMFluids.GAS;
        if (fuel == FuelType.HYDROGEN) return NTMFluids.HYDROGEN;
        if (fuel == FuelType.XENON) return NTMFluids.XENON;
        if (fuel == FuelType.BALEFIRE) return NTMFluids.BALEFIRE_FUEL;
        if (fuel == MissileKindsSpace.HYDRAZINE) return FluidsSpace.hydrazine();
        if (fuel == FuelType.SOLID) return NTMFluids.NONE;
        return null;
    }

    public @Nullable Fluid getOxidizer() {
        if (fuel == MissileKindsSpace.KEROLOX
                || fuel == FuelType.HYDROGEN
                || fuel == MissileKindsSpace.METHALOX) {
            return NTMFluids.OXYGEN;
        }
        if (fuel == FuelType.KEROSENE || fuel == FuelType.BALEFIRE) return NTMFluids.PEROXIDE;
        return null;
    }

    public static double radius(PartSize size) {
        if (size == PartSize.SIZE_10) return 1.0;
        if (size == PartSize.SIZE_15) return 1.5;
        if (size == PartSize.SIZE_20) return 2.0;
        if (size == MissileKindsSpace.SIZE_25) return 2.5;
        if (size == MissileKindsSpace.SIZE_30) return 3.0;
        return 0;
    }

    private static Map<Item, RocketPart> build() {
        Map<Item, Stats> thrusters = new IdentityHashMap<>();
        thrust(thrusters, ModItems.MP_THRUSTER_10_KEROSENE, 16_000, 180, 308);
        thrust(thrusters, ModItems.MP_THRUSTER_10_SOLID, 60_000, 400, 195);
        thrust(thrusters, ModItems.MP_THRUSTER_10_XENON, 2_000, 200, 4200);
        thrust(thrusters, ModItems.MP_THRUSTER_15_KEROSENE, 120_000, 900, 308);
        thrust(thrusters, ModItems.MP_THRUSTER_15_KEROSENE_DUAL, 200_000, 1600, 308);
        thrust(thrusters, ModItems.MP_THRUSTER_15_KEROSENE_TRIPLE, 280_000, 2200, 308);
        thrust(thrusters, ModItems.MP_THRUSTER_15_SOLID, 220_000, 1000, 195);
        thrust(thrusters, ModItems.MP_THRUSTER_15_SOLID_HEXDECUPLE, 260_000, 1200, 195);
        thrust(thrusters, ModItems.MP_THRUSTER_15_HYDROGEN, 100_000, 800, 380);
        thrust(thrusters, ModItems.MP_THRUSTER_15_HYDROGEN_DUAL, 200_000, 1400, 380);
        thrust(thrusters, ModItems.MP_THRUSTER_15_BALEFIRE_SHORT, 800_000, 1600, 666);
        thrust(thrusters, ModItems.MP_THRUSTER_15_BALEFIRE, 1_000_000, 2000, 666);
        thrust(thrusters, ModItems.MP_THRUSTER_15_BALEFIRE_LARGE, 1_200_000, 2400, 666);
        thrust(thrusters, ModItems.MP_THRUSTER_15_BALEFIRE_LARGE_RAD, 1_200_000, 2400, 666);
        thrust(thrusters, ModItems.MP_THRUSTER_20_KEROSENE, 1_536_000, 2400, 308);
        thrust(thrusters, ModItems.MP_THRUSTER_20_KEROSENE_DUAL, 1_934_000, 3200, 308);
        thrust(thrusters, ModItems.MP_THRUSTER_20_KEROSENE_TRIPLE, 2_542_000, 4400, 308);
        thrust(thrusters, MissilePartsSpace.MP_THRUSTER_20_METHALOX, 890_000, 2400, 320);
        thrust(thrusters, MissilePartsSpace.MP_THRUSTER_20_METHALOX_DUAL, 1_184_000, 3200, 320);
        thrust(thrusters, MissilePartsSpace.MP_THRUSTER_20_METHALOX_TRIPLE, 1_456_000, 4400, 320);
        thrust(thrusters, MissilePartsSpace.MP_THRUSTER_20_HYDROGEN, 480_000, 2600, 380);
        thrust(thrusters, MissilePartsSpace.MP_THRUSTER_20_HYDROGEN_DUAL, 640_000, 3400, 380);
        thrust(thrusters, MissilePartsSpace.MP_THRUSTER_20_HYDROGEN_TRIPLE, 938_000, 4500, 380);
        thrust(thrusters, ModItems.MP_THRUSTER_20_SOLID, 1_400_000, 2400, 195);
        thrust(thrusters, ModItems.MP_THRUSTER_20_SOLID_MULTI, 1_830_000, 3200, 195);
        thrust(thrusters, ModItems.MP_THRUSTER_20_SOLID_MULTIER, 2_320_000, 4000, 195);
        thrust(thrusters, MissilePartsSpace.MP_THRUSTER_20_HYDRAZINE, 600_000, 1200, 220);
        thrust(thrusters, MissilePartsSpace.MP_THRUSTER_10_HYDRAZINE, 14_000, 120, 220);

        Map<Item, Integer> masses = new IdentityHashMap<>();
        masses.put(MissilePartsSpace.RP_FUSELAGE_20_12.get(), 4000);
        masses.put(MissilePartsSpace.RP_FUSELAGE_20_6.get(), 2100);
        masses.put(MissilePartsSpace.RP_FUSELAGE_20_3.get(), 1200);
        masses.put(MissilePartsSpace.RP_FUSELAGE_20_1.get(), 500);
        masses.put(MissilePartsSpace.RP_CAPSULE_20.get(), 8_000);
        masses.put(MissilePartsSpace.RP_STATION_CORE_20.get(), 64_000);
        masses.put(MissilePartsSpace.RP_POD_20.get(), 4_000);

        Map<Item, RocketPart> parts = new IdentityHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof ItemCustomMissilePart part) || part.mesh == null) continue;

            double height = Double.parseDouble(Float.toString(part.height));
            Stats stats = thrusters.get(item);
            if (part.type == PartType.THRUSTER && stats == null) {
                throw new IllegalStateException(
                        BuiltInRegistries.ITEM.getKey(item) + " states no rocket thrust");
            }
            int mass =
                    switch (part.type) {
                        case THRUSTER -> stats.mass;
                        case FUSELAGE -> masses.getOrDefault(item, 1000);
                        case WARHEAD ->
                                masses.getOrDefault(
                                        item, Math.round((Float) part.attributes[2] * 1000));
                        default -> 0;
                    };
            parts.put(
                    item,
                    new RocketPart(
                            item,
                            part.type,
                            height,
                            mass,
                            part.top,
                            part.bottom,
                            part.warheadType(),
                            part.fuelType(),
                            (int) part.fuelAmount(),
                            stats == null ? 0 : stats.thrust,
                            stats == null ? 0 : stats.isp));
        }

        payload(parts, SatelliteType.MAPPER, 16_000);
        payload(parts, SatelliteType.SCANNER, 16_000);
        payload(parts, SatelliteType.RADAR, 16_000);
        payload(parts, SatelliteType.DEATH_RAY, 48_000);
        payload(parts, SatelliteType.RELAY, 20_000);
        payload(parts, SatelliteType.RESONATOR, 20_000);
        payload(parts, SatelliteType.MINER, 64_000);
        payload(parts, SatelliteType.LUNAR_MINER, 64_000);
        payload(parts, SatelliteType.HORIZONS, 128_000);
        payload(parts, SatelliteType.PRECISION_LASER, 42_000);
        payload(parts, SatelliteType.DETECTOR, 24_000);
        payload(parts, SatelliteType.RAY_SCAN, 34_000);
        payload(parts, SatelliteType.SCIENCE, 16_000);
        payload(parts, ModItems.SATELLITE_SCIENCE_ASSEMBLER.get(), 16_000);
        payload(parts, ModItems.SATELLITE_SCIENCE_SENSOR.get(), 16_000);
        payload(parts, WarItemsSpace.WAR, 128_000);
        payload(parts, SkyItemsSpace.DYSON_RELAY, 32_000);

        return Map.copyOf(parts);
    }

    private static void thrust(
            Map<Item, Stats> thrusters,
            RegistryHandle<? extends Item> handle,
            int thrust,
            int mass,
            int isp) {
        thrusters.put(handle.get(), new Stats(thrust, mass, isp));
    }

    private static void payload(Map<Item, RocketPart> parts, SatelliteType type, int mass) {
        payload(parts, type.item(), mass);
    }

    private static void payload(Map<Item, RocketPart> parts, Item item, int mass) {
        parts.put(
                item,
                new RocketPart(
                        item,
                        PartType.WARHEAD,
                        7,
                        mass,
                        PartSize.NONE,
                        PartSize.SIZE_20,
                        MissileKindsSpace.SATELLITE,
                        null,
                        0,
                        0,
                        0));
    }

    private record Stats(int thrust, int mass, int isp) {}
}
