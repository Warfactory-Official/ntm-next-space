// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto.biome;

public class BiomeGenVinylSands extends BiomeGenBaseTekto {

    BiomeGenVinylSands() {
        super("tekto_vinyl_desert", 88);
        this.setBiomeName("Vinyl Desert");

        this.setHeight(new Height(0.425F, 0.3F));
    }
}
