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

import com.hbm.api.fluidmk2.FluidTankHandler;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.CoolingTowerParticleOptions;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereBlob;
import com.hbmspace.handler.atmosphere.AtmosphereProvider;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class AirPumpBlockEntity extends MachineBlockEntity
        implements FluidTankAccess, FluidTankHandler, AtmosphereProvider, SyncUnitSchema {

    public static final int MAX_BLOB_RADIUS = 256;
    public static final int FILL_RATE = 20;

    public final FluidTankNTM tank = new FluidTankNTM(NTMFluids.OXYGEN, 16_000);
    private final FluidTankNTM[] tanks = {tank};

    public int onTicks;
    private int airFill;
    private int wasteFill;
    private int bonusFill;
    public @Nullable CBT_Atmosphere currentAtmosphere;

    private boolean registered;
    private int recovering;
    private @Nullable AtmosphereBlob currentBlob;
    private @Nullable AirScrubberBlockEntity scrubber;

    public AirPumpBlockEntity(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.AIR_PUMP.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        int tankBefore = tank.getFill(),
                airBefore = airFill,
                wasteBefore = wasteFill,
                bonusBefore = bonusFill;
        if (onTicks > 0) hbm$syncSet$AirPumpBlockEntity$onTicks((int) (onTicks - 1));

        if (tank.getFill() + bonusFill >= FILL_RATE) {
            hbm$syncSet$AirPumpBlockEntity$onTicks(20);

            if (!registered) {
                ChunkAtmosphereManager.proxy.registerAtmosphere(this);
                registered = true;
                if (airFill > 1) recovering = 100;
            } else if (recovering > 0) {
                recovering--;
                if (currentBlob != null) recovering = 0;
            } else {
                if (currentBlob != null) {
                    int size = currentBlob.getBlobSize();
                    if (size != 0) {
                        fill(size);
                    } else {
                        currentBlob = null;
                    }
                }

                if (currentBlob == null) {
                    consumeFromTank(FILL_RATE);
                    hbm$syncSet$AirPumpBlockEntity$airFill(0);
                    hbm$syncSet$AirPumpBlockEntity$wasteFill(0);
                }
            }
        } else if (registered) {
            unregister();
            hbm$syncSet$AirPumpBlockEntity$airFill(0);
            hbm$syncSet$AirPumpBlockEntity$wasteFill(0);
        }

        if (TickPhase.every(this, 5)) {
            hbm$syncSet$AirPumpBlockEntity$currentAtmosphere(
                    ChunkAtmosphereManager.proxy.getAtmosphere((ServerLevel) level, worldPosition));
        }

        if (tank.getFill() != tankBefore
                || airFill != airBefore
                || wasteFill != wasteBefore
                || bonusFill != bonusBefore) {
            setChanged();
        }
        networkPackNT(100);
    }

    private void fill(int size) {
        if (airFill > size) hbm$syncSet$AirPumpBlockEntity$airFill(size);
        if (wasteFill > size) hbm$syncSet$AirPumpBlockEntity$wasteFill(size);

        int toFill = Math.min(size - airFill, FILL_RATE);
        hbm$syncSet$AirPumpBlockEntity$airFill((int) (airFill + (toFill)));

        if (toFill > 0) {
            consumeFromTank(toFill);
        } else if (level.getRandom().nextBoolean()) {
            consumeFromTank(1);
            hbm$syncSet$AirPumpBlockEntity$wasteFill((int) (wasteFill + 1));
        }

        if (scrubber != null && !scrubber.isRemoved()) {
            int scrubbed = scrubber.scrub(Mth.clamp(wasteFill, 0, FILL_RATE));
            hbm$syncSet$AirPumpBlockEntity$wasteFill((int) (wasteFill - (scrubbed)));
        }
    }

    @Override
    public void tickClient() {
        if (onTicks <= 0 || !TickPhase.every(this, 2)) return;
        RandomSource rand = level.getRandom();
        CoolingTowerParticleOptions puff =
                new CoolingTowerParticleOptions.Builder()
                        .setLift(0.1F)
                        .setBaseScale(0.3F)
                        .setMaxScale(1F)
                        .setLife(20 + rand.nextInt(20))
                        .setColor(color(tank.getFluid()))
                        .build();
        level.addParticle(
                puff,
                worldPosition.getX() + 0.5 + rand.nextDouble() - 0.5,
                worldPosition.getY() + 1,
                worldPosition.getZ() + 0.5 + rand.nextDouble() - 0.5,
                0,
                0,
                0);
    }

    static int color(@Nullable Fluid fluid) {
        var property = NTMFluidProperties.get(fluid);
        return property != null ? property.color() : CoolingTowerParticleOptions.NO_TINT;
    }

    private void consumeFromTank(int amount) {
        int bonusConsumption = Mth.clamp(amount, 0, bonusFill);
        hbm$syncSet$AirPumpBlockEntity$bonusFill((int) (bonusFill - (bonusConsumption)));
        amount -= bonusConsumption;
        tank.setFill(tank.getFill() - amount);
    }

    private void unregister() {
        ChunkAtmosphereManager.proxy.unregisterAtmosphere(this);
        registered = false;
        currentBlob = null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (registered) unregister();
    }

    public boolean hasSeal() {
        return airFill > 1;
    }

    public boolean isRecycling() {
        return bonusFill > 0;
    }

    public boolean registerScrubber(AirScrubberBlockEntity scrubber) {
        if (isRemoved()) return false;
        if (tank.getFluid() != NTMFluids.OXYGEN) return false;
        if (this.scrubber == scrubber) return true;
        if (this.scrubber != null && !this.scrubber.isRemoved() && this.scrubber.canOperate())
            return false;
        this.scrubber = scrubber;
        return true;
    }

    @Override
    public int getMaxBlobRadius() {
        return MAX_BLOB_RADIUS;
    }

    @Override
    public BlockPos getRootPosition() {
        return worldPosition;
    }

    @Override
    public Fluid getFluidType() {
        return tank.getFluid();
    }

    @Override
    public double getFluidPressure() {
        if (currentBlob == null || currentBlob.getBlobSize() == 0) return 0;
        return ((double) airFill / (double) currentBlob.getBlobSize()) * 0.2;
    }

    @Override
    public void onBlobCreated(AtmosphereBlob blob) {
        currentBlob = blob;
    }

    @Override
    public void consume(int amount) {
        hbm$syncSet$AirPumpBlockEntity$airFill((int) (airFill - (amount)));
        hbm$syncSet$AirPumpBlockEntity$wasteFill((int) (wasteFill + (amount)));
        if (airFill < 1) hbm$syncSet$AirPumpBlockEntity$airFill(1);
    }

    @Override
    public void produce(int amount) {
        int produced = Mth.clamp(amount, 0, wasteFill);
        hbm$syncSet$AirPumpBlockEntity$bonusFill((int) (bonusFill + (produced)));
        hbm$syncSet$AirPumpBlockEntity$wasteFill((int) (wasteFill - (produced)));
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return tanks;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("at").ifPresent(tank::deserialize);
        hbm$syncSet$AirPumpBlockEntity$airFill(input.getIntOr("fill", 0));
        hbm$syncSet$AirPumpBlockEntity$wasteFill(input.getIntOr("waste", 0));
        hbm$syncSet$AirPumpBlockEntity$bonusFill(input.getIntOr("bonus", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("at"));
        output.putInt("fill", airFill);
        output.putInt("waste", wasteFill);
        output.putInt("bonus", bonusFill);
    }

    @Override
    public long syncUnitMask() {
        return 0x7L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> {
                output.writeInt(onTicks);
                output.writeInt(airFill);
                output.writeInt(wasteFill);
                output.writeInt(bonusFill);
            }
            case 1 -> tank.packetSerialize(output);
            case 2 -> {
                output.writeBoolean(currentAtmosphere != null);
                if (currentAtmosphere != null) currentAtmosphere.writeToBytes(output);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> {
                hbm$syncSet$AirPumpBlockEntity$onTicks(input.readInt());
                hbm$syncSet$AirPumpBlockEntity$airFill(input.readInt());
                hbm$syncSet$AirPumpBlockEntity$wasteFill(input.readInt());
                hbm$syncSet$AirPumpBlockEntity$bonusFill(input.readInt());
            }
            case 1 -> tank.packetDeserialize(input);
            case 2 -> {
                if (input.readBoolean()) {
                    CBT_Atmosphere atmosphere = new CBT_Atmosphere();
                    atmosphere.readFromBytes(input);
                    hbm$syncSet$AirPumpBlockEntity$currentAtmosphere(atmosphere);
                } else {
                    hbm$syncSet$AirPumpBlockEntity$currentAtmosphere(null);
                }
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public final void hbm$syncSet$AirPumpBlockEntity$onTicks(int next) {
        if (this.onTicks != next) {
            this.onTicks = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$AirPumpBlockEntity$airFill(int next) {
        if (this.airFill != next) {
            this.airFill = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$AirPumpBlockEntity$wasteFill(int next) {
        if (this.wasteFill != next) {
            this.wasteFill = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$AirPumpBlockEntity$bonusFill(int next) {
        if (this.bonusFill != next) {
            this.bonusFill = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$AirPumpBlockEntity$currentAtmosphere(CBT_Atmosphere next) {
        if (this.currentAtmosphere != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.currentAtmosphere);
            this.currentAtmosphere = next;
            if (bound) bindSyncValue(next, 3, 1L << 2);
            if ((1L << 2) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 2);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(AirPumpBlockEntity.class, "tank", 3, 1L << 1);
        com.hbm.packet.SyncBindings.field(
                AirPumpBlockEntity.class, "currentAtmosphere", 3, 1L << 2);
    }
}
