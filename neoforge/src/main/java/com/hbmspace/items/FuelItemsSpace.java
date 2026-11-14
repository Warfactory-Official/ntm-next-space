/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-FileCopyrightText: 2023 TheNewGuy
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023 JamesH_2. All rights reserved.
 * Copyright (C) 2023 TheNewGuy. All rights reserved.
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

package com.hbmspace.items;

import com.hbm.hazard.HazardData;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.modifier.HazardModifierRBMKHot;
import com.hbm.hazard.modifier.HazardModifierRBMKRadiation;
import com.hbm.inventory.material.Materials;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModItems;
import com.hbm.items.machine.FELCrystalItem;
import com.hbm.items.machine.HalfLifeUnit;
import com.hbm.items.machine.RBMKPelletItem;
import com.hbm.items.machine.RBMKRodItem.EnumBurnFunc;
import com.hbm.items.machine.RBMKRodItem;
import com.hbm.items.machine.RTGPelletDepletedItem;
import com.hbm.items.machine.RTGPelletItem;
import com.hbm.items.machine.WatzPelletItem;
import com.hbm.registration.ItemFamily;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.items.machine.RBMKFunctionsSpace;
import com.hbmspace.items.machine.WatzTypesSpace;
import com.hbmspace.items.machine.WavelengthsSpace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.hbm.hazard.HazardRegistry.HOT;
import static com.hbm.hazard.HazardRegistry.RADIATION;
import static com.hbm.hazard.HazardRegistry.mult_billet;
import static com.hbm.hazard.HazardRegistry.mult_rod_rbmk;
import static com.hbm.hazard.HazardRegistry.wst;

public final class FuelItemsSpace {
    public static final Reg.Handle<RTGPelletDepletedItem> PELLET_RTG_DEPLETED_AMERICIUM =
            HbmSpace.REG.item(
                    "pellet_rtg_depleted_americium",
                    RTGPelletDepletedItem::new,
                    () -> new Item.Properties().craftRemainder(ModItems.plate(Materials.IRON)));
    public static final Reg.Handle<RTGPelletItem> PELLET_RTG_BERKELIUM =
            HbmSpace.REG.item(
                    "pellet_rtg_berkelium",
                    props ->
                            new RTGPelletItem(props, 20)
                                    .setDecays(
                                            PELLET_RTG_DEPLETED_AMERICIUM,
                                            HalfLifeUnit.CENTURIES.lifespan(13.8F),
                                            1.5D),
                    () -> new Item.Properties().stacksTo(1));
    public static final Reg.Handle<RTGPelletItem> PELLET_RTG_CF251 =
            californium("pellet_rtg_cf251");
    public static final Reg.Handle<RTGPelletItem> PELLET_RTG_CF252 =
            californium("pellet_rtg_cf252");
    public static final Reg.Handle<FELCrystalItem> LASER_CRYSTAL_IRON =
            HbmSpace.REG.item(
                    "laser_crystal_iron",
                    props -> new FELCrystalItem(props, WavelengthsSpace.XRAY),
                    () -> new Item.Properties().stacksTo(1));
    public static final Reg.Handle<RBMKRodItem> RBMK_FUEL_BK247 =
            ModItems.rbmkFuel(
                    HbmSpace.REG,
                    "rbmk_fuel_bk247",
                    "Highly Enriched Berkelium-247",
                    rod ->
                            rod.setYield(100_000_000D)
                                    .setStats(50)
                                    .setFunction(EnumBurnFunc.LINEAR)
                                    .setHeat(2D)
                                    .setMeltingPoint(2993));
    public static final Reg.Handle<RBMKRodItem> RBMK_FUEL_LECM =
            curium(
                    "rbmk_fuel_lecm",
                    "Low Enriched Curium-245",
                    30_000_000D,
                    20,
                    5,
                    1.25D,
                    1340,
                    0.4D);
    public static final Reg.Handle<RBMKRodItem> RBMK_FUEL_MECM =
            curium(
                    "rbmk_fuel_mecm",
                    "Medium Enriched Curium-245",
                    27_000_000D,
                    30,
                    10,
                    1.25D,
                    1720,
                    0.3D);
    public static final Reg.Handle<RBMKRodItem> RBMK_FUEL_HECM =
            curium(
                    "rbmk_fuel_hecm",
                    "Highly Enriched Curium-245",
                    24_000_000D,
                    60,
                    25,
                    1.3D,
                    1880,
                    0.3D);
    public static final Reg.Handle<RBMKPelletItem> RBMK_PELLET_BK247 =
            ModItems.rbmkPellet(
                    HbmSpace.REG,
                    RBMK_FUEL_BK247,
                    "rbmk_pellet_bk247",
                    "Highly Enriched Berkelium-247",
                    false);
    public static final Reg.Handle<RBMKPelletItem> RBMK_PELLET_LECM =
            ModItems.rbmkPellet(
                    HbmSpace.REG,
                    RBMK_FUEL_LECM,
                    "rbmk_pellet_lecm",
                    "Low Enriched Curium-245",
                    false);
    public static final Reg.Handle<RBMKPelletItem> RBMK_PELLET_MECM =
            ModItems.rbmkPellet(
                    HbmSpace.REG,
                    RBMK_FUEL_MECM,
                    "rbmk_pellet_mecm",
                    "Medium Enriched Curium-245",
                    false);
    public static final Reg.Handle<RBMKPelletItem> RBMK_PELLET_HECM =
            ModItems.rbmkPellet(
                    HbmSpace.REG,
                    RBMK_FUEL_HECM,
                    "rbmk_pellet_hecm",
                    "Highly Enriched Curium-245",
                    false);
    public static final ItemFamily<WatzTypesSpace, WatzPelletItem> WATZ_PELLET =
            watzPellet("watz_pellet", false).joins(ModItems.WATZ_PELLET);
    public static final ItemFamily<WatzTypesSpace, WatzPelletItem> WATZ_PELLET_DEPLETED =
            watzPellet("watz_pellet_depleted", true).joins(ModItems.WATZ_PELLET_DEPLETED);

