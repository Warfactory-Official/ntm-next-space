// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.explosion;

import com.hbm.api.fluidmk2.FluidPipeGraph;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.entity.effect.TorexAntimatter;
import com.hbmspace.entity.logic.EntityAntimatterBlast;
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
                EntityAntimatterBlast.spawn(level, pos.getX(), pos.getY(), pos.getZ(), power);
                TorexAntimatter.startFacAnti(
                        level, pos.getX(), pos.getY(), pos.getZ(), power * 1.5F);
            } else {
                new ExplosionVNT(level, pos.getX(), pos.getY(), pos.getZ(), power)
                        .makeAmat()
                        .explode();
            }
        } else {
            assert fluid == NTMFluids.ASCHRAB;
            var blast =
                    EntityNukeExplosionMK3.statFacFleija(
                            level, pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D, power);
            if (!blast.isRemoved()) {
                level.addFreshEntity(blast);
                level.addFreshEntity(
                        EntityCloudFleija.statFac(
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
