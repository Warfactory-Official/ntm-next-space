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

import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.util.ChunkUtil;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.Target;
import com.hbmspace.inventory.container.TransporterRocketMenu;
import com.hbmspace.inventory.fluid.trait.FT_Rocket;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TransporterRocketBlockEntity extends TransporterBaseBlockEntity {

    public static Consumer<TransporterRocketBlockEntity> CLIENT_EFFECTS = be -> {};

    public static final int SLOTS = 16 + 4 + 2;
    private static final long ROCKET_UNIT = 1L << 3;
    private static final int MASS_MULT = 100;

    public boolean hasRocket;

    {
        hbm$syncSet$TransporterRocketBlockEntity$hasRocket(true);
    }

    public int launchTicks = 0;

    public int threshold;

    {
        hbm$syncSet$TransporterRocketBlockEntity$threshold(0);
    }

    public TransporterRocketBlockEntity(BlockPos pos, BlockState state) {
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
        ChunkUtil.holdForEntity(
                (ServerLevel) level, ChunkPos.containing(worldPosition), ChunkUtil.HOLD_RADIUS);

        if (linkedTransporter instanceof TransporterRocketBlockEntity rocket
                && hasRocket == rocket.hasRocket) {
            hbm$syncSet$TransporterRocketBlockEntity$hasRocket(!hasRocket);
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
            TransporterBaseBlockEntity linkedTransporter, FT_Rocket fuelStats, int mass) {
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
    protected boolean canSend(TransporterBaseBlockEntity linkedTransporter) {
        if (launchTicks > -20) return false;
        if (((TransporterRocketBlockEntity) linkedTransporter).launchTicks < 100) return false;
        if (!hasRocket) return false;

        int mass = itemCount();
        if (mass < getThreshold()) return false;

        FT_Rocket fuelStats = fuelStats();
        if (fuelStats == null) return false;

        int sendCost = sendCost(linkedTransporter, fuelStats, mass);
        return tanks[8].getFill() >= sendCost && tanks[9].getFill() >= sendCost;
    }

    @Override
    protected void hasSent(TransporterBaseBlockEntity linkedTransporter, int quantitySent) {
        int sendCost = sendCost(linkedTransporter, fuelStats(), quantitySent);

        tanks[8].setFill(tanks[8].getFill() - sendCost);
        tanks[9].setFill(tanks[9].getFill() - sendCost);

        hbm$syncSet$TransporterRocketBlockEntity$hasRocket(false);
        ((TransporterRocketBlockEntity) linkedTransporter)
                .hbm$syncSet$TransporterRocketBlockEntity$hasRocket(true);
    }

    @Override
    protected void hasConnected(TransporterBaseBlockEntity linkedTransporter) {
        hbm$syncSet$TransporterRocketBlockEntity$hasRocket(true);
        ((TransporterRocketBlockEntity) linkedTransporter)
                .hbm$syncSet$TransporterRocketBlockEntity$hasRocket(false);
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

    public void setThreshold(int threshold) {
        this.hbm$syncSet$TransporterRocketBlockEntity$threshold(threshold);
        setChanged();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new TransporterRocketMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$TransporterRocketBlockEntity$hasRocket(input.getBooleanOr("rocket", false));
        hbm$syncSet$TransporterRocketBlockEntity$threshold(input.getIntOr("threshold", 0));
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
        hbm$syncSet$TransporterRocketBlockEntity$hasRocket(input.readBoolean());
        hbm$syncSet$TransporterRocketBlockEntity$threshold(input.readInt());
    }

    public final void hbm$syncSet$TransporterRocketBlockEntity$hasRocket(boolean next) {
        if (this.hasRocket != next) {
            this.hasRocket = next;
            if ((ROCKET_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, ROCKET_UNIT);
        }
    }

    public final void hbm$syncSet$TransporterRocketBlockEntity$threshold(int next) {
        if (this.threshold != next) {
            this.threshold = next;
            if ((ROCKET_UNIT) == 0L) syncChanged(3);
            else syncUnitsChanged(3, ROCKET_UNIT);
        }
    }
}
