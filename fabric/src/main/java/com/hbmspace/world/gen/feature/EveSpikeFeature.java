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

package com.hbmspace.world.gen.feature;

import com.hbmspace.blocks.TerrainBlocksSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EveSpikeFeature extends Feature<NoneFeatureConfiguration> {

    public EveSpikeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource rand = context.random();
        Block silt = TerrainBlocksSpace.EVE_SILT.get();
        Block rock = TerrainBlocksSpace.EVE_ROCK.get();
        BlockState rockState = rock.defaultBlockState();
        BlockPos.MutableBlockPos pos = context.origin().mutable();
        int x = pos.getX();
        int z = pos.getZ();

        while (level.isEmptyBlock(pos) && pos.getY() > level.getMinY() + 2) pos.move(0, -1, 0);
        if (!level.getBlockState(pos).is(silt)) return false;

        int y = pos.getY() + rand.nextInt(4);
        int l = rand.nextInt(4) + 10;
        int i1 = l / 4 + rand.nextInt(2);

        if (i1 > 1 && rand.nextInt(2) == 0) {
            y += 10 + rand.nextInt(30);
        }

        for (int j1 = 0; j1 < l; ++j1) {
            float f = (1.0F - (float) j1 / (float) l) * (float) i1;
            int k1 = Mth.ceil(f);

            for (int l1 = -k1; l1 <= k1; ++l1) {
                float f1 = (float) Mth.abs(l1) - 0.25F;

                for (int i2 = -k1; i2 <= k1; ++i2) {
                    float f2 = (float) Mth.abs(i2) - 0.25F;

                    if ((l1 == 0 && i2 == 0 || f1 * f1 + f2 * f2 <= f * f)
                            && (l1 != -k1 && l1 != k1 && i2 != -k1 && i2 != k1
                                    || rand.nextFloat() <= 0.75F)) {
                        if (replaceable(
                                level.getBlockState(pos.set(x + l1, y + j1, z + i2)), silt, rock)) {
                            level.setBlock(pos, rockState, Block.UPDATE_CLIENTS);
                        }

                        if (j1 != 0
                                && k1 > 1
                                && replaceable(
                                        level.getBlockState(pos.set(x + l1, y - j1, z + i2)),
                                        silt,
                                        rock)) {
                            level.setBlock(pos, rockState, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }

        int j1 = Mth.clamp(i1 - 1, 0, 1);

        for (int j2 = -j1; j2 <= j1; ++j2) {
            for (int k1 = -j1; k1 <= j1; ++k1) {
                int l1 = y - 1;
                int k2 = 50;

                if (Math.abs(j2) == 1 && Math.abs(k1) == 1) {
                    k2 = rand.nextInt(5);
                }

                while (l1 > 50
                        && replaceable(
                                level.getBlockState(pos.set(x + j2, l1, z + k1)), silt, rock)) {
                    level.setBlock(pos, rockState, Block.UPDATE_CLIENTS);
                    --l1;
                    --k2;

                    if (k2 <= 0) {
                        l1 -= rand.nextInt(5) + 1;
                        k2 = rand.nextInt(5);
                    }
                }
            }
        }

        return true;
    }

    private static boolean replaceable(BlockState state, Block silt, Block rock) {
        return state.isAir() || state.is(silt) || state.is(rock);
    }
}
