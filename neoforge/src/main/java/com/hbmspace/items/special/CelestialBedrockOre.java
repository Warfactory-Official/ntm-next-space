// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025-2026 George Paton
// SPDX-FileCopyrightText: 2025 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.special;

import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.items.special.BedrockOreSample;
import com.hbm.items.special.BedrockOreSet;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreGrade;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreOutput;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreType;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.lib.Library;
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

import static com.hbm.inventory.material.Mats.*;
import static com.hbm.items.special.ItemBedrockOreNew.o;
import static com.hbmspace.inventory.material.MatsSpace.*;

public final class CelestialBedrockOre {

    private static final Map<Body, BedrockOreSet> BY_BODY = new EnumMap<>(Body.class);
    private static final List<CelestialBedrockOreType> TYPES = new ArrayList<>();

    static {
        register(
                Body.KERBIN,
                t("light", o(MAT_IRON, 18), o(MAT_COPPER, 9), o(MAT_CRYOLITE, 6), o(MAT_SODIUM, 3)),
                t("heavy", o(MAT_TUNGSTEN, 18), o(MAT_TUNGSTEN, 9), o(MAT_ZINC, 6), o(MAT_ZINC, 3)),
                t("nonmetal", o(MAT_COAL, 18), o(MAT_LIGNITE, 9), o(MAT_SULFUR, 6), o(MAT_KNO, 3)),
                t(
                        "crystal",
                        o(MAT_REDSTONE, 18),
                        o(MAT_ASBESTOS, 9),
                        o(MAT_DIAMOND, 6),
                        o(MAT_EMERALD, 3)));
        register(
                Body.MUN,
                t(
                        "light",
                        o(MAT_LITHIUM, 18),
                        o(MAT_IRON, 9),
                        o(MAT_SODIUM, 6),
                        o(MAT_CHLOROCALCITE, 3)),
                t("heavy", o(MAT_LEAD, 18), o(MAT_ZINC, 9), o(MAT_GOLD, 6), o(MAT_BISMUTH, 3)),
                t(
                        "rare",
                        o(MAT_COBALT, 18),
                        o(MAT_RAREEARTH, 9),
                        o(MAT_NEODYMIUM, 6),
                        o(MAT_STRONTIUM, 3)),
                t(
                        "nonmetal",
                        o(MAT_SULFUR, 18),
                        o(MAT_FLUORITE, 9),
                        o(MAT_KNO, 6),
                        o(MAT_SILICON, 3)),
                t(
                        "crystal",
                        o(MAT_QUARTZ, 18),
                        o(MAT_LAPIS, 9),
                        o(MAT_EMERALD, 6),
                        o(MAT_CINNABAR, 3)));
        register(
                Body.MINMUS,
                t(
                        "light",
                        o(MAT_COPPER, 18),
                        o(MAT_TITANIUM, 9),
                        o(MAT_CHLOROCALCITE, 6),
                        o(MAT_COPPER, 3)),
                t("heavy", o(MAT_LEAD, 18), o(MAT_GOLD, 9), o(MAT_TUNGSTEN, 6), o(MAT_BISMUTH, 3)),
                t(
                        "rare",
                        o(MAT_ZIRCONIUM, 18),
                        o(MAT_BORON, 9),
                        o(MAT_COBALT, 6),
                        o(MAT_STRONTIUM, 3)),
                t(
                        "nonmetal",
                        o(MAT_SULFUR, 18),
                        o(MAT_KNO, 9),
                        o(MAT_FLUORITE, 6),
                        o(MAT_SILICON, 3)),
                t(
                        "crystal",
                        o(MAT_EMERALD, 18),
                        o(MAT_LAPIS, 9),
                        o(MAT_DIAMOND, 6),
                        o(MAT_EMERALD, 3)));
        register(
                Body.DUNA,
                t(
                        "light",
                        o(MAT_IRON, 18),
                        o(MAT_NICKEL, 9),
                        o(MAT_TITANIUM, 6),
                        o(MAT_CHLOROCALCITE, 3)),
                t(
                        "heavy",
                        o(MAT_BERYLLIUM, 18),
                        o(MAT_TUNGSTEN, 9),
                        o(MAT_ZINC, 6),
                        o(MAT_BISMUTH, 3)),
                t(
                        "rare",
                        o(MAT_RAREEARTH, 18),
                        o(MAT_BORON, 9),
                        o(MAT_ZIRCONIUM, 6),
                        o(MAT_STRONTIUM, 3)),
                t(
                        "actinide",
                        o(MAT_THORIUM, 18),
                        o(MAT_RADIUM, 9),
                        o(MAT_POLONIUM, 6),
                        o(MAT_U233, 3)),
                t(
                        "nonmetal",
                        o(MAT_FLUORITE, 18),
                        o(MAT_SULFUR, 9),
                        o(MAT_SILICON, 6),
                        o(MAT_PHOSPHORUS, 3)),
                t(
                        "crystal",
                        o(MAT_REDSTONE, 18),
                        o(MAT_CINNABAR, 9),
                        o(MAT_DIAMOND, 6),
                        o(MAT_MOLYSITE, 3)));
        register(
                Body.MOHO,
                t(
                        "light",
                        o(MAT_TITANIUM, 18),
                        o(MAT_CHLOROCALCITE, 9),
                        o(MAT_NICKEL, 6),
                        o(MAT_LITHIUM, 3)),
                t("heavy", o(MAT_GOLD, 18), o(MAT_ZINC, 9), o(MAT_LEAD, 6), o(MAT_BISMUTH, 3)),
                t(
                        "rare",
                        o(MAT_NEODYMIUM, 18),
                        o(MAT_ZIRCONIUM, 9),
                        o(MAT_BROMINE, 6),
                        o(MAT_STRONTIUM, 3)),
                t(
                        "actinide",
                        o(MAT_AUSTRALIUM, 18),
                        o(MAT_AUSTRALIUM, 9),
                        o(MAT_TASMANITE, 6),
                        o(MAT_AYERITE, 3)),
                t(
                        "nonmetal",
                        o(MAT_GLOWSTONE, 18),
                        o(MAT_PHOSPHORUS, 9),
                        o(MAT_SULFUR, 6),
                        o(MAT_PHOSPHORUS_W, 3)),
                t(
                        "crystal",
                        o(MAT_CINNABAR, 18),
                        o(MAT_REDSTONE, 9),
                        o(MAT_QUARTZ, 6),
                        o(MAT_MOLYSITE, 3)));
        register(
                Body.DRES,
                t(
                        "light",
                        o(MAT_NICKEL, 18),
                        o(MAT_TITANIUM, 9),
                        o(MAT_CADMIUM, 6),
                        o(MAT_GALLIUM, 3)),
                t("heavy", o(MAT_ZINC, 18), o(MAT_GOLD, 9), o(MAT_BISMUTH, 6), o(MAT_ARSENIC, 3)),
                t(
                        "rare",
                        o(MAT_TANTALIUM, 18),
                        o(MAT_LANTHANIUM, 9),
                        o(MAT_NIOBIUM, 6),
                        o(MAT_STRONTIUM, 3)),
                t(
                        "actinide",
                        o(MAT_URANIUM, 18),
                        o(MAT_RADIUM, 9),
                        o(MAT_TECHNETIUM, 6),
                        o(MAT_U238, 3)),
                t(
                        "nonmetal",
                        o(MAT_SILICON, 18),
                        o(MAT_SILICON, 9),
                        o(MAT_FLUORITE, 6),
                        o(MAT_FLUORITE, 3)),
                t(
                        "crystal",
                        o(MAT_DIAMOND, 18),
                        o(MAT_BORAX, 9),
                        o(MAT_MOLYSITE, 6),
                        o(MAT_MOLYSITE, 3)));
        register(
                Body.EVE,
                t(
                        "light",
                        o(MAT_SODIUM, 18),
                        o(MAT_CHLOROCALCITE, 9),
                        o(MAT_IRON, 6),
                        o(MAT_CO60, 3)),
                t("heavy", o(MAT_TUNGSTEN, 18), o(MAT_LEAD, 9), o(MAT_ARSENIC, 6), o(MAT_PB209, 3)),
                t(
                        "rare",
                        o(MAT_NIOBIUM, 18),
                        o(MAT_STRONTIUM, 9),
                        o(MAT_IODINE, 6),
                        o(MAT_AU198, 3)),
                t(
                        "actinide",
                        o(MAT_PLUTONIUM, 18),
                        o(MAT_POLONIUM, 9),
                        o(MAT_NEPTUNIUM, 6),
                        o(MAT_PU239, 3)),
                t(
                        "schrabidic",
                        o(MAT_SCHRABIDIUM, 18),
                        o(MAT_SOLINIUM, 9),
                        o(MAT_GHIORSIUM, 6),
                        o(MAT_SCHRABIDIUM, 3)),
                t(
                        "crystal",
                        o(MAT_SODALITE, 18),
                        o(MAT_MOLYSITE, 9),
                        o(MAT_DIAMOND, 6),
                        o(MAT_BORAX, 3)));
        register(
                Body.IKE,
                t(
                        "light",
                        o(MAT_COPPER, 18),
                        o(MAT_BAUXITE, 9),
                        o(MAT_NICKEL, 6),
                        o(MAT_SODIUM, 3)),
                t("heavy", o(MAT_LEAD, 18), o(MAT_ZINC, 9), o(MAT_GOLD, 6), o(MAT_ARSENIC, 3)),
                t(
                        "rare",
                        o(MAT_BORON, 18),
                        o(MAT_NEODYMIUM, 9),
                        o(MAT_STRONTIUM, 6),
                        o(MAT_LANTHANIUM, 3)),
                t(
                        "hazard",
                        o(MAT_URANIUM, 18),
                        o(MAT_U238, 9),
                        o(MAT_PLUTONIUM, 6),
                        o(MAT_TECHNETIUM, 3)));
        register(
                Body.LAYTHE,
                t(
                        "light",
                        o(MAT_ALUMINIUM, 18),
                        o(MAT_TITANIUM, 9),
                        o(MAT_GALLIUM, 6),
                        o(MAT_HAFNIUM, 3)),
                t(
                        "heavy",
                        o(MAT_BERYLLIUM, 18),
                        o(MAT_TUNGSTEN, 9),
                        o(MAT_LEAD, 6),
                        o(MAT_ARSENIC, 3)),
                t(
                        "rare",
                        o(MAT_RAREEARTH, 18),
                        o(MAT_NEODYMIUM, 9),
                        o(MAT_STRONTIUM, 6),
                        o(MAT_NIOBIUM, 3)),
                t(
                        "actinide",
                        o(MAT_URANIUM, 18),
                        o(MAT_THORIUM, 9),
                        o(MAT_POLONIUM, 6),
                        o(MAT_U235, 3)),
                t(
                        "nonmetal",
                        o(MAT_CHLOROCALCITE, 18),
                        o(MAT_COAL, 9),
                        o(MAT_FLUORITE, 6),
                        o(MAT_SILICON, 3)),
                t(
                        "crystal",
                        o(MAT_ASBESTOS, 18),
                        o(MAT_SODALITE, 9),
                        o(MAT_DIAMOND, 6),
                        o(MAT_SODALITE, 3)));
        register(
                Body.TEKTO,
                t(
                        "light",
                        o(MAT_TITANIUM, 18),
                        o(MAT_COPPER, 9),
                        o(MAT_NICKEL, 6),
                        o(MAT_LITHIUM, 3)),
                t("heavy", o(MAT_BERYLLIUM, 18), o(MAT_LEAD, 9), o(MAT_ZINC, 6), o(MAT_ZINC, 3)),
                t(
                        "rare",
                        o(MAT_BORON, 18),
                        o(MAT_RAREEARTH, 9),
                        o(MAT_TANTALIUM, 6),
                        o(MAT_BISMUTH, 3)),
                t(
                        "actinide",
                        o(MAT_URANIUM, 18),
                        o(MAT_NEPTUNIUM, 9),
                        o(MAT_RADIUM, 6),
                        o(MAT_TECHNETIUM, 3)),
                t(
                        "crystal",
                        o(MAT_EMERALD, 18),
                        o(MAT_SILICON, 9),
                        o(MAT_MOLYSITE, 6),
                        o(MAT_BORAX, 3)),
                t(
                        "plastic",
                        o(MAT_POLYMER, 18),
                        o(MAT_RUBBER, 9),
                        o(MAT_SEMTEX, 6),
                        o(MAT_PVC, 3)));
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

        Identifier base = Library.id("bedrock_ore_base");
        Identifier after =
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "bedrock_ore_fragment_pvc");
        CreativeContents.remove(Tab.PARTS, base);
        CreativeContents.remove(Tab.PARTS, Library.id("bedrock_ore"));
        for (Body body : BY_BODY.keySet())
            CreativeContents.insertAfter(Tab.PARTS, after, () -> sample(body));
        for (CelestialBedrockOreType type : TYPES) {
            CreativeContents.insertAfter(
                    Tab.PARTS, after, () -> ItemBedrockOreNew.make(BedrockOreGrade.BASE, type));
        }
        CreativeContents.insertAfter(
                Tab.PARTS, after, () -> new ItemStack(PartsItemsSpace.CRYSTAL_MINERAL.get()));
        CreativeContents.insertAfter(
                Tab.PARTS, after, () -> new ItemStack(PartsItemsSpace.CRYSTAL_CLEANED.get()));
        CreativeContents.insertAfter(
                Tab.PARTS, after, () -> new ItemStack(PartsItemsSpace.MINERAL_DUST.get()));
    }
}
