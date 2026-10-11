// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import com.hbmspace.dim.BiomeGenBaseCelestial;

public interface LegacyWorld {

    long getSeed();

    BiomeGenBaseCelestial getBiomeGenForCoords(int x, int z);

    BiomeGenBaseCelestial[] getBiomesForGeneration(int x, int z, int width, int length);

    BiomeGenBaseCelestial[] loadBlockGeneratorData(int x, int z, int width, int length);
}
