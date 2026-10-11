// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.MachineFieldDisturber;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MachineTrollDisturber extends MachineFieldDisturber {
    public static final double ABSORB = 10_000_000D;

    public static final MapCodec<MachineTrollDisturber> CODEC =
            simpleCodec(MachineTrollDisturber::new);

    public MachineTrollDisturber(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel server) {
            RadiationSystemNT.setRadForCoord(
                    server, pos, RadiationSystemNT.getRadForCoord(server, pos) - ABSORB);
        }
    }
}
