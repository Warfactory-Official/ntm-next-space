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

package com.hbmspace.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FallingTintBlock extends FallingBlock {
    public static final MapCodec<FallingTintBlock> CODEC = simpleCodec(FallingTintBlock::new);
    public static final IntegerProperty SHADE = IntegerProperty.create("shade", 0, 2);

    public FallingTintBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(SHADE, 0));
    }

    @Override
    protected MapCodec<? extends FallingTintBlock> codec() {
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

    public static int tint(BlockState state) {
        int brightness = Math.round(brightness(state) * 255);
        return 0xFF000000 | brightness << 16 | brightness << 8 | brightness;
    }
}
