// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna.biome;

import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.BiomeDecoratorCelestial;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import net.minecraft.resources.Identifier;

public abstract class BiomeGenBaseDuna extends BiomeGenBaseCelestial {

    public static final BiomeGenBaseCelestial dunaPlains = new BiomeGenDunaPlains();
    public static final BiomeGenBaseCelestial dunaLowlands = new BiomeGenDunaLowlands();
    public static final BiomeGenBaseCelestial dunaPolar = new BiomeGenDunaPolar();
    public static final BiomeGenBaseCelestial dunaHills = new BiomeGenDunaHills();
    public static final BiomeGenBaseCelestial dunaPolarHills = new BiomeGenDunaPolarHills();

    protected BiomeGenBaseDuna(String name, int id) {
        super(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, name), id);
        this.setDisableRain();
        this.setTemperatureRainfall(-1.0F, 0.0F);

        this.theBiomeDecorator = new BiomeDecoratorCelestial(TerrainBlocksSpace.DUNA_ROCK.get());
        this.theBiomeDecorator.generateLakes = false;

        this.topBlock = ModBlocksSpace.DUNA_SANDS.get();
        this.fillerBlock = TerrainBlocksSpace.DUNA_ROCK.get();
    }
}
