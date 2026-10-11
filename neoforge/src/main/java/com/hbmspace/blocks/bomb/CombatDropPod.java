// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.bomb;

import com.hbm.blocks.ITickingBlock;
import com.hbmspace.blockentity.bomb.BlockEntityCombatDropPod;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CombatDropPod extends Block implements ITickingBlock {

    private static final MapCodec<CombatDropPod> CODEC = simpleCodec(CombatDropPod::new);

    public CombatDropPod(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityCombatDropPod(pos, state);
    }
}
