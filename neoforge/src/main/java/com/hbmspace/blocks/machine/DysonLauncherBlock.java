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

import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.items.ModItems;
import com.hbm.items.block.PlushieBlockItem;
import com.hbm.sound.ModSounds;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.DysonLauncherBlockEntity;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.blocks.generic.PlushieTypeSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class DysonLauncherBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, LookOverlayProvider, CapabilityBlock {
    private static final Box[] BOXES = {
        new Box(6, 14, 0, 14, 1, 0, 6, -5),
        new Box(6, 14, 0, 14, 1, 0, -5, 6),
        new Box(2, 7, 2, 0, 0, 3, 2, 2),
        new Box(1, 6, 2, 0, 0, 3, 3, 3),
        new Box(0, 5, 2, 0, 0, 3, 3, 3),
        new Box(-1, 4, 2, 0, 0, 3, 3, 3),
        new Box(-2, 3, 2, 0, 0, 3, 2, 2),
        new Box(6, 14, 2, 0, 0, 0, 3, 3),
        new Box(6, 14, 0, 0, 0, 2, 3, 3),
        new Box(5, 13, 2, 0, 0, 2, 10, 5),
        new Box(4, 12, 2, 0, 0, 2, 10, 7),
        new Box(3, 11, 2, 0, 0, 2, 10, 8),
        new Box(2, 10, 2, 0, 0, 2, 10, 9),
        new Box(1, 9, 2, 0, 0, 2, 10, 10),
        new Box(0, 8, 2, 0, 0, 2, 10, 10),
        new Box(-1, 7, 2, 0, 0, 2, 10, 10),
        new Box(-2, 6, 2, 0, 0, 2, 10, 10),
        new Box(-3, 5, 2, 0, 0, 2, 10, 10),
        new Box(-4, 4, 2, 0, 0, 2, 9, 9),
        new Box(-3, 3, 2, 0, 2, 0, 8, 8),
        new Box(-4, 2, 2, 0, 2, 0, 7, 7),
        new Box(-7, 2, 1, 0, 0, 0, 6, 6),
        new Box(0, 2, 0, 0, 8, -8, 3, 3),
        new Box(0, 1, 1, 1, 8, -8, 1, 1),
        new Box(-7, 0, 1, 0, 0, 0, 4, 4),
        new Box(-6, 0, 1, 0, 0, 0, 5, 5),
        new Box(-5, 0, 1, 0, 0, 0, 6, 6),
        new Box(0, 0, 3, 0, 7, 0, 2, 2),
    };

    private record Box(int forward, int up, int... dimensions) {

        BlockPos origin(BlockPos core, Direction facing) {
            return core.offset(facing.getStepX() * forward, up, facing.getStepZ() * forward);
        }
    }

    public DysonLauncherBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DysonLauncherBlockEntity(pos, state);
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
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof DysonLauncherBlockEntity launcher))
            return InteractionResult.PASS;

        if (held.is(ModItems.SAT_CHIP.get())) {
            if (!launcher.getItem(DysonLauncherBlockEntity.SLOT_CHIP).isEmpty())
                return InteractionResult.PASS;
            launcher.setItem(DysonLauncherBlockEntity.SLOT_CHIP, held.copy());
            held.consume(held.getCount(), player);
            plug(level, hit.getBlockPos());
        } else if (!launcher.sunsetOverdrive
                && held.getItem() instanceof PlushieBlockItem plushie
                && plushie.type == PlushieTypeSpace.FATO) {
            launcher.hbm$syncSet$DysonLauncherBlockEntity$sunsetOverdrive(true);
            launcher.setChanged();
            held.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof DysonLauncherBlockEntity launcher))
            return InteractionResult.PASS;

        ItemStack chip = launcher.getItem(DysonLauncherBlockEntity.SLOT_CHIP);
        if (!chip.isEmpty()) {
            launcher.setItem(DysonLauncherBlockEntity.SLOT_CHIP, ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(chip, Prediction.SERVER_ONLY);
            plug(level, hit.getBlockPos());
        }
        return InteractionResult.SUCCESS;
    }

    static void plug(Level level, BlockPos pos) {
        level.playSound(
                null,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                ModSounds.UPGRADE_PLUG.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof DysonLauncherBlockEntity launcher)) return;

        info.title(getName());
        if (launcher.swarmId > 0) {
            info.row(Component.translatable("overlay.hbmspace.dyson.id", launcher.swarmId));
            info.row(Component.translatable("overlay.hbmspace.dyson.swarm", launcher.swarmCount));
            info.row(
                    Component.translatable(
                                    "overlay.hbmspace.dyson_launcher.power",
                                    NumberFormatter.formatCompact(launcher.power))
                            .withStyle(
                                    launcher.power < DysonLauncherBlockEntity.MAX_POWER
                                            ? ChatFormatting.RED
                                            : ChatFormatting.GREEN));
        } else {
            info.row(Component.translatable("overlay.hbmspace.dyson.no_chip"));
        }
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(SkyBlockEntitiesSpace.DYSON_LAUNCHER)
                .powerIn()
                .items()
                .itemsAtCells()
                .fe();
    }
}