    static {
        after("pellet_rtg_americium", PELLET_RTG_BERKELIUM);
        after("pellet_rtg_lead", PELLET_RTG_CF251, PELLET_RTG_CF252);
        after("pellet_rtg_depleted_zirconium", PELLET_RTG_DEPLETED_AMERICIUM);
        after("laser_crystal_cmb", LASER_CRYSTAL_IRON);
        after("rbmk_fuel_hea242", RBMK_FUEL_BK247);
        after("rbmk_fuel_test", RBMK_FUEL_LECM, RBMK_FUEL_MECM, RBMK_FUEL_HECM);
        pellets("rbmk_pellet_men", RBMK_PELLET_BK247);
        pellets("watz_pellet_schrabidium", RBMK_PELLET_LECM, RBMK_PELLET_MECM, RBMK_PELLET_HECM);
        for (WatzTypesSpace type : WatzTypesSpace.values()) {
            after("watz_pellet_nqr", WATZ_PELLET.handle(type));
            after("watz_pellet_depleted_nqr", WATZ_PELLET_DEPLETED.handle(type));
        }
        HazardSystem.addon(FuelItemsSpace::hazards);
    }

    private static final double BK247 = 10.5D;
    private static final double CMRG = 6.0D;
    private static final double CF252 = 15.3D;

    private static void hazards() {
        rbmk(RBMK_FUEL_BK247.get(), BK247 * mult_rod_rbmk, wst * mult_rod_rbmk * 38D, true);
        rbmk(RBMK_FUEL_LECM.get(), CMRG * 2.2D * mult_rod_rbmk / 5D, wst * mult_rod_rbmk, true);
        rbmk(
                RBMK_FUEL_MECM.get(),
                CMRG * 2.2D * mult_rod_rbmk / 3D,
                wst * mult_rod_rbmk * 3D,
                true);
        rbmk(
                RBMK_FUEL_HECM.get(),
                CMRG * 2.2D * mult_rod_rbmk / 1.5D,
                wst * mult_rod_rbmk * 5D,
                true);
        rbmk(RBMK_PELLET_BK247.get(), BK247 * mult_billet, wst * mult_billet * 37D, false);
        rbmk(RBMK_PELLET_LECM.get(), CMRG * mult_billet, wst * CF252 * 2.5D, false);
        rbmk(RBMK_PELLET_MECM.get(), CMRG * mult_billet, wst * CF252 * 2.6D, false);
        rbmk(RBMK_PELLET_HECM.get(), CMRG * mult_billet, wst * CF252 * 2.7D, false);
    }

    private static void rbmk(Item item, double base, double dep, boolean hot) {
        HazardData data = new HazardData();
        data.addEntry(
                new HazardEntry(RADIATION, base)
                        .addMod(new HazardModifierRBMKRadiation(dep, false)));
        if (hot) data.addEntry(new HazardEntry(HOT, 0D).addMod(new HazardModifierRBMKHot()));
        HazardSystem.register(item, data);
    }

    private static ItemFamily<WatzTypesSpace, WatzPelletItem> watzPellet(
            String name, boolean depleted) {
        return HbmSpace.REG.family(
                name,
                WatzTypesSpace.class,
                (props, type) -> new WatzPelletItem(props, depleted, type),
                () -> new Item.Properties().stacksTo(16));
    }

    private static Reg.Handle<RBMKRodItem> curium(
            String name,
            String fullName,
            double yield,
            double reactivity,
            double selfRate,
            double heat,
            double meltingPoint,
            double diffusion) {
        return ModItems.rbmkFuel(
                HbmSpace.REG,
                name,
                fullName,
                rod ->
                        rod.setYield(yield)
                                .setStats(reactivity, selfRate)
                                .setFunction(RBMKFunctionsSpace.Burn.SLOW_LINEAR)
                                .setDepletionFunction(RBMKFunctionsSpace.Depletion.CF_SLOPE)
                                .setHeat(heat)
                                .setMeltingPoint(meltingPoint)
                                .setDiffusion(diffusion));
    }

    @SafeVarargs
    private static void pellets(String before, Reg.Handle<RBMKPelletItem>... pellets) {
        for (Reg.Handle<RBMKPelletItem> pellet : pellets) {
            for (int meta = 0; meta < 10; meta++) {
                int stage = meta;
                CreativeContents.insertBefore(
                        Tab.CONTROL,
                        RegistryUtil.id(before),
                        () -> pellet.get().of(RBMKPelletItem.Stage.ofLegacy(stage)));
            }
        }
    }

    private FuelItemsSpace() {}

    private static Reg.Handle<RTGPelletItem> californium(String name) {
        return HbmSpace.REG.item(
                name,
                props ->
                        new RTGPelletItem(props, 600, 200)
                                .setDecays(
                                        ModItems.PELLET_RTG_DEPLETED_LEAD,
                                        HalfLifeUnit.DAYS.lifespan(1F),
                                        2.5D),
                () -> new Item.Properties().stacksTo(1));
    }

    @SafeVarargs
    private static void after(String anchor, Reg.Handle<? extends Item>... entries) {
        for (Reg.Handle<? extends Item> entry : entries) {
            CreativeContents.insertAfter(
                    Tab.CONTROL, RegistryUtil.id(anchor), () -> new ItemStack(entry.get()));
        }
    }
}
