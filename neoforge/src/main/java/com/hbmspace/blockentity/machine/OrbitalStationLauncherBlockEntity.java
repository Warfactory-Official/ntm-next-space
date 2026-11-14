/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
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
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity.TankPlan;
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.Target;
import com.hbmspace.entity.missile.RideableRocketEntity.RocketState;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.hbmspace.entity.missile.RocketDock;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.container.OrbitalStationLauncherMenu;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.CustomRocketItem;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class OrbitalStationLauncherBlockEntity extends MachineBlockEntity
        implements FluidTankAccess, FluidTankHandler, MenuProvider, SyncUnitSchema, RocketDock {

    public static final int SLOT_DRIVE = 0;
    public static final int SLOT_FUEL_IN = 1;
    public static final int SLOT_FUEL_OUT = 2;
    public static final int SLOT_CAPSULE = 3;
    public static final int FIRST_PROGRAM = SLOT_CAPSULE + 1 + RocketStruct.MAX_STAGES * 3;
    public static final int SLOT_COUNT = FIRST_PROGRAM + RocketStruct.MAX_STAGES * 2;
    public static final int STACK_LIMIT = 8;
    private static final int EMPTY_TANK = 64_000;

    public boolean hasDocked;
    public boolean hasRider;
    public int solidLevel;
    public int solidMax;
    public final FluidTankNTM[] tanks = new FluidTankNTM[LaunchPadRocketBlockEntity.TANK_COUNT];
    public RocketStruct rocket;

    {
        hbm$syncSet$OrbitalStationLauncherBlockEntity$rocket(new RocketStruct());
    }

    public int currentStage;
    public float rot;
    public float prevRot;

    private @Nullable RideableRocketEntity docked;
    private boolean slotsChanged = true;

    public OrbitalStationLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.ORBITAL_STATION_LAUNCHER.get(), pos, state, SLOT_COUNT);
        for (int i = 0; i < tanks.length; i++)
            hbm$syncArraySet$OrbitalStationLauncherBlockEntity$tanks(
                    tanks, i, new FluidTankNTM(EMPTY_TANK));
    }

    public static int fuselage(int stage) {
        return SLOT_CAPSULE + 1 + stage * 3;
    }

    public static int fins(int stage) {
        return SLOT_CAPSULE + 2 + stage * 3;
    }

    public static int thruster(int stage) {
        return SLOT_CAPSULE + 3 + stage * 3;
    }

    public static int programFrom(int stage) {
        return FIRST_PROGRAM + stage * 2;
    }

    public static int programTo(int stage) {
        return FIRST_PROGRAM + 1 + stage * 2;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        slotsChanged = true;
    }

    @Override
    public void tickServer() {
        if (!CelestialBody.inOrbit(level)) return;

        if (slotsChanged) {
            reassemble();
            slotsChanged = false;
        }

        for (FluidTankNTM tank : tanks) tank.loadTank(SLOT_FUEL_IN, SLOT_FUEL_OUT, inventory);
        if (getItem(SLOT_FUEL_IN).is(ModItems.ROCKET_FUEL.get()) && solidLevel < solidMax) {
            removeItem(SLOT_FUEL_IN, 1);
            hbm$syncSet$OrbitalStationLauncherBlockEntity$solidLevel(
                    Math.min(solidLevel + 250, solidMax));
        }

        if (docked != null && (docked.isRemoved() || docked.getState() == RocketState.UNDOCKING))
            docked = null;

        hbm$syncSet$OrbitalStationLauncherBlockEntity$hasDocked(docked != null);
        hbm$syncSet$OrbitalStationLauncherBlockEntity$hasRider(
                hasDocked && docked.getFirstPassenger() != null);

        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        if (!CelestialBody.inOrbit(level)) return;

        prevRot = rot;
        rot = hasDocked ? Math.min(rot + 2.25F, 90) : Math.max(rot - 2.25F, 0);
    }

    private void reassemble() {
        publishStations();

        RocketStruct next = RocketAssemblyBlockEntity.assemble(this, SLOT_CAPSULE);
        if (!next.equals(rocket)) hbm$syncSet$OrbitalStationLauncherBlockEntity$rocket(next);

        if (rocket.validate()) updateTanks();
    }

    private void publishStations() {
        VOTVdriveItem.publishStation(getItem(SLOT_DRIVE), level);
        for (int stage = 0; stage < RocketStruct.MAX_STAGES; stage++) {
            VOTVdriveItem.publishStation(getItem(programFrom(stage)), level);
            VOTVdriveItem.publishStation(getItem(programTo(stage)), level);
        }
    }

    private void updateTanks() {
        TankPlan plan = LaunchPadRocketBlockEntity.planTanks(rocket, tanks, false);
        for (int i = 0; i < tanks.length; i++) {
            if (tanks[i] != plan.tanks().get(i)) {
                hbm$syncArraySet$OrbitalStationLauncherBlockEntity$tanks(
                        tanks, i, plan.tanks().get(i));
                tanks[i].bindOwner(this);
            }
        }
        hbm$syncSet$OrbitalStationLauncherBlockEntity$solidMax(plan.solid());
    }

    public void enterCapsule(Player player) {
        if (docked == null || docked.getFirstPassenger() != null) return;
        docked.interact(player, InteractionHand.MAIN_HAND, docked.position());
    }

    @Override
    public void dockRocket(RideableRocketEntity rocket) {
        docked = rocket;
    }

    public void retrieveRocket(Player player) {
        if (docked == null || docked.getFirstPassenger() != null) return;
        docked.retrieve(player);
        docked = null;
    }

    @Override
    public @Nullable RideableRocketEntity getDocked() {
        return docked;
    }

    private boolean hasDrive() {
        return getItem(SLOT_DRIVE).getItem() instanceof VOTVdriveItem;
    }

    public boolean canLaunch() {
        return rocket.validate()
                && hasDrive()
                && LaunchPadRocketBlockEntity.areTanksFull(tanks, solidLevel, solidMax)
                && LaunchPadRocketBlockEntity.canReachDestination(
                        rocket, getItem(SLOT_DRIVE), level, worldPosition);
    }

    public void launch(@Nullable Player player) {
        if (!canLaunch()) return;

        RideableRocketEntity entity =
                new RideableRocketEntity(
                                level,
                                worldPosition.getX() + 0.5F,
                                worldPosition.getY() + 1.5F,
                                worldPosition.getZ() + 0.5F,
                                CustomRocketItem.build(rocket))
                        .withProgram(getItem(SLOT_DRIVE))
                        .launchedBy(player);
        entity.setPos(entity.getX(), entity.getY() - entity.getBbHeight(), entity.getZ());
        level.addFreshEntity(entity);

        for (int i = 0; i < tanks.length; i++) {
            hbm$syncArraySet$OrbitalStationLauncherBlockEntity$tanks(
                    tanks, i, new FluidTankNTM(EMPTY_TANK));
            tanks[i].bindOwner(this);
        }
        hbm$syncSet$OrbitalStationLauncherBlockEntity$solidMax(0);
        hbm$syncSet$OrbitalStationLauncherBlockEntity$solidLevel(solidMax);

        setItem(SLOT_DRIVE, ItemStack.EMPTY);
        for (int i = SLOT_CAPSULE; i < SLOT_COUNT; i++) setItem(i, ItemStack.EMPTY);

        dockRocket(entity);
    }

    public List<Component> findIssues() {
        List<Component> issues = new ArrayList<>();
        if (!rocket.validate()) return issues;

        LaunchPadRocketBlockEntity.findTankIssues(issues, tanks, solidLevel, solidMax);
        ItemStack drive = getItem(SLOT_DRIVE);
        if (LaunchPadRocketBlockEntity.findDriveIssues(issues, rocket, drive)) return issues;

        Target from = CelestialBody.getTarget(level, worldPosition.getX(), worldPosition.getZ());
        Target to = VOTVdriveItem.getTarget(drive, level);
        LaunchPadRocketBlockEntity.findTravelIssues(issues, rocket, from, to);

        return issues;
    }

    @Override
    public int getMaxStackSize() {
        return STACK_LIMIT;
    }

    @Override
    public boolean dropsSlot(int slot) {
        return slot < FIRST_PROGRAM;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_DRIVE -> stack.getItem() instanceof VOTVdriveItem;
            case SLOT_FUEL_IN -> stack.is(ModItems.ROCKET_FUEL.get());
            default -> false;
        };
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
    public boolean netSubscribed() {
        return rocket.validate();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$OrbitalStationLauncherBlockEntity$solidLevel(input.getIntOr("solid", 0));
        hbm$syncSet$OrbitalStationLauncherBlockEntity$solidMax(input.getIntOr("maxSolid", 0));
        for (int i = 0; i < tanks.length; i++) {
            FluidTankNTM tank = new FluidTankNTM(EMPTY_TANK);
            input.child("t" + i).ifPresent(tank::deserialize);
            hbm$syncArraySet$OrbitalStationLauncherBlockEntity$tanks(tanks, i, tank);
            tank.bindOwner(this);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("solid", solidLevel);
        output.putInt("maxSolid", solidMax);
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child("t" + i));
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        publishStations();
        return new OrbitalStationLauncherMenu(containerId, playerInventory, this);
    }

    @Override
    public long syncUnitMask() {
        return 0xFL;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> {
                output.writeBoolean(hasDocked);
                output.writeBoolean(hasRider);
            }
            case 1 -> {
                output.writeInt(solidLevel);
                output.writeInt(solidMax);
            }
            case 2 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            case 3 ->
                    RocketStruct.STREAM_CODEC.encode(
                            new RegistryFriendlyByteBuf(output, level.registryAccess()), rocket);
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> {
                hbm$syncSet$OrbitalStationLauncherBlockEntity$hasDocked(input.readBoolean());
                hbm$syncSet$OrbitalStationLauncherBlockEntity$hasRider(input.readBoolean());
            }
            case 1 -> {
                hbm$syncSet$OrbitalStationLauncherBlockEntity$solidLevel(input.readInt());
                hbm$syncSet$OrbitalStationLauncherBlockEntity$solidMax(input.readInt());
            }
            case 2 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            case 3 ->
                    hbm$syncSet$OrbitalStationLauncherBlockEntity$rocket(
                            RocketStruct.STREAM_CODEC.decode(
                                    new RegistryFriendlyByteBuf(input, level.registryAccess())));
            default -> throw new IllegalArgumentException();
        }
    }

    public final void hbm$syncSet$OrbitalStationLauncherBlockEntity$hasDocked(boolean next) {
        if (this.hasDocked != next) {
            this.hasDocked = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$OrbitalStationLauncherBlockEntity$hasRider(boolean next) {
        if (this.hasRider != next) {
            this.hasRider = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$OrbitalStationLauncherBlockEntity$solidLevel(int next) {
        if (this.solidLevel != next) {
            this.solidLevel = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$OrbitalStationLauncherBlockEntity$solidMax(int next) {
        if (this.solidMax != next) {
            this.solidMax = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final FluidTankNTM hbm$syncArraySet$OrbitalStationLauncherBlockEntity$tanks(
            FluidTankNTM[] array, int index, FluidTankNTM next) {
        if (array[index] != next) {
            if (array == this.tanks && syncBound())
                com.hbm.packet.SyncBindings.unbind(this, array[index]);
            array[index] = next;
            if (array == this.tanks) {
                syncArrayChanged(array, index, 3, 1L << 2);
            }
        }
        return next;
    }

    public final void hbm$syncSet$OrbitalStationLauncherBlockEntity$rocket(RocketStruct next) {
        if (this.rocket != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.rocket);
            this.rocket = next;
            if (bound) bindSyncValue(next, 3, 1L << 3);
            if ((1L << 3) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 3);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(
                OrbitalStationLauncherBlockEntity.class, "tanks", 3, 1L << 2);
        com.hbm.packet.SyncBindings.field(
                OrbitalStationLauncherBlockEntity.class, "rocket", 3, 1L << 3);
    }
}
