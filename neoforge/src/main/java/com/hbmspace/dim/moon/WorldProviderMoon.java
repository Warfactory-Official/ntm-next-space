// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moon;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderMoon extends WorldProviderCelestial {

    public WorldProviderMoon() {
        super(SpaceDimensions.MUN);
    }

    @Override
    public BlockState getStone() {
        return ModBlocksSpace.MOON_ROCK.get().defaultBlockState();
    }
}
