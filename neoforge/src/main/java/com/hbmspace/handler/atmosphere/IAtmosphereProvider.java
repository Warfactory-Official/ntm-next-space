// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public interface IAtmosphereProvider {

    Level getLevel();

    int getMaxBlobRadius();

    BlockPos getRootPosition();

    Fluid getFluidType();

    double getFluidPressure();

    void consume(int amount);

    void produce(int amount);

    void onBlobCreated(AtmosphereBlob blob);
}
