// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto.biome;

import com.hbmspace.blocks.TerrainBlocksSpace;

public class BiomeGenHalogenHills extends BiomeGenBaseTekto {

    BiomeGenHalogenHills() {
        super("tekto_halogen_hills", 91);
        this.setBiomeName("Halogen Hills");

        this.setHeight(new Height(0.525F, 0.4F));

        this.topBlock = TerrainBlocksSpace.RUBBER_SILT.get();
        this.fillerBlock = TerrainBlocksSpace.RUBBER_SILT.get();
    }
}
