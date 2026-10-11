// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import java.util.Random;

@FunctionalInterface
public interface LegacyWorldGenerator {

    void generate(Random random, int chunkX, int chunkZ, LegacyDecorWorld world);
}
