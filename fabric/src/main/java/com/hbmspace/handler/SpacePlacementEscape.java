// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface SpacePlacementEscape {

    void hbmspace$grantPlacementEscape(ResourceKey<Level> dimension, long core);

    void hbmspace$refreshPlacementEscape();

    boolean hbmspace$hasPlacementEscape();

    boolean hbmspace$ignoresPlacementCollision(Level level, BlockPos pos);
}
