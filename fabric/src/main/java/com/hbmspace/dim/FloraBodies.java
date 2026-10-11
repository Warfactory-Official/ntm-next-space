// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.dim.eve.ChunkProviderEve;
import com.hbmspace.dim.eve.WorldGeneratorEve;
import com.hbmspace.dim.eve.WorldProviderEve;
import com.hbmspace.dim.laythe.ChunkProviderLaythe;
import com.hbmspace.dim.laythe.WorldGeneratorLaythe;
import com.hbmspace.dim.laythe.WorldProviderLaythe;
import com.hbmspace.dim.tekto.ChunkProviderTekto;
import com.hbmspace.dim.tekto.WorldGeneratorTekto;
import com.hbmspace.dim.tekto.WorldProviderTekto;
import com.hbmspace.dim.thatmo.ChunkProviderThatmo;
import com.hbmspace.dim.thatmo.WorldProviderThatmo;

public final class FloraBodies {

    static {
        WorldProviderCelestial.register(new WorldProviderEve());
        WorldChunkManagerCelestial.register(
                SpaceDimensions.EVE.identifier(), WorldProviderEve::createBiomeGenerators);
        SpaceChunkGenerators.register("hbmspace:eve", ChunkProviderEve.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.EVE, new WorldGeneratorEve());

        WorldProviderCelestial.register(new WorldProviderLaythe());
        WorldChunkManagerCelestial.register(
                SpaceDimensions.LAYTHE.identifier(), WorldProviderLaythe::createBiomeGenerators);
        SpaceChunkGenerators.register("hbmspace:laythe", ChunkProviderLaythe.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.LAYTHE, new WorldGeneratorLaythe());

        WorldProviderCelestial.register(new WorldProviderTekto());
        WorldChunkManagerCelestial.register(
                SpaceDimensions.TEKTO.identifier(), WorldProviderTekto::createBiomeGenerators);
        SpaceChunkGenerators.register("hbmspace:tekto", ChunkProviderTekto.CODEC);
        WorldGeneratorCelestial.register(1, SpaceDimensions.TEKTO, new WorldGeneratorTekto());

        WorldProviderCelestial.register(new WorldProviderThatmo());
        SpaceChunkGenerators.register("hbmspace:thatmo", ChunkProviderThatmo.CODEC);
    }

    private FloraBodies() {}
}
