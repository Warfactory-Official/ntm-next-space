// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbmspace.blocks.AtmosphereBlocksSpace;
import com.hbmspace.lib.ModDamageTypesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class EveDeath {

    private EveDeath() {}

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level) || !source.is(ModDamageTypesSpace.EVE))
            return;

        BlockState flesh = AtmosphereBlocksSpace.FLESH_BLOCK.get().defaultBlockState();
        BlockPos center = entity.blockPosition();
        for (BlockPos pos :
                BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (level.getBlockState(pos).isAir() && flesh.canSurvive(level, pos))
                level.setBlockAndUpdate(pos, flesh);
        }
    }
}
