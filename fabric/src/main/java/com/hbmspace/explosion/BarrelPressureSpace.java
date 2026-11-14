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

package com.hbmspace.explosion;

import com.hbm.api.fluidmk2.FluidPipeGraph;
import com.hbm.entity.effect.CloudFleijaEntity;
import com.hbm.entity.logic.NukeExplosionMK3Entity;
import com.hbm.explosion.vanillant.ComposableExplosion;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.entity.effect.TorexAntimatter;
import com.hbmspace.entity.logic.AntimatterBlastEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

public final class BarrelPressureSpace {
    private BarrelPressureSpace() {}

    public static void retire(ServerLevel level, BlockPos pos) {
        var graph = FluidPipeGraph.graphAt(level, pos.asLong());
        if (graph != null) graph.removeNode(pos.asLong());
    }

    public static void emit(ServerLevel level, BlockPos pos, Fluid fluid, int power) {
        if (fluid == NTMFluids.AMAT) {
            if (power >= 25) {
                AntimatterBlastEntity.spawn(level, pos.getX(), pos.getY(), pos.getZ(), power);
                TorexAntimatter.startFacAnti(
                        level, pos.getX(), pos.getY(), pos.getZ(), power * 1.5F);
            } else {
                new ComposableExplosion(level, pos.getX(), pos.getY(), pos.getZ(), power)
                        .antimatter()
                        .explode();
            }
        } else {
            assert fluid == NTMFluids.ASCHRAB;
            var blast =
                    NukeExplosionMK3Entity.statFacFleija(
                            level, pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D, power);
            if (!blast.isRemoved()) {
                level.addFreshEntity(blast);
                level.addFreshEntity(
                        CloudFleijaEntity.statFac(
                                level,
                                power,
                                pos.getX() + .5D,
                                pos.getY() + .5D,
                                pos.getZ() + .5D));
            }
        }
    }

    public static void ordinary(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.explode(
                null,
                pos.getX() + .5D,
                pos.getY() + .5D,
                pos.getZ() + .5D,
                5F,
                Level.ExplosionInteraction.NONE);
    }
}
