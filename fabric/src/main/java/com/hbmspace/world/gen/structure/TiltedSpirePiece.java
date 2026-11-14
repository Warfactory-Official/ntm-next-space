/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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

import com.hbmspace.world.gen.SpaceWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class TiltedSpirePiece extends StructurePiece {
    static final int REACH = 64;

    private final int originX;
    private final int originZ;
    private final float radius;
    private final float stretch;
    private final float tiltX;
    private final float tiltZ;
    private final boolean curve;
    private final int mid;
    private final BlockState rock;
    private final BlockState regolith;

    public TiltedSpirePiece(
            ChunkPos source,
            LevelHeightAccessor height,
            float radius,
            float stretch,
            float tiltX,
            float tiltZ,
            boolean curve,
            int mid,
            BlockState rock,
            BlockState regolith) {
        super(
                SpaceWorldgen.TILTED_SPIRE_PIECE.get(),
                0,
                new BoundingBox(
                        source.getMinBlockX() - REACH,
                        height.getMinY(),
                        source.getMinBlockZ() - REACH,
                        source.getMaxBlockX() + REACH,
                        Math.min(mid + 63, height.getMaxY()),
                        source.getMaxBlockZ() + REACH));
        this.setOrientation(null);
        this.originX = source.getMinBlockX();
        this.originZ = source.getMinBlockZ();
        this.radius = radius;
        this.stretch = stretch;
        this.tiltX = tiltX;
        this.tiltZ = tiltZ;
        this.curve = curve;
        this.mid = mid;
        this.rock = rock;
        this.regolith = regolith;
    }

    public TiltedSpirePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(SpaceWorldgen.TILTED_SPIRE_PIECE.get(), tag);
        var blocks = context.registryAccess().lookupOrThrow(Registries.BLOCK);
        this.originX = tag.getIntOr("OX", 0);
        this.originZ = tag.getIntOr("OZ", 0);
        this.radius = tag.getFloatOr("Radius", 0.0F);
        this.stretch = tag.getFloatOr("Stretch", 0.0F);
        this.tiltX = tag.getFloatOr("TiltX", 0.0F);
        this.tiltZ = tag.getFloatOr("TiltZ", 0.0F);
        this.curve = tag.getBooleanOr("Curve", false);
        this.mid = tag.getIntOr("Mid", 0);
        this.rock = NbtUtils.readBlockState(blocks, tag.getCompoundOrEmpty("Rock"));
        this.regolith = NbtUtils.readBlockState(blocks, tag.getCompoundOrEmpty("Regolith"));
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OX", originX);
        tag.putInt("OZ", originZ);
        tag.putFloat("Radius", radius);
        tag.putFloat("Stretch", stretch);
        tag.putFloat("TiltX", tiltX);
        tag.putFloat("TiltZ", tiltZ);
        tag.putBoolean("Curve", curve);
        tag.putInt("Mid", mid);
        tag.put("Rock", NbtUtils.writeBlockState(rock));
        tag.put("Regolith", NbtUtils.writeBlockState(regolith));
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
                int placed = 0;
                for (int y = boundingBox.maxY(); y >= boundingBox.minY(); y--) {
                    if (level.getBlockState(pos.set(x, y, z)).isSolidRender()) break;

                    if (contains(x, y, z)) {
                        placeBlock(level, rock, x, y, z, chunkBB);
                        if (placed == 1) placeBlock(level, regolith, x, y + 1, z, chunkBB);
                        placed++;
                    } else if (placed > 0) {
                        break;
                    }
                }
            }
        }
    }

    private boolean contains(int x, int y, int z) {
        int offsetX = (int) (tiltX * (curve ? 800 : 8));
        int offsetZ = (int) (tiltZ * (curve ? 800 : 8));
        int height = y - mid;
        float lean = curve ? (float) height * height : height;
        int dx = (int) (x - originX - offsetX + tiltX * lean);
        int dz = (int) (z - originZ - offsetZ + tiltZ * lean);
        float cone = radius - height * stretch;
        if (cone > 0.0F) cone *= cone;
        return dx * dx + dz * dz < cone;
    }

    public boolean intersectsSurface(BoundingBox box, int[] heights) {
        int minX = Math.max(box.minX(), boundingBox.minX());
        int maxX = Math.min(box.maxX(), boundingBox.maxX());
        int minZ = Math.max(box.minZ(), boundingBox.minZ());
        int maxZ = Math.min(box.maxZ(), boundingBox.maxZ());
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                int ground = heights[(z - box.minZ()) * box.getXSpan() + x - box.minX()];
                for (int y = boundingBox.maxY(); y >= Math.max(ground, boundingBox.minY()); y--) {
                    if (contains(x, y, z)) return true;
                }
            }
        }
        return false;
    }
}
