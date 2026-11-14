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

package com.hbmspace.handler;

import com.hbm.blocks.machine.MiningLaserBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public final class SpaceMultiblockPlacement {

    private static final int ASSEMBLED = 8;
    private static final int NO_PLACEMENT = -1;
    private static final ThreadLocal<int[]> PLACEMENT =
            ThreadLocal.withInitial(() -> new int[] {NO_PLACEMENT});

    private SpaceMultiblockPlacement() {}

    public static int begin(Direction face) {
        int[] slot = PLACEMENT.get();
        int previous = slot[0];
        slot[0] = face.ordinal();
        return previous;
    }

    public static void end(int previous) {
        PLACEMENT.get()[0] = previous;
    }

    public static @Nullable Direction clickedFace() {
        int placement = PLACEMENT.get()[0];
        return placement == NO_PLACEMENT ? null : Direction.from3DDataValue(placement & 7);
    }

    public static void assembled() {
        int[] slot = PLACEMENT.get();
        if (slot[0] != NO_PLACEMENT) slot[0] |= ASSEMBLED;
    }

    public static boolean didAssemble() {
        int placement = PLACEMENT.get()[0];
        return placement != NO_PLACEMENT && (placement & ASSEMBLED) != 0;
    }

    private static Direction facing(MultiblockCoreBlock block, Direction face, float yaw) {
        if (face.getAxis().isHorizontal()) return face;
        int quadrant = Mth.floor(yaw * 4F / block.placementYawSpan() + 0.5D) & 3;
        return switch (quadrant) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    public static Direction direction(MultiblockCoreBlock block, Direction face, float yaw) {
        return block.getDirModified(facing(block, face, yaw));
    }

    public static BlockPos core(
            MultiblockCoreBlock block, BlockPos clicked, Direction face, float yaw) {
        Direction facing = facing(block, face, yaw);
        Direction direction = block.getDirModified(facing);
        MultiblockLayout.Placement placement = block.placement();
        if (face.getAxis().isHorizontal()) {
            int[] rotated =
                    MultiblockLayout.Placement.rotateDimensions(placement.dimensions(), direction);
            return clicked.relative(face, rotated[face.getOpposite().ordinal()])
                    .above(placement.heightOffset());
        }
        BlockPos core =
                clicked.relative(facing, -placement.offset()).above(placement.heightOffset());
        if (face == Direction.DOWN && !(block instanceof MiningLaserBlock)) {
            int[] dimensions = placement.dimensions();
            core = core.below(dimensions[0] + dimensions[1]);
        }
        return core;
    }

    public static BlockPos requirementAnchor(
            MultiblockCoreBlock block, BlockPos clicked, Direction face, float yaw) {
        return core(block, clicked, face, yaw)
                .relative(direction(block, face, yaw), block.placement().offset());
    }
}
