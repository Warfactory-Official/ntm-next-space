// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.fog;

import com.mojang.blaze3d.buffers.GpuBuffer;

public interface BoundTerrainFog extends TerrainFogPayload {
    TerrainFogWriter hbmspace$writer();

    void hbmspace$writer(TerrainFogWriter writer);

    GpuBuffer hbmspace$buffer();

    long hbmspace$offset();

    long hbmspace$length();

    void hbmspace$binding(GpuBuffer buffer, long offset, long length);

    void hbmspace$cullDistance(float distance);

    boolean hbmspace$cullOverridden();
}
