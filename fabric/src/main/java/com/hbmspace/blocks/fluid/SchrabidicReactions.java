// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.fluid;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.multiblock.BlockMultiblockCell;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.lib.Library;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class SchrabidicReactions {

    private static final Block BRICK_OBSIDIAN = byId("brick_obsidian");
    private static final Block BRICK_OBSIDIAN_STAIRS = byId("brick_obsidian_stairs");
    private static final Block BLOCK_POLYMER = byId("block_polymer");
    private static final Set<Block> LEGACY_LOGS =
            Set.of(
                    Blocks.OAK_LOG,
                    Blocks.SPRUCE_LOG,
                    Blocks.BIRCH_LOG,
                    Blocks.JUNGLE_LOG,
                    Blocks.ACACIA_LOG,
                    Blocks.DARK_OAK_LOG,
                    Blocks.OAK_WOOD,
                    Blocks.SPRUCE_WOOD,
                    Blocks.BIRCH_WOOD,
                    Blocks.JUNGLE_WOOD,
                    Blocks.ACACIA_WOOD,
                    Blocks.DARK_OAK_WOOD,
                    Blocks.STRIPPED_OAK_LOG,
                    Blocks.STRIPPED_SPRUCE_LOG,
                    Blocks.STRIPPED_BIRCH_LOG,
                    Blocks.STRIPPED_JUNGLE_LOG,
                    Blocks.STRIPPED_ACACIA_LOG,
                    Blocks.STRIPPED_DARK_OAK_LOG,
                    Blocks.STRIPPED_OAK_WOOD,
                    Blocks.STRIPPED_SPRUCE_WOOD,
                    Blocks.STRIPPED_BIRCH_WOOD,
                    Blocks.STRIPPED_JUNGLE_WOOD,
                    Blocks.STRIPPED_ACACIA_WOOD,
                    Blocks.STRIPPED_DARK_OAK_WOOD,
                    Blocks.MANGROVE_LOG,
                    Blocks.MANGROVE_WOOD,
                    Blocks.STRIPPED_MANGROVE_LOG,
                    Blocks.STRIPPED_MANGROVE_WOOD,
                    Blocks.CHERRY_LOG,
                    Blocks.CHERRY_WOOD,
                    Blocks.STRIPPED_CHERRY_LOG,
                    Blocks.STRIPPED_CHERRY_WOOD,
                    Blocks.PALE_OAK_LOG,
                    Blocks.PALE_OAK_WOOD,
                    Blocks.STRIPPED_PALE_OAK_LOG,
                    Blocks.STRIPPED_PALE_OAK_WOOD,
                    Blocks.BAMBOO_BLOCK,
                    Blocks.STRIPPED_BAMBOO_BLOCK,
                    Blocks.CRIMSON_STEM,
                    Blocks.CRIMSON_HYPHAE,
                    Blocks.STRIPPED_CRIMSON_STEM,
                    Blocks.STRIPPED_CRIMSON_HYPHAE,
                    Blocks.WARPED_STEM,
                    Blocks.WARPED_HYPHAE,
                    Blocks.STRIPPED_WARPED_STEM,
                    Blocks.STRIPPED_WARPED_HYPHAE);
    private static final Set<Block> LEGACY_PLANKS =
            Set.of(
                    Blocks.OAK_PLANKS,
                    Blocks.SPRUCE_PLANKS,
                    Blocks.BIRCH_PLANKS,
                    Blocks.JUNGLE_PLANKS,
                    Blocks.ACACIA_PLANKS,
                    Blocks.DARK_OAK_PLANKS,
                    Blocks.MANGROVE_PLANKS,
                    Blocks.CHERRY_PLANKS,
                    Blocks.PALE_OAK_PLANKS,
                    Blocks.BAMBOO_PLANKS,
                    Blocks.BAMBOO_MOSAIC,
                    Blocks.CRIMSON_PLANKS,
                    Blocks.WARPED_PLANKS);
    private static final Set<Block> LEGACY_LEAVES =
            Set.of(
                    Blocks.OAK_LEAVES,
                    Blocks.SPRUCE_LEAVES,
                    Blocks.BIRCH_LEAVES,
                    Blocks.JUNGLE_LEAVES,
                    Blocks.ACACIA_LEAVES,
                    Blocks.DARK_OAK_LEAVES);

    private static final Set<Block> LEGACY_STONE_BRICKS =
            Set.of(
                    Blocks.STONE_BRICKS,
                    Blocks.MOSSY_STONE_BRICKS,
                    Blocks.CRACKED_STONE_BRICKS,
                    Blocks.CHISELED_STONE_BRICKS);
    private static final Set<Block> LEGACY_SANDSTONES =
            Set.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE);
    private static final Set<Block> LEGACY_STONE_SLABS =
            Set.of(
                    Blocks.SMOOTH_STONE_SLAB,
                    Blocks.SANDSTONE_SLAB,
                    Blocks.PETRIFIED_OAK_SLAB,
                    Blocks.COBBLESTONE_SLAB,
                    Blocks.BRICK_SLAB,
                    Blocks.STONE_BRICK_SLAB,
                    Blocks.NETHER_BRICK_SLAB,
                    Blocks.QUARTZ_SLAB);

    private static final Set<Block> NEW_STONE_FAMILIES =
            Set.of(
                    Blocks.GRANITE,
                    Blocks.GRANITE_STAIRS,
                    Blocks.GRANITE_SLAB,
                    Blocks.GRANITE_WALL,
                    Blocks.DIORITE,
                    Blocks.DIORITE_STAIRS,
                    Blocks.DIORITE_SLAB,
                    Blocks.DIORITE_WALL,
                    Blocks.ANDESITE,
                    Blocks.ANDESITE_STAIRS,
                    Blocks.ANDESITE_SLAB,
                    Blocks.ANDESITE_WALL,
                    Blocks.POLISHED_GRANITE,
                    Blocks.POLISHED_GRANITE_STAIRS,
                    Blocks.POLISHED_GRANITE_SLAB,
                    Blocks.POLISHED_DIORITE,
                    Blocks.POLISHED_DIORITE_STAIRS,
                    Blocks.POLISHED_DIORITE_SLAB,
                    Blocks.POLISHED_ANDESITE,
                    Blocks.POLISHED_ANDESITE_STAIRS,
                    Blocks.POLISHED_ANDESITE_SLAB,
                    Blocks.PRISMARINE,
                    Blocks.PRISMARINE_STAIRS,
                    Blocks.PRISMARINE_SLAB,
                    Blocks.PRISMARINE_WALL,
                    Blocks.PRISMARINE_BRICKS,
                    Blocks.PRISMARINE_BRICK_STAIRS,
                    Blocks.PRISMARINE_BRICK_SLAB,
                    Blocks.DARK_PRISMARINE,
                    Blocks.DARK_PRISMARINE_STAIRS,
                    Blocks.DARK_PRISMARINE_SLAB,
                    Blocks.PURPUR_BLOCK,
                    Blocks.PURPUR_PILLAR,
                    Blocks.PURPUR_STAIRS,
                    Blocks.PURPUR_SLAB,
                    Blocks.END_STONE_BRICKS,
                    Blocks.END_STONE_BRICK_STAIRS,
                    Blocks.END_STONE_BRICK_SLAB,
                    Blocks.END_STONE_BRICK_WALL,
                    Blocks.STONE_SLAB,
                    Blocks.STONE_STAIRS,
                    Blocks.SMOOTH_STONE,
                    Blocks.MOSSY_STONE_BRICK_STAIRS,
                    Blocks.MOSSY_STONE_BRICK_SLAB,
                    Blocks.MOSSY_STONE_BRICK_WALL,
                    Blocks.STONE_BRICK_WALL,
                    Blocks.MOSSY_COBBLESTONE_STAIRS,
                    Blocks.MOSSY_COBBLESTONE_SLAB,
                    Blocks.BRICK_WALL,
                    Blocks.RED_NETHER_BRICKS,
                    Blocks.RED_NETHER_BRICK_STAIRS,
                    Blocks.RED_NETHER_BRICK_SLAB,
                    Blocks.RED_NETHER_BRICK_WALL,
                    Blocks.CHISELED_NETHER_BRICKS,
                    Blocks.CRACKED_NETHER_BRICKS,
                    Blocks.NETHER_BRICK_WALL,
                    Blocks.SMOOTH_QUARTZ,
                    Blocks.SMOOTH_QUARTZ_STAIRS,
                    Blocks.SMOOTH_QUARTZ_SLAB,
                    Blocks.QUARTZ_BRICKS,
                    Blocks.BASALT,
                    Blocks.POLISHED_BASALT,
                    Blocks.SMOOTH_BASALT,
                    Blocks.BLACKSTONE,
                    Blocks.BLACKSTONE_STAIRS,
                    Blocks.BLACKSTONE_SLAB,
                    Blocks.BLACKSTONE_WALL,
                    Blocks.POLISHED_BLACKSTONE,
                    Blocks.POLISHED_BLACKSTONE_STAIRS,
                    Blocks.POLISHED_BLACKSTONE_SLAB,
                    Blocks.POLISHED_BLACKSTONE_WALL,
                    Blocks.POLISHED_BLACKSTONE_BRICKS,
                    Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                    Blocks.POLISHED_BLACKSTONE_BRICK_SLAB,
                    Blocks.POLISHED_BLACKSTONE_BRICK_WALL,
                    Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                    Blocks.CHISELED_POLISHED_BLACKSTONE,
                    Blocks.DEEPSLATE,
                    Blocks.COBBLED_DEEPSLATE,
                    Blocks.COBBLED_DEEPSLATE_STAIRS,
                    Blocks.COBBLED_DEEPSLATE_SLAB,
                    Blocks.COBBLED_DEEPSLATE_WALL,
                    Blocks.POLISHED_DEEPSLATE,
                    Blocks.POLISHED_DEEPSLATE_STAIRS,
                    Blocks.POLISHED_DEEPSLATE_SLAB,
                    Blocks.POLISHED_DEEPSLATE_WALL,
                    Blocks.DEEPSLATE_BRICKS,
                    Blocks.DEEPSLATE_BRICK_STAIRS,
                    Blocks.DEEPSLATE_BRICK_SLAB,
                    Blocks.DEEPSLATE_BRICK_WALL,
                    Blocks.DEEPSLATE_TILES,
                    Blocks.DEEPSLATE_TILE_STAIRS,
                    Blocks.DEEPSLATE_TILE_SLAB,
                    Blocks.DEEPSLATE_TILE_WALL,
                    Blocks.CHISELED_DEEPSLATE,
                    Blocks.CRACKED_DEEPSLATE_BRICKS,
                    Blocks.CRACKED_DEEPSLATE_TILES,
                    Blocks.TUFF,
                    Blocks.TUFF_STAIRS,
                    Blocks.TUFF_SLAB,
                    Blocks.TUFF_WALL,
                    Blocks.POLISHED_TUFF,
                    Blocks.POLISHED_TUFF_STAIRS,
                    Blocks.POLISHED_TUFF_SLAB,
                    Blocks.POLISHED_TUFF_WALL,
                    Blocks.TUFF_BRICKS,
                    Blocks.TUFF_BRICK_STAIRS,
                    Blocks.TUFF_BRICK_SLAB,
                    Blocks.TUFF_BRICK_WALL,
                    Blocks.CHISELED_TUFF,
                    Blocks.CHISELED_TUFF_BRICKS,
                    Blocks.SULFUR,
                    Blocks.SULFUR_STAIRS,
                    Blocks.SULFUR_SLAB,
                    Blocks.SULFUR_WALL,
                    Blocks.POLISHED_SULFUR,
                    Blocks.POLISHED_SULFUR_STAIRS,
                    Blocks.POLISHED_SULFUR_SLAB,
                    Blocks.POLISHED_SULFUR_WALL,
                    Blocks.SULFUR_BRICKS,
                    Blocks.SULFUR_BRICK_STAIRS,
                    Blocks.SULFUR_BRICK_SLAB,
                    Blocks.SULFUR_BRICK_WALL,
                    Blocks.CHISELED_SULFUR,
                    Blocks.CINNABAR,
                    Blocks.CINNABAR_STAIRS,
                    Blocks.CINNABAR_SLAB,
                    Blocks.CINNABAR_WALL,
                    Blocks.POLISHED_CINNABAR,
                    Blocks.POLISHED_CINNABAR_STAIRS,
                    Blocks.POLISHED_CINNABAR_SLAB,
                    Blocks.POLISHED_CINNABAR_WALL,
                    Blocks.CINNABAR_BRICKS,
                    Blocks.CINNABAR_BRICK_STAIRS,
                    Blocks.CINNABAR_BRICK_SLAB,
                    Blocks.CINNABAR_BRICK_WALL,
                    Blocks.CHISELED_CINNABAR,
                    Blocks.CALCITE,
                    Blocks.DRIPSTONE_BLOCK,
                    Blocks.POINTED_DRIPSTONE,
                    Blocks.SULFUR_SPIKE);

    private SchrabidicReactions() {}

    private static Block byId(String id) {
        return BuiltInRegistries.BLOCK.getOptional(Library.id(id)).orElseThrow();
    }

    public static void react(ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null) return;
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (state.isAir()) return;
        Block materialCarrier = block;
        if (block instanceof BlockMultiblockCell) {
            long core =
                    MultiblockSurface.indexedCorePacked(level, pos.getX(), pos.getY(), pos.getZ());
            if (!MultiblockSurface.hasCore(core)) return;
            materialCarrier = level.getBlockState(BlockPos.of(core)).getBlock();
        }
        double x = pos.getX() + random.nextFloat();
        double y = pos.getY() + random.nextFloat();
        double z = pos.getZ() + random.nextFloat();

        if (block != ModBlocks.SCHRABIDIC_BLOCK.get()
                && SchrabidicLegacyMaterials.water(materialCarrier)) {
            level.setBlockAndUpdate(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState());
        } else if (LEGACY_LOGS.contains(block) && random.nextInt(4) == 0) {
            change(level, pos, ModBlocks.WASTE_LOG.get().defaultBlockState());
        } else if (LEGACY_PLANKS.contains(block) && random.nextInt(4) == 0) {
            change(level, pos, ModBlocks.WASTE_PLANKS.get().defaultBlockState());
        } else if ((LEGACY_LEAVES.contains(block)
                        || SchrabidicLegacyMaterials.iron(materialCarrier)
                                && block != ModBlocks.GRAVEL_OBSIDIAN.get()
                        || SchrabidicLegacyMaterials.glass(materialCarrier)
                                && block != ModBlocks.REINFORCED_GLASS.get())
                && random.nextInt(4) == 0) {
            destroy(level, pos, x, y, z);
        } else if ((block == Blocks.STONE
                        || block == Blocks.STONE_BRICK_STAIRS
                        || LEGACY_STONE_BRICKS.contains(block)
                        || legacySingleSlab(state)
                        || NEW_STONE_FAMILIES.contains(block))
                && random.nextInt(4) == 0) {
            change(level, pos, Blocks.COBBLESTONE.defaultBlockState());
        } else if (block == Blocks.COBBLESTONE && random.nextInt(4) == 0) {
            change(level, pos, Blocks.GRAVEL.defaultBlockState());
        } else if (block == Blocks.GRAVEL && random.nextInt(4) == 0) {
            change(level, pos, Blocks.CLAY.defaultBlockState());
        } else if ((block == Blocks.OBSIDIAN
                        || block == Blocks.CRYING_OBSIDIAN
                        || block == BRICK_OBSIDIAN
                        || block == BRICK_OBSIDIAN_STAIRS)
                && random.nextInt(4) == 0) {
            change(level, pos, ModBlocks.GRAVEL_OBSIDIAN.get().defaultBlockState());
        } else if ((LEGACY_SANDSTONES.contains(block)
                        || block == Blocks.RED_SANDSTONE
                        || block == Blocks.CHISELED_RED_SANDSTONE
                        || block == Blocks.CUT_RED_SANDSTONE
                        || block == Blocks.END_STONE)
                && random.nextInt(4) == 0) {
            change(level, pos, Blocks.SAND.defaultBlockState());
        } else if (block.getExplosionResistance() < 1.2F
                && block != BLOCK_POLYMER
                && block != ModBlocks.GRAVEL_OBSIDIAN.get()
                && block != Blocks.SAND
                && block != Blocks.RED_SAND
                && block != Blocks.CLAY) {
            destroy(level, pos, x, y, z);
        }
    }

    private static boolean legacySingleSlab(BlockState state) {
        return LEGACY_STONE_SLABS.contains(state.getBlock())
                && state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE;
    }

    private static void change(ServerLevel level, BlockPos pos, BlockState result) {
        fizz(level, pos);
        level.setBlockAndUpdate(pos, result);
    }

    private static void destroy(ServerLevel level, BlockPos pos, double x, double y, double z) {
        fizz(level, pos);
        level.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0D, 0D, 0D, 0D);
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    private static void fizz(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 1.0F);
    }
}
