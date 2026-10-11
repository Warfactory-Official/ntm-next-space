// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbmspace.api.tile.IPropulsion;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityStationPropulsionCreative extends BlockEntity implements IPropulsion {

    private boolean hasRegistered = false;

    public BlockEntityStationPropulsionCreative(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.PROPULSION_CREATIVE.get(), pos, state);
    }

    public void tickServer() {
        if (!CelestialBody.inOrbit(level)) return;

        if (!hasRegistered) {
            registerPropulsion();
            hasRegistered = true;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (hasRegistered) {
            unregisterPropulsion();
            hasRegistered = false;
        }
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public boolean canPerformBurn(int shipMass, double deltaV) {
        return true;
    }

    @Override
    public void addErrors(List<Component> errors) {}

    @Override
    public float getThrust() {
        return 10_000_000;
    }

    @Override
    public int startBurn() {
        return 20;
    }

    @Override
    public int endBurn() {
        return 20;
    }
}
