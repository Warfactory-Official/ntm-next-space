/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.packet.PacketWire;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.sound.WorldSounds;
import com.hbm.util.ForeignItems;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.dim.trait.CBT_Dyson;
import com.hbmspace.items.SkyItemsSpace;
import com.hbmspace.packet.layout.SpaceSpinlaunch;
import com.hbmspace.sound.SkySoundsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DysonLauncherBlockEntity extends MachineBlockEntity
        implements EnergyHandler, SyncUnitSchema {

    public static final int SLOT_MEMBERS = 0;
    public static final int SLOT_CHIP = 1;
    public static final long MAX_POWER = 20_000_000;
    private static final int MEMBERS_PER_LAUNCH = 4;

    public int swarmId;
    public int swarmCount;
    public long power;
    public boolean sunsetOverdrive;
    public boolean isOperating;
    public boolean isSpinningDown;
    public int satCount;
    private int operatingTime;

    public float rotation;
    public float lastRotation;
    public float speed;
    public int payloadTicks;

    public DysonLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(SkyBlockEntitiesSpace.DYSON_LAUNCHER.get(), pos, state, 2);
    }

    @Override
    public void tickServer() {
        networkPackNT(250);
        ServerLevel level = (ServerLevel) this.level;
        Direction dir = MultiblockCoreBlock.coreFacing(getBlockState());
        tryLoad(level, dir);

        hbm$syncSet$DysonLauncherBlockEntity$swarmId(
                SatelliteFrequencyProvider.frequency(getItem(SLOT_CHIP)));
        hbm$syncSet$DysonLauncherBlockEntity$swarmCount(CBT_Dyson.count(level, swarmId));

        ItemStack members = getItem(SLOT_MEMBERS);
        hbm$syncSet$DysonLauncherBlockEntity$isOperating(
                !isSpinningDown
                        && power >= getPowerPerTick()
                        && members.is(SkyItemsSpace.SWARM_MEMBER.get())
                        && swarmId > 0);

        if (isSpinningDown) {
            operatingTime++;

            if (operatingTime > getSpinDownTime()) {
                hbm$syncSet$DysonLauncherBlockEntity$isSpinningDown(false);
                operatingTime = 0;
            }
        } else if (isOperating) {
            if (operatingTime == 0) {
                WorldSounds.machine(
                        level,
                        null,
                        worldPosition.getX(),
                        worldPosition.getY() + 8,
                        worldPosition.getZ(),
                        SkySoundsSpace.SPINCHARGE.get(),
                        SoundSource.BLOCKS,
                        1.5F,
                        sunsetOverdrive ? 1.0F : 0.25F);
            }

            operatingTime++;
            hbm$syncSet$DysonLauncherBlockEntity$power((long) (power - (getPowerPerTick())));

            if (operatingTime > getSpinUpTime()) launch(level, dir, members);
        } else {
            operatingTime = 0;
        }

        hbm$syncSet$DysonLauncherBlockEntity$satCount(getItem(SLOT_MEMBERS).getCount());
    }

    private void launch(ServerLevel level, Direction dir, ItemStack members) {
        int toLaunch = Math.min(members.getCount(), MEMBERS_PER_LAUNCH);
        CBT_Dyson.launch(level, swarmId, toLaunch);

        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        double pressure = atmosphere != null ? atmosphere.getPressure() : 0;
        double scaledPressure = 1.0 - Math.pow(1.0 - pressure, 3);

        float volume = Math.min((float) scaledPressure * 16.0F, 4.0F);
        Direction rot = dir.getClockWise();
        RandomSource rand = level.getRandom();
        double soundX = worldPosition.getX() + rot.getStepX() * 6;
        double soundZ = worldPosition.getZ() + rot.getStepZ() * 6;
        WorldSounds.machine(
                level,
                null,
                soundX,
                worldPosition.getY() + 8,
                soundZ,
                SkySoundsSpace.SPINSHOT.get(),
                SoundSource.BLOCKS,
                volume,
                0.9F + rand.nextFloat() * 0.3F);
        WorldSounds.machine(
                level,
                null,
                soundX,
                worldPosition.getY() + 8,
                soundZ,
                SkySoundsSpace.SPINSHOT.get(),
                SoundSource.BLOCKS,
                volume,
                1.0F + rand.nextFloat() * 0.3F);

        int count = Math.min(20, (int) (pressure * 80));
        double x = worldPosition.getX() + rot.getStepX() * 9;
        double y = worldPosition.getY() + 12;
        double z = worldPosition.getZ() + rot.getStepZ() * 9;
        PacketWire.sendToAllAround(
                SpaceSpinlaunch.encode(
                        x,
                        y,
                        z,
                        count,
                        3.0F,
                        dir.getStepX() * 10,
                        10,
                        dir.getStepZ() * 10,
                        10 + count / 2 + rand.nextInt(5)),
                new TargetPoint(
                        level,
                        worldPosition.getX(),
                        worldPosition.getY(),
                        worldPosition.getZ(),
                        150));

        members.shrink(toLaunch);
        setChanged();

        operatingTime = 0;
        hbm$syncSet$DysonLauncherBlockEntity$isSpinningDown(true);
    }

    private void tryLoad(ServerLevel level, Direction dir) {
        ItemStack members = getItem(SLOT_MEMBERS);
        if (!members.isEmpty() && members.getCount() >= MEMBERS_PER_LAUNCH) return;

        BlockPos source = worldPosition.relative(dir.getOpposite(), 9);
        ItemStack taken =
                ForeignItems.extract(
                        level,
                        source,
                        dir.getOpposite(),
                        stack -> stack.is(SkyItemsSpace.SWARM_MEMBER.get()) ? 1 : 0);
        if (taken.isEmpty()) return;

        if (members.isEmpty()) setItem(SLOT_MEMBERS, taken);
        else members.grow(1);
        setChanged();
    }

    @Override
    public void tickClient() {
        float acceleration = sunsetOverdrive ? 2.5F : 0.75F;
        float deceleration = sunsetOverdrive ? 15.0F : 3.0F;
        float resetSpeed = sunsetOverdrive ? 30.0F : 8.0F;

        if (isOperating) {
            speed += acceleration;
            if (speed > 90) speed = 90;
        } else if (speed > 0.1F) {
            speed -= deceleration;
            if (speed < resetSpeed) speed = resetSpeed;
        }

        lastRotation = rotation;
        if (!isOperating && speed <= resetSpeed && rotation > 360 - resetSpeed * 1.5) {
            lastRotation -= 360;
            rotation = 0;
            speed = 0;
        } else {
            rotation += speed;
        }

        if (rotation >= 360) {
            rotation -= 360;
            lastRotation -= 360;
        }

        if (isSpinningDown) payloadTicks++;
        else payloadTicks = 0;
    }

    private int getSpinUpTime() {
        return sunsetOverdrive ? 38 : 132;
    }

    private int getSpinDownTime() {
        return sunsetOverdrive ? 12 : 68;
    }

    private long getPowerPerTick() {
        return MAX_POWER / getSpinUpTime();
    }

    @Override
    public int getMaxStackSize() {
        return 4;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_MEMBERS && stack.is(SkyItemsSpace.SWARM_MEMBER.get());
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[] {SLOT_MEMBERS};
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.hbm$syncSet$DysonLauncherBlockEntity$power(power);
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$DysonLauncherBlockEntity$power(input.getLongOr("power", 0));
        hbm$syncSet$DysonLauncherBlockEntity$isSpinningDown(input.getBooleanOr("spinDown", false));
        operatingTime = input.getIntOr("time", 0);
        hbm$syncSet$DysonLauncherBlockEntity$sunsetOverdrive(
                input.getBooleanOr("overdrive", false));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        output.putBoolean("spinDown", isSpinningDown);
        output.putInt("time", operatingTime);
        output.putBoolean("overdrive", sunsetOverdrive);
    }

    @Override
    public long syncUnitMask() {
        return 1L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 0) throw new IllegalArgumentException();
        output.writeInt(swarmId);
        output.writeLong(power);
        output.writeBoolean(isOperating);
        output.writeBoolean(isSpinningDown);
        output.writeInt(swarmCount);
        output.writeBoolean(sunsetOverdrive);
        output.writeInt(satCount);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 0) throw new IllegalArgumentException();
        hbm$syncSet$DysonLauncherBlockEntity$swarmId(input.readInt());
        hbm$syncSet$DysonLauncherBlockEntity$power(input.readLong());
        hbm$syncSet$DysonLauncherBlockEntity$isOperating(input.readBoolean());
        hbm$syncSet$DysonLauncherBlockEntity$isSpinningDown(input.readBoolean());
        hbm$syncSet$DysonLauncherBlockEntity$swarmCount(input.readInt());
        hbm$syncSet$DysonLauncherBlockEntity$sunsetOverdrive(input.readBoolean());
        hbm$syncSet$DysonLauncherBlockEntity$satCount(input.readInt());
    }

    public final void hbm$syncSet$DysonLauncherBlockEntity$swarmId(int next) {
        if (this.swarmId != next) {
            this.swarmId = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonLauncherBlockEntity$swarmCount(int next) {
        if (this.swarmCount != next) {
            this.swarmCount = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonLauncherBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonLauncherBlockEntity$sunsetOverdrive(boolean next) {
        if (this.sunsetOverdrive != next) {
            this.sunsetOverdrive = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonLauncherBlockEntity$isOperating(boolean next) {
        if (this.isOperating != next) {
            this.isOperating = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonLauncherBlockEntity$isSpinningDown(boolean next) {
        if (this.isSpinningDown != next) {
            this.isSpinningDown = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }

    public final void hbm$syncSet$DysonLauncherBlockEntity$satCount(int next) {
        if (this.satCount != next) {
            this.satCount = next;
            if ((1L) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L);
        }
    }
}
