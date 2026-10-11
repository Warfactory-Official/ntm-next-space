// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.api.environment.Environment;
import com.hbm.client.ClientEffects;
import com.hbmspace.blockentity.deco.BlockEntityGeysirDCM;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class FloraClientEffects {

    private FloraClientEffects() {}

    public static void install() {
        BlockEntityGeysirDCM.CLIENT_EFFECTS = FloraClientEffects::dcmContrail;
    }

    private static void dcmContrail(BlockEntityGeysirDCM be) {
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        double x = pos.getX() + 0.5D, y = pos.getY(), z = pos.getZ() + 0.5D;
        Player player = Minecraft.getInstance().player;
        if (player == null || player.distanceToSqr(x, y, z) > 350D * 350D) return;
        ClientEffects.spawnRocketFlameInAir(
                level,
                x,
                y,
                z,
                1.5F,
                0D,
                4D,
                0D,
                100 + level.getRandom().nextInt(20),
                Environment.particlePressure(level),
                0xA4 / 255F,
                0xD7 / 255F,
                0xDD / 255F);
    }
}
