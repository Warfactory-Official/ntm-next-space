// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity;

import net.minecraft.core.BlockPos;

public interface IDysonConverter {
    boolean provideEnergy(BlockPos pos, long energy);
}
