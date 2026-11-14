/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025-2026 George Paton
 * SPDX-FileCopyrightText: 2025 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025-2026 George Paton. All rights reserved.
 * Copyright (C) 2025 JamesH_2. All rights reserved.
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

package com.hbmspace.items.special;

import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.inventory.material.Materials;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.items.special.BedrockOreNewItem.BedrockOreGrade;
import com.hbm.items.special.BedrockOreNewItem.BedrockOreOutput;
import com.hbm.items.special.BedrockOreNewItem.BedrockOreType;
import com.hbm.items.special.BedrockOreNewItem;
import com.hbm.items.special.BedrockOreSample;
import com.hbm.items.special.BedrockOreSet;
import com.hbm.util.RegistryUtil;
import com.hbm.world.feature.BedrockOreFeature;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.items.PartsItemsSpace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import static com.hbm.items.special.BedrockOreNewItem.o;
import static com.hbmspace.inventory.material.MatsSpace.*;

public final class CelestialBedrockOre {

    private static final Map<Body, BedrockOreSet> BY_BODY = new EnumMap<>(Body.class);
    private static final List<CelestialBedrockOreType> TYPES = new ArrayList<>();

    static {
        register(
                Body.KERBIN,
                t(
                        "light",
                        o(Materials.IRON, 18),
                        o(Materials.COPPER, 9),
                        o(Materials.CRYOLITE, 6),
                        o(Materials.SODIUM, 3)),
                t(
                        "heavy",
                        o(Materials.TUNGSTEN, 18),
                        o(Materials.TUNGSTEN, 9),
                        o(MAT_ZINC, 6),
                        o(MAT_ZINC, 3)),
                t(
                        "nonmetal",
                        o(Materials.COAL, 18),
                        o(Materials.LIGNITE, 9),
                        o(Materials.SULFUR, 6),
                        o(Materials.SALTPETER, 3)),
                t(
                        "crystal",
                        o(Materials.REDSTONE, 18),
                        o(Materials.ASBESTOS, 9),
                        o(Materials.DIAMOND, 6),
                        o(Materials.EMERALD, 3)));
        register(
                Body.MUN,
                t(
                        "light",
                        o(Materials.LITHIUM, 18),
                        o(Materials.IRON, 9),
                        o(Materials.SODIUM, 6),
                        o(Materials.CHLOROCALCITE, 3)),
                t(
                        "heavy",
                        o(Materials.LEAD, 18),
                        o(MAT_ZINC, 9),
                        o(Materials.GOLD, 6),
                        o(Materials.BISMUTH, 3)),
                t(
                        "rare",
                        o(Materials.COBALT, 18),
                        o(Materials.RARE_EARTH, 9),
                        o(Materials.NEODYMIUM, 6),
                        o(Materials.STRONTIUM, 3)),
                t(
                        "nonmetal",
                        o(Materials.SULFUR, 18),
                        o(Materials.FLUORITE, 9),
                        o(Materials.SALTPETER, 6),
                        o(Materials.SILICON, 3)),
                t(
                        "crystal",
                        o(MAT_QUARTZ, 18),
                        o(MAT_LAPIS, 9),
                        o(Materials.EMERALD, 6),
                        o(Materials.CINNABAR, 3)));
        register(
                Body.MINMUS,
                t(
                        "light",
                        o(Materials.COPPER, 18),
                        o(Materials.TITANIUM, 9),
                        o(Materials.CHLOROCALCITE, 6),
                        o(Materials.COPPER, 3)),
                t(
                        "heavy",
                        o(Materials.LEAD, 18),
                        o(Materials.GOLD, 9),
                        o(Materials.TUNGSTEN, 6),
                        o(Materials.BISMUTH, 3)),
                t(
                        "rare",
                        o(Materials.ZIRCONIUM, 18),
                        o(Materials.BORON, 9),
                        o(Materials.COBALT, 6),
                        o(Materials.STRONTIUM, 3)),
                t(
                        "nonmetal",
                        o(Materials.SULFUR, 18),
                        o(Materials.SALTPETER, 9),
                        o(Materials.FLUORITE, 6),
                        o(Materials.SILICON, 3)),
                t(
                        "crystal",
                        o(Materials.EMERALD, 18),
                        o(MAT_LAPIS, 9),
                        o(Materials.DIAMOND, 6),
                        o(Materials.EMERALD, 3)));
        register(
                Body.DUNA,
                t(
                        "light",
                        o(Materials.IRON, 18),
                        o(MAT_NICKEL, 9),
                        o(Materials.TITANIUM, 6),
                        o(Materials.CHLOROCALCITE, 3)),
                t(
                        "heavy",
                        o(Materials.BERYLLIUM, 18),
                        o(Materials.TUNGSTEN, 9),
                        o(MAT_ZINC, 6),
                        o(Materials.BISMUTH, 3)),
                t(
                        "rare",
                        o(Materials.RARE_EARTH, 18),
                        o(Materials.BORON, 9),
                        o(Materials.ZIRCONIUM, 6),
                        o(Materials.STRONTIUM, 3)),
                t(
                        "actinide",
                        o(Materials.THORIUM, 18),
                        o(Materials.RADIUM_226, 9),
                        o(Materials.POLONIUM_210, 6),
                        o(Materials.URANIUM_233, 3)),
                t(
                        "nonmetal",
                        o(Materials.FLUORITE, 18),
                        o(Materials.SULFUR, 9),
                        o(Materials.SILICON, 6),
                        o(Materials.RED_PHOSPHORUS, 3)),
                t(
                        "crystal",
                        o(Materials.REDSTONE, 18),
                        o(Materials.CINNABAR, 9),
                        o(Materials.DIAMOND, 6),
                        o(Materials.MOLYSITE, 3)));
        register(
                Body.MOHO,
                t(
                        "light",
                        o(Materials.TITANIUM, 18),
                        o(Materials.CHLOROCALCITE, 9),
                        o(MAT_NICKEL, 6),
                        o(Materials.LITHIUM, 3)),
                t(
                        "heavy",
                        o(Materials.GOLD, 18),
                        o(MAT_ZINC, 9),
                        o(Materials.LEAD, 6),
                        o(Materials.BISMUTH, 3)),
                t(
                        "rare",
                        o(Materials.NEODYMIUM, 18),
                        o(Materials.ZIRCONIUM, 9),
                        o(MAT_BROMINE, 6),
                        o(Materials.STRONTIUM, 3)),
                t(
                        "actinide",
                        o(MAT_AUSTRALIUM, 18),
                        o(MAT_AUSTRALIUM, 9),
                        o(MAT_TASMANITE, 6),
                        o(MAT_AYERITE, 3)),
                t(
                        "nonmetal",
                        o(MAT_GLOWSTONE, 18),
                        o(Materials.RED_PHOSPHORUS, 9),
                        o(Materials.SULFUR, 6),
                        o(MAT_PHOSPHORUS_W, 3)),
                t(
                        "crystal",
                        o(Materials.CINNABAR, 18),
                        o(Materials.REDSTONE, 9),
                        o(MAT_QUARTZ, 6),
                        o(Materials.MOLYSITE, 3)));
        register(
                Body.DRES,
                t(
                        "light",
                        o(MAT_NICKEL, 18),
                        o(Materials.TITANIUM, 9),
                        o(Materials.CADMIUM, 6),
                        o(MAT_GALLIUM, 3)),
                t(
                        "heavy",
                        o(MAT_ZINC, 18),
                        o(Materials.GOLD, 9),
                        o(Materials.BISMUTH, 6),
                        o(Materials.ARSENIC, 3)),
                t(
                        "rare",
                        o(Materials.TANTALUM, 18),
                        o(Materials.LANTHANUM, 9),
                        o(Materials.NIOBIUM, 6),
                        o(Materials.STRONTIUM, 3)),
                t(
                        "actinide",
                        o(Materials.URANIUM, 18),
                        o(Materials.RADIUM_226, 9),
                        o(Materials.TECHNETIUM_99, 6),
                        o(Materials.URANIUM_238, 3)),
                t(
                        "nonmetal",
                        o(Materials.SILICON, 18),
                        o(Materials.SILICON, 9),
                        o(Materials.FLUORITE, 6),
                        o(Materials.FLUORITE, 3)),
                t(
                        "crystal",
                        o(Materials.DIAMOND, 18),
                        o(Materials.BORAX, 9),
                        o(Materials.MOLYSITE, 6),
                        o(Materials.MOLYSITE, 3)));
        register(
                Body.EVE,
                t(
                        "light",
                        o(Materials.SODIUM, 18),
                        o(Materials.CHLOROCALCITE, 9),
                        o(Materials.IRON, 6),
                        o(Materials.COBALT_60, 3)),
                t(
                        "heavy",
                        o(Materials.TUNGSTEN, 18),
                        o(Materials.LEAD, 9),
                        o(Materials.ARSENIC, 6),
                        o(Materials.LEAD_209, 3)),
                t(
                        "rare",
                        o(Materials.NIOBIUM, 18),
                        o(Materials.STRONTIUM, 9),
                        o(MAT_IODINE, 6),
                        o(Materials.GOLD_198, 3)),
                t(
                        "actinide",
                        o(Materials.PLUTONIUM, 18),
                        o(Materials.POLONIUM_210, 9),
                        o(Materials.NEPTUNIUM_237, 6),
                        o(Materials.PLUTONIUM_239, 3)),
                t(
                        "schrabidic",
                        o(Materials.SCHRABIDIUM, 18),
                        o(Materials.SOLINIUM, 9),
                        o(Materials.GHIORSIUM_336, 6),
                        o(Materials.SCHRABIDIUM, 3)),
                t(
                        "crystal",
                        o(Materials.SODALITE, 18),
                        o(Materials.MOLYSITE, 9),
                        o(Materials.DIAMOND, 6),
                        o(Materials.BORAX, 3)));
        register(
                Body.IKE,
                t(
                        "light",
                        o(Materials.COPPER, 18),
                        o(Materials.BAUXITE, 9),
                        o(MAT_NICKEL, 6),
                        o(Materials.SODIUM, 3)),
                t(
                        "heavy",
                        o(Materials.LEAD, 18),
                        o(MAT_ZINC, 9),
                        o(Materials.GOLD, 6),
                        o(Materials.ARSENIC, 3)),
                t(
                        "rare",
                        o(Materials.BORON, 18),
                        o(Materials.NEODYMIUM, 9),
                        o(Materials.STRONTIUM, 6),
                        o(Materials.LANTHANUM, 3)),
                t(
                        "hazard",
                        o(Materials.URANIUM, 18),
                        o(Materials.URANIUM_238, 9),
                        o(Materials.PLUTONIUM, 6),
                        o(Materials.TECHNETIUM_99, 3)));
        register(
                Body.LAYTHE,
                t(
                        "light",
                        o(Materials.ALUMINUM, 18),
                        o(Materials.TITANIUM, 9),
                        o(MAT_GALLIUM, 6),
                        o(MAT_HAFNIUM, 3)),
                t(
                        "heavy",
                        o(Materials.BERYLLIUM, 18),
                        o(Materials.TUNGSTEN, 9),
                        o(Materials.LEAD, 6),
                        o(Materials.ARSENIC, 3)),
                t(
                        "rare",
                        o(Materials.RARE_EARTH, 18),
                        o(Materials.NEODYMIUM, 9),
                        o(Materials.STRONTIUM, 6),
                        o(Materials.NIOBIUM, 3)),
                t(
                        "actinide",
                        o(Materials.URANIUM, 18),
                        o(Materials.THORIUM, 9),
                        o(Materials.POLONIUM_210, 6),
                        o(Materials.URANIUM_235, 3)),
                t(
                        "nonmetal",
                        o(Materials.CHLOROCALCITE, 18),
                        o(Materials.COAL, 9),
                        o(Materials.FLUORITE, 6),
                        o(Materials.SILICON, 3)),
                t(
                        "crystal",
                        o(Materials.ASBESTOS, 18),
                        o(Materials.SODALITE, 9),
                        o(Materials.DIAMOND, 6),
                        o(Materials.SODALITE, 3)));
        register(
                Body.TEKTO,
                t(
                        "light",
                        o(Materials.TITANIUM, 18),
                        o(Materials.COPPER, 9),
                        o(MAT_NICKEL, 6),
                        o(Materials.LITHIUM, 3)),
                t(
                        "heavy",
                        o(Materials.BERYLLIUM, 18),
                        o(Materials.LEAD, 9),
                        o(MAT_ZINC, 6),
                        o(MAT_ZINC, 3)),
                t(
                        "rare",
                        o(Materials.BORON, 18),
                        o(Materials.RARE_EARTH, 9),
                        o(Materials.TANTALUM, 6),
                        o(Materials.BISMUTH, 3)),
                t(
                        "actinide",
                        o(Materials.URANIUM, 18),
                        o(Materials.NEPTUNIUM_237, 9),
                        o(Materials.RADIUM_226, 6),
                        o(Materials.TECHNETIUM_99, 3)),
                t(
                        "crystal",
                        o(Materials.EMERALD, 18),
                        o(Materials.SILICON, 9),
                        o(Materials.MOLYSITE, 6),
                        o(Materials.BORAX, 3)),
                t(
                        "plastic",
                        o(Materials.POLYMER, 18),
                        o(Materials.RUBBER, 9),
                        o(MAT_SEMTEX, 6),
                        o(Materials.PVC, 3)));
    }

