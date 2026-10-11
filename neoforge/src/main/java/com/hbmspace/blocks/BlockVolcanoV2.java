// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blocks.ITickingBlock;
import com.hbmspace.blockentity.deco.BlockEntityLightningVolcano;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockVolcanoV2 extends Block implements ITickingBlock {

    public static final MapCodec<BlockVolcanoV2> CODEC = simpleCodec(BlockVolcanoV2::new);

    public BlockVolcanoV2(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockVolcanoV2> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityLightningVolcano(pos, state);
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        if (!level.isClientSide()
                && level.getBlockEntity(pos) instanceof BlockEntityLightningVolcano volcano) {
            volcano.charge(level.getRandom().nextInt(400));
        }
    }
}
