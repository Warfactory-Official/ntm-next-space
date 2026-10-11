// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.minmus.biome;

import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.BiomeDecoratorCelestial;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import net.minecraft.resources.Identifier;

public abstract class BiomeGenBaseMinmus extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial minmusPlains = new BiomeGenMinmusHills();
    public static final BiomeGenBaseCelestial minmusCanyon = new BiomeGenMinmusBasin();

    protected BiomeGenBaseMinmus(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.setEnableSnow();
        this.setTemperatureRainfall(-1.0F, 0.0F);

        this.theBiomeDecorator = new BiomeDecoratorCelestial(ModBlocksSpace.MINMUS_REGOLITH.get());
        this.theBiomeDecorator.generateLakes = false;

        this.topBlock = ModBlocksSpace.MINMUS_REGOLITH.get();
        this.fillerBlock = ModBlocksSpace.MINMUS_REGOLITH.get();
    }
}
