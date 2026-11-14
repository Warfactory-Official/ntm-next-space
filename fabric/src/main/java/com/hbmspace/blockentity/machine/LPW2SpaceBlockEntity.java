/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
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

import com.hbm.api.fluidmk2.FluidTankHandler;
import com.hbm.blockentity.AudioLoop;
import com.hbm.blockentity.SoundEmitter;
import com.hbm.blockentity.machine.LPW2BlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.SoundHandle;
import com.hbm.sound.WorldSounds;
import com.hbmspace.api.tile.PropulsionHandler;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.inventory.fluid.trait.FT_Rocket;
import com.hbmspace.sound.ModSoundsSpace;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LPW2SpaceBlockEntity extends LPW2BlockEntity
        implements SoundEmitter,
                FluidTankAccess,
                PropulsionHandler,
                FluidTankHandler,
                AudioLoop,
                SyncUnitSchema {

    public static Consumer<LPW2SpaceBlockEntity> CLIENT_EFFECTS = be -> {};

    public final FluidTankNTM[] tanks = {
        new FluidTankNTM(NTMFluids.KEROSENE_REFORM, 256_000),
        new FluidTankNTM(NTMFluids.OXYGEN, 256_000)
    };

    public boolean isOn;
    public float soundtime;
    private int fuelCost;

    private float speed;
    public double lastTime;
    public double time;

    private boolean attached;

    public LPW2SpaceBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void onGraphLoad(ServerLevel server) {
        if (!CelestialBody.inOrbit(server)) return;
        hbm$syncSet$LPW2SpaceBlockEntity$isOn(false);
        attached = attachPropulsion(attached);
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        if (level instanceof ServerLevel) attached = attachPropulsion(attached);
    }

    @Override
    public void tickServer() {
        if (!CelestialBody.inOrbit(level)) return;

        if (isOn) {
            hbm$syncSet$LPW2SpaceBlockEntity$soundtime((float) (soundtime + 1));

            if (soundtime == 1) {
                WorldSounds.machine(
                        level,
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_LPW_START.get(),
                        SoundSource.BLOCKS,
                        1.5F,
                        1F);
            } else if (soundtime > 20) {
                hbm$syncSet$LPW2SpaceBlockEntity$soundtime(20);
            }
        } else {
            hbm$syncSet$LPW2SpaceBlockEntity$soundtime((float) (soundtime - 1));

            if (soundtime == 19) {
                WorldSounds.machine(
                        level,
                        null,
                        worldPosition,
                        ModSoundsSpace.MISC_LPW_STOP.get(),
                        SoundSource.BLOCKS,
                        2.0F,
                        1F);
            } else if (soundtime <= 0) {
                hbm$syncSet$LPW2SpaceBlockEntity$soundtime(0);
            }
        }

        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        if (!CelestialBody.inOrbit(level)) return;

        if (isOn) {
            speed += 0.05D;
            if (speed > 1) speed = 1;
        } else {
            speed -= 0.05D;
            if (speed < 0) speed = 0;
        }

        CLIENT_EFFECTS.accept(this);

        lastTime = time;
        time += speed;
    }

    @Override
    public SoundHandle createAudioLoop() {
        return AudioSystem.getLoopedSound(
                ModSoundsSpace.MISC_LPW_LOOP.get(),
                SoundSource.BLOCKS,
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                0.25F,
                27.5F,
                1.0F,
                20);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        attached = attachPropulsion(attached);

        com$hbm$blockentity$AudioLoop$setRemoved();
    }

    public double renderTime(float partialTicks) {
        return lastTime + (time - lastTime) * partialTicks;
    }

    @Override
    public boolean isFacingPrograde() {
        return getBlockState().getValue(MultiblockCoreBlock.FACING) == Direction.SOUTH;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public boolean canPerformBurn(int shipMass, double deltaV) {
        FT_Rocket trait = NTMFluidProperties.getTrait(tanks[0].getTankType(), FT_Rocket.class);
        int isp = trait != null ? trait.getISP() : 300;

        hbm$syncSet$LPW2SpaceBlockEntity$fuelCost(SolarSystem.getFuelCost(deltaV, shipMass, isp));

        for (FluidTankNTM tank : tanks) {
            if (tank.getFill() < fuelCost) return false;
        }

        return true;
    }

    @Override
    public void addErrors(List<Component> errors) {
        for (FluidTankNTM tank : tanks) {
            if (tank.getFill() < fuelCost) {
                errors.add(
                        Component.translatable("overlay.hbmspace.propulsion.fuel", fuelCost)
                                .withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public float getThrust() {
        return 2_000_000_000.0F;
    }

    @Override
    public int startBurn() {
        hbm$syncSet$LPW2SpaceBlockEntity$isOn(true);
        for (FluidTankNTM tank : tanks) {
            tank.setFill(tank.getFill() - fuelCost);
        }
        setChanged();
        return 120;
    }

    @Override
    public int endBurn() {
        hbm$syncSet$LPW2SpaceBlockEntity$isOn(false);
        setChanged();
        return 120;
    }

    @Override
    public int tankCount() {
        return tanks.length;
    }

    @Override
    public FluidTankNTM tank(int index) {
        return tanks[index];
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return tanks;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$LPW2SpaceBlockEntity$isOn(input.getBooleanOr("on", false));
        for (int i = 0; i < tanks.length; i++) {
            input.child("t" + i).ifPresent(tanks[i]::deserialize);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("on", isOn);
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child("t" + i));
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> {
                output.writeBoolean(isOn);
                output.writeFloat(soundtime);
                output.writeInt(fuelCost);
            }
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> {
                hbm$syncSet$LPW2SpaceBlockEntity$isOn(input.readBoolean());
                hbm$syncSet$LPW2SpaceBlockEntity$soundtime(input.readFloat());
                hbm$syncSet$LPW2SpaceBlockEntity$fuelCost(input.readInt());
            }
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> super.readSyncUnit(unit, input);
        }
    }

    @Override
    public boolean tickSound() {
        return CelestialBody.inOrbit(level) && isOn && soundtime > 18;
    }

    public final FluidTankNTM hbm$syncArraySet$LPW2SpaceBlockEntity$tanks(
            FluidTankNTM[] array, int index, FluidTankNTM next) {
        if (array[index] != next) {
            if (array == this.tanks && syncBound())
                com.hbm.packet.SyncBindings.unbind(this, array[index]);
            array[index] = next;
            if (array == this.tanks) {
                syncArrayChanged(array, index, 3, 1L << 1);
            }
        }
        return next;
    }

    public final void hbm$syncSet$LPW2SpaceBlockEntity$isOn(boolean next) {
        if (this.isOn != next) {
            this.isOn = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$LPW2SpaceBlockEntity$soundtime(float next) {
        if (Float.floatToRawIntBits(this.soundtime) != Float.floatToRawIntBits(next)) {
            this.soundtime = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$LPW2SpaceBlockEntity$fuelCost(int next) {
        if (this.fuelCost != next) {
            this.fuelCost = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(LPW2SpaceBlockEntity.class, "tanks", 3, 1L << 1);
    }

    private boolean hbm$soundActive;

    public boolean soundStarted(boolean active) {
        boolean started = active && !hbm$soundActive;
        hbm$soundActive = active;
        return started;
    }

    private SoundHandle hbm$audio;

    public void audioLoop(boolean running, float volume) {
        hbm$audio = AudioLoop.loop(this, hbm$audio, running, volume, 0F, false);
    }

    public void audioLoop(boolean running, float volume, float pitch) {
        hbm$audio = AudioLoop.loop(this, hbm$audio, running, volume, pitch, true);
    }

    private void hbm$audioRemove() {
        audioLoop(false, 0F);
    }

    private void com$hbm$blockentity$AudioLoop$setRemoved() {
        hbm$audioRemove();
    }
}
