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

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.inventory.container.DriveProcessorMenu;
import com.hbmspace.items.ComponentItemsSpace;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.MissilePartsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class DriveProcessorBlockEntity extends MachineBlockEntity
        implements EnergyHandler, MenuProvider, SyncUnitSchema {

    public static final int SLOT_DRIVE = 0;
    public static final int SLOT_CLONE = 1;
    public static final int SLOT_CIRCUIT = 2;
    public static final int SLOT_BATTERY = 3;
    public static final int SLOT_COUNT = 4;

    public static final long MAX_POWER = 2_000;
    public static final int MAX_PROGRESS = 100;
    private static final long CONSUMPTION = 200;

    public long power;
    public boolean isProcessing;
    public int progress;
    public boolean hasDrive;
    public Status status;

    {
        hbm$syncSet$DriveProcessorBlockEntity$status(Status.NONE);
    }

    private int lastTier;

    public DriveProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.DRIVE_PROCESSOR_BE.get(), pos, state, SLOT_COUNT);
    }

    @Override
    public void tickServer() {
        long charge = ItemEnergyTransfer.extract(this, SLOT_BATTERY, MAX_POWER - power, false);
        if (charge > 0) hbm$syncSet$DriveProcessorBlockEntity$power((long) (power + (charge)));

        ItemStack drive = getItem(SLOT_DRIVE);
        boolean loaded = drive.getItem() instanceof VOTVdriveItem;
        int tier = processingTier();

        boolean processing = isProcessing;
        Status next = status;
        if (power < MAX_POWER * 0.75) {
            processing = false;
            next = Status.NO_POWER;
        } else if (!loaded) {
            processing = false;
            next = Status.NONE;
        } else if (tier < VOTVdriveItem.getProcessingTier(drive, CelestialBody.getBody(level))) {
            processing = false;
            next = Status.LOW_TIER;
        }

        if (lastTier != tier) next = Status.NONE;

        int nextProgress = 0;
        if (processing) {
            hbm$syncSet$DriveProcessorBlockEntity$power((long) (power - (CONSUMPTION)));
            next = Status.PROCESSING;
            nextProgress = progress + 1;

            if (nextProgress >= MAX_PROGRESS) {
                nextProgress = 0;
                processing = false;
                VOTVdriveItem.setProcessed(drive, true);
                setChanged();
                next = Status.DONE;
            }
        }

        if (isProcessing != processing)
            hbm$syncSet$DriveProcessorBlockEntity$isProcessing(processing);
        if (progress != nextProgress) hbm$syncSet$DriveProcessorBlockEntity$progress(nextProgress);
        if (status != next) hbm$syncSet$DriveProcessorBlockEntity$status(next);
        if (hasDrive != loaded) hbm$syncSet$DriveProcessorBlockEntity$hasDrive(loaded);
        lastTier = tier;

        networkPackNT(15);
    }

    private int processingTier() {
        ItemStack circuit = getItem(SLOT_CIRCUIT);
        if (circuit.is(ComponentItemsSpace.CIRCUIT_PROCESST1.get())) return 1;
        if (circuit.is(ComponentItemsSpace.CIRCUIT_PROCESST2.get())) return 2;
        if (circuit.is(ComponentItemsSpace.CIRCUIT_PROCESST3.get())) return 3;
        return 0;
    }

    private void processDrive(boolean process) {
        if (!process) {
            hbm$syncSet$DriveProcessorBlockEntity$isProcessing(false);
            return;
        }

        if (power < MAX_POWER * 0.75) return;
        ItemStack drive = getItem(SLOT_DRIVE);
        if (!(drive.getItem() instanceof VOTVdriveItem) || VOTVdriveItem.getProcessed(drive))
            return;

        if (processingTier()
                >= VOTVdriveItem.getProcessingTier(drive, CelestialBody.getBody(level))) {
            hbm$syncSet$DriveProcessorBlockEntity$isProcessing(true);
        }
    }

    private void cloneDriveInternal() {
        if (power < MAX_POWER * 0.75) return;
        ItemStack drive = getItem(SLOT_DRIVE);
        if (!(drive.getItem() instanceof VOTVdriveItem)) return;
        if (!getItem(SLOT_CLONE).is(MissilePartsSpace.HARD_DRIVE.get())) {
            hbm$syncSet$DriveProcessorBlockEntity$status(Status.NO_TARGET);
            return;
        }

        VOTVdriveItem.markCopied(drive);
        setItem(SLOT_CLONE, drive.copy());
        hbm$syncSet$DriveProcessorBlockEntity$status(Status.CLONED);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public void setProcessing(boolean process) {
        processDrive(process);
        setChanged();
    }

    public void cloneDrive() {
        cloneDriveInternal();
        setChanged();
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.hbm$syncSet$DriveProcessorBlockEntity$power(power);
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$DriveProcessorBlockEntity$power(input.getLongOr("power", 0));
        hbm$syncSet$DriveProcessorBlockEntity$isProcessing(
                input.getBooleanOr("isProcessing", false));
        hbm$syncSet$DriveProcessorBlockEntity$progress(input.getIntOr("progress", 0));
        hbm$syncSet$DriveProcessorBlockEntity$status(
                Status.valueOf(input.getStringOr("status", Status.NONE.name())));
        lastTier = input.getIntOr("lastTier", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        output.putBoolean("isProcessing", isProcessing);
        output.putInt("progress", progress);
        output.putString("status", status.name());
        output.putInt("lastTier", lastTier);
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new DriveProcessorMenu(containerId, playerInventory, this);
    }

    @Override
    public long syncUnitMask() {
        return 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> {
                output.writeBoolean(isProcessing);
                output.writeInt(progress);
                output.writeBoolean(hasDrive);
                output.writeByte(status.ordinal());
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$DriveProcessorBlockEntity$power(input.readLong());
            case 1 -> {
                hbm$syncSet$DriveProcessorBlockEntity$isProcessing(input.readBoolean());
                hbm$syncSet$DriveProcessorBlockEntity$progress(input.readInt());
                hbm$syncSet$DriveProcessorBlockEntity$hasDrive(input.readBoolean());
                hbm$syncSet$DriveProcessorBlockEntity$status(Status.values()[input.readByte()]);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public enum Status {
        NONE(null),
        NO_POWER("noPower", ChatFormatting.RED),
        LOW_TIER("lowTier", ChatFormatting.RED),
        PROCESSING("processing", ChatFormatting.GREEN, ChatFormatting.ITALIC),
        DONE("done", ChatFormatting.GREEN),
        NO_TARGET("noTarget", ChatFormatting.RED),
        CLONED("cloned", ChatFormatting.GREEN);

        private final @Nullable String key;
        private final ChatFormatting[] style;

        Status(@Nullable String key, ChatFormatting... style) {
            this.key = key;
            this.style = style;
        }

        public Component text() {
            return key == null
                    ? Component.empty()
                    : Component.translatable("desc.gui.driveProcessor." + key).withStyle(style);
        }
    }

    public final void hbm$syncSet$DriveProcessorBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$DriveProcessorBlockEntity$isProcessing(boolean next) {
        if (this.isProcessing != next) {
            this.isProcessing = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$DriveProcessorBlockEntity$progress(int next) {
        if (this.progress != next) {
            this.progress = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$DriveProcessorBlockEntity$hasDrive(boolean next) {
        if (this.hasDrive != next) {
            this.hasDrive = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$DriveProcessorBlockEntity$status(Status next) {
        if (this.status != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.status);
            this.status = next;
            if (bound) bindSyncValue(next, 3, 1L << 1);
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(DriveProcessorBlockEntity.class, "status", 3, 1L << 1);
    }
}
