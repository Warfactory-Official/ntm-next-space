/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
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

package com.hbmspace.dim.tekto;

import com.google.common.collect.Iterables;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class TTree extends Feature<TTree.Shape> {
    private static final int TREE_UPDATE = Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE;

    public TTree() {
        super(Shape.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Shape> context) {
        WorldGenLevel world = context.level();
        RandomSource rand = context.random();
        Shape shape = context.config();
        int x = context.origin().getX();
        int y = context.origin().getY();
        int z = context.origin().getZ();
        int height = rand.nextInt(shape.smallest()) + shape.tallest();

        if (y < world.getMinY() + 1 || y + height + 1 > world.getMaxY() + 1) {
            return false;
        }

        BlockState below = world.getBlockState(new BlockPos(x, y - 1, z));
        if (!below.is(FloraBlocksSpace.RUBBER_GRASS.get())
                && !below.is(TerrainBlocksSpace.RUBBER_SILT.get())) {
            return false;
        }

        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();
        Set<BlockPos> vines = new HashSet<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < height; i++) {
            world.setBlock(pos.set(x, y + i, z), shape.log(), TREE_UPDATE);
            logs.add(pos.immutable());
        }

        int bulbStartY = y + height - shape.offset();

        for (int dy = -shape.y(); dy <= shape.y(); dy++) {
            for (int dx = -shape.xz(); dx <= shape.xz(); dx++) {
                for (int dz = -shape.xz(); dz <= shape.xz(); dz++) {

                    if (Math.pow(dx / (double) shape.xz(), 2)
                                    + Math.pow(dy / (double) shape.y(), 2)
                                    + Math.pow(dz / (double) shape.xz(), 2)
                            <= 1) {
                        if (world.getBlockState(pos.set(x + dx, bulbStartY + dy, z + dz)).isAir()) {
                            world.setBlock(pos, shape.leaves(), TREE_UPDATE);
                            leaves.add(pos.immutable());
                            if (shape.vines())
                                hangVines(world, rand, x + dx, bulbStartY + dy, z + dz, vines);
                        }
                    }
                }
            }
        }

        BoundingBox.encapsulatingPositions(Iterables.concat(logs, leaves, vines))
                .ifPresent(
                        bounds ->
                                StructureTemplate.updateShapeAtEdge(
                                        world,
                                        Block.UPDATE_ALL,
                                        TreeFeature.updateLeaves(
                                                world, bounds, logs, vines, Set.of()),
                                        bounds.minX(),
                                        bounds.minY(),
                                        bounds.minZ()));
        return true;
    }

    private static void hangVines(
            WorldGenLevel world,
            RandomSource rand,
            int leafX,
            int leafY,
            int leafZ,
            Set<BlockPos> vines) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 4; i++) {
            int vineX = leafX + (i % 2 == 0 ? (i - 1) : 0);
            int vineZ = leafZ + (i % 2 == 1 ? (i - 2) : 0);

            if (rand.nextInt(12) == 0) {
                for (int j = 0; j < rand.nextInt(15); j++) {
                    int vineY = leafY - j;
                    if (!world.getBlockState(pos.set(vineX, vineY, vineZ)).isAir()) break;
                    BlockState vine = FloraBlocksSpace.VINYL_VINE.get().defaultBlockState();
                    if (vineX > leafX) vine = vine.setValue(VineBlock.WEST, true);
                    else if (vineX < leafX) vine = vine.setValue(VineBlock.EAST, true);
                    else if (vineZ > leafZ) vine = vine.setValue(VineBlock.NORTH, true);
                    else if (vineZ < leafZ) vine = vine.setValue(VineBlock.SOUTH, true);
                    world.setBlock(pos, vine, TREE_UPDATE);
                    vines.add(pos.immutable());
                }
            }
        }
    }

    public record Shape(
            int offset,
            int smallest,
            int tallest,
            int xz,
            int y,
            boolean vines,
            BlockState log,
            BlockState leaves)
            implements FeatureConfiguration {

        public static final Codec<Shape> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                Codec.INT
                                                        .fieldOf("offset")
                                                        .forGetter(Shape::offset),
                                                ExtraCodecs.POSITIVE_INT
                                                        .fieldOf("smallest")
                                                        .forGetter(Shape::smallest),
                                                ExtraCodecs.NON_NEGATIVE_INT
                                                        .fieldOf("tallest")
                                                        .forGetter(Shape::tallest),
                                                ExtraCodecs.POSITIVE_INT
                                                        .fieldOf("xz")
                                                        .forGetter(Shape::xz),
                                                ExtraCodecs.POSITIVE_INT
                                                        .fieldOf("y")
                                                        .forGetter(Shape::y),
                                                Codec.BOOL.fieldOf("vines").forGetter(Shape::vines),
                                                BlockState.CODEC
                                                        .fieldOf("log")
                                                        .forGetter(Shape::log),
                                                BlockState.CODEC
                                                        .fieldOf("leaves")
                                                        .forGetter(Shape::leaves))
                                        .apply(i, Shape::new));
    }
}
