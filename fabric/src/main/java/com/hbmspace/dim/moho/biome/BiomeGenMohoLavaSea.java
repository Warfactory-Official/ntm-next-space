// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho.biome;

public class BiomeGenMohoLavaSea extends BiomeGenBaseMoho {

    BiomeGenMohoLavaSea() {
        super("moho_lava_sea", 99);
        this.setBiomeName("Moho Lava Sea");

        this.setHeight(new Height(-0.8F, 0.01F));
    }
}
