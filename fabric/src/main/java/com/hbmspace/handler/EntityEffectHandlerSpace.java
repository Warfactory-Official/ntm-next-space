/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.handler;

import com.hbm.api.environment.SuffocationImmune;
import com.hbm.entity.mob.CyberCrabEntity;
import com.hbm.entity.mob.glyphid.GlyphidEntity;
import com.hbm.lib.ModDamageTypes;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import com.hbmspace.items.armor.ArmorUtilSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class EntityEffectHandlerSpace {

    private EntityEffectHandlerSpace() {}

    public static void onUpdate(LivingEntity entity) {
        CBT_Atmosphere atmosphere = getAtmosphereCached(entity);

        handleOxy(entity, atmosphere);
        handleCorrosion(entity, atmosphere);

        if (entity instanceof ServerPlayer player) SpaceLivingProps.sync(player);
    }

    private static @Nullable CBT_Atmosphere getAtmosphereCached(LivingEntity entity) {
        if (entity instanceof ServerPlayer || entity.tickCount % 20 == 0) {
            CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(entity);
            SpaceLivingProps.setAtmosphere(entity, atmosphere);
            return atmosphere;
        }

        return SpaceLivingProps.getAtmosphere(entity);
    }

    public static void handleOxy(LivingEntity entity, @Nullable CBT_Atmosphere atmosphere) {
        if (entity instanceof SuffocationImmune) return;
        if (entity.getVehicle() instanceof RideableRocketEntity) {
            SpaceLivingProps.setTankInUse(entity, false);
            return;
        }

        if (!ArmorUtilSpace.checkForOxy(entity, atmosphere)) {
            SpaceLivingProps.setOxy(entity, SpaceLivingProps.getOxy(entity) - 1);
        } else {
            SpaceLivingProps.setOxy(entity, SpaceLivingProps.FULL_BREATH);
        }
    }

    public static void handleCorrosion(LivingEntity entity, @Nullable CBT_Atmosphere atmosphere) {
        if (entity instanceof GlyphidEntity) return;
        if (entity instanceof CyberCrabEntity) return;
        if (entity.getVehicle() instanceof RideableRocketEntity) return;

        if (ArmorUtilSpace.checkForCorrosion(entity, atmosphere)) {
            ServerLevel level = (ServerLevel) entity.level();
            entity.hurtServer(level, level.damageSources().source(ModDamageTypes.ACID), 1);
        }
    }
}
