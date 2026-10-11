// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BlockFallingTint extends FallingBlock {

    public static final MapCodec<BlockFallingTint> CODEC = simpleCodec(BlockFallingTint::new);
    public static final IntegerProperty SHADE = IntegerProperty.create("shade", 0, 2);

    public BlockFallingTint(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(SHADE, 0));
    }

    @Override
    protected MapCodec<? extends BlockFallingTint> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHADE);
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getMapColor(level, pos).col;
    }

    public static float brightness(BlockState state) {
        return 1.0F - state.getValue(SHADE) * 3 / 15.0F;
    }
}
