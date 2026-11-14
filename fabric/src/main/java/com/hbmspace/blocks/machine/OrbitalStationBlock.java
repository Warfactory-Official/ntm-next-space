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

package com.hbmspace.blocks.machine;

import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.client.ClientPlayerAccess;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.Blink;
import com.hbmspace.blockentity.machine.OrbitalStationBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.blocks.StationBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.handler.atmosphere.SealableBlock;
import com.hbmspace.items.weapon.CustomRocketItem;
import com.hbmspace.items.weapon.MissilePartsSpace;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class OrbitalStationBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, CapabilityBlock, LookOverlayProvider, SealableBlock {
    private final boolean core;

    public OrbitalStationBlock(Properties props, boolean core) {
        super(props);
        this.core = core;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OrbitalStationBlockEntity(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.ORBITAL_STATION)
                .fluidIn()
                .itemsAtCells()
                .fluidFaces(OrbitalStationBlockEntity.class, (be, face) -> be.needsFuel);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return core ? null : super.getStateForPlacement(ctx);
    }

    @Override
    public boolean isSealed(Level level, BlockPos pos) {
        return true;
    }

    protected static boolean onPad(BlockPos core, BlockPos hit) {
        return Math.abs(core.getX() - hit.getX()) < 2 && Math.abs(core.getZ() - hit.getZ()) < 2;
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
        return use(level, core, player, hit);
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        return use(level, core, player, hit);
    }

    private static boolean placesRocket(ItemStack held) {
        return held.is(MissilePartsSpace.ROCKET_CUSTOM.get()) && CustomRocketItem.hasFuel(held)
                || isCapsule(held);
    }

    private static boolean isCapsule(ItemStack held) {
        return held.is(MissilePartsSpace.RP_CAPSULE_20.get())
                || held.is(MissilePartsSpace.RP_POD_20.get());
    }

    private InteractionResult use(Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (!CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (!onPad(core, hit.getBlockPos())) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof OrbitalStationBlockEntity station))
            return InteractionResult.PASS;

        RideableRocketEntity docked = station.getDocked();
        if (station.hasStoredItems()) {
            station.giveStoredItems(player);
        } else if (docked != null) {
            if (docked.getFirstPassenger() == null) {
                if (player.isShiftKeyDown()) {
                    if (player.getMainHandItem().isEmpty()) {
                        station.despawnRocket();
                        station.giveStoredItems(player);
                    }
                } else {
                    station.enterCapsule(player);
                }
            }
        } else {
            ItemStack held = player.getMainHandItem();
            if (placesRocket(held)) {
                station.spawnRocket(
                        isCapsule(held)
                                ? CustomRocketItem.build(new RocketStruct(held))
                                : held.copyWithCount(1));
                held.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static boolean refusesRemoval(Level level, BlockPos pos, BlockState state) {
        if (state.is(StationBlocksSpace.ORBITAL_STATION.get())) return true;
        if (!MultiblockSurface.isSurface(state)) return false;
        BlockPos core = MultiblockSurface.coreOfAny(level, pos, state);
        return core != null
                && level.getBlockState(core).is(StationBlocksSpace.ORBITAL_STATION.get());
    }

    protected final boolean printsNoOrbit(Level level, LookOverlay info) {
        if (CelestialBody.inOrbit(level)) return false;
        info.title(getName());
        info.row(
                Component.literal("! ! ! ")
                        .append(Component.translatable("atmosphere.noOrbit"))
                        .append(" ! ! !"),
                Blink.lit() ? 0xFF0000 : 0xFFFF00);
        return true;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (printsNoOrbit(level, info)) return;

        BlockPos hit = info.hit();
        if (hit != null && !onPad(pos, hit)) return;
        if (!(level.getBlockEntity(pos) instanceof OrbitalStationBlockEntity station)) return;

        List<Component> text = new ArrayList<>();
        for (ItemStack stack : station.inventory) {
            if (!stack.isEmpty()) {
                text.add(
                        Component.literal("<- ")
                                .withStyle(ChatFormatting.RED)
                                .append(
                                        stack.getHoverName()
                                                .copy()
                                                .withStyle(ChatFormatting.WHITE)));
            }
        }

        if (!text.isEmpty()) {
            text.add(Component.translatable("station.retrieveRocket"));
        } else if (station.hasDocked) {
            Player player = ClientPlayerAccess.player();
            if (!station.hasRider) {
                if (player != null && player.isShiftKeyDown()) {
                    if (player.getMainHandItem().isEmpty())
                        text.add(Component.translatable("station.removeRocket"));
                } else {
                    text.add(Component.translatable("station.enterRocket"));
                }
            } else {
                text.add(
                        Component.translatable("station.occupiedRocket")
                                .withStyle(ChatFormatting.YELLOW));
            }

            if (station.needsFuel) {
                for (FluidTankNTM tank : station.tanks) {
                    text.add(
                            Component.literal("-> ")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(
                                            NTMFluidProperties.getDisplayName(tank.getTankType())
                                                    .copy()
                                                    .withStyle(ChatFormatting.WHITE))
                                    .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                                    .append(
                                            Component.translatable(
                                                            "unit.millibuckets",
                                                            tank.getFill()
                                                                    + "/"
                                                                    + tank.getMaxFill())
                                                    .withStyle(ChatFormatting.WHITE)));
                }

                if (!station.hasFuel)
                    text.add(
                            Component.translatable("station.emptyRocket")
                                    .withStyle(ChatFormatting.RED));
            }
        } else {
            Player player = ClientPlayerAccess.player();
            if (player != null
                    && !player.isShiftKeyDown()
                    && placesRocket(player.getMainHandItem())) {
                text.add(Component.translatable("station.placeRocket"));
            }
        }

        if (text.isEmpty()) return;

        info.title(getName());
        for (Component line : text) info.row(line);
    }
}
