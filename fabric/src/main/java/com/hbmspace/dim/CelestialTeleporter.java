/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.dim;

import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class CelestialTeleporter {

    private CelestialTeleporter() {}

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

        Entity root = entity.getRootVehicle();
        root.teleport(
                new TeleportTransition(
                        target,
                        placement(target, x, y, z, grounded),
                        root.getDeltaMovement(),
                        root.getYRot(),
                        root.getXRot(),
                        TeleportTransition.DO_NOTHING));
    }

    private static Vec3 placement(
            ServerLevel target, double x, double y, double z, boolean grounded) {
        int ix = Mth.floor(x);
        int iz = Mth.floor(z);
        LevelChunk chunk =
                target.getChunk(
                        SectionPos.blockToSectionCoord(ix), SectionPos.blockToSectionCoord(iz));
        if (!grounded) return new Vec3(x, y, z);

        int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, ix, iz);
        return new Vec3(x, top > target.getMinY() ? top + 5 : y, z);
    }
}
