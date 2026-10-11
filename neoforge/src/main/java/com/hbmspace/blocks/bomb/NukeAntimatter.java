// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.bomb;

import com.hbm.blocks.machine.BlockMachineHorizontal;
import com.hbm.interfaces.IBomb;
import com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class NukeAntimatter extends BlockMachineHorizontal implements IBomb {

    public NukeAntimatter(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityNukeAntimatter(pos, state);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            @Nullable Orientation orientation,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (level.isClientSide()) return;
        if (level.hasNeighborSignal(pos)) explode(level, pos, null);
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos, @Nullable Entity detonator) {
        if (level.isClientSide()) return BombReturnCode.UNDEFINED;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityNukeAntimatter bomb))
            return BombReturnCode.UNDEFINED;
        if (!bomb.isLoaded()) return BombReturnCode.ERROR_MISSING_COMPONENT;

        bomb.explode();
        return BombReturnCode.DETONATED;
    }
}
