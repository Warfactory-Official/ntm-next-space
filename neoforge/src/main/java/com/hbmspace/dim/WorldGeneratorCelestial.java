// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.items.ModItems;
import com.hbm.items.special.BedrockOreSet;
import com.hbm.lib.Library;
import com.hbm.world.NtmWorldgenFields;
import com.hbm.world.feature.BedrockOreFeature;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DepthDeposit;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import com.hbmspace.dim.legacy.feature.WorldGenMinable;
import com.hbmspace.items.special.CelestialBedrockOre;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static com.hbmspace.data.SpaceData.*;

public final class WorldGeneratorCelestial {

    private record Entry(
            int weight, @Nullable ResourceKey<Level> dimension, LegacyWorldGenerator generator) {}

    private static final List<Entry> GENERATORS = new ArrayList<>();

    static {
        register(2, null, WorldGeneratorCelestial::generate);
    }

    private WorldGeneratorCelestial() {}

    public static void register(
            int weight, @Nullable ResourceKey<Level> dimension, LegacyWorldGenerator generator) {
        GENERATORS.add(new Entry(weight, dimension, generator));
        GENERATORS.sort(Comparator.comparingInt(Entry::weight));
    }

    public static void generateWorld(
            ResourceKey<Level> dimension, int chunkX, int chunkZ, LegacyDecorWorld world) {
        long worldSeed = world.getSeed();
        Random fmlRandom = new Random(worldSeed);
        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
        long chunkSeed = (xSeed * chunkX + zSeed * chunkZ) ^ worldSeed;

        for (Entry entry : GENERATORS) {
            if (entry.dimension() != null && entry.dimension() != dimension) continue;
            fmlRandom.setSeed(chunkSeed);
            entry.generator().generate(fmlRandom, chunkX, chunkZ, world);
        }
    }

