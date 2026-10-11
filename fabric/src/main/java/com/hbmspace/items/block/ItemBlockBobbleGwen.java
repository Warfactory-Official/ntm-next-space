// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.block;

import com.hbm.blockentity.machine.BlockEntityMachineShredder;
import com.hbm.items.block.ItemBlockTrinket;
import com.hbmspace.blocks.generic.BobbleTypeSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ItemBlockBobbleGwen extends ItemBlockTrinket<BobbleTypeSpace>
        implements BlockEntityMachineShredder.Wrecker {

    public ItemBlockBobbleGwen(Block block, Properties props) {
        super(block, props, BobbleTypeSpace.GWEN);
    }

    @Override
    public void wreck(Level level, BlockPos pos) {
        level.destroyBlock(pos, false);
        level.explode(
                null,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                5.0F,
                true,
                Level.ExplosionInteraction.BLOCK);
    }
}
