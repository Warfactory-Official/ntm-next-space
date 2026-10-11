// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho.biome;

public class BiomeGenMohoPlateau extends BiomeGenBaseMoho {

    BiomeGenMohoPlateau() {
        super("moho_plateau", 98);
        this.setBiomeName("Moho Plateau");

        this.setHeight(new Height(0.255F, 0.432F));
    }
}
