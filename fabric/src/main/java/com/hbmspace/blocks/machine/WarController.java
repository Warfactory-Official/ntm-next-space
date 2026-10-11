// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.BlockMachineBase;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbmspace.blockentity.machine.BlockEntityMachineWarController;
import com.hbmspace.blocks.WarBlocksSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class WarController extends BlockMachineBase implements ICapabilityBlock {

    public WarController(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineWarController(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(WarBlocksSpace.WAR_CONTROLLER_BE).powerIn();
    }
}
