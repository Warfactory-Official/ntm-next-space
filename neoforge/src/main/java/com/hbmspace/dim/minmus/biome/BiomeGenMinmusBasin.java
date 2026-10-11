// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.minmus.biome;

import com.hbmspace.blocks.ModBlocksSpace;

public class BiomeGenMinmusBasin extends BiomeGenBaseMinmus {

    BiomeGenMinmusBasin() {
        super("minmus_basins", 123);
        this.setBiomeName("Minmus Basins");

        this.setHeight(new Height(-1F, 0.02F));
        this.topBlock = ModBlocksSpace.MINMUS_SMOOTH.get();
        this.fillerBlock = ModBlocksSpace.MINMUS_REGOLITH.get();
    }
}
