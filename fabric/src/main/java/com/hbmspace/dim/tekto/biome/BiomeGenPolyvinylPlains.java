// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto.biome;

import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;

public class BiomeGenPolyvinylPlains extends BiomeGenBaseTekto {

    BiomeGenPolyvinylPlains() {
        super("tekto_polyvinyl_plains", 92);
        this.setBiomeName("Polyvinyl Plains");

        this.setHeight(new Height(0.125F, 0.05F));

        this.topBlock = FloraBlocksSpace.RUBBER_GRASS.get();
        this.fillerBlock = TerrainBlocksSpace.RUBBER_SILT.get();
    }
}
