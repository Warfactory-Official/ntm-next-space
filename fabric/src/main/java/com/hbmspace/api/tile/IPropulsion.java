// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.api.tile;

import com.hbmspace.dim.orbit.OrbitalStation;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPropulsion {

    default void registerPropulsion() {
        OrbitalStation.addPropulsion(this);
    }

    default void unregisterPropulsion() {
        OrbitalStation.removePropulsion(this);
    }

    BlockEntity getBlockEntity();

    boolean canPerformBurn(int shipMass, double deltaV);

    void addErrors(List<Component> errors);

    float getThrust();

    int startBurn();

    int endBurn();
}
