// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbm.saveddata.satellites.Satellite;
import com.hbmspace.dim.CelestialBody;

public abstract class SatelliteWar extends Satellite {
    public long lastOp;

    public abstract void setTarget(CelestialBody body);
}
