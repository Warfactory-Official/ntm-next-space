/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.saveddata.satellites;

import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.items.WarItemsSpace;
import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.Nullable;

public class SatelliteRailgun extends SatelliteWar {
    public static final MapCodec<SatelliteRailgun> CODEC = MapCodec.unit(SatelliteRailgun::new);

    private @Nullable CelestialBody target;

    @Override
    public SatelliteType kind() {
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
