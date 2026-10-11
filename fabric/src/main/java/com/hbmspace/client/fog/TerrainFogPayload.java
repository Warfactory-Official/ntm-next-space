// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.fog;

public interface TerrainFogPayload {
    float hbmspace$density();

    int hbmspace$flags();

    void hbmspace$payload(float density, int flags);
}
