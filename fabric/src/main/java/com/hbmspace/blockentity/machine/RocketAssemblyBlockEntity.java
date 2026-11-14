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

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.blocks.machine.RocketAssemblyBlock;
import com.hbmspace.handler.RocketStruct.RocketStage;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.container.RocketAssemblyMenu;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.CustomRocketItem;
import com.hbmspace.saveddata.satellites.SatelliteOrbit;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RocketAssemblyBlockEntity extends MachineBlockEntity
        implements MenuProvider, SyncUnitSchema {

    public static final int SLOT_CAPSULE = 0;
    public static final int SLOT_RESULT = 1 + RocketStruct.MAX_STAGES * 3;
    public static final int SLOT_COUNT = SLOT_RESULT + 1 + RocketStruct.MAX_STAGES * 2;
    public static final int STACK_LIMIT = 8;

    public RocketStruct rocket;

    {
        hbm$syncSet$RocketAssemblyBlockEntity$rocket(new RocketStruct());
    }

    public int currentStage;

    private int previousHeight;
    private boolean platformFailed;
    private int spire;
    private int[] platforms;

    {
        hbm$syncSet$RocketAssemblyBlockEntity$platforms(new int[0]);
    }

    private boolean slotsChanged = true;

    public RocketAssemblyBlockEntity(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.ROCKET_ASSEMBLY.get(), pos, state, SLOT_COUNT);
    }

    public static int fuselage(int stage) {
        return 1 + stage * 3;
    }

    public static int fins(int stage) {
        return 2 + stage * 3;
    }

    public static int thruster(int stage) {
        return 3 + stage * 3;
    }

    public static int driveFrom(int stage) {
        return SLOT_RESULT + 1 + stage * 2;
    }

    public static int driveTo(int stage) {
        return SLOT_RESULT + 2 + stage * 2;
    }

    public int spireHeight() {
        return spire;
    }

    public int[] platforms() {
        return platforms;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        slotsChanged = true;
    }

    @Override
    public void tickServer() {
        if (slotsChanged) {
            reassemble();
            slotsChanged = false;
        }
        networkPackNT(250);
    }

    private void reassemble() {
        publishStations();
        RocketStruct next = assemble(this, SLOT_CAPSULE);

        int height = (int) next.getHeight();
        if (height != previousHeight) {
            raise(next);
            previousHeight = height;
        }

        if (platformFailed) {
            next.addIssue(
                    Component.translatable("desc.gui.rocket.ceilingTooLow")
                            .withStyle(ChatFormatting.RED));
        }

        hbm$syncSet$RocketAssemblyBlockEntity$rocket(next);
    }

    private void publishStations() {
        for (int stage = 0; stage < RocketStruct.MAX_STAGES; stage++) {
            VOTVdriveItem.publishStation(getItem(driveFrom(stage)), level);
            VOTVdriveItem.publishStation(getItem(driveTo(stage)), level);
        }
    }

    public static RocketStruct assemble(Container slots, int capsuleSlot) {
        ItemStack capsule = slots.getItem(capsuleSlot);
        RocketStruct rocket = new RocketStruct(capsule);
        if (capsule.getItem() instanceof SatelliteFrequencyProvider)
            rocket.satFreq = SatelliteFrequencyProvider.frequency(capsule);
        rocket.satOrbit = capsule.get(ModDataComponentsSpace.SATELLITE_ORBIT.get());

        int end = capsuleSlot + 1 + RocketStruct.MAX_STAGES * 3;
        for (int i = capsuleSlot + 1; i < end; i += 3) {
            if (emptyGroup(slots, i)) {
                if (i + 3 >= end || emptyGroup(slots, i + 3)) break;
                for (int j = 0; j < 3; j++) {
                    slots.setItem(i + j, slots.getItem(i + 3 + j));
                    slots.setItem(i + 3 + j, ItemStack.EMPTY);
                }
            }
            rocket.addStage(slots.getItem(i), slots.getItem(i + 1), slots.getItem(i + 2));
        }
        return rocket;
    }

    private static boolean emptyGroup(Container slots, int first) {
        return slots.getItem(first).isEmpty()
                && slots.getItem(first + 1).isEmpty()
                && slots.getItem(first + 2).isEmpty();
    }

    private void raise(RocketStruct next) {
        int oldSpire = spire;
        int[] oldPlatforms = platforms;

        int maxHeight = Integer.MAX_VALUE;
        for (int h = 1; worldPosition.getY() + h < level.getMaxY(); h++) {
            BlockState above = level.getBlockState(worldPosition.above(h));
            if (!above.canBeReplaced() && !above.is(getBlockState().getBlock())) {
                maxHeight = h;
                break;
            }
        }

        double checkHeight = next.getHeight();
        if (next.capsule != null) checkHeight -= next.capsule.height;
        if (!next.stages.isEmpty() && next.stages.getFirst().thruster() != null) {
            checkHeight -= next.stages.getFirst().thruster().height;
        }

        List<Integer> raised = new ArrayList<>();
        if (checkHeight < maxHeight) {
            int targetHeight = 0;
            for (int i = 0; i < next.stages.size(); i++) {
                RocketStage stage = next.stages.get(i);
                RocketStage nextStage = i < next.stages.size() - 1 ? next.stages.get(i + 1) : null;

                if (stage.fuselage() != null)
                    targetHeight += stage.fuselage().height * stage.getStack();
                if (nextStage != null && nextStage.thruster() != null)
                    targetHeight += nextStage.thruster().height;

                int platform = Math.round(targetHeight);
                if (platform > 0) raised.add(platform);
            }
            hbm$syncSet$RocketAssemblyBlockEntity$spire(
                    Math.min(targetHeight, level.getMaxY() - worldPosition.getY()));
            platformFailed = false;
        } else {
            platformFailed = true;
        }
        hbm$syncSet$RocketAssemblyBlockEntity$platforms(
                raised.stream().mapToInt(Integer::intValue).toArray());

        MultiblockCoreBlock.withoutTeardown(
                () -> {
                    BlockPos.MutableBlockPos kept = new BlockPos.MutableBlockPos();
                    List<BlockPos> removed = new ArrayList<>();
                    int[] gone =
                            Arrays.stream(oldPlatforms)
                                    .filter(y -> Arrays.stream(platforms).noneMatch(n -> n == y))
                                    .toArray();
                    RocketAssemblyBlock.visitRaised(
                            worldPosition,
                            oldSpire,
                            gone,
                            pos -> {
                                if (pos.getY() - worldPosition.getY() <= spire && isColumn(pos))
                                    return;
                                removed.add(kept.set(pos).immutable());
                            });
                    long core = worldPosition.asLong();
                    for (BlockPos pos : removed) {
                        if (!level.getBlockState(pos).is(getBlockState().getBlock())) continue;
                        if (MultiblockSurface.recordedCorePacked(
                                        (ServerLevel) level, pos.getX(), pos.getY(), pos.getZ())
                                != core) {
                            continue;
                        }
                        level.removeBlock(pos, false);
                    }
                });

        Direction facing = getBlockState().getValue(MultiblockCoreBlock.FACING);
        ((RocketAssemblyBlock) getBlockState().getBlock()).fillSpace(level, worldPosition, facing);
        setChanged();
    }

    private boolean isColumn(BlockPos pos) {
        int x = pos.getX() - worldPosition.getX(), z = pos.getZ() - worldPosition.getZ();
        return x == 0 && z == 0 || Math.abs(x) == 4 && Math.abs(z) == 4;
    }

    public void construct() {
        if (!rocket.validate()) return;
        if (!getItem(SLOT_RESULT).isEmpty()) return;

        setItem(SLOT_RESULT, CustomRocketItem.build(rocket));
        for (int i = 0; i < SLOT_RESULT; i++) setItem(i, ItemStack.EMPTY);
    }

    public boolean canDeconstruct() {
        if (CustomRocketItem.get(getItem(SLOT_RESULT)) == null) return false;
        for (int i = 0; i < SLOT_RESULT; i++) {
            if (!getItem(i).isEmpty()) return false;
        }
        return true;
    }

    public void deconstruct() {
        if (!canDeconstruct()) return;
        ItemStack source = getItem(SLOT_RESULT);
        int satFreq = SatelliteFrequencyProvider.frequency(source);
        RocketStruct built = CustomRocketItem.get(source);

        ItemStack capsule = new ItemStack(built.capsule.item);
        if (capsule.getItem() instanceof SatelliteFrequencyProvider) {
            SatelliteFrequencyProvider.setFrequency(capsule, satFreq);
            SatelliteOrbit orbit = source.get(ModDataComponentsSpace.SATELLITE_ORBIT.get());
            if (orbit != null) capsule.set(ModDataComponentsSpace.SATELLITE_ORBIT.get(), orbit);
        }
        setItem(SLOT_CAPSULE, capsule);

        for (int i = 0; i < built.stages.size(); i++) {
            RocketStage stage = built.stages.get(built.stages.size() - 1 - i);
            int o = i * 3;
            setItem(o + 1, new ItemStack(stage.fuselage().item, stage.fuselageCount()));
            if (stage.fins() != null) setItem(o + 2, new ItemStack(stage.fins().item));
            setItem(o + 3, new ItemStack(stage.thruster().item, stage.thrusterCount()));
        }

        setItem(SLOT_RESULT, ItemStack.EMPTY);
    }

    @Override
    public int getMaxStackSize() {
        return STACK_LIMIT;
    }

    @Override
    public boolean dropsSlot(int slot) {
        return slot <= SLOT_RESULT;
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        publishStations();
        return new RocketAssemblyMenu(containerId, playerInventory, this);
    }

    @Override
    public long syncUnitMask() {
        return 0b11L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 ->
                    RocketStruct.STREAM_CODEC.encode(
                            new RegistryFriendlyByteBuf(output, level.registryAccess()), rocket);
            case 1 -> {
                output.writeInt(spire);
                output.writeInt(platforms.length);
                for (int platform : platforms) output.writeInt(platform);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 ->
                    hbm$syncSet$RocketAssemblyBlockEntity$rocket(
                            RocketStruct.STREAM_CODEC.decode(
                                    new RegistryFriendlyByteBuf(input, level.registryAccess())));
            case 1 -> {
                hbm$syncSet$RocketAssemblyBlockEntity$spire(input.readInt());
                int[] read = new int[input.readInt()];
                for (int i = 0; i < read.length; i++) read[i] = input.readInt();
                hbm$syncSet$RocketAssemblyBlockEntity$platforms(read);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$RocketAssemblyBlockEntity$platforms(
                input.getIntArray("platforms").orElse(new int[0]));
        hbm$syncSet$RocketAssemblyBlockEntity$spire(input.getIntOr("spire", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putIntArray("platforms", platforms);
        output.putInt("spire", spire);
    }

    public final void hbm$syncSet$RocketAssemblyBlockEntity$rocket(RocketStruct next) {
        if (this.rocket != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.rocket);
            this.rocket = next;
            if (bound) bindSyncValue(next, 3, 1L << 0);
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$RocketAssemblyBlockEntity$spire(int next) {
        if (this.spire != next) {
            this.spire = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$RocketAssemblyBlockEntity$platforms(int[] next) {
        if (this.platforms != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.platforms);
            this.platforms = next;
            if (bound) bindSyncValue(next, 3, 1L << 1);
            syncArrayChanged(next, -1, 3, 1L << 1);
        }
    }

    public final int hbm$syncArraySet$RocketAssemblyBlockEntity$platforms(
            int[] array, int index, int next) {
        if (array[index] != next) {
            array[index] = next;
            if (array == this.platforms) {
                syncArrayChanged(array, index, 3, 1L << 1);
            }
        }
        return next;
    }

    static {
        com.hbm.packet.SyncBindings.field(RocketAssemblyBlockEntity.class, "rocket", 3, 1L << 0);
        com.hbm.packet.SyncBindings.field(RocketAssemblyBlockEntity.class, "platforms", 3, 1L << 1);
    }
}
