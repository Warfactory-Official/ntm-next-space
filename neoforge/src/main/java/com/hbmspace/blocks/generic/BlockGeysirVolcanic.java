// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.generic;

import com.hbm.blocks.generic.BlockGeysir;
import com.hbm.client.ClientEffects;
import com.hbmspace.blockentity.deco.BlockEntityGeysirSpace;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockGeysirVolcanic extends BlockGeysir {

    public static final MapCodec<BlockGeysirVolcanic> CODEC = simpleCodec(BlockGeysirVolcanic::new);

    public BlockGeysirVolcanic(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityGeysirSpace(pos, state);
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
