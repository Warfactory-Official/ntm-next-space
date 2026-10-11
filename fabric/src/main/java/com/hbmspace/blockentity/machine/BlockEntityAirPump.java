// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.CoolingTowerParticleOptions;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereBlob;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.handler.atmosphere.IAtmosphereProvider;
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

public class BlockEntityAirPump extends BlockEntityMachineBase
        implements FluidTankEndpoint, IAtmosphereProvider, SyncUnitSchema {

    public static final int MAX_BLOB_RADIUS = 256;
    public static final int FILL_RATE = 20;

    @SyncField(units = 1L << 1)
    public final FluidTankNTM tank = new FluidTankNTM(NTMFluids.OXYGEN, 16_000);

    private final FluidTankNTM[] tanks = {tank};

    @SyncField(units = 1L << 0)
    public int onTicks;

    @SyncField(units = 1L << 0)
    private int airFill;

    @SyncField(units = 1L << 0)
    private int wasteFill;

    @SyncField(units = 1L << 0)
    private int bonusFill;

    @SyncField(units = 1L << 2)
    public @Nullable CBT_Atmosphere currentAtmosphere;

    private boolean registered;
    private int recovering;
    private int registerWait = 10;
    private @Nullable AtmosphereBlob currentBlob;
    private @Nullable BlockEntityAirScrubber scrubber;

    public BlockEntityAirPump(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.AIR_PUMP.get(), pos, state, 0);
    }

    @Override
    public void tickServer() {
        int tankBefore = tank.getFill(),
                airBefore = airFill,
                wasteBefore = wasteFill,
                bonusBefore = bonusFill;
        if (onTicks > 0) onTicks--;
        if (registerWait > 0) registerWait--;

        if (tank.getFill() + bonusFill >= FILL_RATE) {
            onTicks = 20;

            if (registerWait > 0) {
            } else if (!registered) {
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
                    airFill = 0;
                    wasteFill = 0;
                }
            }
        } else if (registered) {
            unregister();
            airFill = 0;
            wasteFill = 0;
        }

        if (TickPhase.every(this, 5)) {
            currentAtmosphere =
                    ChunkAtmosphereManager.proxy.getAtmosphere((ServerLevel) level, worldPosition);
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
        if (airFill > size) airFill = size;
        if (wasteFill > size) wasteFill = size;

        int toFill = Math.min(size - airFill, FILL_RATE);
        airFill += toFill;

        if (toFill > 0) {
            consumeFromTank(toFill);
        } else if (level.getRandom().nextBoolean()) {
            consumeFromTank(1);
            wasteFill++;
        }

        if (scrubber != null && !scrubber.isRemoved()) {
            int scrubbed = scrubber.scrub(Mth.clamp(wasteFill, 0, FILL_RATE));
            wasteFill -= scrubbed;
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
        bonusFill -= bonusConsumption;
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

    public boolean registerScrubber(BlockEntityAirScrubber scrubber) {
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
        airFill -= amount;
        wasteFill += amount;
        if (airFill < 1) airFill = 1;
    }

    @Override
    public void produce(int amount) {
        int produced = Mth.clamp(amount, 0, wasteFill);
        bonusFill += produced;
        wasteFill -= produced;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("at").ifPresent(tank::deserialize);
        airFill = input.getIntOr("fill", 0);
        wasteFill = input.getIntOr("waste", 0);
        bonusFill = input.getIntOr("bonus", 0);
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
                onTicks = input.readInt();
                airFill = input.readInt();
                wasteFill = input.readInt();
                bonusFill = input.readInt();
            }
            case 1 -> tank.packetDeserialize(input);
            case 2 -> {
                if (input.readBoolean()) {
                    CBT_Atmosphere atmosphere = new CBT_Atmosphere();
                    atmosphere.readFromBytes(input);
                    currentAtmosphere = atmosphere;
                } else {
                    currentAtmosphere = null;
                }
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
