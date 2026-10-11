// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.fog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TerrainExpFog {
    public static final int NATIVE_PREFIX = 40;
    public static final int NATIVE_SIZE = 48;
    public static final int SODIUM_PREFIX = 184;
    public static final int EXP = 1;
    public static final int EDGE = 2;
    public static final int DARKNESS = 4;

    public enum EdgeRamp {
        SOURCE_ONLY,
        KEEP_VANILLA
    }

    public enum Darkness {
        SPACE_REPLACES_RANGE,
        COMBINE_RANGES
    }

    private TerrainExpFog() {}

    public static int flags(float density, boolean darknessSelected) {
        assert Float.isFinite(density) && density >= 0F;
        return density > 0F ? EXP | EDGE | (darknessSelected ? DARKNESS : 0) : 0;
    }

    public static void write(
            ByteBuffer destination,
            int offset,
            float density,
            EdgeRamp edge,
            Darkness darkness,
            boolean darknessSelected) {
        assert Float.isFinite(density) && density >= 0F;
        int flags = density > 0F ? EXP : 0;
        if (density > 0F && edge == EdgeRamp.KEEP_VANILLA) flags |= EDGE;
        if (density > 0F && darknessSelected && darkness == Darkness.COMBINE_RANGES)
            flags |= DARKNESS;
        destination
                .order(ByteOrder.nativeOrder())
                .putFloat(offset, density)
                .putInt(offset + 4, flags);
        destination.position(offset + 8);
    }

    public static void clear(ByteBuffer destination, int offset) {
        destination.order(ByteOrder.nativeOrder()).putFloat(offset, 0F).putInt(offset + 4, 0);
        destination.position(offset + 8);
    }
}
