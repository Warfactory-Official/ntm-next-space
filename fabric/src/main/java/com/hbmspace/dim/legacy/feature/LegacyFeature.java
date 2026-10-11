// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy.feature;

import com.hbmspace.dim.legacy.LegacyDecorWorld;
import java.util.Random;

@FunctionalInterface
public interface LegacyFeature {

    boolean generate(LegacyDecorWorld world, Random rand, int x, int y, int z);
}
