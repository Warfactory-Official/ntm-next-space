// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy.layer;

public class GenLayerFuzzyZoom extends GenLayerZoom {

    public GenLayerFuzzyZoom(long seed, GenLayer parent) {
        super(seed, parent);
    }

    @Override
    protected int selectModeOrRandom(int a, int b, int c, int d) {
        return this.selectRandom(a, b, c, d);
    }
}
