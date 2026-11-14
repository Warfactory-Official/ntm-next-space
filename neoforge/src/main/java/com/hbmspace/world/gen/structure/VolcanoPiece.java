/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.world.gen.structure;

import com.hbmspace.data.SpaceData;
import com.hbmspace.world.gen.SpaceWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class VolcanoPiece extends StructurePiece {
    private static final int JITTER = 16;

    private final int centreX;
    private final int centreZ;
    private final int radius;
    private final BlockState core;
    private final BlockState material;

    public VolcanoPiece(
            BlockPos centre, int radius, int minY, int maxY, BlockState core, BlockState material) {
        super(
                SpaceWorldgen.VOLCANO_PIECE.get(),
                0,
                new BoundingBox(
                        centre.getX() - radius - JITTER,
                        minY,
                        centre.getZ() - radius - JITTER,
                        centre.getX() + radius + JITTER,
                        maxY,
                        centre.getZ() + radius + JITTER));
        this.setOrientation(null);
        this.centreX = centre.getX();
        this.centreZ = centre.getZ();
        this.radius = radius;
        this.core = core;
        this.material = material;
    }

    public VolcanoPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(SpaceWorldgen.VOLCANO_PIECE.get(), tag);
        var blocks = context.registryAccess().lookupOrThrow(Registries.BLOCK);
        this.centreX = tag.getIntOr("CX", 0);
        this.centreZ = tag.getIntOr("CZ", 0);
        this.radius = tag.getIntOr("Radius", 0);
        this.core = NbtUtils.readBlockState(blocks, tag.getCompoundOrEmpty("Core"));
        this.material = NbtUtils.readBlockState(blocks, tag.getCompoundOrEmpty("Material"));
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", centreX);
        tag.putInt("CZ", centreZ);
        tag.putInt("Radius", radius);
        tag.put("Core", NbtUtils.writeBlockState(core));
        tag.put("Material", NbtUtils.writeBlockState(material));
    }

    private double height(double r) {
        double x = r / (radius * 2.0D);
        return Math.min(r * r * r / (radius / 4.0D) + 32.0D, 1.0D / (x * x)) * 0.75D;
    }

    public boolean intersectsHorizontal(BoundingBox box) {
        long dx = Math.clamp(centreX, box.minX(), box.maxX()) - (long) centreX;
        long dz = Math.clamp(centreZ, box.minZ(), box.maxZ()) - (long) centreZ;
        long reach = radius + JITTER - 1L;
        return dx * dx + dz * dz <= reach * reach;
    }

    public int maxAddedHeight(int x, int z) {
        long dx = (long) x - centreX, dz = (long) z - centreZ;
        long reach = radius + JITTER - 1L;
        if (dx * dx + dz * dz > reach * reach) return 0;
        int raise = (int) height(Math.sqrt((double) dx * dx + (double) dz * dz));
        if (x == centreX
                && z == centreZ
                && core.blocksMotion()
                && SpaceData.ENABLE_VOLCANO_GEN.get()) return raise + 1;
        return Math.max(0, raise - 1);
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
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(chunkBB.minX(), boundingBox.minX());
        int maxX = Math.min(chunkBB.maxX(), boundingBox.maxX());
        int minZ = Math.max(chunkBB.minZ(), boundingBox.minZ());
        int maxZ = Math.min(chunkBB.maxZ(), boundingBox.maxZ());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double r = Math.sqrt(Mth.square(x - centreX) + Mth.square(z - centreZ));
                if (r - random.nextInt(JITTER) > radius) continue;

                int y = level.getMaxY();
                while (y > level.getMinY()
                        && !level.getBlockState(pos.set(x, y, z)).isSolidRender()) y--;
                if (y <= level.getMinY()) continue;

                int raise = (int) Mth.clamp(height(r), 0, y - 1);
                for (int i = 0; i < raise && y + i <= level.getMaxY(); i++) {
                    placeBlock(level, material, x, y + i, z, chunkBB);
                }
                if (x == centreX && z == centreZ && SpaceData.ENABLE_VOLCANO_GEN.get()) {
                    placeBlock(level, core, x, y + raise + 1, z, chunkBB);
                }
            }
        }
    }
}
