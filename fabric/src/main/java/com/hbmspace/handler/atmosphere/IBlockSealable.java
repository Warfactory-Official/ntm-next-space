// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public interface IBlockSealable {

    boolean isSealed(Level level, BlockPos pos);

    default void updateSealedState(ServerLevel level, BlockPos pos) {
        BlockPos at = pos.immutable();
        ChunkAtmosphereManager.proxy.onSealableChange(
                level, at, this, ChunkAtmosphereManager.proxy.getBlobsWithinRadius(level, at));
    }
}
