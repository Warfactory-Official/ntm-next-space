// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.world.feature.Stratum3DConfig;
import com.hbm.world.feature.Stratum3DFeature;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.laythe.OreCave;
import com.hbmspace.dim.legacy.LegacyDecorWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import org.jspecify.annotations.Nullable;

public final class CelestialStrata {

    @FunctionalInterface
    public interface Stratum {
        void generate(LegacyDecorWorld world, Random rand, int chunkX, int chunkZ, Block stone);
    }

    private record Entry(@Nullable ResourceKey<Level> dimension, Stratum stratum) {}

    private static final List<Entry> STRATA = new ArrayList<>();

    static {
        register(
                null,
                layer(
                        Library.id("noise/stratum_hematite"),
                        () -> ModBlocks.STONE_RESOURCE_HEMATITE.get().defaultBlockState(),
                        0.04D,
                        0.25D,
                        230.0D));
        register(
                null,
                layer(
                        Library.id("noise/stratum_bauxite"),
                        () -> ModBlocks.STONE_RESOURCE_BAUXITE.get().defaultBlockState(),
                        0.03D,
                        0.15D,
                        300.0D));
        register(
                null,
                layer(
                        Library.id("noise/stratum_malachite"),
                        () -> ModBlocks.STONE_RESOURCE_MALACHITE.get().defaultBlockState(),
                        0.10D,
                        0.15D,
                        275.0D));
        register(SpaceDimensions.MUN, sulfurCave()::generate);
        register(SpaceDimensions.MUN, conglomerate("mun"));
        register(SpaceDimensions.DUNA, sulfurCave()::generate);
        register(SpaceDimensions.IKE, sulfurCave()::generate);
        register(SpaceDimensions.IKE, conglomerate("ike"));
        register(
                SpaceDimensions.EVE,
                sulfurCave()
                                .withFluid(
                                        () ->
                                                ModBlocks.SULFURIC_ACID_BLOCK
                                                        .get()
                                                        .defaultBlockState())
                        ::generate);
        register(
                SpaceDimensions.EVE,
                new OreCave(
                                        () ->
                                                BuiltInRegistries.BLOCK
                                                        .getOptional(Library.id("basalt"))
                                                        .orElseThrow()
                                                        .defaultBlockState(),
                                        "hbm:tile.basalt",
                                        0)
                                .setThreshold(0.15D)
                                .setRangeMult(40)
                                .setYLevel(54)
                                .setMaxRange(24)
                                .setBlockOverride(TerrainBlocksSpace.EVE_SILT)
                                .setIgnoreWater(true)
                        ::generate);
        register(SpaceDimensions.MOHO, sulfurCave()::generate);
        register(SpaceDimensions.MINMUS, sulfurCave()::generate);
        register(
                SpaceDimensions.MINMUS,
                layer(
                        domain("minmus_regolith"),
                        () -> ModBlocksSpace.MINMUS_REGOLITH.get().defaultBlockState(),
                        0.06D,
                        0.25D,
                        220.0D));
        register(
                SpaceDimensions.MINMUS,
                layer(
                        domain("minmus_smooth"),
                        () -> ModBlocksSpace.MINMUS_SMOOTH.get().defaultBlockState(),
                        0.05D,
                        0.15D,
                        280.0D));
        register(SpaceDimensions.MINMUS, conglomerate("minmus"));
        for (OreCave cave : OreCave.laythe()) register(SpaceDimensions.LAYTHE, cave::generate);
    }

    private CelestialStrata() {}

    public static void register(@Nullable ResourceKey<Level> dimension, Stratum stratum) {
        STRATA.add(new Entry(dimension, stratum));
    }

    public static void generate(LegacyDecorWorld world, Random rand, int chunkX, int chunkZ) {
        Level level = world.level().getLevel();
        Block stone = WorldProviderCelestial.of(level).getStone().getBlock();
        for (Entry entry : STRATA) {
            if (entry.dimension() == null || entry.dimension() == level.dimension()) {
                entry.stratum().generate(world, rand, chunkX, chunkZ, stone);
            }
        }
    }

    public static Stratum layer(
            Identifier domain,
            Supplier<BlockState> state,
            double scaleH,
            double scaleV,
            double threshold) {
        Identifier x = domain.withSuffix("/x");
        Identifier y = domain.withSuffix("/y");
        Identifier z = domain.withSuffix("/z");
        return (world, rand, chunkX, chunkZ, stone) ->
                Stratum3DFeature.place(
                        world.level(),
                        world.level().getRandom(),
                        new Stratum3DConfig(
                                x,
                                y,
                                z,
                                state.get(),
                                new BlockMatchTest(stone),
                                scaleH,
                                scaleV,
                                threshold),
                        chunkX + 8,
                        chunkZ + 8);
    }

    private static Identifier domain(String name) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "noise/stratum_" + name);
    }

    private static Stratum conglomerate(String body) {
        return layer(
                domain("conglomerate_" + body),
                () -> ModBlocksSpace.STONE_RESOURCE_CONGLOMERATE.get().defaultBlockState(),
                0.04D,
                0.25D,
                220.0D);
    }

    private static OreCave sulfurCave() {
        return new OreCave(
                        () -> ModBlocks.STONE_RESOURCE_SULFUR.get().defaultBlockState(),
                        "hbm:tile.stone_resource",
                        0)
                .setThreshold(1.5D)
                .setRangeMult(20)
                .setYLevel(30)
                .setMaxRange(20)
                .setStalagmites(
                        () -> ModBlocks.STALACTITE_SULFUR.get().defaultBlockState(),
                        () -> ModBlocks.STALAGMITE_SULFUR.get().defaultBlockState());
    }
}
