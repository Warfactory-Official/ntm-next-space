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

import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.items.ModDataComponents;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.PositionWatchers;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.blocks.RocketryBlocksSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class DishControlBlockEntity extends MachineBlockEntity implements SyncUnitSchema {

    static final PositionWatchers<DishControlBlockEntity> LINKED = new PositionWatchers<>();

    public BlockPos linkPosition = BlockPos.ZERO;
    public boolean isLinked;
    public boolean starDarHasDisk;

    public DishControlBlockEntity(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.DISH_CONTROL.get(), pos, state, 0);
    }

    @Override
    public void onGraphLoad(ServerLevel server) {
        if (!isLinked) return;
        LINKED.add(server, linkPosition.asLong(), this);
        MinecraftServer host = server.getServer();
        host.schedule(
                host.wrapRunnable(
                        () -> {
                            if (isRemoved()) return;
                            if (server.isLoaded(linkPosition) && linkedStardar() == null) {
                                unlink();
                                markChanged();
                            }
                            refreshDisk();
                        }));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (isLinked && level instanceof ServerLevel server)
            LINKED.remove(server, linkPosition.asLong(), this);
    }

    public @Nullable StardarBlockEntity linkedStardar() {
        if (!isLinked || !level.isLoaded(linkPosition)) return null;
        return level.getBlockEntity(linkPosition) instanceof StardarBlockEntity stardar
                ? stardar
                : null;
    }

    private void refreshDisk() {
        StardarBlockEntity stardar = linkedStardar();
        showDisk(stardar != null && !stardar.getItem(0).isEmpty());
    }

    void showDisk(boolean disk) {
        if (disk == starDarHasDisk) return;
        hbm$syncSet$DishControlBlockEntity$starDarHasDisk(disk);
        networkPackNTTracking();
    }

    public void tryLink(ItemStack sensor) {
        unlink();
        Long packed = sensor.get(ModDataComponents.REACTOR_POS.get());
        isLinked = packed != null && establishLink(BlockPos.of(packed));
        if (isLinked) LINKED.add((ServerLevel) level, linkPosition.asLong(), this);
        refreshDisk();
        setChanged();
    }

    private void unlink() {
        if (!isLinked) return;
        LINKED.remove((ServerLevel) level, linkPosition.asLong(), this);
        isLinked = false;
    }

    private boolean establishLink(BlockPos pos) {
        if (!level.isLoaded(pos)) return false;
        BlockPos core = MultiblockSurface.coreOfAny(level, pos);
        if (core == null
                || !level.getBlockState(core).is(RocketryBlocksSpace.MACHINE_STARDAR.get()))
            return false;
        if (!(level.getBlockEntity(core) instanceof StardarBlockEntity)) return false;
        linkPosition = core.immutable();
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        isLinked = input.getBooleanOr("isLinked", false);
        linkPosition =
                new BlockPos(
                        input.getIntOr("linkPosX", 0),
                        input.getIntOr("linkPosY", 0),
                        input.getIntOr("linkPosZ", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("isLinked", isLinked);
        output.putInt("linkPosX", linkPosition.getX());
        output.putInt("linkPosY", linkPosition.getY());
        output.putInt("linkPosZ", linkPosition.getZ());
    }

    @Override
    public long syncUnitMask() {
        return 0x1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeBoolean(starDarHasDisk);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        hbm$syncSet$DishControlBlockEntity$starDarHasDisk(input.readBoolean());
    }

    public final void hbm$syncSet$DishControlBlockEntity$starDarHasDisk(boolean next) {
        if (this.starDarHasDisk != next) {
            this.starDarHasDisk = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }
}
