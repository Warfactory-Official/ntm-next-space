// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.MachineMilkReformer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocksSpace {
    public static final Reg.Handle<Block> MOON_ROCK =
            HbmSpace.REG.blockItem(
                    "moon_rock",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(1.5F, 6.0F)
                                    .sound(SoundType.STONE)
                                    .requiresCorrectToolForDrops());

    public static final Reg.Handle<ColoredFallingBlock> DUNA_SANDS =
            HbmSpace.REG.blockItem(
                    "duna_sands",
                    props -> new ColoredFallingBlock(new ColorRGBA(0x94412A), props),
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.SAND)
                                    .strength(0.5F)
                                    .sound(SoundType.SAND));
    public static final Reg.Handle<Block> DUNA_COBBLE = rock("duna_cobble", 2.0F);
    public static final Reg.Handle<Block> DRY_ICE =
            HbmSpace.REG.blockItem(
                    "dry_ice",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.ICE)
                                    .strength(0.5F)
                                    .sound(SoundType.STONE));
    public static final Reg.Handle<Block> FERRIC_CLAY =
            HbmSpace.REG.blockItem(
                    "ferric_clay",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.CLAY)
                                    .strength(0.6F)
                                    .sound(SoundType.GRAVEL));
    public static final Reg.Handle<Block> IKE_STONE = rock("ike_stone", 1.5F);
    public static final Reg.Handle<Block> DRES_ROCK = rock("dres_rock", 1.5F);
    public static final Reg.Handle<Block> MOHO_REGOLITH = rock("moho_regolith", 1.5F);
    public static final Reg.Handle<Block> MOHO_STONE = rock("moho_stone", 1.5F);
    public static final Reg.Handle<Block> MINMUS_REGOLITH = rock("minmus_regolith", 1.5F);
    public static final Reg.Handle<Block> MINMUS_STONE = rock("minmus_stone", 1.5F);
    public static final Reg.Handle<Block> MINMUS_SMOOTH =
            HbmSpace.REG.blockItem(
                    "minmus_smooth",
                    Block::new,
                    ModBlocksSpace::rockProperties,
                    (block, props) -> new DescBlockItem(block, props, 1, DescBlockItem.Desc.PLAIN));

    public static final Reg.Handle<Block> STONE_RESOURCE_CONGLOMERATE =
            HbmSpace.REG.blockItem(
                    "stone_resource_conglomerate",
                    Block::new,
                    () -> rockProperties().strength(5.0F, 6.0F));
    public static final Reg.Handle<Block> STONE_RESOURCE_CALCIUM =
            HbmSpace.REG.blockItem(
                    "stone_resource_calcium",
                    Block::new,
                    () -> rockProperties().strength(5.0F, 6.0F));

    public static final Reg.Handle<Block> DECO_STAINLESS =
            HbmSpace.REG.blockItem(
                    "deco_stainless",
                    Block::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .sound(SoundType.STONE)
                                    .requiresCorrectToolForDrops());

    private static Reg.Handle<Block> rock(String name, float hardness) {
        return HbmSpace.REG.blockItem(
                name, Block::new, () -> rockProperties().strength(hardness, 6.0F));
    }

    private static BlockBehaviour.Properties rockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    public static final Reg.Handle<MachineMilkReformer> MACHINE_MILK_REFORMER =
            HbmSpace.REG.blockItem(
                    "machine_milk_reformer",
                    MachineMilkReformer::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion());

    static {
        CreativeContents.insertAfter(
                Tab.BLOCKS, Library.id("moon_turf"), () -> new ItemStack(MOON_ROCK.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS, Library.id("deco_asbestos"), () -> new ItemStack(DECO_STAINLESS.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Library.id("stone_resource_bauxite"),
                () -> new ItemStack(STONE_RESOURCE_CONGLOMERATE.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "stone_resource_conglomerate"),
                () -> new ItemStack(STONE_RESOURCE_CALCIUM.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Library.id("machine_catalytic_reformer"),
                () -> new ItemStack(MACHINE_MILK_REFORMER.get()));
    }

    private ModBlocksSpace() {}
}
