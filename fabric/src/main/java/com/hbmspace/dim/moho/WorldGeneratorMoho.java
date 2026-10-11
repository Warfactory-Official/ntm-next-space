// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockVolcano;
import com.hbm.lib.Library;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.dim.SolarSystem.Body;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import com.hbmspace.dim.legacy.LegacyWorldGenerator;
import com.hbmspace.dim.legacy.feature.DungeonToolbox;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.hbmspace.data.SpaceData.*;

public class WorldGeneratorMoho implements LegacyWorldGenerator {

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, LegacyDecorWorld world) {
        int i = chunkX * 16;
        int j = chunkZ * 16;
        Block stone = WorldProviderCelestial.of(world.level().getLevel()).getStone().getBlock();

        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                MINERAL_SPAWN.get(),
                10,
                12,
                32,
                CelestialOre.MINERAL.state(Body.MOHO),
                stone);

        DungeonToolbox.generateOre(
                world, rand, i, j, 14, 12, 5, 30, CelestialOre.GLOWSTONE.state(Body.MOHO), stone);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                NETHER_PHOSPHORUS_SPAWN.get(),
                6,
                8,
                64,
                CelestialOre.FIRE.state(Body.MOHO),
                stone);
        DungeonToolbox.generateOre(
                world, rand, i, j, 8, 4, 0, 24, CelestialOre.AUSTRALIUM.state(Body.MOHO), stone);

        DungeonToolbox.generateOre(
                world, rand, i, j, 1, 12, 8, 32, CelestialOre.SHALE.state(Body.MOHO), stone);

        Block basalt = ntm("basalt");
        DungeonToolbox.generateOre(
                world, rand, i, j, 10, 32, 0, 128, basalt.defaultBlockState(), stone);

        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                16,
                6,
                16,
                64,
                ntm("ore_basalt_sulfur").defaultBlockState(),
                basalt);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                12,
                8,
                8,
                32,
                ntm("ore_basalt_fluorite").defaultBlockState(),
                basalt);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                8,
                9,
                8,
                48,
                ModBlocks.ORE_BASALT_ASBESTOS.get().defaultBlockState(),
                basalt);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                2,
                4,
                0,
                24,
                ModBlocks.ORE_BASALT_GEM.get().defaultBlockState(),
                basalt);
        DungeonToolbox.generateOre(
                world,
                rand,
                i,
                j,
                8,
                10,
                16,
                64,
                ntm("ore_basalt_molysite").defaultBlockState(),
                basalt);

        Block mohoStone = ModBlocksSpace.MOHO_STONE.get();
        Block mohoRegolith = ModBlocksSpace.MOHO_REGOLITH.get();
        BlockState geysir = ntm("geysir_nether").defaultBlockState();
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        for (int k = 0; k < 2; k++) {
            int x = i + rand.nextInt(16) + 8;
            int z = j + rand.nextInt(16) + 8;
            int d = 16 + rand.nextInt(96);

            for (int y = d - 5; y <= d; y++) {
                if (world.isAirBlock(x, y + 1, z)
                        && (world.is(x, y, z, mohoStone) || world.is(x, y, z, mohoRegolith))) {
                    world.setBlock(x, y, z, geysir, Block.UPDATE_ALL);
                    world.setBlock(x + 1, y, z, netherrack, Block.UPDATE_ALL);
                    world.setBlock(x - 1, y, z, netherrack, Block.UPDATE_ALL);
                    world.setBlock(x, y, z + 1, netherrack, Block.UPDATE_ALL);
                    world.setBlock(x, y, z - 1, netherrack, Block.UPDATE_ALL);
                    world.setBlock(x + 1, y - 1, z, netherrack, Block.UPDATE_ALL);
                    world.setBlock(x - 1, y - 1, z, netherrack, Block.UPDATE_ALL);
                    world.setBlock(x, y - 1, z + 1, netherrack, Block.UPDATE_ALL);
                    world.setBlock(x, y - 1, z - 1, netherrack, Block.UPDATE_ALL);
                }
            }
        }

        Block core = ModBlocks.VOLCANO_CORE.get();
        BlockState extinguishing =
                core.defaultBlockState()
                        .setValue(BlockVolcano.MODE, BlockVolcano.Mode.STATIC_EXTINGUISHING);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 32; y < 128; y++) {
                    int ox = i + x + 8;
                    int oz = j + z + 8;

                    if (world.is(ox, y, oz, Blocks.LAVA) && world.isAirBlock(ox, y - 1, oz)) {
                        world.flowLiquid(ox, y - 1, oz, Blocks.LAVA.defaultBlockState());
                    } else if (world.is(ox, y, oz, core)) {
                        world.setBlock(ox, y, oz, extinguishing, 0);
                    }
                }
            }
        }
    }

    private static Block ntm(String path) {
        return BuiltInRegistries.BLOCK.getOptional(Library.id(path)).orElseThrow();
    }
}
