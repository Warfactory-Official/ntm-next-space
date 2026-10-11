// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.ike;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderIke extends WorldProviderCelestial {

    public WorldProviderIke() {
        super(SpaceDimensions.IKE);
    }

    @Override
    public BlockState getStone() {
        return ModBlocksSpace.IKE_STONE.get().defaultBlockState();
    }
}
