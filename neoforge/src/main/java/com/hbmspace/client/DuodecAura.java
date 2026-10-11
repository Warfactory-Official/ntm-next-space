// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbmspace.particle.SpaceParticles;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public final class DuodecAura {
    private static final UUID DUODEC = UUID.fromString("85d54b8c-1862-4c86-b351-5ef1b998aa32");

    private DuodecAura() {}

    public static void playerTick(Player player) {
        if (!player.getUUID().equals(DUODEC) || player.isInvisible() || player.isShiftKeyDown())
            return;
        RandomSource random = player.getRandom();
        double radius = 3D * random.nextDouble();
        float zAngle = (float) (random.nextDouble() * Math.PI);
        float yAngle = (float) (random.nextDouble() * Math.PI * 2D);
        double x = radius * cosine(zAngle);
        double y = -radius * sine(zAngle);
        double z = -x * sine(yAngle);
        x *= cosine(yAngle);
        Minecraft mc = Minecraft.getInstance();
        emit(
                mc,
                SpaceParticles.DUOEWE.get(),
                player.getX() + x,
                player.getY() + 1D + y,
                player.getZ() + z,
                random);
        emit(
                mc,
                SpaceParticles.DUODEC.get(),
                player.getX() + x,
                player.getY() + 1D + y,
                player.getZ() + z,
                random);
    }

    private static void emit(
            Minecraft mc,
            SimpleParticleType type,
            double x,
            double y,
            double z,
            RandomSource random) {
        if (random.nextInt(12) != 0) return;
        ParticleStatus settings = mc.options.particles().get();
        if (settings == ParticleStatus.ALL
                || settings == ParticleStatus.DECREASED && random.nextBoolean()) {
            mc.particleEngine.createParticle(type, x, y, z, 0D, 0D, 0D);
        }
    }

    private static float sine(float angle) {
        int index = (int) (angle * 10430.378F) & 65535;
        return (float) Math.sin((double) index * Math.PI * 2D / 65536D);
    }

    private static float cosine(float angle) {
        int index = (int) (angle * 10430.378F + 16384F) & 65535;
        return (float) Math.sin((double) index * Math.PI * 2D / 65536D);
    }
}
