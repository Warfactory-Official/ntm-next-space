// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy.layer;

public final class IntCache {

    private IntCache() {}

    public static int[] getIntCache(int size) {
        return new int[size];
    }
}