    private CelestialBedrockOre() {}

    private static Function<Body, CelestialBedrockOreType> t(
            String suffix,
            BedrockOreOutput primary,
            BedrockOreOutput acid,
            BedrockOreOutput solvent,
            BedrockOreOutput rad) {
        return body -> new CelestialBedrockOreType(body, suffix, primary, acid, solvent, rad);
    }

    @SafeVarargs
    private static void register(Body body, Function<Body, CelestialBedrockOreType>... rows) {
        List<CelestialBedrockOreType> types =
                Arrays.stream(rows).map(row -> row.apply(body)).toList();
        for (CelestialBedrockOreType type : types) BedrockOreType.KINDS.add(type);
        TYPES.addAll(types);
        BedrockOreSet set =
                new BedrockOreSet(
                        body.name,
                        List.copyOf(types),
                        Component.translatable(
                                "item.hbmspace.bedrock_ore_base.mined_on",
                                Component.translatable("body." + body.name)));
        BedrockOreSet.SETS.add(set);
        BY_BODY.put(body, set);
    }

    public static @Nullable BedrockOreSet get(Body body) {
        return BY_BODY.get(body);
    }

    public static List<CelestialBedrockOreType> getAllTypes() {
        return List.copyOf(TYPES);
    }

    public static BedrockOreSet of(ResourceKey<Level> dimension) {
        Body body = CelestialBody.getBody(dimension).getEnum();
        BedrockOreSet set = body == null ? null : BY_BODY.get(body);
        return set != null ? set : BY_BODY.get(Body.KERBIN);
    }

