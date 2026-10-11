// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.items.SkyItemsSpace;
import com.mojang.serialization.MapCodec;

public class SatelliteDysonRelay extends Satellite {

    public static final MapCodec<SatelliteDysonRelay> CODEC =
            MapCodec.unit(SatelliteDysonRelay::new);

    @Override
    public SatelliteType type() {
        return SkyItemsSpace.DYSON_RELAY;
    }
}
