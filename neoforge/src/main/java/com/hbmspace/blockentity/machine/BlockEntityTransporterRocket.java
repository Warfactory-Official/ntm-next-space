// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.packet.SyncField;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.Target;
import com.hbmspace.inventory.container.MenuTransporterRocket;
import com.hbmspace.inventory.fluid.trait.FT_Rocket;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityTransporterRocket extends BlockEntityTransporterBase {

    public static Consumer<BlockEntityTransporterRocket> CLIENT_EFFECTS = be -> {};

    public static final int SLOTS = 16 + 4 + 2;
    private static final long ROCKET_UNIT = 1L << 3;
    private static final int MASS_MULT = 100;

    @SyncField(units = ROCKET_UNIT)
    public boolean hasRocket = true;

    public int launchTicks = 0;

    @SyncField(units = ROCKET_UNIT)
    public int threshold = 0;

    public BlockEntityTransporterRocket(BlockPos pos, BlockState state) {
        super(
                StationBlockEntitiesSpace.TRANSPORTER_ROCKET.get(),
                pos,
                state,
                16,
                8,
                128_000,
                0,
                2,
                64_000);

        tanks[8].setTankType(NTMFluids.HYDROGEN);
        tanks[9].setTankType(NTMFluids.OXYGEN);
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (linkedTransporter instanceof BlockEntityTransporterRocket rocket
                && hasRocket == rocket.hasRocket) {
            hasRocket = !hasRocket;
        }

        stepLaunch();
    }

    @Override
    public void tickClient() {
        stepLaunch();
        CLIENT_EFFECTS.accept(this);
    }

    private void stepLaunch() {
        launchTicks = Mth.clamp(launchTicks + (hasRocket ? -1 : 1), hasRocket ? -20 : 0, 100);
    }

    public int getThreshold() {
        return threshold == 0 ? 0 : (int) Math.pow(2, threshold - 1);
    }

    private @Nullable FT_Rocket fuelStats() {
        FT_Rocket stats = NTMFluidProperties.getTrait(tanks[8].getTankType(), FT_Rocket.class);
        return stats != null
                ? stats
                : NTMFluidProperties.getTrait(tanks[9].getTankType(), FT_Rocket.class);
    }

    private int sendCost(
            BlockEntityTransporterBase linkedTransporter, FT_Rocket fuelStats, int mass) {
        Target from = CelestialBody.getTarget(level, worldPosition.getX(), worldPosition.getZ());
        BlockPos to = linkedTransporter.getBlockPos();
        Target target = CelestialBody.getTarget(linkedTransporter.getLevel(), to.getX(), to.getZ());
        return Math.min(
                64_000,
                SolarSystem.getCostBetween(
                        from.body(),
                        target.body(),
                        mass * MASS_MULT,
                        (int) fuelStats.getThrust(),
                        fuelStats.getISP(),
                        from.inOrbit(),
                        target.inOrbit()));
    }

    @Override
    protected boolean canSend(BlockEntityTransporterBase linkedTransporter) {
        if (launchTicks > -20) return false;
        if (((BlockEntityTransporterRocket) linkedTransporter).launchTicks < 100) return false;
        if (!hasRocket) return false;

        int mass = itemCount();
        if (mass < getThreshold()) return false;

        FT_Rocket fuelStats = fuelStats();
        if (fuelStats == null) return false;

        int sendCost = sendCost(linkedTransporter, fuelStats, mass);
        return tanks[8].getFill() >= sendCost && tanks[9].getFill() >= sendCost;
    }

    @Override
    protected void hasSent(BlockEntityTransporterBase linkedTransporter, int quantitySent) {
        int sendCost = sendCost(linkedTransporter, fuelStats(), quantitySent);

        tanks[8].setFill(tanks[8].getFill() - sendCost);
        tanks[9].setFill(tanks[9].getFill() - sendCost);

        hasRocket = false;
        ((BlockEntityTransporterRocket) linkedTransporter).hasRocket = true;
    }

    @Override
    protected void hasConnected(BlockEntityTransporterBase linkedTransporter) {
        hasRocket = true;
        ((BlockEntityTransporterRocket) linkedTransporter).hasRocket = false;
    }

    private BlockPos local(int forward, int right) {
        return worldPosition.relative(facing(), forward).relative(facing().getClockWise(), right);
    }

    @Override
    protected BlockPos[] getTankCells() {
        return new BlockPos[] {local(1, -2), local(-1, -2)};
    }

    @Override
    protected BlockPos getInsertPos() {
        return local(-2, 0);
    }

    @Override
    protected BlockPos getExtractPos() {
        return local(2, 0);
    }

    @Override
    public void receiveControl(CompoundTag nbt) {
        super.receiveControl(nbt);
        nbt.getInt("threshold").ifPresent(threshold -> this.threshold = threshold);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new MenuTransporterRocket(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hasRocket = input.getBooleanOr("rocket", false);
        threshold = input.getIntOr("threshold", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("rocket", hasRocket);
        output.putInt("threshold", threshold);
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | ROCKET_UNIT;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        if (unit != 3) {
            super.writeSyncUnit(unit, output);
            return;
        }
        output.writeBoolean(hasRocket);
        output.writeInt(threshold);
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        if (unit != 3) {
            super.readSyncUnit(unit, input);
            return;
        }
        hasRocket = input.readBoolean();
        threshold = input.readInt();
    }
}
