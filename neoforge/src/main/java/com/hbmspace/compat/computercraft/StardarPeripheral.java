// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 BallOfEnergy
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2026 Voxelstice
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.compat.computercraft;

import com.hbm.compat.computercraft.SnapshotPeripheral;
import com.hbmspace.blockentity.machine.BlockEntityMachineStardar;
import com.hbmspace.dim.CelestialBody;
import dan200.computercraft.api.lua.LuaFunction;

public final class StardarPeripheral extends SnapshotPeripheral<BlockEntityMachineStardar> {

    public StardarPeripheral(BlockEntityMachineStardar star) {
        super(star, "ntm_stardar", 0, 0);
    }

    @Override
    protected void capture(BlockEntityMachineStardar star) {}

    @LuaFunction(mainThread = true)
    public final Object[] getPlanetStats(String name) {
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
            body.getProcessingLevel(CelestialBody.getBody(machine().getLevel())),
            body.radiusKm,
            body.semiMajorAxisKm,
            body.getSunPower(),
            body.getSurfaceGravity(),
            body.getRotationalPeriod(),
            orbitalPeriod
        };
    }

    @LuaFunction(mainThread = true)
    public final Object[] getCurrentPlanet() {
        return new Object[] {CelestialBody.getBody(machine().getLevel()).name};
    }

    @LuaFunction(mainThread = true)
    public final Object[] getSatellites(String name) {
        CelestialBody body = CelestialBody.getBodyOrNull(name);
        if (body == null) return new Object[] {null, "No body with that name found."};
        return body.satellites.stream().map(moon -> moon.name).toArray();
    }
}
