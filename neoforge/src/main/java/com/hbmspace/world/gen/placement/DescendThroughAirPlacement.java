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

package com.hbmspace.world.gen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class DescendThroughAirPlacement implements PlacementModifier {
    public static final MapCodec<DescendThroughAirPlacement> CODEC =
            Codec.INT
                    .fieldOf("floor")
                    .xmap(DescendThroughAirPlacement::new, placement -> placement.floor);

    private final int floor;

    public DescendThroughAirPlacement(int floor) {
        this.floor = floor;
    }

    @Override
    public void modify(
            PlacementContext context,
            RandomSource random,
            BlockPos origin,
            Consumer<BlockPos> output) {
        WorldGenLevel level = context.getLevel();
        BlockPos.MutableBlockPos pos = origin.mutable();
        while (pos.getY() > floor && level.isEmptyBlock(pos)) pos.move(Direction.DOWN);
        output.accept(pos.immutable());
    }

    @Override
    public MapCodec<DescendThroughAirPlacement> codec() {
        return CODEC;
    }
}
