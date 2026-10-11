// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.items.WarItemsSpace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

public class SatelliteRailgun extends SatelliteWar {

    public static final MapCodec<SatelliteRailgun> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Codec.LONG
                                                    .optionalFieldOf("lastOp", 0L)
                                                    .forGetter(sat -> sat.lastOp))
                                    .apply(i, SatelliteRailgun::new));

    private @Nullable CelestialBody target;

    public SatelliteRailgun() {
        this(0L);
    }

    private SatelliteRailgun(long lastOp) {
        this.lastOp = lastOp;
    }

    @Override
    public SatelliteType type() {
        return WarItemsSpace.WAR;
    }

    @Override
    public void setTarget(CelestialBody body) {
        target = body;
    }

    public @Nullable CelestialBody getTarget() {
        return target;
    }
}
