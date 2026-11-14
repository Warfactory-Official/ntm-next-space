/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-FileCopyrightText: 2023 Pu-238
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2023 JamesH_2. All rights reserved.
 * Copyright (C) 2023 Pu-238. All rights reserved.
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

package com.hbmspace.entity.logic;

import com.hbm.entity.logic.BalefireEntity;
import com.hbm.explosion.BlastSmoke;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.SectionAccess;
import com.hbmspace.entity.BombEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AntimatterBlastEntity extends BalefireEntity {

    public AntimatterBlastEntity(EntityType<? extends AntimatterBlastEntity> type, Level level) {
        super(type, level);
    }

    public static AntimatterBlastEntity spawn(
            Level level, double x, double y, double z, int range) {
        AntimatterBlastEntity blast =
                new AntimatterBlastEntity(BombEntitiesSpace.ANTIMATTER_BLAST.get(), level);
        blast.setPos(x, y, z);
        blast.destructionRange = range;
        level.addFreshEntity(blast);
        return blast;
    }

    public void flash() {
        if (level() instanceof ServerLevel server && tickCount < 10) {
            radiate(server, 40000F * destructionRange, destructionRange * 2);
        }
    }

    private void radiate(ServerLevel level, float rads, double range) {
        Vec3 centre = position();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        var reader = new SectionAccess.LoadedReader(level);
        entities:
        for (LivingEntity e :
                level.getEntitiesOfClass(
                        LivingEntity.class, new AABB(centre, centre).inflate(range))) {
            Vec3 vec = new Vec3(e.getX() - centre.x, e.getEyeY() - centre.y, e.getZ() - centre.z);
            double len = vec.length();
            vec = vec.normalize();

            float res = 0;
            for (int i = 1; i < len; i++) {
                pos.set(centre.x + vec.x * i, centre.y + vec.y * i, centre.z + vec.z * i);
                BlockState state = reader.read(pos);
                if (state == null) continue entities;
                res += state.getBlock().getExplosionResistance();
            }
            if (res < 1) res = 1;

            float eRads = rads;
            eRads /= res;
            eRads /= (float) (len * len);

            ContaminationUtil.contaminate(
                    e, HazardType.RADIATION, ContaminationType.CREATIVE, eRads);
            if (eRads >= 500 && !(e instanceof Player player && player.getAbilities().instabuild)) {
                BlastSmoke.shock(level, e.getX(), e.getY(), e.getZ(), 10, 10);
                if (e instanceof Player) {
                    e.kill(level);
                } else {
                    e.setHealth(0);
                    e.discard();
                }
            }
        }
    }

    public static void stepEffects(BalefireEntity blast, boolean finished) {
        Level level = blast.level();
        RandomSource rand = blast.getRandom();
        if (rand.nextInt(5) == 0) {
            level.playSound(
                    null,
                    blast.getX(),
                    blast.getY(),
                    blast.getZ(),
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS,
                    10000.0F,
                    0.8F + rand.nextFloat() * 0.2F);
        }
        if (!finished) {
            level.playSound(
                    null,
                    blast.getX(),
                    blast.getY(),
                    blast.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER,
                    10000.0F,
                    0.8F + rand.nextFloat() * 0.2F);
        }
    }
}
