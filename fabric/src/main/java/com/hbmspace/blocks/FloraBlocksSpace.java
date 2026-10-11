// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.generic.BlockRubberPlant;
import com.hbmspace.blocks.generic.BlockTallPlantWater;
import com.hbmspace.blocks.generic.BlockWildFlower;
import com.hbmspace.client.model.CoralModel;
import com.hbmspace.client.model.KelpModel;
import com.hbmspace.dim.tekto.TTree;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.items.TerrainItemsSpace;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mov.movblock.tenon.strip.api.DropSafe;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class FloraBlocksSpace {

    public enum Coral {
        TUBE,
        BRAIN,
        BUBBLE,
        FIRE,
        HORN;

        @DropSafe
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum RubberPlant {
        GRASS,
        FERN,
        SHRUB
    }

    public enum RubberTall {
        CACT,
        BUSH,
        FLOWER
    }

    private static final String SPIKE = "models/plant_spike.obj";
    private static final String[] SPIKE_PARTS = {
        "cube", "cube.001", "cube.002", "cube.003", "cube.004", "cube.005"
    };

    public static final Reg.Handle<BlockTallPlantWater> PLANT_TALL_LAYTHE =
            HbmSpace.REG.blockItem(
                    "plant_tall_laythe", BlockTallPlantWater::new, FloraBlocksSpace::waterPlant);
    public static final Reg.Handle<BlockKelp> LAYTHE_KELP =
            HbmSpace.REG
                    .blockItem("laythe_kelp", BlockKelp::new, FloraBlocksSpace::waterPlant)
                    .bakedBy(() -> new KelpModel.Family());
    public static final Reg.Handle<BlockWaterPlant> LAYTHE_SEAGRASS =
            waterGrass("laythe_seagrass", 0);
    public static final Reg.Handle<BlockWaterPlant> LAYTHE_GLOWGRASS =
            waterGrass("laythe_glowgrass", 15);

    @SuppressWarnings("unchecked")
    public static final Reg.Handle<BlockCoral>[] LAYTHE_CORAL =
            new Reg.Handle[Coral.values().length];

    @SuppressWarnings("unchecked")
    public static final Reg.Handle<Block>[] LAYTHE_CORAL_BLOCK =
            new Reg.Handle[Coral.values().length];

    static {
        for (Coral coral : Coral.values()) {
            String sheet = "block/laythe_coral." + coral.id();
            LAYTHE_CORAL[coral.ordinal()] =
                    HbmSpace.REG
                            .blockItem(
                                    "laythe_coral_" + coral.id(),
                                    BlockCoral::new,
                                    FloraBlocksSpace::waterPlant)
                            .bakedBy(() -> new CoralModel.Family());
        }
        for (Coral coral : Coral.values()) {
            LAYTHE_CORAL_BLOCK[coral.ordinal()] =
                    HbmSpace.REG.blockItem(
                            "laythe_coral_block_" + coral.id(),
                            Block::new,
                            () ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.PLANT)
                                            .strength(0.5F, 0.5F)
                                            .sound(SoundType.GRASS)
                                            .pushReaction(PushReaction.DESTROY));
        }
    }

    @SuppressWarnings("unchecked")
    public static final Reg.Handle<BlockRubberPlant>[] RUBBER_PLANT =
            new Reg.Handle[RubberPlant.values().length];

    static {
        for (RubberPlant plant : RubberPlant.values()) {
            String sheet = "block/rubber_plant." + plant.name().toLowerCase(Locale.ROOT);
            RUBBER_PLANT[plant.ordinal()] =
                    HbmSpace.REG.blockItem(
                            "rubber_plant_" + plant.name().toLowerCase(Locale.ROOT),
                            props -> new BlockRubberPlant(props, FloraBlocksSpace::rubberSoil),
                            () ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.PLANT)
                                            .replaceable()
                                            .noCollision()
                                            .instabreak()
                                            .sound(SoundType.GRASS)
                                            .pushReaction(PushReaction.DESTROY));
        }
    }

    public static final Reg.Handle<BlockWildFlower> PLANT_FLOWER_STRAWBERRY =
            wildFlower(
                    "strawberry",
                    6,
                    "hbmspace:strawberry",
                    () -> TerrainItemsSpace.STRAWBERRY.get());
    public static final Reg.Handle<BlockWildFlower> PLANT_FLOWER_MINT =
            wildFlower(
                    "mint", 7, "hbmspace:mint_leaves", () -> TerrainItemsSpace.MINT_LEAVES.get());

    public static final Reg.Handle<BlockCrop> CROP_STRAWBERRY =
            crop(
                    "crop_strawberry",
                    Blocks.FARMLAND::defaultBlockState,
                    FloraBlocksSpace::breathesAir,
                    "strawberry",
                    () -> TerrainItemsSpace.STRAWBERRY.get(),
                    true);
    public static final Reg.Handle<BlockCrop> CROP_MINT =
            crop(
                    "crop_mint",
                    Blocks.FARMLAND::defaultBlockState,
                    FloraBlocksSpace::breathesAir,
                    "mint",
                    () -> TerrainItemsSpace.MINT_LEAVES.get(),
                    true);
    public static final Reg.Handle<BlockCrop> CROP_PARAFFIN =
            crop(
                    "crop_paraffin",
                    () -> FloraBlocksSpace.RUBBER_FARMLAND.get().defaultBlockState(),
                    FloraBlocksSpace::breathesTektoAir,
                    "paraffin",
                    () -> TerrainItemsSpace.PARAFFIN_SEEDS.get(),
                    false);
    public static final Reg.Handle<BlockCrop> CROP_COFFEE =
            crop(
                    "crop_coffee",
                    Blocks.FARMLAND::defaultBlockState,
                    FloraBlocksSpace::breathesAir,
                    "coffee",
                    () -> TerrainItemsSpace.BEAN_RAW.get(),
                    true);
    public static final Reg.Handle<BlockCrop> CROP_TEA =
            crop(
                    "crop_tea",
                    Blocks.FARMLAND::defaultBlockState,
                    FloraBlocksSpace::breathesAir,
                    "tea",
                    () -> TerrainItemsSpace.TEASEEDS.get(),
                    true);

    static {
        for (Reg.Handle<BlockCrop> crop :
                List.of(CROP_STRAWBERRY, CROP_MINT, CROP_PARAFFIN, CROP_COFFEE, CROP_TEA)) {
            Supplier<ItemStack> entry = () -> new ItemStack(crop.get().asItem());
            CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
                    .register(output -> output.accept(entry.get()));
        }
    }

    public static final Reg.Handle<BlockNTSapling> SAPLING_VINYL =
            sapling(
                    "sapling_vinyl",
                    0,
                    () ->
                            new TTree(
                                    2,
                                    4,
                                    5,
                                    3,
                                    2,
                                    false,
                                    FloraBlocksSpace.VINYL_LOG,
                                    FloraBlocksSpace.PET_LEAVES));
    public static final Reg.Handle<BlockNTSapling> SAPLING_PVC =
            sapling(
                    "sapling_pvc",
                    1,
                    () ->
                            new TTree(
                                    2,
                                    5,
                                    7,
                                    4,
                                    3,
                                    false,
                                    FloraBlocksSpace.PVC_LOG,
                                    FloraBlocksSpace.RUBBER_LEAVES));

    public static final Reg.Handle<RotatedPillarBlock> PRIMED_LOG = log("primed_log", "primed_log");
    public static final Reg.Handle<RotatedPillarBlock> EU_LOG = log("eu_log", "eu_log");
    public static final Reg.Handle<RotatedPillarBlock> BF_LOG = log("bf_log", "bf_log");
    public static final Reg.Handle<RotatedPillarBlock> LATTICE_LOG = log("lattice_log", "rad_log");
    public static final Reg.Handle<RotatedPillarBlock> PVC_LOG = log("pvc_log", "pvc_log");
    public static final Reg.Handle<BlockRubberLeaves> RUBBER_LEAVES =
            leaves("rubber_leaves", "hbmspace:leaf_rubber", "hbmspace:sapling_pvc");
    public static final Reg.Handle<RubberGrass> RUBBER_GRASS =
            HbmSpace.REG.blockItem(
                    "rubber_grass",
                    RubberGrass::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.GRASS)
                                    .strength(0.5F, 1.5F)
                                    .sound(SoundType.GRASS));

    @SuppressWarnings("unchecked")
    public static final Reg.Handle<BlockRubberPlant>[] RUBBER_TALL =
            new Reg.Handle[RubberTall.values().length];

    static {
        for (RubberTall tall : RubberTall.values()) {
            String name = tall.name().toLowerCase(Locale.ROOT);
            String sheet = "block/rubber_tall." + name;
            RUBBER_TALL[tall.ordinal()] =
                    HbmSpace.REG.blockItem(
                            "rubber_tall_" + name,
                            props -> new BlockRubberPlant(props, FloraBlocksSpace::cactusSoil),
                            () ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.PLANT)
                                            .noCollision()
                                            .instabreak()
                                            .sound(SoundType.GRASS)
                                            .pushReaction(PushReaction.DESTROY));
        }
    }

    public static final Reg.Handle<BlockRubberFarm> RUBBER_FARMLAND =
            HbmSpace.REG.blockItem(
                    "rubber_farmland",
                    BlockRubberFarm::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.SAND)
                                    .strength(1.0F, 0.6F)
                                    .sound(SoundType.GRAVEL));
    public static final Reg.Handle<VineBlock> VINYL_VINE =
            HbmSpace.REG.blockItem(
                    "vinyl_vine",
                    VineBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.PLANT)
                                    .replaceable()
                                    .noCollision()
                                    .randomTicks()
                                    .strength(1.0F, 0.6F)
                                    .sound(SoundType.GRASS)
                                    .ignitedByLava()
                                    .pushReaction(PushReaction.DESTROY));
    public static final Reg.Handle<RotatedPillarBlock> VINYL_LOG = log("vinyl_log", "vinyl_log");
    public static final Reg.Handle<BlockRubberLeaves> PET_LEAVES =
            leaves("pet_leaves", "hbmspace:leaf_pet", "hbmspace:sapling_vinyl");

    public static final Reg.Handle<Block> ASTROTURF =
            HbmSpace.REG.blockItem(
                    "astroturf",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.GRASS)
                                    .strength(0.6F, 0.6F)
                                    .sound(SoundType.GRASS)
                                    .isValidSpawn((state, level, pos, type) -> false));

    static {
        after("vinyl_tile_small", TerrainBlocksSpace.VINYL_PLANKS, TerrainBlocksSpace.PVC_PLANKS);
        after("plant_flower_cd1", PLANT_FLOWER_STRAWBERRY, PLANT_FLOWER_MINT);
        after("plant_tall_cd4", PLANT_TALL_LAYTHE, LAYTHE_KELP, LAYTHE_SEAGRASS, LAYTHE_GLOWGRASS);
        after("plant_tall_cd4", LAYTHE_CORAL);
        after("plant_tall_cd4", LAYTHE_CORAL_BLOCK);
        after("plant_dead_fern", RUBBER_PLANT);
        after(
                "moon_turf",
                SAPLING_VINYL,
                SAPLING_PVC,
                ModBlocksSpace.DUNA_SANDS,
                ModBlocksSpace.DUNA_COBBLE,
                TerrainBlocksSpace.DUNA_ROCK,
                ModBlocksSpace.DRY_ICE,
                ModBlocksSpace.FERRIC_CLAY,
                TerrainBlocksSpace.EVE_SILT,
                TerrainBlocksSpace.EVE_ROCK,
                TerrainBlocksSpace.LAYTHE_SILT,
                TerrainBlocksSpace.LAYTHE_ROOT,
                TerrainBlocksSpace.IKE_REGOLITH,
                ModBlocksSpace.IKE_STONE,
                ModBlocksSpace.DRES_ROCK,
                ModBlocksSpace.MOHO_REGOLITH,
                ModBlocksSpace.MOHO_STONE,
                ModBlocksSpace.MINMUS_REGOLITH,
                ModBlocksSpace.MINMUS_STONE,
                ModBlocksSpace.MINMUS_SMOOTH);
        after(
                "impact_dirt",
                TerrainBlocksSpace.TUMOR,
                PRIMED_LOG,
                EU_LOG,
                BF_LOG,
                LATTICE_LOG,
                PVC_LOG,
                RUBBER_LEAVES,
                RUBBER_GRASS,
                TerrainBlocksSpace.RUBBER_SILT);
        after("impact_dirt", RUBBER_TALL);
        after(
                "impact_dirt",
                RUBBER_FARMLAND,
                TerrainBlocksSpace.VINYL_SAND,
                VINYL_VINE,
                VINYL_LOG,
                PET_LEAVES,
                ASTROTURF);
    }

    private FloraBlocksSpace() {}

    @SafeVarargs
    private static void after(String anchor, Reg.Handle<? extends Block>... blocks) {
        for (Reg.Handle<? extends Block> block : blocks) {
            CreativeContents.insertAfter(
                    Tab.BLOCKS, Library.id(anchor), () -> new ItemStack(block.get()));
        }
    }

    private static BlockBehaviour.Properties waterPlant() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WATER)
                .replaceable()
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .pushReaction(PushReaction.DESTROY);
    }

    private static Reg.Handle<BlockWaterPlant> waterGrass(String name, int light) {
        String sheet = "block/" + name;
        return HbmSpace.REG.blockItem(
                name, BlockWaterPlant::new, () -> waterPlant().lightLevel(s -> light));
    }

    private static boolean rubberSoil(BlockState state) {
        return state.is(RUBBER_GRASS.get())
                || state.is(TerrainBlocksSpace.RUBBER_SILT.get())
                || state.is(RUBBER_FARMLAND.get());
    }

    private static boolean cactusSoil(BlockState state) {
        return state.is(TerrainBlocksSpace.VINYL_SAND.get()) || rubberSoil(state);
    }

    private static boolean breathesAir(CBT_Atmosphere atmosphere) {
        return atmosphere.hasFluid(FluidsSpace.earthair(), 0.1)
                || atmosphere.hasFluid(NTMFluids.OXYGEN, 0.1);
    }

    private static boolean breathesTektoAir(CBT_Atmosphere atmosphere) {
        return atmosphere.hasFluid(FluidsSpace.tektoair(), 0.1)
                || atmosphere.hasFluid(NTMFluids.CHLORINE, 0.1);
    }

    private static Reg.Handle<BlockCrop> crop(
            String name,
            Supplier<BlockState> soil,
            Predicate<CBT_Atmosphere> atmosphere,
            String sheet,
            Supplier<? extends Item> seed,
            boolean canHydro) {
        int[] stages = {1, 1, 2, 2, 3, 3, 4, 5};
        return HbmSpace.REG.blockItem(
                name,
                props -> new BlockCrop(props, soil, atmosphere, seed, canHydro),
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.PLANT)
                                .noCollision()
                                .randomTicks()
                                .instabreak()
                                .sound(SoundType.GRASS)
                                .pushReaction(PushReaction.DESTROY));
    }

    private static Reg.Handle<BlockWildFlower> wildFlower(
            String type, int meta, String dropId, Supplier<? extends Item> drop) {
        String sheet = "block/plant_flower." + type;
        String key = "tile.plant_flower." + type + ".name";
        return HbmSpace.REG.blockItem(
                "plant_flower_" + type,
                props -> new BlockWildFlower(props, drop),
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.PLANT)
                                .noCollision()
                                .noOcclusion()
                                .instabreak()
                                .sound(SoundType.GRASS)
                                .pushReaction(PushReaction.DESTROY));
    }

    private static Reg.Handle<BlockNTSapling> sapling(String name, int meta, Supplier<TTree> tree) {
        String sheet = "block/" + name;
        return HbmSpace.REG.blockItem(
                name,
                props -> new BlockNTSapling(props, tree),
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.PLANT)
                                .noCollision()
                                .randomTicks()
                                .instabreak()
                                .sound(SoundType.GRASS)
                                .pushReaction(PushReaction.DESTROY));
    }

    private static Reg.Handle<RotatedPillarBlock> log(String name, String sheets) {
        return HbmSpace.REG.blockItem(
                name,
                RotatedPillarBlock::new,
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.WOOD)
                                .strength(0.5F, 1.5F)
                                .sound(SoundType.WOOD)
                                .ignitedByLava());
    }

    private static Reg.Handle<BlockRubberLeaves> leaves(String name, String leaf, String sapling) {
        return HbmSpace.REG.blockItem(
                name,
                BlockRubberLeaves::new,
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.PLANT)
                                .strength(0.5F, 1.5F)
                                .randomTicks()
                                .sound(SoundType.GRASS)
                                .noOcclusion()
                                .isValidSpawn(Blocks::ocelotOrParrot)
                                .isSuffocating((state, level, pos) -> false)
                                .isViewBlocking((state, level, pos) -> false)
                                .isRedstoneConductor((state, level, pos) -> false)
                                .ignitedByLava()
                                .pushReaction(PushReaction.DESTROY));
    }
}
