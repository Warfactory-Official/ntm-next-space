// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.generic;

import com.hbm.blocks.generic.BlockFallout;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSaltedFallout extends BlockFallout {

    public static final MapCodec<BlockSaltedFallout> CODEC = simpleCodec(BlockSaltedFallout::new);

    public BlockSaltedFallout(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockSaltedFallout> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        RadiationSystemNT.incrementRad(level, pos, 50D);
    }
}
