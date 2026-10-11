// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.blocks.machine.MachineMiningLaser;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
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

    private static Direction facing(BlockMultiblockCore block, Direction face, float yaw) {
        if (face.getAxis().isHorizontal()) return face;
        int quadrant = Mth.floor(yaw * 4F / block.placementYawSpan() + 0.5D) & 3;
        return switch (quadrant) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    public static Direction direction(BlockMultiblockCore block, Direction face, float yaw) {
        return block.getDirModified(facing(block, face, yaw));
    }

    public static BlockPos core(
            BlockMultiblockCore block, BlockPos clicked, Direction face, float yaw) {
        Direction facing = facing(block, face, yaw);
        Direction direction = block.getDirModified(facing);
        if (face.getAxis().isHorizontal()) {
            int[] rotated = MultiblockHandlerXR.rotate(block.getDimensions(), direction);
            return clicked.relative(face, rotated[face.getOpposite().ordinal()])
                    .above(block.getHeightOffset());
        }
        BlockPos core = clicked.relative(facing, -block.getOffset()).above(block.getHeightOffset());
        if (face == Direction.DOWN && !(block instanceof MachineMiningLaser)) {
            int[] dimensions = block.getDimensions();
            core = core.below(dimensions[0] + dimensions[1]);
        }
        return core;
    }

    public static BlockPos requirementAnchor(
            BlockMultiblockCore block, BlockPos clicked, Direction face, float yaw) {
        return core(block, clicked, face, yaw)
                .relative(direction(block, face, yaw), block.getOffset());
    }
}
