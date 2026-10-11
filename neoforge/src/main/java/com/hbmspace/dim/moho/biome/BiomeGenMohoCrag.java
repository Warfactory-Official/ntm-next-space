// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho.biome;

public class BiomeGenMohoCrag extends BiomeGenBaseMoho {

    BiomeGenMohoCrag() {
        super("moho_crag", 101);
        this.setBiomeName("Moho Crag");

        this.setHeight(new Height(0.275F, 0.666F));
    }
}
