// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto.biome;

import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;

public class BiomeGenForest extends BiomeGenBaseTekto {

    BiomeGenForest() {
        super("tekto_forest", 89);
        this.setBiomeName("Tekto Forest");

        this.setHeight(new Height(0.425F, 0.3F));

        this.topBlock = FloraBlocksSpace.RUBBER_GRASS.get();
        this.fillerBlock = TerrainBlocksSpace.RUBBER_SILT.get();
    }
}
