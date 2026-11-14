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

package com.hbmspace.entity.mob;

import com.hbm.entity.mob.CreeperBaseEntity;
import com.hbm.explosion.vanillant.ComposableExplosion;
import com.hbm.explosion.vanillant.standard.CellDestruction;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.LimitedRayBlockSelector;
import com.hbm.explosion.vanillant.standard.ShellBlockTransform;
import com.hbm.explosion.vanillant.standard.SinglePointExplosionEntityProcessor;
import com.hbm.particle.EffectKinds.GibletType;
import com.hbm.particle.helper.ParticleCreators;
import com.hbmspace.blocks.TerrainBlocksSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class CreeperFleshEntity extends CreeperBaseEntity {

    public CreeperFleshEntity(EntityType<? extends CreeperFleshEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void detonate(ServerLevel level) {
        ParticleCreators.giblets(level, this, GibletType.MEAT, 2);
        ComposableExplosion vnt =
                new ComposableExplosion(level, getX(), getY(), getZ(), isPowered() ? 7 : 4, this);
        vnt.blockSelector(new LimitedRayBlockSelector(30, isPowered() ? 16 : 8));
        vnt.blockProcessor(
                CellDestruction.decay()
                        .transform(
                                new ShellBlockTransform(
                                        () -> TerrainBlocksSpace.TUMOR.get().defaultBlockState())));
        vnt.entityProcessor(new SinglePointExplosionEntityProcessor().range(0.25F));
        vnt.effects(new ExplosionEffectStandard());
        vnt.explode();
    }
}