    private static void generate(Random rand, int chunkX, int chunkZ, LegacyDecorWorld world) {
        Level level = world.level().getLevel();
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        Body body = CelestialBody.getEnum(level);
        Block stone = provider.getStone().getBlock();
        int x = chunkX * 16;
        int z = chunkZ * 16;

        if (stone != Blocks.STONE) {
            vanillaOre(world, rand, x, z, 0, 64, 20, 8, CelestialOre.IRON.state(body), stone);
            vanillaOre(world, rand, x, z, 0, 32, 2, 8, CelestialOre.GOLD.state(body), stone);
            vanillaOre(world, rand, x, z, 0, 16, 8, 7, CelestialOre.REDSTONE.state(body), stone);
            vanillaOre(world, rand, x, z, 0, 16, 1, 7, CelestialOre.DIAMOND.state(body), stone);
        }

        int alexandriteSpawn = ALEXANDRITE_SPAWN.get();
        if (alexandriteSpawn > 0 && rand.nextInt(alexandriteSpawn) == 0) {
            DungeonToolbox.generateOre(
                    world,
                    rand,
                    x,
                    z,
                    1,
                    3,
                    10,
                    5,
                    ModBlocks.ORE_ALEXANDRITE.get().defaultBlockState(),
                    stone);
        }

        BlockState depth = ModBlocks.STONE_DEPTH.get().defaultBlockState();
        DepthDeposit.generateCondition(
                world,
                x,
                0,
                3,
                z,
                5,
                0.6D,
                ModBlocks.CLUSTER_DEPTH_IRON.get().defaultBlockState(),
                rand,
                24,
                stone,
                depth);
        DepthDeposit.generateCondition(
                world,
                x,
                0,
                3,
                z,
                5,
                0.6D,
                ModBlocks.CLUSTER_DEPTH_TITANIUM.get().defaultBlockState(),
                rand,
                32,
                stone,
                depth);
        DepthDeposit.generateCondition(
                world,
                x,
                0,
                3,
                z,
                5,
                0.6D,
                ModBlocks.CLUSTER_DEPTH_TUNGSTEN.get().defaultBlockState(),
                rand,
                32,
                stone,
                depth);
        DepthDeposit.generateCondition(
                world,
                x,
                0,
                3,
                z,
                5,
                0.8D,
                ModBlocks.ORE_DEPTH_CINNABAR.get().defaultBlockState(),
                rand,
                16,
                stone,
                depth);
        DepthDeposit.generateCondition(
                world,
                x,
                0,
                3,
                z,
                5,
                0.8D,
                ModBlocks.ORE_DEPTH_ZIRCONIUM.get().defaultBlockState(),
                rand,
                16,
                stone,
                depth);
        DepthDeposit.generateCondition(
                world,
                x,
                0,
                3,
                z,
                5,
                0.8D,
                ModBlocks.ORE_DEPTH_BORAX.get().defaultBlockState(),
                rand,
                16,
                stone,
                depth);

        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                URANIUM_SPAWN.get(),
                5,
                5,
                20,
                CelestialOre.URANIUM.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                THORIUM_SPAWN.get(),
                5,
                5,
                25,
                CelestialOre.THORIUM.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                TITANIUM_SPAWN.get(),
                6,
                5,
                30,
                CelestialOre.TITANIUM.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                SULFUR_SPAWN.get(),
                8,
                5,
                30,
                CelestialOre.SULFUR.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                ALUMINIUM_SPAWN.get(),
                6,
                5,
                40,
                CelestialOre.ALUMINIUM.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                COPPER_SPAWN.get(),
                6,
                5,
                45,
                CelestialOre.COPPER.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                NICKEL_SPAWN.get(),
                6,
                5,
                10,
                CelestialOre.NICKEL.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                ZINC_SPAWN.get(),
                6,
                5,
                32,
                CelestialOre.ZINC.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                FLUORITE_SPAWN.get(),
                4,
                5,
                45,
                CelestialOre.FLUORITE.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                NITER_SPAWN.get(),
                6,
                5,
                30,
                CelestialOre.NITER.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                TUNGSTEN_SPAWN.get(),
                8,
                5,
                30,
                CelestialOre.TUNGSTEN.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                LEAD_SPAWN.get(),
                9,
                5,
                30,
                CelestialOre.LEAD.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                BERYLLIUM_SPAWN.get(),
                4,
                5,
                30,
                CelestialOre.BERYLLIUM.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                RARE_SPAWN.get(),
                5,
                5,
                20,
                CelestialOre.RARE.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                CINNEBAR_SPAWN.get(),
                4,
                8,
                16,
                CelestialOre.CINNEBAR.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                COBALT_SPAWN.get(),
                4,
                4,
                8,
                CelestialOre.COBALT.state(body),
                stone);

        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                IRON_CLUSTER_SPAWN.get(),
                6,
                15,
                45,
                CelestialOre.CLUSTER_IRON.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                TITANIUM_CLUSTER_SPAWN.get(),
                6,
                15,
                30,
                CelestialOre.CLUSTER_TITANIUM.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                ALUMINIUM_CLUSTER_SPAWN.get(),
                6,
                15,
                35,
                CelestialOre.CLUSTER_ALUMINIUM.state(body),
                stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                COPPER_CLUSTER_SPAWN.get(),
                6,
                15,
                20,
                CelestialOre.CLUSTER_COPPER.state(body),
                stone);

        DungeonToolbox.generateOre(
                world,
                rand,
                x,
                z,
                LIMESTONE_SPAWN.get(),
                12,
                25,
                30,
                ModBlocksSpace.STONE_RESOURCE_CALCIUM.get().defaultBlockState(),
                stone);

        if (rand.nextInt(4) == 0) {
            int rx = x + rand.nextInt(16) + 8;
            int ry = 6 + rand.nextInt(13);
            int rz = z + rand.nextInt(16) + 8;

            if (world.is(rx, ry, rz, stone)) {
                world.setBlock(
                        rx,
                        ry,
                        rz,
                        BuiltInRegistries.BLOCK
                                .getOptional(Library.id("stone_keyhole"))
                                .orElseThrow()
                                .defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }

        bedrockOres(world, rand, x, z, stone, provider.hasIce(), provider.getBedrockAcid());
    }

    private static void bedrockOres(
            LegacyDecorWorld world,
            Random rand,
            int x,
            int z,
            Block stone,
            boolean hasIce,
            @Nullable FluidStackNTM acid) {
        WorldGenLevel region = world.level();
        BlockState depth = ModBlocks.STONE_DEPTH.get().defaultBlockState();
        if (rand.nextInt(10) == 0) {
            int bx = x + rand.nextInt(2) + 8;
            int bz = z + rand.nextInt(2) + 8;
            BedrockOreSet set = CelestialBedrockOre.of(region.getLevel());
            int tier =
                    BedrockOreFeature.getTier(
                            NtmWorldgenFields.get(region).bedrock().averageOreLevel(set, bx, bz));
            BedrockOreFeature.generate(
                    region,
                    bx,
                    bz,
                    new ItemStack(ModItems.BEDROCK_ORE_BASE),
                    acid != null ? acid : BedrockOreFeature.getBoreFluid(tier),
                    0xD78A16,
                    tier,
                    depth,
                    state -> state.is(stone),
                    region.getRandom());
        } else if (hasIce && rand.nextInt(3) == 0) {
            int bx = x + rand.nextInt(2) + 8;
            int bz = z + rand.nextInt(2) + 8;
            BedrockOreFeature.generate(
                    region,
                    bx,
                    bz,
                    new ItemStack(Blocks.PACKED_ICE, 8 * 4),
                    null,
                    0xAAFFFF,
                    1,
                    depth,
                    state -> state.is(Blocks.STONE),
                    region.getRandom());
        }
    }

    private static void vanillaOre(
            LegacyDecorWorld world,
            Random rand,
            int x,
            int z,
            int yMin,
            int yMax,
            int count,
            int size,
            BlockState ore,
            Block target) {
        WorldGenMinable vein = new WorldGenMinable(ore, size, target);
        for (int l = 0; l < count; ++l) {
            int genX = x + rand.nextInt(16);
            int genY = rand.nextInt(yMax - yMin) + yMin;
            int genZ = z + rand.nextInt(16);
            vein.generate(world, rand, genX, genY, genZ);
        }
    }
}
