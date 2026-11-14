/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 BallOfEnergy
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2026 Voxelstice
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 BallOfEnergy. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2026 Voxelstice. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.compat.computercraft;

import com.hbm.compat.computercraft.BlockEntityPeripheral;
import com.hbmspace.blockentity.machine.StardarBlockEntity;
import com.hbmspace.dim.CelestialBody;
import dan200.computercraft.api.lua.LuaFunction;

public final class StardarPeripheral extends BlockEntityPeripheral<StardarBlockEntity> {

    public StardarPeripheral(StardarBlockEntity star) {
        super(star, "ntm_stardar");
    }

    @LuaFunction
    public Object[] getPlanetStats(String name) {
        CelestialBody body = CelestialBody.getBodyOrNull(name);
        if (body == null) return new Object[] {null, "No body with that name found."};

        String parentName = null;
        double orbitalPeriod = 0.0;
        if (body.parent != null) {
            parentName = body.parent.name;
            orbitalPeriod = body.getOrbitalPeriod();
        }
        return new Object[] {
            body.name,
            parentName,
            body.getStar().name,
            body.tidallyLockedTo,
            body.axialTilt,
            body.canLand,
            body.massKg,
            body.getProcessingLevel(currentPlanet()),
            body.radiusKm,
            body.semiMajorAxisKm,
            body.getSunPower(),
            body.getSurfaceGravity(),
            body.getRotationalPeriod(),
            orbitalPeriod
        };
    }

    @LuaFunction
    public Object[] getCurrentPlanet() {
        return new Object[] {currentPlanet().name};
    }

    private CelestialBody currentPlanet() {
        return CelestialBody.getBody(((StardarBlockEntity) getTarget()).getLevel());
    }

    @LuaFunction
    public Object[] getSatellites(String name) {
        CelestialBody body = CelestialBody.getBodyOrNull(name);
        if (body == null) return new Object[] {null, "No body with that name found."};
        return body.satellites.stream().map(moon -> moon.name).toArray();
    }
}
