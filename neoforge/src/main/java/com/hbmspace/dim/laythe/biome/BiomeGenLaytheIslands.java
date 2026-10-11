// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe.biome;

public class BiomeGenLaytheIslands extends BiomeGenBaseLaythe {

    BiomeGenLaytheIslands() {
        super("laythe_islands", 97);
        this.setBiomeName("Laythe Islands");
        this.waterColorMultiplier = 0x5b209a;

        this.setHeight(new Height(0.256F, 0.05F));
        this.setTemperatureRainfall(0.2F, 0.2F);
    }
}
