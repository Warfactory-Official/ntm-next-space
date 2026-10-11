// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.fog;

import com.mojang.blaze3d.buffers.GpuBufferSlice;

public interface TerrainFogWriter {
    boolean hbmspace$fogEnabled();

    GpuBufferSlice hbmspace$worldFog();

    float hbmspace$writtenDensity();

    int hbmspace$writtenFlags();
}
