// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.generic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockFenceGate extends DoorBlock {

    public static final MapCodec<BlockFenceGate> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BlockSetType.CODEC
                                                    .fieldOf("block_set_type")
                                                    .forGetter(DoorBlock::type),
                                            propertiesCodec())
                                    .apply(i, BlockFenceGate::new));

    private static final VoxelShape ACROSS_X = Block.box(7.5, 0, 0, 8.5, 16, 16);
    private static final VoxelShape ACROSS_Z = Block.box(0, 0, 7.5, 16, 16, 8.5);

    public BlockFenceGate(BlockSetType type, Properties props) {
        super(type, props);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? ACROSS_X : ACROSS_Z;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(OPEN) ? Shapes.empty() : getShape(state, level, pos, ctx);
    }
}
