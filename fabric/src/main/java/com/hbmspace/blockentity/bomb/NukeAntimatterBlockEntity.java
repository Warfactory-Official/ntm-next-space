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

package com.hbmspace.blockentity.bomb;

import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.inventory.material.MaterialShape;
import com.hbm.items.MaterialShapeRoster;
import com.hbm.items.ModItems;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.sound.ModSounds;
import com.hbm.sound.WorldSounds;
import com.hbmspace.blocks.BombBlocksSpace;
import com.hbmspace.entity.effect.TorexAntimatter;
import com.hbmspace.entity.logic.AntimatterBlastEntity;
import com.hbmspace.inventory.container.NukeAntimatterMenu;
import com.hbmspace.inventory.material.MatsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NukeAntimatterBlockEntity extends MachineBlockEntity
        implements MenuProvider, SyncUnitSchema {

    public static final int SLOT_LEAD_A = 0;
    public static final int SLOT_LEAD_B = 1;
    public static final int SLOT_CORE = 2;
    public static final int SLOT_HAFNIUM_A = 3;
    public static final int SLOT_HAFNIUM_B = 4;
    public static final int SLOT_COUNT = 8;

    public static final int DEFAULT_TIMER = 18_000;
    public static final int DESTRUCTION_RANGE = 150;
    public static final int MIN_SECONDS = 1;
    public static final int MAX_SECONDS = 999;

    public int timer;

    {
        hbm$syncSet$NukeAntimatterBlockEntity$timer(DEFAULT_TIMER);
    }

    public boolean started;
    public boolean core;

    public NukeAntimatterBlockEntity(BlockPos pos, BlockState state) {
        super(BombBlocksSpace.NUKE_ANTIMATTER_BE.get(), pos, state, SLOT_COUNT);
    }

    @Override
    public void tickServer() {
        hbm$syncSet$NukeAntimatterBlockEntity$core(hasAmatCore());
        if (!isLoaded()) hbm$syncSet$NukeAntimatterBlockEntity$started(false);

        if (started) {
            hbm$syncSet$NukeAntimatterBlockEntity$timer((int) (timer - 1));
            if (timer % 20 == 0) {
                WorldSounds.machine(
                        level,
                        null,
                        worldPosition,
                        ModSounds.FSTBMB_PING.get(),
                        SoundSource.BLOCKS,
                        5.0F,
                        1.0F);
            }
        }

        if (timer <= 0) explode();
        networkPackNT(250);
    }

    public boolean isLoaded() {
        return hasAmatCore() && hasRest();
    }

    public boolean hasAmatCore() {
        return getItem(SLOT_CORE).is(ModItems.PELLET_ANTIMATTER.get());
    }

    public boolean hasRest() {
        Item hafnium = MaterialShapeRoster.require(MaterialShape.INGOT, MatsSpace.MAT_HAFNIUM);
        Item lead = ModItems.PARTICLE_LEAD.get();
        return getItem(SLOT_LEAD_A).is(lead)
                && getItem(SLOT_LEAD_B).is(lead)
                && getItem(SLOT_HAFNIUM_A).is(hafnium)
                && getItem(SLOT_HAFNIUM_B).is(hafnium);
    }

    public void explode() {
        if (!(level instanceof ServerLevel server)) return;
        clearContent();
        server.destroyBlock(worldPosition, false);

        double x = worldPosition.getX() + 0.5;
        double z = worldPosition.getZ() + 0.5;
        AntimatterBlastEntity.spawn(server, x, worldPosition.getY() + 0.5, z, DESTRUCTION_RANGE);
        TorexAntimatter.startFacAnti(server, x, worldPosition.getY() + 5, z, 1000);
    }

    public String getMinutes() {
        return pad(timer / 1200);
    }

    public String getSeconds() {
        return pad((timer / 20) % 60);
    }

    private static String pad(int value) {
        String text = Integer.toString(value);
        return text.length() == 1 ? "0" + text : text;
    }

    public void start() {
        if (!isLoaded()) return;
        WorldSounds.machine(
                level,
                null,
                worldPosition,
                ModSounds.FSTBMB_START.get(),
                SoundSource.BLOCKS,
                5.0F,
                1.0F);
        hbm$syncSet$NukeAntimatterBlockEntity$started(true);
        setChanged();
    }

    public void setTimer(int seconds) {
        hbm$syncSet$NukeAntimatterBlockEntity$timer(
                Math.clamp(seconds, MIN_SECONDS, MAX_SECONDS) * 20);
        setChanged();
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | 7L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeInt(timer);
            case 1 -> output.writeBoolean(started);
            case 2 -> output.writeBoolean(core);
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$NukeAntimatterBlockEntity$timer(input.readInt());
            case 1 -> hbm$syncSet$NukeAntimatterBlockEntity$started(input.readBoolean());
            case 2 -> hbm$syncSet$NukeAntimatterBlockEntity$core(input.readBoolean());
            default -> super.readSyncUnit(unit, input);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$NukeAntimatterBlockEntity$started(input.getBooleanOr("started", false));
        hbm$syncSet$NukeAntimatterBlockEntity$timer(input.getIntOr("timer", DEFAULT_TIMER));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("started", started);
        output.putInt("timer", timer);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.nukeAntimatter");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new NukeAntimatterMenu(containerId, playerInventory, this);
    }

    public final void hbm$syncSet$NukeAntimatterBlockEntity$timer(int next) {
        if (this.timer != next) {
            this.timer = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$NukeAntimatterBlockEntity$started(boolean next) {
        if (this.started != next) {
            this.started = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$NukeAntimatterBlockEntity$core(boolean next) {
        if (this.core != next) {
            this.core = next;
            if ((1L << 2) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 2);
        }
    }
}