    public static BedrockOreSet of(Level level) {
        return of(level.dimension());
    }

    public static ItemStack sample(Body body) {
        BedrockOreSet set = BY_BODY.get(body);
        double[] amounts = new double[set.kinds().size()];
        Arrays.fill(amounts, 1D);
        ItemStack stack = new ItemStack(ModItems.BEDROCK_ORE_BASE);
        stack.set(ModDataComponents.BEDROCK_ORE_SAMPLE.get(), new BedrockOreSample(set, amounts));
        return stack;
    }

    private static List<ItemStack> samples() {
        return BY_BODY.keySet().stream().map(CelestialBedrockOre::sample).toList();
    }

    private static @Nullable FluidStackNTM boreFluid(Level level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        return provider == null ? null : provider.getBedrockAcid();
    }

    public static void install() {
        BedrockOreSet.OF.install(HbmSpace.MOD_ID, CelestialBedrockOre::of);
        BedrockOreSet.SAMPLES.install(HbmSpace.MOD_ID, CelestialBedrockOre::samples);
        BedrockOreFeature.BORE_FLUID.install(HbmSpace.MOD_ID, CelestialBedrockOre::boreFluid);

        Identifier base = RegistryUtil.id("bedrock_ore_base");
        Identifier after =
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "bedrock_ore_fragment_pvc");
        CreativeContents.remove(Tab.PARTS, base);
        CreativeContents.remove(Tab.PARTS, RegistryUtil.id("bedrock_ore"));
        for (Body body : BY_BODY.keySet())
            CreativeContents.insertAfter(Tab.PARTS, after, () -> sample(body));
        for (CelestialBedrockOreType type : TYPES) {
            CreativeContents.insertAfter(
                    Tab.PARTS, after, () -> BedrockOreNewItem.make(BedrockOreGrade.BASE, type));
        }
        CreativeContents.insertAfter(
                Tab.PARTS, after, () -> new ItemStack(PartsItemsSpace.CRYSTAL_MINERAL.get()));
        CreativeContents.insertAfter(
                Tab.PARTS, after, () -> new ItemStack(PartsItemsSpace.CRYSTAL_CLEANED.get()));
        CreativeContents.insertAfter(
                Tab.PARTS, after, () -> new ItemStack(PartsItemsSpace.MINERAL_DUST.get()));
    }
}
