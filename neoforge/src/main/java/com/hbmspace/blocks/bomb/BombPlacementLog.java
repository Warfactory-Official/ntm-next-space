// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.bomb;

import com.google.common.base.Suppliers;
import com.hbm.NuclearTech;
import com.hbm.api.block.BlockPlacements;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.NeoForgeHbmConfig;
import com.hbmspace.blocks.BombBlocksSpace;
import com.hbmspace.blocks.MaterialBlocksSpace;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public final class BombPlacementLog {

    private static final Supplier<Set<Block>> LOGGED =
            Suppliers.memoize(
                    () ->
                            Set.of(
                                    ModBlocks.CHARGE_C4.get(),
                                    ModBlocks.CHARGE_DYNAMITE.get(),
                                    ModBlocks.CHARGE_MINER.get(),
                                    ModBlocks.CHARGE_SEMTEX.get(),
                                    ModBlocks.DYNAMITE.get(),
                                    ModBlocks.FIREWORKS.get(),
                                    ModBlocks.FISSURE_BOMB.get(),
                                    ModBlocks.SEMTEX.get(),
                                    ModBlocks.TNT_NTM.get(),
                                    ModBlocks.VOLCANO_CORE.get(),
                                    ModBlocks.VOLCANO_RAD_CORE.get(),
                                    ModBlocks.FLAME_WAR.get(),
                                    ModBlocks.EMP_BOMB.get(),
                                    ModBlocks.FLOAT_BOMB.get(),
                                    ModBlocks.BOMB_MULTI.get(),
                                    ModBlocks.THERM_ENDO.get(),
                                    ModBlocks.THERM_EXO.get(),
                                    ModBlocks.DET_CHARGE.get(),
                                    ModBlocks.DET_NUKE.get(),
                                    MaterialBlocksSpace.DET_SALT.get(),
                                    ModBlocks.MINE_AP.get(),
                                    ModBlocks.MINE_FAT.get(),
                                    ModBlocks.MINE_HE.get(),
                                    ModBlocks.MINE_NAVAL.get(),
                                    ModBlocks.MINE_SHRAP.get(),
                                    BombBlocksSpace.NUKE_ANTIMATTER.get(),
                                    ModBlocks.NUKE_FSTBMB.get(),
                                    ModBlocks.NUKE_BOY.get(),
                                    ModBlocks.NUKE_FLEIJA.get(),
                                    ModBlocks.NUKE_GADGET.get(),
                                    ModBlocks.NUKE_MAN.get(),
                                    ModBlocks.NUKE_MIKE.get(),
                                    ModBlocks.NUKE_N2.get(),
                                    ModBlocks.NUKE_PROTOTYPE.get(),
                                    ModBlocks.NUKE_SOLINIUM.get(),
                                    ModBlocks.NUKE_TSAR.get()));

    private BombPlacementLog() {}

    public static void install() {
        BlockPlacements.onPlacedBy(BombPlacementLog::placed);
    }

    private static void placed(ServerPlayer player, BlockPos pos) {
        if (!NeoForgeHbmConfig.runtime().extendedLogging()) return;
        Block block = player.level().getBlockState(pos).getBlock();
        if (!LOGGED.get().contains(block)) return;
        NuclearTech.LOGGER.info(
                "[BOMBPL]{} placed at {} / {} / {}! by {}",
                block.getName().getString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                player.getName().getString());
    }
}
