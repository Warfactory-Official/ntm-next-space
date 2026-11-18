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

package com.hbmspace.blocks;

import com.hbm.blocks.fluid.ClassicFluid;
import com.hbm.blocks.generic.BaseFallingBlock;
import com.hbm.blocks.generic.StalactiteBlock;
import com.hbm.blocks.generic.StalagmiteBlock;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.fluid.GenericFluidBlock;
import com.hbmspace.blocks.generic.FallingTintBlock;
import com.hbmspace.blocks.generic.GeysirVolcanicBlock;
import com.hbmspace.blocks.machine.AlgaeFilmBlock;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.items.TerrainItemsSpace;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class TerrainBlocksSpace {
    private static final TagKey<Item> STONES =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "stones"));

    public static final Reg.Handle<Block> DUNA_ROCK =
            HbmSpace.REG.blockItem("duna_rock", Block::new, () -> rock(1.5F, 6.0F));

    public static final Reg.Handle<BaseFallingBlock> EVE_SILT =
            HbmSpace.REG.blockItem("eve_silt", BaseFallingBlock::new, () -> sand(0.5F, 0.5F));
    public static final Reg.Handle<Block> EVE_ROCK =
            HbmSpace.REG.blockItem("eve_rock", Block::new, () -> rock(1.5F, 6.0F));

    public static final Reg.Handle<FallingTintBlock> LAYTHE_SILT =
            HbmSpace.REG.blockItem("laythe_silt", FallingTintBlock::new, () -> sand(0.5F, 0.5F));
    public static final Reg.Handle<Block> LAYTHE_ROOT =
            HbmSpace.REG.blockItem(
                    "laythe_root",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.WOOD)
                                    .strength(2.0F, 2.0F)
                                    .sound(SoundType.STONE)
                                    .ignitedByLava());

    public static final Reg.Handle<Block> IKE_REGOLITH =
            HbmSpace.REG.blockItem("ike_regolith", Block::new, () -> rock(1.5F, 6.0F));

    public static final Reg.Handle<Block> TUMOR =
            HbmSpace.REG.blockItem(
                    "tumor",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.CLAY)
                                    .strength(1.0F, 1.0F)
                                    .sound(SoundType.SNOW));
    public static final Reg.Handle<StalagmiteBlock> STALAGMITE_FLESH =
            fleshSpike("stalagmite", StalagmiteBlock::new);
    public static final Reg.Handle<StalactiteBlock> STALACTITE_FLESH =
            fleshSpike("stalactite", StalactiteBlock::new);

    public static final Reg.Handle<Block> RUBBER_SILT =
            HbmSpace.REG.blockItem(
                    "rubber_silt",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.SAND)
                                    .strength(1.0F, 0.6F)
                                    .sound(SoundType.GRAVEL));
    public static final Reg.Handle<BaseFallingBlock> VINYL_SAND =
            HbmSpace.REG.blockItem("vinyl_sand", BaseFallingBlock::new, () -> sand(1.0F, 0.6F));

    public static final Reg.Handle<Block> VINYL_PLANKS = planks("vinyl_planks");
    public static final Reg.Handle<Block> PVC_PLANKS = planks("pvc_planks");

    public static final Reg.Handle<GeysirVolcanicBlock> GEYSIR_VOLCANIC =
            HbmSpace.REG.block(
                    "geysir_volcanic",
                    GeysirVolcanicBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(2.0F, 2.0F)
                                    .sound(SoundType.STONE)
                                    .lightLevel(s -> 15)
                                    .noLootTable()
                                    .requiresCorrectToolForDrops());
    public static final Reg.Handle<VolcanoV2Block> GEYSIR_ELECTRIC =
            HbmSpace.REG.blockItem(
                    "geysir_electric", VolcanoV2Block::new, TerrainBlocksSpace::geysir);
    public static final Reg.Handle<GeysierDCMBlock> GEYSIR_CHLORIC =
            HbmSpace.REG.blockItem(
                    "geysir_chloric", GeysierDCMBlock::new, TerrainBlocksSpace::geysir);

    public static final ClassicFluid.Pair MERCURY_FLUID =
            fluid(
                    "mercury_fluid",
                    1840,
                    5,
                    "mercury",
                    () -> TerrainBlocksSpace.MERCURY_BLOCK.get(),
                    () -> new ItemStack(TerrainItemsSpace.BUCKET_MERCURY.get()),
                    () -> NTMFluids.MERCURY);
    public static final Reg.Handle<GenericFluidBlock> MERCURY_BLOCK =
            fluidBlock("mercury_block", MERCURY_FLUID, "mercury");
    public static final ClassicFluid.Pair BROMINE_FLUID =
            fluid(
                    "bromine_fluid",
                    3000,
                    15,
                    "bromine",
                    () -> TerrainBlocksSpace.BROMINE_BLOCK.get(),
                    () -> new ItemStack(TerrainItemsSpace.BUCKET_BROMINE.get()),
                    FluidsSpace::bromine);
    public static final Reg.Handle<GenericFluidBlock> BROMINE_BLOCK =
            fluidBlock("bromine_block", BROMINE_FLUID, "bromine");
    public static final ClassicFluid.Pair CCL_FLUID =
            fluid(
                    "ccl_fluid",
                    1840,
                    5,
                    "ccl",
                    () -> TerrainBlocksSpace.CCL_BLOCK.get(),
                    () -> new ItemStack(TerrainItemsSpace.BUCKET_CCL.get()),
                    FluidsSpace::ccl);
    public static final Reg.Handle<GenericFluidBlock> CCL_BLOCK =
            fluidBlock("ccl_block", CCL_FLUID, "ccl");

    public static final Reg.Handle<AlgaeFilmBlock> ALGAE_FILM =
            HbmSpace.REG.blockItem(
                    "algae_film",
                    AlgaeFilmBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 2));

    static {
        CreativeContents.insertAfter(
                Tab.MACHINES,
                RegistryUtil.id("gas_explosive"),
                () -> new ItemStack(ALGAE_FILM.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                RegistryUtil.id("stalagmite_asbestos"),
                () -> new ItemStack(STALAGMITE_FLESH.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                RegistryUtil.id("stalactite_asbestos"),
                () -> new ItemStack(STALACTITE_FLESH.get()));
    }

    private TerrainBlocksSpace() {}

    private static <B extends Block> Reg.Handle<B> fleshSpike(
            String kind, Function<BlockBehaviour.Properties, B> factory) {
        String texture = "block/" + kind + ".flesh";
        String key = "tile." + kind + ".flesh.name";
        return HbmSpace.REG.blockItem(
                kind + "_flesh",
                factory,
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.STONE)
                                .strength(0.5F, 1.2F)
                                .sound(SoundType.STONE)
                                .noOcclusion()
                                .noCollision()
                                .forceSolidOn()
                                .requiresCorrectToolForDrops());
    }

    private static BlockBehaviour.Properties rock(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(hardness, resistance)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties sand(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(hardness, resistance)
                .sound(SoundType.SAND);
    }

    private static BlockBehaviour.Properties geysir() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 2.0F)
                .sound(SoundType.STONE)
                .lightLevel(s -> 15)
                .requiresCorrectToolForDrops();
    }

    private static Reg.Handle<Block> planks(String name) {
        return HbmSpace.REG.blockItem(
                name,
                Block::new,
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.WOOD)
                                .strength(1.0F, 0.6F)
                                .sound(SoundType.WOOD)
                                .ignitedByLava());
    }

    private static ClassicFluid.Pair fluid(
            String name,
            int density,
            int tickDelay,
            String sprite,
            Supplier<GenericFluidBlock> block,
            Supplier<ItemStack> bucket,
            Supplier<Fluid> kind) {
        return HbmSpace.REG.classicFluid(
                name,
                new ClassicFluid.Spec(
                        ClassicFluid.Physics.WATER,
                        8,
                        tickDelay,
                        density,
                        true,
                        Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "block/fluid/" + sprite),
                        block,
                        bucket,
                        kind));
    }

    private static Reg.Handle<GenericFluidBlock> fluidBlock(
            String name, ClassicFluid.Pair fluid, String sprite) {
        return HbmSpace.REG.block(
                name,
                props -> new GenericFluidBlock(fluid.source().get(), props),
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.WATER)
                                .replaceable()
                                .noCollision()
                                .strength(0.0F, 300.0F)
                                .pushReaction(((PushReaction) (PushReaction.POPPED)))
                                .noLootTable()
                                .liquid()
                                .sound(SoundType.EMPTY));
    }
}
