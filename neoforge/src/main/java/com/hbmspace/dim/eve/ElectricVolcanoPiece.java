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

package com.hbmspace.dim.eve;

import com.hbm.util.RegistryUtil;
import com.hbmspace.blockentity.deco.LightningVolcanoBlockEntity;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.eve.genlayer.WorldGenElectricVolcano.Shape;
import com.hbmspace.dim.eve.genlayer.WorldGenElectricVolcano;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class ElectricVolcanoPiece extends StructurePiece {

    private record Cone(Long2ObjectMap<BlockState> writes, int chargeRoll) {}

    private record Key(int x, int y, int z, long seed) {}

    private static final Map<Key, Cone> CONES =
            new LinkedHashMap<>(8, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Key, Cone> eldest) {
                    return size() > 4;
                }
            };

    private final int x;
    private final int y;
    private final int z;
    private final long seed;

    public ElectricVolcanoPiece(BlockPos origin, long seed) {
        super(EveStructureTypes.ELECTRIC_VOLCANO_PIECE.get(), 0, box(origin, seed));
        this.setOrientation(null);
        this.x = origin.getX();
        this.y = origin.getY();
        this.z = origin.getZ();
        this.seed = seed;
    }

    public ElectricVolcanoPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(EveStructureTypes.ELECTRIC_VOLCANO_PIECE.get(), tag);
        this.x = tag.getIntOr("X", 0);
        this.y = tag.getIntOr("Y", 0);
        this.z = tag.getIntOr("Z", 0);
        this.seed = tag.getLongOr("Seed", 0L);
    }

    private static BoundingBox box(BlockPos origin, long seed) {
        Shape shape = WorldGenElectricVolcano.shape(new Random(seed), origin.getY());
        int r = shape.i1();
        return new BoundingBox(
                origin.getX() - r,
                shape.y() - shape.l() + 1,
                origin.getZ() - r,
                origin.getX() + r,
                Math.max(shape.y() + shape.l() - 1, shape.y() + 5),
                origin.getZ() + r);
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("X", x);
        tag.putInt("Y", y);
        tag.putInt("Z", z);
        tag.putLong("Seed", seed);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBB,
            ChunkPos chunkPos,
            BlockPos referencePos) {
        Cone cone = cone(generator, level, level.getLevel().getChunkSource().randomState());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Long2ObjectMap.Entry<BlockState> write : cone.writes().long2ObjectEntrySet()) {
            long key = write.getLongKey();
            pos.set(BlockPos.getX(key), BlockPos.getY(key), BlockPos.getZ(key));
            if (!chunkBB.isInside(pos)
                    || pos.getY() < level.getMinY()
                    || pos.getY() > level.getMaxY()) continue;
            level.setBlock(pos, write.getValue(), Block.UPDATE_CLIENTS);
        }

        pos.set(x, y + 5, z);
        if (chunkBB.isInside(pos)
                && level.getBlockEntity(pos) instanceof LightningVolcanoBlockEntity volcano) {
            volcano.charge(cone.chargeRoll());
        }
    }

    private Cone cone(
            ChunkGenerator generator, LevelHeightAccessor height, RandomState randomState) {
        Key key = new Key(x, y, z, seed);
        synchronized (CONES) {
            Cone cone = CONES.get(key);
            if (cone != null) return cone;
        }

        Long2ObjectMap<BlockState> writes = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<NoiseColumn> columns = new Long2ObjectOpenHashMap<>();
        WorldGenElectricVolcano.Terrain terrain =
                new WorldGenElectricVolcano.Terrain() {
                    @Override
                    public BlockState get(int bx, int by, int bz) {
                        BlockState written = writes.get(BlockPos.asLong(bx, by, bz));
                        if (written != null) return written;
                        return columns.computeIfAbsent(
                                        ChunkPos.pack(bx, bz),
                                        packed ->
                                                generator.getBaseColumn(
                                                        bx, bz, height, randomState))
                                .getBlock(by);
                    }

                    @Override
                    public void set(int bx, int by, int bz, BlockState state) {
                        writes.put(BlockPos.asLong(bx, by, bz), state);
                    }
                };

        Random rand = new Random(seed);
        Shape shape = WorldGenElectricVolcano.shape(rand, y);
        BlockState geysir = TerrainBlocksSpace.GEYSIR_ELECTRIC.get().defaultBlockState();
        WorldGenElectricVolcano.build(
                terrain,
                rand,
                x,
                z,
                shape,
                TerrainBlocksSpace.EVE_SILT.get(),
                TerrainBlocksSpace.EVE_ROCK.get(),
                block("basalt"),
                block("ore_depth_nether_neodymium"),
                geysir,
                Blocks.AIR.defaultBlockState());
        Cone cone = new Cone(writes, rand.nextInt(400));

        synchronized (CONES) {
            CONES.put(key, cone);
        }
        return cone;
    }

    private static BlockState block(String path) {
        return BuiltInRegistries.BLOCK
                .getOptional(RegistryUtil.id(path))
                .orElseThrow()
                .defaultBlockState();
    }
}
