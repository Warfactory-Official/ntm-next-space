// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toclient.GasFlamePayload;
import com.hbm.particle.HbmParticles;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ReentryFlames {

    private static final Random RANDOM = new Random();

    private ReentryFlames() {}

    public static void playerTick(Player player) {
        double y = player.getY();
        if (y <= 300D || y >= 1000D) return;
        double x = player.getX() - 1D + 3D * RANDOM.nextDouble();
        Level level = player.level();
        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        if (!eligible(
                y,
                atmosphere == null ? 0D : atmosphere.getPressure(),
                player.isPassenger(),
                player.getDeltaMovement())) return;

        double z = player.getZ();
        if (level instanceof ServerLevel server) {
            PacketWire.sendToAllAround(
                    new GasFlamePayload(x, y, z, 0D, 0D, 0D, GasFlamePayload.DEFAULT_SCALE),
                    new TargetPoint(server, x, y, z, 150D));
        } else {
            level.addParticle(HbmParticles.GAS_FLAME.get(), true, false, x, y, z, 0D, 0D, 0D);
        }
    }

    private static boolean eligible(double y, double pressure, boolean riding, Vec3 motion) {
        return y > 300D
                && y < 1000D
                && pressure > 0.05D
                && !riding
                && (Math.abs(motion.x) > 1D || Math.abs(motion.y) > 1D || Math.abs(motion.z) > 1D);
    }
}
