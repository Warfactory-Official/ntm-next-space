/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 Bufka2011
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2024 HbmMods (The Bobcat)
 * SPDX-FileCopyrightText: 2025 RealSilverMoon
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 Bufka2011. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2024 HbmMods (The Bobcat). All rights reserved.
 * Copyright (C) 2025 RealSilverMoon. All rights reserved.
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

package com.hbmspace.items.weapon;

import com.hbm.blockentity.ordnance.launch.CompactLauncherBlockEntity.FuelAssociation;
import com.hbm.blockentity.ordnance.launch.CompactLauncherBlockEntity;
import com.hbm.handler.MissileStruct;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.CustomMissileItem;
import com.hbm.items.weapon.CustomMissilePartItem.FuelType;
import com.hbm.items.weapon.CustomMissilePartItem.PartSize;
import com.hbm.items.weapon.CustomMissilePartItem.PartType;
import com.hbm.items.weapon.CustomMissilePartItem;
import com.hbm.registration.Reg;
import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.handler.RocketPart;
import com.hbmspace.inventory.fluid.FluidsSpace;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import static com.hbm.items.weapon.CustomMissilePartItem.stat;

public final class MissileEditsSpace {
    public static final float KEROSENE_20_HEIGHT = 2F;
    public static final float KEROSENE_20_GUI_HEIGHT = 2F;

    private MissileEditsSpace() {}

    public static void install() {
        CustomMissilePartItem.DESCRIBE.install(HbmSpace.MOD_ID, MissileEditsSpace::describe);
        CustomMissileItem.DESCRIBE.install(HbmSpace.MOD_ID, MissileEditsSpace::describeMissile);
        CustomMissilePartItem.VERTEX_NORMAL_MESHES.install(
                HbmSpace.MOD_ID, MissilePartsSpace.VERTEX_NORMAL_MESHES);
        Reg.afterRegistration(
                ModItems.MP_THRUSTER_20_KEROSENE,
                part ->
                        part.setAssembly(
                                KEROSENE_20_HEIGHT, KEROSENE_20_GUI_HEIGHT, part.mesh, part.skin));
        CompactLauncherBlockEntity.registerFuelAssociation(
                new FuelAssociation(MissileKindsSpace.HYDRAZINE, FluidsSpace::hydrazine, null));

        for (RegistryHandle<? extends Item> handle : ModItems.MISSILE_ALL) {
            Registrar.modifyDefaultComponents(
                    handle,
                    components -> {
                        if (stacks(handle.get())) components.set(DataComponents.MAX_STACK_SIZE, 64);
                        if (handle.get() instanceof CustomMissilePartItem part && restyled(part)) {
                            components.set(DataComponents.ITEM_MODEL, restyle(handle.id()));
                        }
                    });
        }
    }

    public static boolean restyled(CustomMissilePartItem part) {
        return part.type == PartType.FUSELAGE || part == ModItems.MP_THRUSTER_20_KEROSENE.get();
    }

