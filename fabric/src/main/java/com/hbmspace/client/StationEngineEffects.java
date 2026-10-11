// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.api.environment.Environment;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.ClientEffects;
import com.hbmspace.blockentity.machine.BlockEntityMachineGasDock;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTR3;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTRF4;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTRNeo;
import com.hbmspace.blockentity.machine.BlockEntityMachineLPW2Space;
import com.hbmspace.blockentity.machine.BlockEntityTransporterRocket;
import com.hbmspace.client.gui.ScreenTransporterLinker;
import com.hbmspace.items.tool.ItemTransporterLinker;
import com.hbmspace.particle.RocketFlamesSpace;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class StationEngineEffects {

    private StationEngineEffects() {}

    public static void install() {
        BlockEntityMachineLPW2Space.CLIENT_EFFECTS =
                be -> {
                    boolean burning = be.isOn && be.soundtime > 18;
                    be.audioLoop(burning, 1F);
                    if (burning) lpw2Contrail(be);
                };
        BlockEntityMachineHTR3.CLIENT_EFFECTS =
                be -> {
                    boolean burning = be.isOn && be.soundtime > 18;
                    be.audioLoop(burning, 1F);
                    if (burning)
                        contrail(
                                be.getLevel(),
                                be.getBlockPos(),
                                be.getBlockState(),
                                7,
                                be.contrailType());
                };
        BlockEntityMachineHTRF4.CLIENT_EFFECTS =
                be -> {
                    boolean burning = be.isOn && be.soundtime > 18;
                    be.audioLoop(burning, 1F);
                    if (burning)
                        contrail(
                                be.getLevel(),
                                be.getBlockPos(),
                                be.getBlockState(),
                                12,
                                be.contrailType());
                };
        BlockEntityMachineHTRNeo.CLIENT_EFFECTS =
                be -> be.audioLoop(be.isOn && be.soundtime > 28, 1F);
        BlockEntityTransporterRocket.CLIENT_EFFECTS =
                be -> launch(be.getLevel(), be.getBlockPos(), be.launchTicks);
        BlockEntityMachineGasDock.CLIENT_EFFECTS =
                be -> launch(be.getLevel(), be.getBlockPos(), be.launchTicks);
        ItemTransporterLinker.OPEN_SCREEN =
                (from, transporters) ->
                        Minecraft.getInstance()
                                .gui
                                .setScreen(new ScreenTransporterLinker(from, transporters));
    }

    private static void lpw2Contrail(BlockEntityMachineLPW2Space be) {
        Level level = be.getLevel();
        Direction rot = be.getBlockState().getValue(BlockMultiblockCore.FACING).getClockWise();
        BlockPos pos = be.getBlockPos();
        double x = pos.getX() + rot.getStepX() * 8.5;
        double y = pos.getY() + 4.5;
        double z = pos.getZ() + rot.getStepZ() * 8.5;
        Player player = Minecraft.getInstance().player;
        if (player == null
                || Mth.lengthSquared(player.getX() - x, player.getEyeY() - y, player.getZ() - z)
                        > 350 * 350) return;
        ClientEffects.spawnRocketFlameInAir(
                level,
                x,
                y,
                z,
                3F,
                rot.getStepX() * 10,
                0,
                rot.getStepZ() * 10,
                20 + level.getRandom().nextInt(20),
                Environment.particlePressure(level));
    }

    private static void contrail(
            Level level, BlockPos pos, BlockState state, int reach, String type) {
        Direction rot = state.getValue(BlockMultiblockCore.FACING).getClockWise();
        double x = pos.getX() + rot.getStepX() * reach;
        double y = pos.getY() + 1;
        double z = pos.getZ() + rot.getStepZ() * reach;
        Player player = Minecraft.getInstance().player;
        if (player == null
                || Mth.lengthSquared(player.getX() - x, player.getEyeY() - y, player.getZ() - z)
                        > 350 * 350) return;

        ClientEffects.spawnRocketFlameInAir(
                level,
                x,
                y,
                z,
                3F,
                rot.getStepX() * 10,
                0,
                rot.getStepZ() * 10,
                40 + level.getRandom().nextInt(40),
                Environment.particlePressure(level),
                RocketFlamesSpace.contrail(type));
    }

    private static void launch(Level level, BlockPos pos, int launchTicks) {
        if (launchTicks <= 0 || launchTicks >= 100) return;

        double x = pos.getX() + 0.5, z = pos.getZ() + 0.5;
        ClientEffects.spawnGasFlame(
                level, x, pos.getY() + 0.5 + launchTicks, z, 0.0, -1.0, 0.0, 6.5F);

        if (launchTicks < 10) {
            RandomSource rand = level.getRandom();
            ClientEffects.spawnSmokeShock(
                    level, x, pos.getY() + 0.5, z, 1 + rand.nextInt(3), 1 + rand.nextGaussian());
        }
    }
}
