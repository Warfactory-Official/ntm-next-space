/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
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

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.HorizontalMachineBlock;
import com.hbm.client.ClientPlayerAccess;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.BlockMenus;
import com.hbm.sound.ModSounds;
import com.hbm.util.Blink;
import com.hbmspace.api.tile.PropulsionHandler;
import com.hbmspace.blockentity.machine.OrbitalStationComputerBlockEntity;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.items.VOTVdriveItem.Destination;
import com.hbmspace.items.VOTVdriveItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class OrbitalStationComputerBlock extends HorizontalMachineBlock
        implements LookOverlayProvider {

    public OrbitalStationComputerBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OrbitalStationComputerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack held,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (held.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof OrbitalStationComputerBlockEntity computer)) {
            return InteractionResult.PASS;
        }
        if (computer.isTravelling()) return InteractionResult.PASS;

        if (held.getItem() instanceof VOTVdriveItem) {
            if (!computer.getItem(0).isEmpty()) return InteractionResult.PASS;

            Destination destination = VOTVdriveItem.getDestination(held);
            if (destination.body() == SolarSystem.Body.ORBIT) return InteractionResult.PASS;
            if (!computer.travelTo(destination.body().getBody(), held.copy()))
                return InteractionResult.PASS;

            held.setCount(0);
            level.playSound(
                    null, pos, ModSounds.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        BlockMenus.open(player, computer);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof OrbitalStationComputerBlockEntity computer)) {
            return InteractionResult.PASS;
        }
        if (computer.isTravelling()) return InteractionResult.PASS;

        ItemStack drive = computer.getItem(0);
        if (!drive.isEmpty()) {
            player.getInventory().placeItemBackInInventory(drive.copy(), Prediction.SERVER_ONLY);
            computer.setItem(0, ItemStack.EMPTY);
        } else {
            BlockMenus.open(player, computer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        info.title(getName());

        if (!CelestialBody.inOrbit(level)) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.noOrbit"))
                            .append(" ! ! !"),
                    Blink.lit() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof OrbitalStationComputerBlockEntity computer))
            return;

        OrbitalStation station = OrbitalStation.clientStation;
        double progress = station.getUnscaledProgress(0);

        if (!station.hasEngines) {
            info.row(
                    Component.translatable("overlay.hbmspace.station.no_engines")
                            .withStyle(ChatFormatting.RED));
        } else if (!station.errorsAt.isEmpty()) {
            for (BlockPos errorAt : station.errorsAt) {
                if (!(level.getBlockEntity(errorAt) instanceof PropulsionHandler engine)) continue;

                List<Component> errors = new ArrayList<>();
                engine.addErrors(errors);

                if (!errors.isEmpty()) {
                    info.row(
                            engine.getBlockEntity()
                                    .getBlockState()
                                    .getBlock()
                                    .getName()
                                    .copy()
                                    .withStyle(ChatFormatting.RED));
                    for (Component error : errors) info.row(error);
                }
            }
        } else if (progress > 0) {
            Component target = Component.translatable("body." + station.target.name);
            if (station.state == StationState.LEAVING) {
                info.row(
                        Component.translatable("station.engage")
                                .append(": ")
                                .withStyle(ChatFormatting.AQUA)
                                .append(target.copy().withStyle(ChatFormatting.WHITE)));
            } else if (station.state == StationState.ARRIVING) {
                info.row(
                        Component.translatable("station.disengage").withStyle(ChatFormatting.AQUA));
            } else {
                info.row(
                        Component.translatable("station.travelling")
                                .append(": ")
                                .withStyle(ChatFormatting.AQUA)
                                .append(target.copy().withStyle(ChatFormatting.WHITE)));
            }
            info.row(
                    Component.translatable("station.progress")
                            .append(": ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(
                                    Component.literal(Math.round(progress * 100) + "%")
                                            .withStyle(ChatFormatting.WHITE)));
        } else if (computer.hasDrive) {
            Player player = ClientPlayerAccess.player();
            boolean holding = player != null && !player.getMainHandItem().isEmpty();
            info.row(
                    Component.translatable(
                            holding ? "station.removeDrive" : "station.interactDrive"));
        } else {
            info.row(Component.translatable("station.insertDrive"));
        }
    }
}
