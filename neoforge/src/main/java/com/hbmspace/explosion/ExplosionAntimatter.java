// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 George Paton
// SPDX-FileCopyrightText: 2020 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.explosion;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.ExplosionBalefire;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toclient.GasFlamePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class ExplosionAntimatter extends ExplosionBalefire {

    public ExplosionAntimatter(int x, int y, int z, Level level, int rad) {
        super(x, y, z, level, rad);
    }

    @Override
    protected void breakColumn(int x, int z) {
        int dist = (int) (radius - Math.sqrt(x * x + z * z));

        if (dist <= 0) return;

        int pX = posX + x;
        int pZ = posZ + z;

        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, pX, pZ);
        int maxdepth = (int) (10 + radius * 0.25);
        int depth = (int) ((maxdepth * dist / (double) radius) + (Math.sin(dist * 0.15 + 2) * 2));

        depth = Math.max(y - depth, level.getMinY());

        while (y > depth) {
            BlockPos pos = new BlockPos(pX, y, pZ);

            if (flames(level.getBlockState(pos))) {
                if (level instanceof ServerLevel server) {
                    PacketWire.sendToAllAround(
                            new GasFlamePayload(pX, y, pZ, x, y, z, GasFlamePayload.DEFAULT_SCALE),
                            new TargetPoint(server, pX, y, pZ, 150));
                }
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }

            y--;
        }

        for (int i = depth; i > depth - 5; i--) {
            if (level.getRandom().nextInt(dist) == 0) {
                level.setBlock(
                        new BlockPos(pX, depth, pZ),
                        ModBlocks.VOLCANIC_LAVA_BLOCK.get().defaultBlockState(),
                        Block.UPDATE_ALL);
            }

            BlockPos pos = new BlockPos(pX, i, pZ);
            if (slakes(level.getBlockState(pos))) {
                level.setBlock(
                        pos,
                        ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }
    }

    private static boolean flames(BlockState state) {
        return state.is(BlockTags.SNOW) || state.is(BlockTags.ICE);
    }
}
