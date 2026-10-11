// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe.biome;

import com.hbmspace.entity.MobEntitiesSpace;

public class BiomeGenLaytheOcean extends BiomeGenBaseLaythe {

    BiomeGenLaytheOcean() {
        super("laythe_ocean", 96);
        this.setBiomeName("Sagan Sea");

        this.setHeight(new Height(-1.8F, 0.24F));
        this.setTemperatureRainfall(0.2F, 0.2F);

        this.waterCreatures.add(new SpawnListEntry(MobEntitiesSpace.SCRAP_FISH, 2, 1, 4));
        this.waterCreatures.add(new SpawnListEntry(MobEntitiesSpace.SIFTER_EEL, 1, 1, 4));
        this.waterCreatures.add(new SpawnListEntry(MobEntitiesSpace.DEPTH_SQUID, 1, 1, 4));
    }
}
