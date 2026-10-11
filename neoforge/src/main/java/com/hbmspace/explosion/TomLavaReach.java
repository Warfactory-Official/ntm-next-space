// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.explosion;

public interface TomLavaReach {

    void hbmspace$setLavaReach(float reach);

    boolean hbmspace$floods(int x, int z);

    boolean hbmspace$inside(int x, int z);

    int hbmspace$floor(int x, int z);
}
