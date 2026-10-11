// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.generic;

import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRubberPlant extends VegetationBlock {

    private final Predicate<BlockState> soil;
    private final MapCodec<BlockRubberPlant> ownCodec;

    public BlockRubberPlant(Properties properties, Predicate<BlockState> soil) {
        super(properties);
        this.soil = soil;
        this.ownCodec = simpleCodec(p -> new BlockRubberPlant(p, soil));
    }

    @Override
    protected MapCodec<? extends BlockRubberPlant> codec() {
        return ownCodec;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return soil.test(state);
    }
}
