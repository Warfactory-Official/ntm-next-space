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

package com.hbmspace.blockentity.bomb;

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.api.energymk2.EnergyItem;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.api.fluidmk2.PlainFluidReceiver;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.blocks.bomb.LaunchPadRocket;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.Target;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.container.LaunchPadRocketMenu;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.CustomRocketItem;
import com.hbmspace.items.weapon.MissilePartsSpace;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class LaunchPadRocketBlockEntity extends MachineBlockEntity
        implements FluidTankAccess,
                EnergyHandler,
                PlainFluidReceiver,
                MenuProvider,
                SyncUnitSchema {

    public static final int SLOT_ROCKET = 0;
    public static final int SLOT_DRIVE = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_FUEL_IN = 3;
    public static final int SLOT_FUEL_OUT = 4;
    public static final int SLOT_COUNT = 5;

    public static final long MAX_POWER = 100_000;
    public static final int TANK_COUNT = RocketStruct.MAX_STAGES * 2;
    private static final int EMPTY_TANK = 64_000;

    public long power;
    public int solidLevel;
    public int solidMax;
    public final FluidTankNTM[] tanks = new FluidTankNTM[TANK_COUNT];
    public boolean canSeeSky;

    {
        hbm$syncSet$LaunchPadRocketBlockEntity$canSeeSky(true);
    }

    public int height;
    private int tower;
    public @Nullable RocketStruct rocket;

    private boolean slotsChanged = true;

    public LaunchPadRocketBlockEntity(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.LAUNCH_PAD_ROCKET_BE.get(), pos, state, SLOT_COUNT);
        for (int i = 0; i < tanks.length; i++)
            hbm$syncArraySet$LaunchPadRocketBlockEntity$tanks(
                    tanks, i, new FluidTankNTM(EMPTY_TANK));
    }

    public int towerHeight() {
        return tower;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        slotsChanged = true;
    }

    @Override
    public void tickServer() {
        if (slotsChanged) {
            loadRocket();
            slotsChanged = false;
        }

        long charge = ItemEnergyTransfer.extract(this, SLOT_BATTERY, MAX_POWER - power, false);
        if (charge > 0) hbm$syncSet$LaunchPadRocketBlockEntity$power((long) (power + (charge)));
        for (FluidTankNTM tank : tanks) tank.loadTank(SLOT_FUEL_IN, SLOT_FUEL_OUT, inventory);
        if (getItem(SLOT_FUEL_IN).is(ModItems.ROCKET_FUEL.get()) && solidLevel < solidMax) {
            removeItem(SLOT_FUEL_IN, 1);
            hbm$syncSet$LaunchPadRocketBlockEntity$solidLevel(Math.min(solidLevel + 250, solidMax));
        }

        networkPackNT(250);
    }

    private void loadRocket() {
        VOTVdriveItem.publishStation(getItem(SLOT_DRIVE), level);

        ItemStack stack = getItem(SLOT_ROCKET);
        RocketStruct held = heldRocket();
        boolean fuelled = held != null && CustomRocketItem.hasFuel(stack);
        if (!fuelled && Objects.equals(held, rocket)) return;

        hbm$syncSet$LaunchPadRocketBlockEntity$rocket(held == null ? null : held.copy());
        if (rocket == null) return;
        updateTanks(stack, fuelled);

        int newHeight = (int) Math.floor(rocket.getHeight() - rocket.capsule.height + 1);
        if (newHeight <= 8) newHeight = 0;

        if (newHeight != height) {
            hbm$syncSet$LaunchPadRocketBlockEntity$canSeeSky(!isPadObstructed());
            if (canSeeSky) raise(newHeight);
            hbm$syncSet$LaunchPadRocketBlockEntity$height(newHeight);
        }
    }

    private boolean isPadObstructed() {
        return !level.canSeeSky(worldPosition.above(3));
    }

    private void raise(int newHeight) {
        Direction facing = getBlockState().getValue(MultiblockCoreBlock.FACING);
        int maxY = level.getMaxY();
        List<BlockPos> old = new ArrayList<>();
        LaunchPadRocket.visitTower(
                worldPosition, facing, tower, maxY, pos -> old.add(pos.immutable()));
        List<BlockPos> next = new ArrayList<>();
        LaunchPadRocket.visitTower(
                worldPosition, facing, newHeight, maxY, pos -> next.add(pos.immutable()));
        hbm$syncSet$LaunchPadRocketBlockEntity$tower(newHeight);

        MultiblockCoreBlock.withoutTeardown(
                () -> {
                    long core = worldPosition.asLong();
                    for (BlockPos pos : old) {
                        if (next.contains(pos)
                                || !level.getBlockState(pos).is(getBlockState().getBlock()))
                            continue;
                        if (MultiblockSurface.recordedCorePacked(
                                        (ServerLevel) level, pos.getX(), pos.getY(), pos.getZ())
                                != core) {
                            continue;
                        }
                        level.removeBlock(pos, false);
                    }
                });

        ((LaunchPadRocket) getBlockState().getBlock()).fillSpace(level, worldPosition, facing);
        setChanged();
    }

    public void launch(@Nullable Player player) {
        if (!canLaunch()) return;

        RideableRocketEntity entity =
                new RideableRocketEntity(
                                level,
                                worldPosition.getX() + 0.5,
                                worldPosition.getY() + 3.0,
                                worldPosition.getZ() + 0.5,
                                getItem(SLOT_ROCKET))
                        .withProgram(getItem(SLOT_DRIVE))
                        .launchedBy(player);
        level.addFreshEntity(entity);

        for (int i = 0; i < tanks.length; i++) {
            hbm$syncArraySet$LaunchPadRocketBlockEntity$tanks(
                    tanks, i, new FluidTankNTM(EMPTY_TANK));
            tanks[i].bindOwner(this);
        }
        hbm$syncSet$LaunchPadRocketBlockEntity$solidMax(0);
        hbm$syncSet$LaunchPadRocketBlockEntity$solidLevel(solidMax);

        hbm$syncSet$LaunchPadRocketBlockEntity$power((long) (power - ((long) (MAX_POWER * 0.75))));

        setItem(SLOT_ROCKET, ItemStack.EMPTY);
        setItem(SLOT_DRIVE, ItemStack.EMPTY);
    }

    private @Nullable RocketStruct heldRocket() {
        ItemStack stack = getItem(SLOT_ROCKET);
        return stack.getItem() instanceof CustomRocketItem
                ? stack.get(ModDataComponentsSpace.ROCKET.get())
                : null;
    }

    private boolean hasRocket() {
        return heldRocket() != null;
    }

    private boolean hasDrive() {
        return getItem(SLOT_DRIVE).getItem() instanceof VOTVdriveItem;
    }

    public static boolean areTanksFull(FluidTankNTM[] tanks, int solidLevel, int solidMax) {
        for (FluidTankNTM tank : tanks) {
            if (tank.getTankType() != null && tank.getFill() < tank.getMaxFill()) return false;
        }
        return solidLevel >= solidMax;
    }

    public static boolean canReachDestination(
            RocketStruct rocket, ItemStack drive, Level level, BlockPos launchedAt) {
        if (!VOTVdriveItem.getProcessed(drive)) return false;
        if (orbitRefused(rocket, drive)) return false;

        Target from = CelestialBody.getTarget(level, launchedAt.getX(), launchedAt.getZ());
        Target to = VOTVdriveItem.getTarget(drive, level);

        boolean core = rocket.capsule.item == MissilePartsSpace.RP_STATION_CORE_20.get();
        if (!to.isValid() && !core) return false;
        if (to.isValid() && core) return false;

        return rocket.hasSufficientFuel(from.body(), to.body(), from.inOrbit(), to.inOrbit());
    }

    public boolean canLaunch() {
        RocketStruct held = heldRocket();
        return held != null
                && hasDrive()
                && power >= MAX_POWER * 0.75
                && areTanksFull(tanks, solidLevel, solidMax)
                && canReachDestination(held, getItem(SLOT_DRIVE), level, worldPosition);
    }

    private static boolean orbitRefused(RocketStruct rocket, ItemStack drive) {
        return VOTVdriveItem.getDestination(drive).body() == SolarSystem.Body.ORBIT
                && rocket.capsule.item != MissilePartsSpace.RP_CAPSULE_20.get()
                && rocket.capsule.item != MissilePartsSpace.RP_STATION_CORE_20.get();
    }

    private void updateTanks(ItemStack stack, boolean fuelled) {
        if (fuelled) CustomRocketItem.setFuel(stack, false);

        TankPlan plan = planTanks(rocket, tanks, fuelled);
        for (int i = 0; i < tanks.length; i++) {
            if (tanks[i] != plan.tanks().get(i)) {
                hbm$syncArraySet$LaunchPadRocketBlockEntity$tanks(tanks, i, plan.tanks().get(i));
                tanks[i].bindOwner(this);
            }
        }
        hbm$syncSet$LaunchPadRocketBlockEntity$solidMax(plan.solid());
        if (fuelled) hbm$syncSet$LaunchPadRocketBlockEntity$solidLevel(solidMax);
        setChanged();
    }

    public record TankPlan(List<FluidTankNTM> tanks, int solid) {}

    public static TankPlan planTanks(RocketStruct rocket, FluidTankNTM[] tanks, boolean hasFuel) {
        Map<Fluid, Integer> fuels = rocket.getFillRequirement();
        Integer solid = fuels.remove(NTMFluids.NONE);

        List<FluidTankNTM> keep = new ArrayList<>();
        List<FluidTankNTM> spare = new ArrayList<>();
        for (FluidTankNTM tank : tanks) {
            Integer amount = fuels.remove(tank.getTankType());
            if (amount == null) {
                spare.add(tank);
                continue;
            }
            if (tank.getMaxFill() != amount) tank.changeTankSize(amount);
            keep.add(tank);
        }
        for (Map.Entry<Fluid, Integer> entry : fuels.entrySet()) {
            keep.add(new FluidTankNTM(entry.getKey(), entry.getValue()));
        }

        if (hasFuel) {
            for (FluidTankNTM tank : keep) tank.setFill(tank.getMaxFill());
        }

        keep.sort(
                (a, b) ->
                        NTMFluids.legacyId(b.getTankType()) - NTMFluids.legacyId(a.getTankType()));
        for (FluidTankNTM tank : spare) {
            if (keep.size() == tanks.length) break;
            boolean empty =
                    tank.getTankType() == null
                            && tank.getFill() == 0
                            && tank.getMaxFill() == EMPTY_TANK;
            keep.add(empty ? tank : new FluidTankNTM(EMPTY_TANK));
        }
        return new TankPlan(keep, solid == null ? 0 : solid);
    }

    public static void findTankIssues(
            List<Component> issues, FluidTankNTM[] tanks, int solidLevel, int solidMax) {
        for (FluidTankNTM tank : tanks) {
            if (tank.getTankType() == null) continue;
            String name = NTMFluidProperties.clientName(tank.getTankType());
            name = name.substring(name.lastIndexOf(' ') + 1);
            issues.add(
                    Component.translatable(
                                    "desc.gui.launchPadRocket.tank",
                                    tank.getFill(),
                                    tank.getMaxFill(),
                                    name)
                            .withStyle(
                                    tank.getFill() < tank.getMaxFill()
                                            ? ChatFormatting.YELLOW
                                            : ChatFormatting.GREEN));
        }

        if (solidMax > 0) {
            issues.add(
                    Component.translatable("desc.gui.launchPadRocket.solid", solidLevel, solidMax)
                            .withStyle(
                                    solidLevel < solidMax
                                            ? ChatFormatting.YELLOW
                                            : ChatFormatting.GREEN));
        }
    }

    public static boolean findDriveIssues(
            List<Component> issues, RocketStruct rocket, ItemStack drive) {
        if (!(drive.getItem() instanceof VOTVdriveItem)) {
            issues.add(issue("noDrive", ChatFormatting.YELLOW));
            return true;
        }

        if (!VOTVdriveItem.getProcessed(drive)) {
            issues.add(issue("unprocessed", ChatFormatting.RED));
            return true;
        }

        if (orbitRefused(rocket, drive)) {
            issues.add(issue("satelliteOrbit", ChatFormatting.RED));
            return true;
        }

        return false;
    }

    public static void findTravelIssues(
            List<Component> issues, RocketStruct rocket, Target from, Target to) {
        boolean core = rocket.capsule.item == MissilePartsSpace.RP_STATION_CORE_20.get();
        if (to.inOrbit() && !to.isValid() && !core)
            issues.add(issue("notLaunched", ChatFormatting.RED));
        if (to.inOrbit() && to.isValid() && core) issues.add(issue("launched", ChatFormatting.RED));

        if (!rocket.hasSufficientFuel(from.body(), to.body(), from.inOrbit(), to.inOrbit())) {
            issues.add(issue("unreachable", ChatFormatting.RED));
        }
    }

    private static Component issue(String key, ChatFormatting colour) {
        return Component.translatable("desc.gui.launchPadRocket." + key).withStyle(colour);
    }

    public List<Component> findIssues() {
        List<Component> issues = new ArrayList<>();

        RocketStruct rocket = heldRocket();
        if (rocket == null) return issues;

        if (!canSeeSky) issues.add(issue("obstructed", ChatFormatting.RED));
        if (power < MAX_POWER * 0.75) issues.add(issue("noPower", ChatFormatting.RED));

        findTankIssues(issues, tanks, solidLevel, solidMax);
        ItemStack drive = getItem(SLOT_DRIVE);
        if (findDriveIssues(issues, rocket, drive)) return issues;

        Target from = CelestialBody.getTarget(level, worldPosition.getX(), worldPosition.getZ());
        Target to = VOTVdriveItem.getTarget(drive, level);
        findTravelIssues(issues, rocket, from, to);

        return issues;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_ROCKET -> stack.getItem() instanceof CustomRocketItem;
            case SLOT_DRIVE -> stack.getItem() instanceof VOTVdriveItem;
            case SLOT_BATTERY ->
                    EnergyItem.matches(stack) || stack.is(ModItems.BATTERY_CREATIVE.get());
            case SLOT_FUEL_IN -> stack.is(ModItems.ROCKET_FUEL.get());
            default -> false;
        };
    }

    @Override
    protected double interactionRangeSq() {
        return 1024;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.hbm$syncSet$LaunchPadRocketBlockEntity$power(power);
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
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
    public boolean netSubscribed() {
        return hasRocket();
    }

    @Override
    public boolean canReceiveFluid(Fluid type) {
        for (FluidTankNTM tank : tanks) {
            if (tank.joins(type)) return true;
        }
        return false;
    }

    @Override
    public void forEachTank(Consumer<FluidTankNTM> visitor) {
        for (FluidTankNTM tank : tanks) visitor.accept(tank);
    }

    @Override
    public long getDemand(Fluid type, int pressure) {
        if (pressure != 0) return 0;
        long demand = 0;
        for (FluidTankNTM tank : tanks)
            if (tank.accepts(type)) demand += tank.getMaxFill() - tank.getFill();
        return demand;
    }

    @Override
    public long transferFluid(Fluid type, int pressure, long amount) {
        if (pressure != 0) return amount;
        long remaining = amount;
        for (FluidTankNTM tank : tanks) {
            if (remaining <= 0) break;
            if (tank.accepts(type))
                remaining -= tank.fill(type, (int) Math.min(remaining, Integer.MAX_VALUE), true);
        }
        return remaining;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hbm$syncSet$LaunchPadRocketBlockEntity$power(input.getLongOr("power", 0));
        hbm$syncSet$LaunchPadRocketBlockEntity$solidLevel(input.getIntOr("solid", 0));
        hbm$syncSet$LaunchPadRocketBlockEntity$solidMax(input.getIntOr("maxSolid", 0));
        hbm$syncSet$LaunchPadRocketBlockEntity$height(input.getIntOr("height", 0));
        hbm$syncSet$LaunchPadRocketBlockEntity$tower(input.getIntOr("tower", 0));
        hbm$syncSet$LaunchPadRocketBlockEntity$canSeeSky(input.getBooleanOr("sky", true));
        for (int i = 0; i < tanks.length; i++) {
            FluidTankNTM tank = new FluidTankNTM(EMPTY_TANK);
            input.child("t" + i).ifPresent(tank::deserialize);
            hbm$syncArraySet$LaunchPadRocketBlockEntity$tanks(tanks, i, tank);
            tank.bindOwner(this);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        output.putInt("solid", solidLevel);
        output.putInt("maxSolid", solidMax);
        output.putInt("height", height);
        output.putInt("tower", tower);
        output.putBoolean("sky", canSeeSky);
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child("t" + i));
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        VOTVdriveItem.publishStation(getItem(SLOT_DRIVE), level);
        return new LaunchPadRocketMenu(containerId, playerInventory, this);
    }

    @Override
    public long syncUnitMask() {
        return 0x1FL;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> {
                output.writeInt(solidLevel);
                output.writeInt(solidMax);
            }
            case 2 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            case 3 -> {
                output.writeBoolean(canSeeSky);
                output.writeInt(height);
                output.writeInt(tower);
            }
            case 4 -> {
                output.writeBoolean(rocket != null);
                if (rocket != null) {
                    RocketStruct.STREAM_CODEC.encode(
                            new RegistryFriendlyByteBuf(output, level.registryAccess()), rocket);
                }
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$LaunchPadRocketBlockEntity$power(input.readLong());
            case 1 -> {
                hbm$syncSet$LaunchPadRocketBlockEntity$solidLevel(input.readInt());
                hbm$syncSet$LaunchPadRocketBlockEntity$solidMax(input.readInt());
            }
            case 2 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            case 3 -> {
                hbm$syncSet$LaunchPadRocketBlockEntity$canSeeSky(input.readBoolean());
                hbm$syncSet$LaunchPadRocketBlockEntity$height(input.readInt());
                hbm$syncSet$LaunchPadRocketBlockEntity$tower(input.readInt());
            }
            case 4 ->
                    hbm$syncSet$LaunchPadRocketBlockEntity$rocket(
                            input.readBoolean()
                                    ? RocketStruct.STREAM_CODEC.decode(
                                            new RegistryFriendlyByteBuf(
                                                    input, level.registryAccess()))
                                    : null);
            default -> throw new IllegalArgumentException();
        }
    }

    public final void hbm$syncSet$LaunchPadRocketBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$LaunchPadRocketBlockEntity$solidLevel(int next) {
        if (this.solidLevel != next) {
            this.solidLevel = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$LaunchPadRocketBlockEntity$solidMax(int next) {
        if (this.solidMax != next) {
            this.solidMax = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final FluidTankNTM hbm$syncArraySet$LaunchPadRocketBlockEntity$tanks(
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

    public final void hbm$syncSet$LaunchPadRocketBlockEntity$canSeeSky(boolean next) {
        if (this.canSeeSky != next) {
            this.canSeeSky = next;
            if ((1L << 3) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 3);
        }
    }

    public final void hbm$syncSet$LaunchPadRocketBlockEntity$height(int next) {
        if (this.height != next) {
            this.height = next;
            if ((1L << 3) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 3);
        }
    }

    public final void hbm$syncSet$LaunchPadRocketBlockEntity$tower(int next) {
        if (this.tower != next) {
            this.tower = next;
            if ((1L << 3) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 3);
        }
    }

    public final void hbm$syncSet$LaunchPadRocketBlockEntity$rocket(RocketStruct next) {
        if (this.rocket != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.rocket);
            this.rocket = next;
            if (bound) bindSyncValue(next, 3, 1L << 4);
            if ((1L << 4) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 4);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(LaunchPadRocketBlockEntity.class, "tanks", 3, 1L << 2);
        com.hbm.packet.SyncBindings.field(LaunchPadRocketBlockEntity.class, "rocket", 3, 1L << 4);
    }
}
