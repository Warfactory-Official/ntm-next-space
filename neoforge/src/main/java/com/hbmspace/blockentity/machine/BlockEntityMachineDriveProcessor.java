// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.control.IControlReceiver;
import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.inventory.container.MenuMachineDriveProcessor;
import com.hbmspace.items.ComponentItemsSpace;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.weapon.MissilePartsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

public class BlockEntityMachineDriveProcessor extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, IControlReceiver, MenuProvider, SyncUnitSchema {

    public static final int SLOT_DRIVE = 0;
    public static final int SLOT_CLONE = 1;
    public static final int SLOT_CIRCUIT = 2;
    public static final int SLOT_BATTERY = 3;
    public static final int SLOT_COUNT = 4;

    public static final long MAX_POWER = 2_000;
    public static final int MAX_PROGRESS = 100;
    private static final long CONSUMPTION = 200;

    @SyncField(units = 1L << 0)
    public long power;

    @SyncField(units = 1L << 1)
    public boolean isProcessing;

    @SyncField(units = 1L << 1)
    public int progress;

    @SyncField(units = 1L << 1)
    public boolean hasDrive;

    @SyncField(units = 1L << 1)
    public Status status = Status.NONE;

    private int lastTier;

    public BlockEntityMachineDriveProcessor(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.DRIVE_PROCESSOR_BE.get(), pos, state, SLOT_COUNT);
    }

    @Override
    public void tickServer() {
        long charge = ItemEnergyTransfer.extract(this, SLOT_BATTERY, MAX_POWER - power, false);
        if (charge > 0) power += charge;

        ItemStack drive = getItem(SLOT_DRIVE);
        boolean loaded = drive.getItem() instanceof ItemVOTVdrive;
        int tier = processingTier();

        boolean processing = isProcessing;
        Status next = status;
        if (power < MAX_POWER * 0.75) {
            processing = false;
            next = Status.NO_POWER;
        } else if (!loaded) {
            processing = false;
            next = Status.NONE;
        } else if (tier < ItemVOTVdrive.getProcessingTier(drive, CelestialBody.getBody(level))) {
            processing = false;
            next = Status.LOW_TIER;
        }

        if (lastTier != tier) next = Status.NONE;

        int nextProgress = 0;
        if (processing) {
            power -= CONSUMPTION;
            next = Status.PROCESSING;
            nextProgress = progress + 1;

            if (nextProgress >= MAX_PROGRESS) {
                nextProgress = 0;
                processing = false;
                ItemVOTVdrive.setProcessed(drive, true);
                setChanged();
                next = Status.DONE;
            }
        }

        if (isProcessing != processing) isProcessing = processing;
        if (progress != nextProgress) progress = nextProgress;
        if (status != next) status = next;
        if (hasDrive != loaded) hasDrive = loaded;
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
            isProcessing = false;
            return;
        }

        if (power < MAX_POWER * 0.75) return;
        ItemStack drive = getItem(SLOT_DRIVE);
        if (!(drive.getItem() instanceof ItemVOTVdrive) || ItemVOTVdrive.getProcessed(drive))
            return;

        if (processingTier()
                >= ItemVOTVdrive.getProcessingTier(drive, CelestialBody.getBody(level))) {
            isProcessing = true;
        }
    }

    private void cloneDrive() {
        if (power < MAX_POWER * 0.75) return;
        ItemStack drive = getItem(SLOT_DRIVE);
        if (!(drive.getItem() instanceof ItemVOTVdrive)) return;
        if (!getItem(SLOT_CLONE).is(MissilePartsSpace.HARD_DRIVE.get())) {
            status = Status.NO_TARGET;
            return;
        }

        ItemVOTVdrive.markCopied(drive);
        setItem(SLOT_CLONE, drive.copy());
        status = Status.CLONED;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean hasPermission(Player player) {
        return stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if (data.contains("process")) processDrive(data.getBooleanOr("process", false));
        if (data.contains("clone")) cloneDrive();
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        power = input.getLongOr("power", 0);
        isProcessing = input.getBooleanOr("isProcessing", false);
        progress = input.getIntOr("progress", 0);
        status = Status.valueOf(input.getStringOr("status", Status.NONE.name()));
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
        return new MenuMachineDriveProcessor(containerId, playerInventory, this);
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
            case 0 -> power = input.readLong();
            case 1 -> {
                isProcessing = input.readBoolean();
                progress = input.readInt();
                hasDrive = input.readBoolean();
                status = Status.values()[input.readByte()];
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
}
