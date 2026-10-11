// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbmspace.blocks.generic.BlockFallingTint;
import java.awt.Color;
import java.util.Set;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class FallingTintSource implements BlockTintSource {

    public static final FallingTintSource INSTANCE = new FallingTintSource();

    private FallingTintSource() {}

    @Override
    public int color(BlockState state) {
        return Color.HSBtoRGB(0F, 0F, BlockFallingTint.brightness(state));
    }

    @Override
    public Set<Property<?>> relevantProperties() {
        return Set.of(BlockFallingTint.SHADE);
    }
}
