// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.bomb;

import com.hbm.api.control.IControlReceiver;
import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.api.fluidmk2.IFluidHandlerMK2;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.blocks.bomb.LaunchPadRocket;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.Target;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.container.MenuLaunchPadRocket;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.weapon.ItemCustomRocket;
import com.hbmspace.items.weapon.MissilePartsSpace;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

public class BlockEntityLaunchPadRocket extends BlockEntityMachineBase
        implements IEnergyHandlerMK2,
                IFluidHandlerMK2,
                IControlReceiver,
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

    @SyncField(units = 1L << 0)
    public long power;

    @SyncField(units = 1L << 1)
    public int solidLevel;

    @SyncField(units = 1L << 1)
    public int solidMax;

    @SyncField(units = 1L << 2)
    public final FluidTankNTM[] tanks = new FluidTankNTM[TANK_COUNT];

    @SyncField(units = 1L << 3)
    public boolean canSeeSky = true;

    @SyncField(units = 1L << 3)
    public int height;

    @SyncField(units = 1L << 3)
    private int tower;

    @SyncField(units = 1L << 4)
    public @Nullable RocketStruct rocket;

    public BlockEntityLaunchPadRocket(BlockPos pos, BlockState state) {
        super(RocketryBlocksSpace.LAUNCH_PAD_ROCKET_BE.get(), pos, state, SLOT_COUNT);
        for (int i = 0; i < tanks.length; i++) tanks[i] = new FluidTankNTM(EMPTY_TANK);
    }

    public int towerHeight() {
        return tower;
    }

    @Override
    public void tickServer() {
        ItemVOTVdrive.getTarget(getItem(SLOT_DRIVE), level);

        updateTanks();

        long charge = ItemEnergyTransfer.extract(this, SLOT_BATTERY, MAX_POWER - power, false);
        if (charge > 0) power += charge;
        for (FluidTankNTM tank : tanks) tank.loadTank(SLOT_FUEL_IN, SLOT_FUEL_OUT, inventory);
        if (getItem(SLOT_FUEL_IN).is(ModItems.ROCKET_FUEL.get()) && solidLevel < solidMax) {
            removeItem(SLOT_FUEL_IN, 1);
            solidLevel = Math.min(solidLevel + 250, solidMax);
        }

        RocketStruct loaded = ItemCustomRocket.get(getItem(SLOT_ROCKET));
        if (!Objects.equals(loaded, rocket)) rocket = loaded;
        if (rocket != null) {
            int newHeight = (int) Math.floor(rocket.getHeight() - rocket.capsule.height + 1);
            if (newHeight <= 8) newHeight = 0;

            if (newHeight != height) {
                canSeeSky = !isPadObstructed();
                if (canSeeSky) raise(newHeight);
                height = newHeight;
            }
        }

        networkPackNT(250);
    }

    private boolean isPadObstructed() {
        return !level.canSeeSky(worldPosition.above(3));
    }

    private void raise(int newHeight) {
        Direction facing = getBlockState().getValue(BlockMultiblockCore.FACING);
        int maxY = level.getMaxY();
        List<BlockPos> old = new ArrayList<>();
        LaunchPadRocket.visitTower(
                worldPosition, facing, tower, maxY, (pos, mask) -> old.add(pos.immutable()));
        List<BlockPos> next = new ArrayList<>();
        LaunchPadRocket.visitTower(
                worldPosition, facing, newHeight, maxY, (pos, mask) -> next.add(pos.immutable()));
        tower = newHeight;

        BlockMultiblockCore.withoutTeardown(
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

        EntityRideableRocket entity =
                new EntityRideableRocket(
                                level,
                                worldPosition.getX() + 0.5,
                                worldPosition.getY() + 3.0,
                                worldPosition.getZ() + 0.5,
                                getItem(SLOT_ROCKET))
                        .withProgram(getItem(SLOT_DRIVE))
                        .launchedBy(player);
        level.addFreshEntity(entity);

        for (int i = 0; i < tanks.length; i++) tanks[i] = new FluidTankNTM(EMPTY_TANK);
        solidLevel = solidMax = 0;

        power -= (long) (MAX_POWER * 0.75);

        setItem(SLOT_ROCKET, ItemStack.EMPTY);
        setItem(SLOT_DRIVE, ItemStack.EMPTY);
    }

    private boolean hasRocket() {
        return ItemCustomRocket.get(getItem(SLOT_ROCKET)) != null;
    }

    private boolean hasDrive() {
        return getItem(SLOT_DRIVE).getItem() instanceof ItemVOTVdrive;
    }

    public static boolean areTanksFull(FluidTankNTM[] tanks, int solidLevel, int solidMax) {
        for (FluidTankNTM tank : tanks) {
            if (tank.getTankType() != null && tank.getFill() < tank.getMaxFill()) return false;
        }
        return solidLevel >= solidMax;
    }

    public static boolean canReachDestination(
            RocketStruct rocket, ItemStack drive, Level level, BlockPos launchedAt) {
        if (!ItemVOTVdrive.getProcessed(drive)) return false;
        if (orbitRefused(rocket, drive)) return false;

        Target from = CelestialBody.getTarget(level, launchedAt.getX(), launchedAt.getZ());
        Target to = ItemVOTVdrive.getTarget(drive, level);

        boolean core = rocket.capsule.item == MissilePartsSpace.RP_STATION_CORE_20.get();
        if (!to.isValid() && !core) return false;
        if (to.isValid() && core) return false;

        return rocket.hasSufficientFuel(from.body(), to.body(), from.inOrbit(), to.inOrbit());
    }

    public boolean canLaunch() {
        return hasRocket()
                && hasDrive()
                && power >= MAX_POWER * 0.75
                && areTanksFull(tanks, solidLevel, solidMax)
                && canReachDestination(
                        ItemCustomRocket.get(getItem(SLOT_ROCKET)),
                        getItem(SLOT_DRIVE),
                        level,
                        worldPosition);
    }

    private static boolean orbitRefused(RocketStruct rocket, ItemStack drive) {
        return ItemVOTVdrive.getDestination(drive).body() == SolarSystem.Body.ORBIT
                && rocket.capsule.item != MissilePartsSpace.RP_CAPSULE_20.get()
                && rocket.capsule.item != MissilePartsSpace.RP_STATION_CORE_20.get();
    }

    private void updateTanks() {
        ItemStack stack = getItem(SLOT_ROCKET);
        RocketStruct rocket = ItemCustomRocket.get(stack);
        if (rocket == null) return;

        boolean hasFuel = ItemCustomRocket.hasFuel(stack);
        if (hasFuel) ItemCustomRocket.setFuel(stack, false);

        TankPlan plan = planTanks(rocket, tanks, hasFuel);
        for (int i = 0; i < tanks.length; i++) {
            if (tanks[i] != plan.tanks().get(i)) tanks[i] = plan.tanks().get(i);
        }
        solidMax = plan.solid();
        if (hasFuel) solidLevel = solidMax;
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
        if (!(drive.getItem() instanceof ItemVOTVdrive)) {
            issues.add(issue("noDrive", ChatFormatting.YELLOW));
            return true;
        }

        if (!ItemVOTVdrive.getProcessed(drive)) {
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

        RocketStruct rocket = ItemCustomRocket.get(getItem(SLOT_ROCKET));
        if (rocket == null) return issues;

        if (!canSeeSky) issues.add(issue("obstructed", ChatFormatting.RED));
        if (power < MAX_POWER * 0.75) issues.add(issue("noPower", ChatFormatting.RED));

        findTankIssues(issues, tanks, solidLevel, solidMax);
        ItemStack drive = getItem(SLOT_DRIVE);
        if (findDriveIssues(issues, rocket, drive)) return issues;

        Target from = CelestialBody.getTarget(level, worldPosition.getX(), worldPosition.getZ());
        Target to = ItemVOTVdrive.getTarget(drive, level);
        findTravelIssues(issues, rocket, from, to);

        return issues;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_ROCKET -> stack.getItem() instanceof ItemCustomRocket;
            case SLOT_DRIVE -> stack.getItem() instanceof ItemVOTVdrive;
            case SLOT_BATTERY ->
                    IBatteryItem.isBattery(stack) || stack.is(ModItems.BATTERY_CREATIVE.get());
            case SLOT_FUEL_IN -> stack.is(ModItems.ROCKET_FUEL.get());
            default -> false;
        };
    }

    @Override
    protected double interactionRangeSq() {
        return 1024;
    }

    @Override
    public boolean hasPermission(Player player) {
        return stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        receiveControl(null, data);
    }

    @Override
    public void receiveControl(@Nullable Player player, CompoundTag data) {
        if (data.getBooleanOr("launch", false)) launch(player);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public boolean netSubscribed() {
        return hasRocket();
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
        power = input.getLongOr("power", 0);
        solidLevel = input.getIntOr("solid", 0);
        solidMax = input.getIntOr("maxSolid", 0);
        height = input.getIntOr("height", 0);
        tower = input.getIntOr("tower", 0);
        canSeeSky = input.getBooleanOr("sky", true);
        for (int i = 0; i < tanks.length; i++) {
            FluidTankNTM tank = new FluidTankNTM(EMPTY_TANK);
            input.child("t" + i).ifPresent(tank::deserialize);
            tanks[i] = tank;
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
        return new MenuLaunchPadRocket(containerId, playerInventory, this);
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
            case 0 -> power = input.readLong();
            case 1 -> {
                solidLevel = input.readInt();
                solidMax = input.readInt();
            }
            case 2 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            case 3 -> {
                canSeeSky = input.readBoolean();
                height = input.readInt();
                tower = input.readInt();
            }
            case 4 ->
                    rocket =
                            input.readBoolean()
                                    ? RocketStruct.STREAM_CODEC.decode(
                                            new RegistryFriendlyByteBuf(
                                                    input, level.registryAccess()))
                                    : null;
            default -> throw new IllegalArgumentException();
        }
    }
}
