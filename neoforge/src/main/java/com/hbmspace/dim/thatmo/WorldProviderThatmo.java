// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.thatmo;

import com.hbm.blocks.ModBlocks;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderThatmo extends WorldProviderCelestial {

    public WorldProviderThatmo() {
        super(SpaceDimensions.THATMO);
    }

    @Override
    public BlockState getStone() {
        return ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();
    }

    @Override
    public float getSunBrightness(float vanilla, Sky sky) {
        return super.getSunBrightness(vanilla, sky) * 0.1F;
    }
}
