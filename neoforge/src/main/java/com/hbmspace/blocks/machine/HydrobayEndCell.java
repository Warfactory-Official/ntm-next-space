// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.multiblock.BlockMultiblockCell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HydrobayEndCell extends BlockMultiblockCell {
    public static final BooleanProperty ALONG_X = BooleanProperty.create("along_x");

    private static final VoxelShape X_COLLISION = Shapes.box(0, 0, -0.25, 1, 1, 1.25);
    private static final VoxelShape Z_COLLISION = Shapes.box(-0.25, 0, 0, 1.25, 1, 1);

    public HydrobayEndCell(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ALONG_X, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ALONG_X);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(ALONG_X) ? X_COLLISION : Z_COLLISION;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> state.cycle(ALONG_X);
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state;
    }
}
