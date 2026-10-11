// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe.biome;

import com.hbmspace.entity.MobEntitiesSpace;

public class BiomeGenLaytheCoast extends BiomeGenBaseLaythe {

    BiomeGenLaytheCoast() {
        super("laythe_coast", 94);
        this.setBiomeName("Laythe Reef");

        this.setHeight(new Height(-0.4F, 0.01F));
        this.setTemperatureRainfall(0.2F, 0.2F);
        this.theBiomeDecorator.waterPlantsPerChunk = 16;
        this.theBiomeDecorator.coralPerChunk = 32;

        this.waterCreatures.add(new SpawnListEntry(MobEntitiesSpace.SCUTTLECRAB, 4, 1, 3));
    }
}
