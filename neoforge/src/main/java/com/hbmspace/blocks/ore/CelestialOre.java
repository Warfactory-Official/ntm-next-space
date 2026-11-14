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

package com.hbmspace.blocks.ore;

import com.hbm.inventory.material.MaterialTagRegistry;
import com.hbm.inventory.material.MaterialTags;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.inventory.OreDictManagerSpace;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public enum CelestialOre {
    IRON(
            "ore_iron",
            vanilla(() -> Blocks.IRON_ORE)
                    .strength(3.0F, 3.0F)
                    .dict(MaterialTagRegistry.IRON)
                    .flat()
                    .needs(BlockTags.NEEDS_STONE_TOOL)
                    .drop(Drop.fragment("minecraft:raw_iron"))
                    .everywhere()),
    GOLD(
            "ore_gold",
            vanilla(() -> Blocks.GOLD_ORE)
                    .strength(3.0F, 3.0F)
                    .dict(MaterialTagRegistry.GOLD)
                    .flat()
                    .needs(BlockTags.NEEDS_IRON_TOOL)
                    .drop(Drop.fragment("minecraft:raw_gold"))
                    .everywhere()),
    REDSTONE(
            "ore_redstone",
            vanilla(() -> Blocks.REDSTONE_ORE)
                    .strength(3.0F, 3.0F)
                    .dict(MaterialTagRegistry.REDSTONE)
                    .bouncy()
                    .needs(BlockTags.NEEDS_IRON_TOOL)
                    .drop(Drop.uniformBonus("minecraft:redstone", 4, 5))
                    .everywhere()),
    LAPIS(
            "ore_lapis",
            vanilla(() -> Blocks.LAPIS_ORE)
                    .strength(3.0F, 3.0F)
                    .dict(MaterialTagRegistry.LAPIS)
                    .bouncy()
                    .needs(BlockTags.NEEDS_STONE_TOOL)
                    .drop(Drop.fragment("minecraft:lapis_lazuli", 4, 9))
                    .on(Body.KERBIN, Body.LAYTHE)),
    EMERALD(
            "ore_emerald",
            vanilla(() -> Blocks.EMERALD_ORE)
                    .strength(3.0F, 3.0F)
                    .dict(MaterialTagRegistry.EMERALD)
                    .bouncy()
                    .needs(BlockTags.NEEDS_IRON_TOOL)
                    .drop(Drop.fragment("minecraft:emerald"))
                    .on(Body.KERBIN, Body.LAYTHE)),
    QUARTZ(
            "ore_quartz",
            space().strength(3.0F, 3.0F)
                    .dict(MaterialTagRegistry.NETHERQUARTZ)
                    .bouncy()
                    .drop(Drop.fragment("minecraft:quartz"))
                    .on(Body.MUN)),
    DIAMOND(
            "ore_diamond",
            vanilla(() -> Blocks.DIAMOND_ORE)
                    .strength(3.0F, 3.0F)
                    .dict(MaterialTagRegistry.DIAMOND)
                    .bouncy()
                    .needs(BlockTags.NEEDS_IRON_TOOL)
                    .drop(Drop.fragment("minecraft:diamond"))
                    .everywhere()),

    URANIUM(
            "ore_uranium",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.U)
                    .flat()
                    .gas(Gas.RADON)
                    .drop(Drop.fragment("hbm:raw_uranium"))
                    .everywhere()),
    PLUTONIUM(
            "ore_plutonium",
            space().after("ore_uranium_scorched")
                    .strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.PU)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_plutonium"))),
    THORIUM(
            "ore_thorium",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.TH232)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_thorium"))
                    .everywhere()),
    MORKITE("ore_morkite", space().strength(5.0F, 6.0F).bouncy().morkite()),
    TITANIUM(
            "ore_titanium",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.TI)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_titanium"))
                    .everywhere()),
    SULFUR(
            "ore_sulfur",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.S)
                    .bouncy()
                    .drop(Drop.fragment("hbm:sulfur", 2, 4))
                    .everywhere()),
    GLOWSTONE(
            "ore_glowstone",
            space().strength(5.0F, 6.0F)
                    .light(4)
                    .bouncy()
                    .drop(Drop.fragment("minecraft:glowstone_dust", 1, 3))
                    .on(Body.MOHO)),
    NITER(
            "ore_niter",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.KNO)
                    .bouncy()
                    .drop(Drop.fragment("hbm:niter", 2, 4))
                    .everywhere()),
    COPPER(
            "ore_copper",
            vanilla(() -> Blocks.COPPER_ORE)
                    .strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.CU)
                    .flat()
                    .needs(BlockTags.NEEDS_STONE_TOOL)
                    .drop(Drop.fragment("minecraft:raw_copper", 2, 5))
                    .everywhere()),
    NICKEL(
            "ore_nickel",
            space().strength(5.0F, 6.0F)
                    .dict(OreDictManagerSpace.NI)
                    .flat()
                    .drop(Drop.fragment("hbmspace:raw_nickel"))
                    .everywhereBut(Body.KERBIN)),
    MINERAL("ore_mineral", space().strength(5.0F, 6.0F).bouncy().on(Body.IKE, Body.MOHO)),
    ZINC(
            "ore_zinc",
            space().strength(5.0F, 6.0F)
                    .dict(OreDictManagerSpace.ZI)
                    .flat()
                    .drop(Drop.fragment("hbmspace:raw_zinc"))
                    .everywhere()),
    TUNGSTEN(
            "ore_tungsten",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.W)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_tungsten"))
                    .everywhere()),
    ALUMINIUM(
            "ore_aluminium",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.AL)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_aluminium"))
                    .everywhere()),
    FLUORITE(
            "ore_fluorite",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.F)
                    .bouncy()
                    .drop(Drop.fragment("hbm:fluorite", 2, 4))
                    .everywhere()),
    BERYLLIUM(
            "ore_beryllium",
            ntm().strength(5.0F, 9.0F)
                    .dict(MaterialTagRegistry.BE)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_beryllium"))
                    .everywhere()),
    LEAD(
            "ore_lead",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.PB)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_lead"))
                    .everywhere()),
    OIL(
            "ore_oil",
            ntm().strength(5.0F, 6.0F)
                    .bouncy()
                    .reserve(OreFluidBlock.ReserveType.OIL, "OIL_EMPTY")
                    .drop(Drop.unsilkable("hbm:oil_tar_crude"))
                    .on(Body.KERBIN, Body.DUNA, Body.LAYTHE)),
    OIL_EMPTY("ore_oil_empty", ntm().strength(5.0F, 6.0F).bouncy().drained()),
    GAS(
            "ore_gas",
            space().strength(5.0F, 6.0F)
                    .bouncy()
                    .reserve(OreFluidBlock.ReserveType.GAS, "GAS_EMPTY")
                    .drop(Drop.unsilkable("hbmspace:ore_gas_empty"))
                    .on(Body.EVE)),
    GAS_EMPTY(
            "ore_gas_empty",
            space().strength(5.0F, 6.0F).bouncy().overlay("ore_oil_empty").drained()),
    LIGNITE(
            "ore_lignite",
            ntm().strength(5.0F, 9.0F)
                    .dict(MaterialTagRegistry.LIGNITE)
                    .bouncy()
                    .drop(Drop.fragment("hbm:lignite"))
                    .on(Body.KERBIN)),
    ASBESTOS(
            "ore_asbestos",
            ntm().strength(5.0F, 9.0F)
                    .dict(MaterialTagRegistry.ASBESTOS)
                    .bouncy()
                    .gas(Gas.ASBESTOS)
                    .drop(Drop.fragment("hbm:ingot_asbestos"))
                    .on(Body.KERBIN, Body.IKE, Body.LAYTHE)),
    SCHRABIDIUM(
            "ore_schrabidium",
            ntm().strength(15.0F, 360.0F)
                    .dict(MaterialTagRegistry.SA326)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_schrabidium"))
                    .on(Body.EVE)),
    LITHIUM(
            "ore_lithium",
            space().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.LI)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_lithium"))
                    .on(Body.MUN, Body.IKE)),
    NIOBIUM(
            "ore_niobium",
            space().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.NB)
                    .flat()
                    .drop(Drop.fragment("hbmspace:raw_niobium"))
                    .on(Body.DRES, Body.EVE)),
    CADMIUM("ore_cadmium", space().strength(5.0F, 6.0F).flat()),
    PALLADIUM("ore_palladium", space().strength(5.0F, 6.0F).flat()),
    IODINE(
            "ore_iodine",
            space().strength(5.0F, 6.0F).dict(MaterialTagRegistry.I).bouncy().on(Body.EVE)),
    ARSENIC("ore_arsenic", space().strength(5.0F, 6.0F).bouncy()),
    SILICON("ore_silicon", space().strength(5.0F, 6.0F).bouncy()),
    FIRE(
            "ore_fire",
            space().strength(0.4F, 6.0F)
                    .dict(MaterialTagRegistry.P_RED)
                    .bouncy()
                    .fire()
                    .drop(Drop.silkOr("hbm:powder_fire"))
                    .on(Body.MOHO)),
    SHALE("ore_shale", space().strength(1.5F, 6.0F).bouncy().on(Body.MUN, Body.MOHO, Body.DRES)),
    BRINE(
            "ore_brine",
            space().strength(5.0F, 6.0F)
                    .bouncy()
                    .reserve(OreFluidBlock.ReserveType.BRINE, "BRINE_EMPTY")
                    .drop(Drop.unsilkable("hbmspace:ore_brine_empty"))
                    .on(Body.MUN, Body.MINMUS, Body.IKE)),
    BRINE_EMPTY(
            "ore_brine_empty",
            space().strength(5.0F, 6.0F).bouncy().overlay("ore_oil_empty").drained()),
    TEKTO(
            "ore_tekto",
            space().strength(5.0F, 6.0F)
                    .bouncy()
                    .reserve(OreFluidBlock.ReserveType.OIL, "TEKTO_EMPTY")
                    .drop(Drop.unsilkable("hbmspace:ore_tekto_empty"))
                    .on(Body.TEKTO)),
    TEKTO_EMPTY(
            "ore_tekto_empty",
            space().strength(5.0F, 6.0F).bouncy().overlay("ore_oil_empty").drained()),

    AUSTRALIUM(
            "ore_australium",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.AUSTRALIUM)
                    .flat()
                    .drop(Drop.fragment("hbmspace:raw_australium"))
                    .on(Body.MOHO)),
    RARE(
            "ore_rare",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.RAREEARTH)
                    .bouncy()
                    .drop(Drop.fragment("hbm:chunk_ore_rare"))
                    .everywhere()),
    COBALT(
            "ore_cobalt",
            ntm().strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.CO)
                    .flat()
                    .drop(Drop.fragment("hbm:raw_cobalt"))
                    .everywhere()),
    CINNEBAR(
            "ore_cinnebar",
            ntm("ore_cinnabar")
                    .strength(5.0F, 6.0F)
                    .dict(MaterialTagRegistry.CINNABAR)
                    .bouncy()
                    .drop(Drop.fragment("hbm:cinnabar"))
                    .everywhere()),
    COLTAN(
            "ore_coltan",
            ntm().strength(15.0F, 6.0F)
                    .dict(MaterialTagRegistry.COLTAN)
                    .flat()
                    .drop(Drop.fragment("hbm:fragment_coltan"))
                    .on(Body.KERBIN, Body.IKE, Body.DRES)),
    LANTHANIUM(
            "ore_lanthanium",
            space().strength(15.0F, 6.0F)
                    .dict(MaterialTagRegistry.LA)
                    .flat()
                    .drop(Drop.fragment("hbmspace:raw_lanthanium"))
                    .on(Body.DRES)),

    CLUSTER_IRON(
            "cluster_iron",
            ntm().strength(5.0F, 9.0F)
                    .dict(MaterialTagRegistry.IRON)
                    .flat()
                    .drop(Drop.silkOr("hbm:crystal_iron"))
                    .everywhere()),
    CLUSTER_TITANIUM(
            "cluster_titanium",
            ntm().strength(5.0F, 9.0F)
                    .dict(MaterialTagRegistry.TI)
                    .flat()
                    .drop(Drop.silkOr("hbm:crystal_titanium"))
                    .everywhere()),
    CLUSTER_ALUMINIUM(
            "cluster_aluminium",
            ntm().strength(5.0F, 9.0F)
                    .dict(MaterialTagRegistry.AL)
                    .flat()
                    .drop(Drop.silkOr("hbm:crystal_aluminium"))
                    .everywhere()),
    CLUSTER_COPPER(
            "cluster_copper",
            ntm().strength(5.0F, 9.0F)
                    .dict(MaterialTagRegistry.CU)
                    .flat()
                    .drop(Drop.silkOr("hbm:crystal_copper"))
                    .everywhere()),

    BEDROCK_OIL(
            "ore_bedrock_oil",
            ntm().strength(-1.0F, 600000.0F)
                    .noSpawn()
                    .opaque()
                    .reserve(OreFluidBlock.ReserveType.OIL, null)
                    .drop(Drop.nothing())
                    .on(Body.KERBIN));

    private static final List<Body> BODIES =
            List.of(
                    Body.KERBIN,
                    Body.MUN,
                    Body.MINMUS,
                    Body.DUNA,
                    Body.MOHO,
                    Body.DRES,
                    Body.EVE,
                    Body.IKE,
                    Body.LAYTHE,
                    Body.TEKTO);
    private static final List<Body> VANILLA_BODIES =
            List.of(
                    Body.ORBIT,
                    Body.KERBIN,
                    Body.MUN,
                    Body.MINMUS,
                    Body.DUNA,
                    Body.MOHO,
                    Body.DRES,
                    Body.EVE,
                    Body.IKE,
                    Body.LAYTHE,
                    Body.TEKTO);

    public final String name;
    final Spec spec;
    final Map<Body, Reg.Handle<? extends Block>> bodies = new EnumMap<>(Body.class);
    Reg.@Nullable Handle<? extends Block> spaceHost;

    CelestialOre(String name, Spec spec) {
        this.name = name;
        this.spec = spec;
        if (spec.host == Host.NTM && spec.ntmPath == null) spec.ntmPath = name;
        if (spec.overlay == null) spec.overlay = name;
    }

    public List<Body> bodies() {
        return spec.host == Host.VANILLA ? VANILLA_BODIES : BODIES;
    }

    public BlockState state(Body body) {
        return block(body).defaultBlockState();
    }

    public Block block(Body body) {
        Reg.Handle<? extends Block> own = bodies.get(body);
        return own != null ? own.get() : host();
    }

    public Block host() {
        return switch (spec.host) {
            case NTM ->
                    BuiltInRegistries.BLOCK
                            .getOptional(RegistryUtil.id(spec.ntmPath))
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    name + ": no hbm:" + spec.ntmPath));
            case VANILLA -> spec.vanilla.get();
            case SPACE -> spaceHost.get();
        };
    }

    public Set<Body> spawnsOn() {
        return Collections.unmodifiableSet(spec.spawnsOn);
    }

    String id(Body body) {
        return (spec.host == Host.NTM ? spec.ntmPath : name)
                + "_"
                + (body == Body.ORBIT ? "orbit" : body.name);
    }

    Identifier hostId() {
        return switch (spec.host) {
            case NTM -> RegistryUtil.id(spec.ntmPath);
            case VANILLA -> BuiltInRegistries.BLOCK.getKey(spec.vanilla.get());
            case SPACE -> Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name);
        };
    }

    enum Host {
        NTM,
        VANILLA,
        SPACE
    }

    enum Gas {
        NONE,
        RADON,
        ASBESTOS
    }

    record Drop(Kind kind, String item, int min, int max) {

        enum Kind {
            SELF,
            FRAGMENT,
            UNIFORM_BONUS,
            UNSILKABLE,
            SILK_OR,
            NOTHING
        }

        static Drop fragment(String item) {
            return new Drop(Kind.FRAGMENT, item, 0, 0);
        }

        static Drop fragment(String item, int min, int max) {
            return new Drop(Kind.FRAGMENT, item, min, max);
        }

        static Drop uniformBonus(String item, int min, int max) {
            return new Drop(Kind.UNIFORM_BONUS, item, min, max);
        }

        static Drop unsilkable(String item) {
            return new Drop(Kind.UNSILKABLE, item, 0, 0);
        }

        static Drop silkOr(String item) {
            return new Drop(Kind.SILK_OR, item, 0, 0);
        }

        static Drop nothing() {
            return new Drop(Kind.NOTHING, "", 0, 0);
        }
    }

    static final class Spec {
        final Host host;
        @Nullable String ntmPath;
        @Nullable Supplier<Block> vanilla;
        @Nullable String after;
        float hardness;
        float resistance;
        int light;
        boolean morkite;
        boolean fire;
        boolean noSpawn;
        boolean opaque;
        boolean drainedDeposit;
        @Nullable String overlay;
        Gas gas = Gas.NONE;
        OreFluidBlock.@Nullable ReserveType reserve;
        @Nullable String drained;
        @Nullable MaterialTags dict;
        @Nullable TagKey<Item> archetype;
        @Nullable TagKey<Block> needs;
        Drop drop = new Drop(Drop.Kind.SELF, "", 0, 0);
        final EnumSet<Body> spawnsOn = EnumSet.noneOf(Body.class);

        private Spec(Host host) {
            this.host = host;
        }

        Spec after(String anchor) {
            this.after = anchor;
            return this;
        }

        Spec strength(float hardness, float resistance) {
            this.hardness = hardness;
            this.resistance = resistance;
            return this;
        }

        Spec light(int light) {
            this.light = light;
            return this;
        }

        Spec morkite() {
            this.morkite = true;
            return this;
        }

        Spec fire() {
            this.fire = true;
            return this;
        }

        Spec noSpawn() {
            this.noSpawn = true;
            return this;
        }

        Spec opaque() {
            this.opaque = true;
            return this;
        }

        Spec drained() {
            this.drainedDeposit = true;
            return this;
        }

        Spec overlay(String texture) {
            this.overlay = texture;
            return this;
        }

        Spec gas(Gas gas) {
            this.gas = gas;
            return this;
        }

        Spec reserve(OreFluidBlock.ReserveType type, @Nullable String drained) {
            this.reserve = type;
            this.drained = drained;
            return this;
        }

        Spec dict(MaterialTags dict) {
            this.dict = dict;
            return this;
        }

        Spec flat() {
            this.archetype = ItemTags.SULFUR_CUBE_ARCHETYPE_SLOW_FLAT;
            return this;
        }

        Spec bouncy() {
            this.archetype = ItemTags.SULFUR_CUBE_ARCHETYPE_SLOW_BOUNCY;
            return this;
        }

        Spec needs(TagKey<Block> tier) {
            this.needs = tier;
            return this;
        }

        Spec drop(Drop drop) {
            this.drop = drop;
            return this;
        }

        Spec everywhere() {
            spawnsOn.addAll(EnumSet.allOf(Body.class));
            return this;
        }

        Spec everywhereBut(Body body) {
            everywhere();
            spawnsOn.remove(body);
            return this;
        }

        Spec on(Body... bodies) {
            spawnsOn.addAll(List.of(bodies));
            return this;
        }
    }

    private static Spec ntm() {
        return ntm(null);
    }

    private static Spec ntm(@Nullable String path) {
        Spec spec = new Spec(Host.NTM);
        spec.ntmPath = path;
        return spec;
    }

    private static Spec vanilla(Supplier<Block> block) {
        Spec spec = new Spec(Host.VANILLA);
        spec.vanilla = block;
        return spec;
    }

    private static Spec space() {
        return new Spec(Host.SPACE);
    }
}
