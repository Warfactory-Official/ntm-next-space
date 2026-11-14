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

import com.hbm.blocks.generic.GeysirBlock;
import com.hbm.client.ClientEffects;
import com.hbmspace.blockentity.deco.GeysirSpaceBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class GeysirVolcanicBlock extends GeysirBlock {
    public static final MapCodec<GeysirVolcanicBlock> CODEC = simpleCodec(GeysirVolcanicBlock::new);

    public GeysirVolcanicBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeysirSpaceBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        ClientEffects.spawnGasFlame(
                level,
                pos.getX() + rand.nextDouble(),
                pos.getY() + 0.5 + rand.nextDouble(),
                pos.getZ() + rand.nextDouble(),
                rand.nextGaussian() * 0.2,
                0.1,
                rand.nextGaussian() * 0.2,
                6.5F);
    }
}
