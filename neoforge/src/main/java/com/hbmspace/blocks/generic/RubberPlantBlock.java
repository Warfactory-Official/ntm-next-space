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
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RubberPlantBlock extends VegetationBlock {

    private final Predicate<BlockState> soil;
    private final MapCodec<RubberPlantBlock> ownCodec;

    public RubberPlantBlock(Properties properties, Predicate<BlockState> soil) {
        super(properties);
        this.soil = soil;
        this.ownCodec = simpleCodec(p -> new RubberPlantBlock(p, soil));
    }

    @Override
    protected MapCodec<? extends RubberPlantBlock> codec() {
        return ownCodec;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return soil.test(state);
    }
}
