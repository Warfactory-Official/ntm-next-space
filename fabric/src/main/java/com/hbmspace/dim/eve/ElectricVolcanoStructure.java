// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve;

import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.ChunkProviderCelestial;
import com.hbmspace.dim.eve.biome.BiomeGenBaseEve;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class ElectricVolcanoStructure extends Structure {

    public static final MapCodec<ElectricVolcanoStructure> CODEC =
            simpleCodec(ElectricVolcanoStructure::new);

    public ElectricVolcanoStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!(context.chunkGenerator() instanceof ChunkProviderCelestial generator))
            return Optional.empty();

        ChunkPos chunk = context.chunkPos();
        WorldgenRandom random = context.random();
        int x = chunk.getMinBlockX() + random.nextInt(16) + 8;
        int z = chunk.getMinBlockZ() + random.nextInt(16) + 8;

        if (generator.getBiomeGenForCoords(x, z) == BiomeGenBaseEve.eveSeismicPlains)
            return Optional.empty();

        BlockState[] column = generator.getChunkPrimer(x >> 4, z >> 4);
        int base = ((x & 15) * 16 + (z & 15)) * 256;
        int y = 255;
        while (y > 2 && (column[base + y] == null || column[base + y].isAir())) --y;
        BlockState surface = column[base + y];
        if (surface == null || !surface.is(TerrainBlocksSpace.EVE_SILT.get()))
            return Optional.empty();

        long seed = random.nextLong();
        BlockPos origin = new BlockPos(x, y, z);
        return Optional.of(
                new GenerationStub(
                        origin,
                        builder -> builder.addPiece(new ElectricVolcanoPiece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return EveStructureTypes.ELECTRIC_VOLCANO.get();
    }
}
