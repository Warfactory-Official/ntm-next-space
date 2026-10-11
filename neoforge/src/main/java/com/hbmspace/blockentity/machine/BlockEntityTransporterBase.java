// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.control.IControlReceiver;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.port.FluidPort;
import com.hbm.capability.port.IPortHost;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.ForeignItems;
import com.hbm.util.InventoryUtil;
import com.hbmspace.items.tool.TransporterInfo;
import com.hbmspace.main.ChunkLoaderManager;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Utf8String;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class BlockEntityTransporterBase extends BlockEntityMachineBase
        implements MenuProvider, IControlReceiver, FluidTankEndpoint, IPortHost, SyncUnitSchema {

    private static final long NAME_UNIT = 1L << 0;
    private static final long LINK_UNIT = 1L << 1;
    private static final long TANKS_UNIT = 1L << 2;
    private static final String LINKED_NAME = "Linked Transporter";

    @SyncField(units = NAME_UNIT)
    private String name = "Transporter";

    @SyncField(units = TANKS_UNIT)
    public final FluidTankNTM[] tanks;

    protected @Nullable BlockEntityTransporterBase linkedTransporter;

    @SyncField(units = LINK_UNIT)
    private @Nullable TransporterInfo linkedTransporterInfo;

    private final int inputSlotMax;
    private final int outputSlotMax;
    private final int inputTankMax;
    private final int outputTankMax;

    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;
    private final FluidPort fuelPort;
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    @Override
    public Component getDisplayName() {
        return getName();
    }

    protected BlockEntityTransporterBase(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int slotCount,
            int tankCount,
            int tankSize,
            int extraSlots,
            int extraTanks,
            int extraTankSize) {
        super(type, pos, state, slotCount + tankCount / 2 + extraSlots + extraTanks);

        tanks = new FluidTankNTM[tankCount + extraTanks];
        for (int i = 0; i < tankCount; i++) tanks[i] = new FluidTankNTM(tankSize);
        for (int i = tankCount; i < tankCount + extraTanks; i++)
            tanks[i] = new FluidTankNTM(extraTankSize);

        inputSlotMax = slotCount / 2;
        outputSlotMax = slotCount;
        inputTankMax = tankCount / 2;
        outputTankMax = tankCount;

        receiving = Arrays.copyOfRange(tanks, 0, inputTankMax);
        sending = Arrays.copyOfRange(tanks, inputTankMax, outputTankMax);
        fuelPort =
                FluidPort.of(
                        tanks,
                        IntStream.range(outputTankMax, tanks.length).toArray(),
                        new int[0],
                        this::setChanged);
    }

    @Override
    public void tickServer() {
        for (int i = 0; i < inputTankMax; i++) {
            tanks[i].setType(outputSlotMax + i, outputSlotMax + i, inventory);
            for (int o = i + 1; o < inputTankMax; o++) splitFill(tanks[i], tanks[o]);
        }
        for (int i = inputTankMax; i < outputTankMax; i++) {
            for (int o = i + 1; o < outputTankMax; o++) splitFill(tanks[i], tanks[o]);
        }
        for (int i = outputTankMax; i < tanks.length; i++) {
            int slot = outputSlotMax + inputTankMax + i - outputTankMax;
            tanks[i].setType(slot, slot, inventory);
        }

        flush.provide((ServerLevel) level, this);
        tryLoad();
        tryUnload();

        fetchLinkedTransporter();

        if (linkedTransporter != null && canSend(linkedTransporter)) {
            boolean isDirty = false;

            int sentItems = 0;
            int sentFluid = 0;

            for (int i = 0; i < inputSlotMax; i++) {
                ItemStack stack = inventory.get(i);
                if (stack.isEmpty()) continue;
                int beforeSize = stack.getCount();
                ItemStack rest =
                        InventoryUtil.tryAddItemToInventory(
                                linkedTransporter.inventory,
                                linkedTransporter.inputSlotMax,
                                linkedTransporter.outputSlotMax - 1,
                                stack);
                inventory.set(i, rest);
                sentItems += beforeSize - rest.getCount();
                isDirty = true;
            }

            for (int i = 0; i < inputTankMax; i++) {
                FluidTankNTM target = linkedTransporter.tanks[i + inputTankMax];
                target.setTankType(tanks[i].getTankType());

                int sourceFillLevel = tanks[i].getFill();
                int targetFillLevel = target.getFill();
                int amountToSend = Math.min(sourceFillLevel, target.getMaxFill() - targetFillLevel);

                if (amountToSend > 0) {
                    target.setFill(targetFillLevel + amountToSend);
                    tanks[i].setFill(sourceFillLevel - amountToSend);
                    sentFluid += amountToSend;
                    isDirty = true;
                }
            }

            hasSent(linkedTransporter, sentItems + (sentFluid / 1000));

            if (isDirty) {
                setChanged();
                linkedTransporter.setChanged();
            }
        }

        networkPackNT(250);
    }

    protected Direction facing() {
        return getBlockState().getValue(BlockMultiblockCore.FACING);
    }

    private void tryLoad() {
        ItemStack pulled =
                ForeignItems.extract(
                        level, getInsertPos(), facing(), stack -> fitsInput(stack) ? 1 : 0);
        if (pulled.isEmpty()) return;

        for (int j = 0; j < inputSlotMax; j++) {
            ItemStack slot = inventory.get(j);
            if (!slot.isEmpty()
                    && slot.getCount() < slot.getMaxStackSize()
                    && ItemStack.isSameItemSameComponents(slot, pulled)) {
                slot.grow(1);
                setChanged();
                return;
            }
        }
        for (int j = 0; j < inputSlotMax; j++) {
            if (inventory.get(j).isEmpty()) {
                inventory.set(j, pulled);
                setChanged();
                return;
            }
        }
    }

    private boolean fitsInput(ItemStack stack) {
        for (int j = 0; j < inputSlotMax; j++) {
            ItemStack slot = inventory.get(j);
            if (slot.isEmpty()) return true;
            if (slot.getCount() < slot.getMaxStackSize()
                    && ItemStack.isSameItemSameComponents(slot, stack)) {
                return true;
            }
        }
        return false;
    }

    private void tryUnload() {
        for (int i = inputSlotMax; i < outputSlotMax; i++) {
            ItemStack out = inventory.get(i);
            if (out.isEmpty()) continue;
            if (ForeignItems.insert(
                            level, getExtractPos(), facing().getOpposite(), out.copyWithCount(1))
                    .isEmpty()) {
                removeItem(i, 1);
                return;
            }
        }
    }

    private static void splitFill(FluidTankNTM in, FluidTankNTM out) {
        if (in.getTankType() != out.getTankType()) return;
        int fill = in.getFill() + out.getFill();

        float iFill = in.getMaxFill();
        float oFill = out.getMaxFill();
        float total = iFill + oFill;
        float iFrac = iFill / total;
        float oFrac = oFill / total;

        in.setFill(Mth.ceil(iFrac * (float) fill));
        out.setFill(Mth.floor(oFrac * (float) fill));

        if (out.getFill() == out.getMaxFill() - 1) out.setFill(out.getMaxFill());
    }

    @Override
    public @Nullable FluidPort fluidAccess(BlockPos cell, Direction side) {
        for (BlockPos fuel : getTankCells()) {
            if (fuel.equals(cell)) return fuelPort;
        }
        return null;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return sending;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return receiving;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    protected abstract BlockPos[] getTankCells();

    protected abstract BlockPos getInsertPos();

    protected abstract BlockPos getExtractPos();

    protected abstract boolean canSend(BlockEntityTransporterBase linkedTransporter);

    protected abstract void hasSent(BlockEntityTransporterBase linkedTransporter, int quantitySent);

    protected abstract void hasConnected(BlockEntityTransporterBase linkedTransporter);

    protected int itemCount() {
        int count = 0;
        for (int i = 0; i < inputSlotMax; i++) count += inventory.get(i).getCount();
        for (int i = 0; i < inputTankMax; i++) count += tanks[i].getFill() / 1000;
        return count;
    }

    public String getTransporterName() {
        return name;
    }

    public @Nullable TransporterInfo getLinkedTransporter() {
        return linkedTransporterInfo;
    }

    private void fetchLinkedTransporter() {
        if (linkedTransporter != null && linkedTransporter.isRemoved()) linkedTransporter = null;
        if (linkedTransporter != null || linkedTransporterInfo == null) return;
        MinecraftServer server = level.getServer();
        ServerLevel transporterLevel =
                server == null ? null : server.getLevel(linkedTransporterInfo.dimension());
        if (transporterLevel == null) return;
        BlockPos pos = linkedTransporterInfo.pos();
        if (transporterLevel.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null)
            return;
        if (transporterLevel.getBlockEntity(pos)
                instanceof BlockEntityTransporterBase transporter) {
            linkedTransporter = transporter;
        }
    }

    public void unlinkTransporter() {
        if (linkedTransporter != null) {
            linkedTransporter.linkedTransporter = null;
            linkedTransporter.linkedTransporterInfo = null;
        }

        linkedTransporter = null;
        linkedTransporterInfo = null;
    }

    @Override
    public void receiveControl(CompoundTag nbt) {
        nbt.getString("name").ifPresent(name -> this.name = name);
        if (nbt.contains("unlink")) unlinkTransporter();
        if (nbt.contains("linkedTo")) {
            if (linkedTransporter != null) {
                linkedTransporter.linkedTransporter = null;
                linkedTransporter.linkedTransporterInfo = null;
            }

            linkedTransporter = null;

            int[] coords = nbt.getIntArray("linkedTo").orElseThrow();
            ResourceKey<Level> dimension =
                    ResourceKey.create(
                            Registries.DIMENSION,
                            Identifier.parse(nbt.getStringOr("dimension", "")));
            linkedTransporterInfo =
                    new TransporterInfo(
                            LINKED_NAME,
                            dimension,
                            new BlockPos(coords[0], coords[1], coords[2]),
                            null);

            fetchLinkedTransporter();

            if (linkedTransporter != null) {
                linkedTransporter.linkedTransporterInfo =
                        TransporterInfo.from(level.dimension(), this);
                linkedTransporter.fetchLinkedTransporter();

                hasConnected(linkedTransporter);
            }
        }

        setChanged();
    }

    @Override
    public boolean hasPermission(Player player) {
        return stillValid(player);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        ChunkLoaderManager.unforceChunk((ServerLevel) level, pos);
        unlinkTransporter();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, outputSlotMax).toArray();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < inputSlotMax;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot >= inputSlotMax && slot < outputSlotMax;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        name = input.getStringOr("name", "");
        linkedTransporter = null;
        linkedTransporterInfo =
                input.getIntArray("linkedTo")
                        .map(
                                coords ->
                                        new TransporterInfo(
                                                LINKED_NAME,
                                                ResourceKey.create(
                                                        Registries.DIMENSION,
                                                        Identifier.parse(
                                                                input.getStringOr(
                                                                        "dimension", ""))),
                                                new BlockPos(coords[0], coords[1], coords[2]),
                                                null))
                        .orElse(null);
        for (int i = 0; i < tanks.length; i++)
            input.child("t" + i).ifPresent(tanks[i]::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("name", name);
        if (linkedTransporterInfo != null) {
            output.putString(
                    "dimension", linkedTransporterInfo.dimension().identifier().toString());
            BlockPos pos = linkedTransporterInfo.pos();
            output.putIntArray("linkedTo", new int[] {pos.getX(), pos.getY(), pos.getZ()});
        }
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child("t" + i));
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | NAME_UNIT | LINK_UNIT | TANKS_UNIT;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> Utf8String.write(output, name, Short.MAX_VALUE);
            case 1 -> {
                output.writeBoolean(linkedTransporterInfo != null);
                if (linkedTransporterInfo != null) {
                    Utf8String.write(
                            output,
                            linkedTransporterInfo.dimension().identifier().toString(),
                            Short.MAX_VALUE);
                    output.writeLong(linkedTransporterInfo.pos().asLong());
                }
            }
            case 2 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> name = Utf8String.read(input, Short.MAX_VALUE);
            case 1 -> {
                linkedTransporter = null;
                linkedTransporterInfo =
                        input.readBoolean()
                                ? new TransporterInfo(
                                        LINKED_NAME,
                                        ResourceKey.create(
                                                Registries.DIMENSION,
                                                Identifier.parse(
                                                        Utf8String.read(input, Short.MAX_VALUE))),
                                        BlockPos.of(input.readLong()),
                                        null)
                                : null;
            }
            case 2 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> super.readSyncUnit(unit, input);
        }
    }
}
