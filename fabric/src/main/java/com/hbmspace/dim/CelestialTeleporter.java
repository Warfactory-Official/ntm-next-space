// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import java.util.ArrayDeque;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class CelestialTeleporter {

    private static final Queue<CelestialTeleporter> queue = new ArrayDeque<>();

    private final Entity entity;
    private final ServerLevel target;
    private final double x;
    private final double y;
    private final double z;
    private final boolean grounded;

    private CelestialTeleporter(
            Entity entity, ServerLevel target, double x, double y, double z, boolean grounded) {
        this.entity = entity;
        this.target = target;
        this.x = x;
        this.y = y;
        this.z = z;
        this.grounded = grounded;
    }

    public static void teleport(
            Entity entity,
            ResourceKey<Level> dimension,
            double x,
            double y,
            double z,
            boolean grounded) {
        if (entity.level().dimension() == dimension) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        ServerLevel target = level.getServer().getLevel(dimension);
        if (target == null)
            throw new IllegalArgumentException("no level is declared for " + dimension);

        queue.add(new CelestialTeleporter(entity, target, x, y, z, grounded));
    }

    public static void runQueuedTeleports(MinecraftServer server) {
        CelestialTeleporter teleporter;
        while ((teleporter = queue.poll()) != null) teleporter.runTeleport();
    }

    public static void onServerStopping() {
        queue.clear();
    }

    private void runTeleport() {
        if (entity.isRemoved()) return;
        Entity root = entity.getRootVehicle();
        root.teleport(
                new TeleportTransition(
                        target,
                        placement(),
                        root.getDeltaMovement(),
                        root.getYRot(),
                        root.getXRot(),
                        TeleportTransition.DO_NOTHING));
    }

    private Vec3 placement() {
        int ix = Mth.floor(x);
        int iz = Mth.floor(z);

        double placedY = y;
        if (grounded) {
            for (int i = target.getMaxY(); i > target.getMinY(); i--) {
                if (!target.getBlockState(new BlockPos(ix, i, iz)).isAir()) {
                    placedY = i + 5;
                    break;
                }
            }
        } else {
            target.getBlockState(
                    new BlockPos(
                            ix,
                            Mth.clamp(Mth.floor(y), target.getMinY() + 1, target.getMaxY()),
                            iz));
        }

        return new Vec3(x, placedY, z);
    }
}