    public static Identifier restyle(Identifier ntm) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, ntm.getPath());
    }

    private static boolean stacks(Item item) {
        if (!(item instanceof CustomMissilePartItem part)) return false;
        return part.type == PartType.THRUSTER
                || part.type == PartType.FUSELAGE
                || item == ModItems.MP_WARHEAD_15_NUCLEAR_SHARK.get()
                || item == ModItems.MP_WARHEAD_15_NUCLEAR_MIMI.get();
    }

    static void describe(CustomMissilePartItem part, Consumer<Component> adder) {
        if (part == MissilePartsSpace.RP_POD_20.get()) return;

        if (part.title() != null) {
            adder.accept(
                    Component.literal("\"" + part.title() + "\"")
                            .withStyle(ChatFormatting.DARK_PURPLE));
        }

        RocketPart rocket = RocketPart.of(part);
        String mass = (rocket == null ? 0 : rocket.mass) + "kg";

        switch (part.type) {
            case CHIP ->
                    adder.accept(
                            stat("inaccuracy", Component.literal(part.inaccuracy() * 100 + "%")));
            case WARHEAD -> {
                adder.accept(stat("size", part.bottom.getDisplay()));
                adder.accept(stat("type", part.warheadType().getDisplay()));
                if (part.warheadType() != MissileKindsSpace.APOLLO
                        && part.warheadType() != MissileKindsSpace.SATELLITE) {
                    adder.accept(
                            stat(
                                    "strength",
                                    Component.literal(String.valueOf(part.warheadStrength()))));
                }
                adder.accept(space("mass", mass));
            }
            case FUSELAGE -> {
                adder.accept(stat("topSize", part.top.getDisplay()));
                adder.accept(stat("bottomSize", part.bottom.getDisplay()));
                adder.accept(stat("fuelType", fuelName(part.fuelType())));
                adder.accept(stat("fuelAmount", Component.literal((int) part.fuelAmount() + "mB")));
                adder.accept(space("mass", mass));
            }
            case FINS -> {
                adder.accept(stat("size", part.top.getDisplay()));
                adder.accept(stat("inaccuracy", Component.literal(part.inaccuracy() * 100 + "%")));
            }
            case THRUSTER -> {
                adder.accept(stat("size", part.top.getDisplay()));
                adder.accept(stat("fuelType", fuelName(part.fuelType())));
                adder.accept(
                        stat("fuelConsumption", Component.literal(part.consumption() + "l/tick")));
                adder.accept(
                        stat(
                                "maxPayload",
                                Component.literal((Float) part.attributes[2] * 1000 + "kg")));
                adder.accept(space("thrust", (rocket == null ? 0 : rocket.thrust) + "N"));
                adder.accept(space("isp", (rocket == null ? 0 : rocket.isp) + "s"));
                adder.accept(space("mass", mass));
            }
        }

        if (part.rarity != null) adder.accept(stat("rarity", part.rarity.getDisplay()));
        if (part.author() != null) {
            adder.accept(
                    Component.translatable("item.hbm.missile.part.by")
                            .append(" " + part.author())
                            .withStyle(ChatFormatting.WHITE));
        }
        if (part.witty() != null) {
            adder.accept(
                    Component.literal("\"" + part.witty() + "\"")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
    }

    static void describeMissile(
            MissileStruct parts, @Nullable CustomMissilePartItem chip, Consumer<Component> adder) {
        CustomMissilePartItem warhead = parts.warhead();
        CustomMissilePartItem fuselage = parts.fuselage();
        if (warhead != null) {
            adder.accept(CustomMissileItem.stat("warhead", warhead.warheadType().getDisplay()));
            adder.accept(
                    CustomMissileItem.stat(
                            "strength",
                            Component.literal(String.valueOf(warhead.warheadStrength()))));
        }
        if (fuselage != null) {
            adder.accept(CustomMissileItem.stat("fuelType", fuelName(fuselage.fuelType())));
            adder.accept(
                    CustomMissileItem.stat(
                            "fuelAmount", Component.literal((int) fuselage.fuelAmount() + "l")));
        }

        adder.accept(
                CustomMissileItem.stat(
                        "chipInaccuracy",
                        Component.literal((chip != null ? chip.inaccuracy() * 100 : 100F) + "%")));
        adder.accept(
                CustomMissileItem.stat(
                        "finInaccuracy",
                        Component.literal(
                                parts.fins() != null
                                        ? parts.fins().inaccuracy() * 100 + "%"
                                        : "100%")));
        if (fuselage != null) {
            adder.accept(
                    CustomMissileItem.stat(
                            "size",
                            fuselage.top
                                    .getDisplay()
                                    .copy()
                                    .append("/")
                                    .append(fuselage.bottom.getDisplay())));
        }

        float health = 0;
        if (warhead != null) health += warhead.health;
        if (fuselage != null) health += fuselage.health;
        if (parts.thruster() != null) health += parts.thruster().health;
        if (parts.fins() != null) health += parts.fins().health;
        adder.accept(CustomMissileItem.stat("health", Component.literal(health + "HP")));
    }

    public static void describeSatellite(int mass, Consumer<Component> adder) {
        adder.accept(stat("size", PartSize.SIZE_20.getDisplay()));
        adder.accept(stat("type", MissileKindsSpace.SATELLITE.getDisplay()));
        adder.accept(space("mass", mass + "kg"));
    }

    private static Component fuelName(FuelType fuel) {
        ChatFormatting color;
        if (fuel == FuelType.KEROSENE) color = ChatFormatting.LIGHT_PURPLE;
        else if (fuel == FuelType.SOLID) color = ChatFormatting.GOLD;
        else if (fuel == FuelType.HYDROGEN) color = ChatFormatting.DARK_AQUA;
        else if (fuel == FuelType.XENON) color = ChatFormatting.DARK_PURPLE;
        else if (fuel == FuelType.BALEFIRE) color = ChatFormatting.GREEN;
        else return fuel.getDisplay();
        return Component.translatable(
                        "item.hbmspace.custom_missile_part.fuel."
                                + fuel.name().toLowerCase(Locale.ROOT))
                .withStyle(color);
    }

    private static Component space(String key, String value) {
        return Component.translatable("item.hbmspace.missile.part." + key)
                .withStyle(ChatFormatting.BOLD)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value));
    }
}
