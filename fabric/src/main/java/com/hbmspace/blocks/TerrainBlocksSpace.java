// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blocks.fluid.ClassicFluid;
import com.hbm.blocks.generic.BlockFallingBase;
import com.hbm.blocks.generic.BlockStalactite;
import com.hbm.blocks.generic.BlockStalagmite;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.fluid.GenericFluidBlock;
import com.hbmspace.blocks.generic.BlockFallingTint;
import com.hbmspace.blocks.generic.BlockGeysirVolcanic;
import com.hbmspace.blocks.machine.BlockAlgaeFilm;
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

    public static final Reg.Handle<BlockFallingBase> EVE_SILT =
            HbmSpace.REG.blockItem("eve_silt", BlockFallingBase::new, () -> sand(0.5F, 0.5F));
    public static final Reg.Handle<Block> EVE_ROCK =
            HbmSpace.REG.blockItem("eve_rock", Block::new, () -> rock(1.5F, 6.0F));

    public static final Reg.Handle<BlockFallingTint> LAYTHE_SILT =
            HbmSpace.REG.blockItem("laythe_silt", BlockFallingTint::new, () -> sand(0.5F, 0.5F));
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
    public static final Reg.Handle<BlockStalagmite> STALAGMITE_FLESH =
            fleshSpike("stalagmite", BlockStalagmite::new);
    public static final Reg.Handle<BlockStalactite> STALACTITE_FLESH =
            fleshSpike("stalactite", BlockStalactite::new);

    public static final Reg.Handle<Block> RUBBER_SILT =
            HbmSpace.REG.blockItem(
                    "rubber_silt",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.SAND)
                                    .strength(1.0F, 0.6F)
                                    .sound(SoundType.GRAVEL));
    public static final Reg.Handle<BlockFallingBase> VINYL_SAND =
            HbmSpace.REG.blockItem("vinyl_sand", BlockFallingBase::new, () -> sand(1.0F, 0.6F));

    public static final Reg.Handle<Block> VINYL_PLANKS = planks("vinyl_planks");
    public static final Reg.Handle<Block> PVC_PLANKS = planks("pvc_planks");

    public static final Reg.Handle<BlockGeysirVolcanic> GEYSIR_VOLCANIC =
            HbmSpace.REG.block(
                    "geysir_volcanic",
                    BlockGeysirVolcanic::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(2.0F, 2.0F)
                                    .sound(SoundType.STONE)
                                    .lightLevel(s -> 15)
                                    .noLootTable()
                                    .requiresCorrectToolForDrops());
    public static final Reg.Handle<BlockVolcanoV2> GEYSIR_ELECTRIC =
            HbmSpace.REG.blockItem(
                    "geysir_electric", BlockVolcanoV2::new, TerrainBlocksSpace::geysir);
    public static final Reg.Handle<BlockGeysierDCM> GEYSIR_CHLORIC =
            HbmSpace.REG.blockItem(
                    "geysir_chloric", BlockGeysierDCM::new, TerrainBlocksSpace::geysir);

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

    public static final Reg.Handle<BlockAlgaeFilm> ALGAE_FILM =
            HbmSpace.REG.blockItem(
                    "algae_film",
                    BlockAlgaeFilm::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 2));

    static {
        CreativeContents.insertAfter(
                Tab.MACHINES, Library.id("gas_explosive"), () -> new ItemStack(ALGAE_FILM.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Library.id("stalagmite_asbestos"),
                () -> new ItemStack(STALAGMITE_FLESH.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Library.id("stalactite_asbestos"),
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
                                .pushReaction(PushReaction.DESTROY)
                                .noLootTable()
                                .liquid()
                                .sound(SoundType.EMPTY));
    }
}
