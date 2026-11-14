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

package com.hbmspace.blocks.machine;

import com.hbm.capability.MachineCaps;
import com.hbm.client.ClientPlayerAccess;
import com.hbm.interfaces.LookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbmspace.blockentity.machine.OrbitalStationLauncherBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.entity.missile.RideableRocketEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class OrbitalStationLauncherBlock extends OrbitalStationBlock {

    public OrbitalStationLauncherBlock(Properties props) {
        super(props, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OrbitalStationLauncherBlockEntity(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.ORBITAL_STATION_LAUNCHER)
                .fluidIn()
                .itemsAtCells();
    }

    @Override
    protected InteractionResult useItemOnAtCore(
            ItemStack held,
            BlockState coreState,
            Level level,
            BlockPos core,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (!CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (!onPad(core, hit.getBlockPos())) return InteractionResult.PASS;
        if (!(level.getBlockEntity(core) instanceof OrbitalStationLauncherBlockEntity launcher)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        RideableRocketEntity docked = launcher.getDocked();
        if (docked != null) {
            if (docked.getFirstPassenger() == null) {
                if (player.isSecondaryUseActive()) {
                    if (player.getMainHandItem().isEmpty()) launcher.retrieveRocket(player);
                } else {
                    launcher.enterCapsule(player);
                }
            }
        } else if (!player.isSecondaryUseActive()) {
            openCoreMenu(player, core, launcher);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (printsNoOrbit(level, info)) return;
        if (!(level.getBlockEntity(pos) instanceof OrbitalStationLauncherBlockEntity launcher))
            return;

        Component title = getName();
        if (launcher.hasDocked) {
            info.title(title);
            if (launcher.hasRider) {
                info.row(
                        Component.translatable("station.occupiedRocket")
                                .withStyle(ChatFormatting.YELLOW));
            } else {
                Player player = ClientPlayerAccess.player();
                if (player != null && player.isSecondaryUseActive()) {
                    if (player.getMainHandItem().isEmpty())
                        info.row(Component.translatable("station.removeRocket"));
                } else {
                    info.row(Component.translatable("station.enterRocket"));
                }
            }
            return;
        }

        if (!launcher.rocket.validate()) return;

        List<Component> fuels = new ArrayList<>();
        for (FluidTankNTM tank : launcher.tanks) {
            if (tank.getTankType() == null) continue;
            fuels.add(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(
                                    Component.translatable(
                                                    "overlay.hbmspace.launchPadRocket.tank",
                                                    NTMFluidProperties.getDisplayName(
                                                            tank.getTankType()),
                                                    tank.getFill(),
                                                    tank.getMaxFill())
                                            .withStyle(ChatFormatting.WHITE)));
        }
        if (launcher.solidMax > 0) {
            fuels.add(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(
                                    Component.translatable(
                                                    "overlay.hbmspace.launchPadRocket.solid",
                                                    ModItems.ROCKET_FUEL
                                                            .get()
                                                            .getDefaultInstance()
                                                            .getHoverName(),
                                                    launcher.solidLevel,
                                                    launcher.solidMax)
                                            .withStyle(ChatFormatting.WHITE)));
        }
        if (fuels.isEmpty()) return;

        info.title(title);
        info.row(Component.translatable("overlay.hbmspace.launchPadRocket.fuels"));
        for (Component line : fuels) info.row(line);
    }
}
