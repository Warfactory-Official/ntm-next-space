// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.bomb;

import com.hbm.api.control.IControlReceiver;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.IGUIProvider;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.items.MaterialShapeRoster;
import com.hbm.items.ModItems;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.sound.ModSounds;
import com.hbmspace.blocks.BombBlocksSpace;
import com.hbmspace.entity.effect.TorexAntimatter;
import com.hbmspace.entity.logic.EntityAntimatterBlast;
import com.hbmspace.inventory.container.MenuNukeAntimatter;
import com.hbmspace.inventory.material.MatsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockEntityNukeAntimatter extends BlockEntityMachineBase
        implements IControlReceiver, IGUIProvider, SyncUnitSchema {

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

    @SyncField(units = 1L << 0)
    public int timer = DEFAULT_TIMER;

    @SyncField(units = 1L << 1)
    public boolean started;

    @SyncField(units = 1L << 2)
    public boolean core;

    public BlockEntityNukeAntimatter(BlockPos pos, BlockState state) {
        super(BombBlocksSpace.NUKE_ANTIMATTER_BE.get(), pos, state, SLOT_COUNT);
    }

    @Override
    public void tickServer() {
        core = hasAmatCore();
        if (!isLoaded()) started = false;

        if (started) {
            timer--;
            if (timer % 20 == 0) {
                level.playSound(
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
        Item hafnium = MaterialShapeRoster.require(MaterialShapes.INGOT, MatsSpace.MAT_HAFNIUM);
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
        EntityAntimatterBlast.spawn(server, x, worldPosition.getY() + 0.5, z, DESTRUCTION_RANGE);
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

    @Override
    public boolean hasPermission(Player player) {
        return stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if (data.contains("start") && isLoaded()) {
            level.playSound(
                    null,
                    worldPosition,
                    ModSounds.FSTBMB_START.get(),
                    SoundSource.BLOCKS,
                    5.0F,
                    1.0F);
            started = true;
        }
        if (data.contains("timer")) {
            timer = Math.clamp(data.getIntOr("timer", timer / 20), MIN_SECONDS, MAX_SECONDS) * 20;
        }
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
            case 0 -> timer = input.readInt();
            case 1 -> started = input.readBoolean();
            case 2 -> core = input.readBoolean();
            default -> super.readSyncUnit(unit, input);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        started = input.getBooleanOr("started", false);
        timer = input.getIntOr("timer", DEFAULT_TIMER);
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
        return new MenuNukeAntimatter(containerId, playerInventory, this);
    }
}
