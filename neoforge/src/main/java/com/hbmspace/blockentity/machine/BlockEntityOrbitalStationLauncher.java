// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.control.IControlReceiver;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket.TankPlan;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.Target;
import com.hbmspace.entity.missile.EntityRideableRocket.RocketState;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.entity.missile.RocketDock;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.container.MenuOrbitalStationLauncher;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.weapon.ItemCustomRocket;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

public class BlockEntityOrbitalStationLauncher extends BlockEntityMachineBase
        implements FluidTankEndpoint, IControlReceiver, MenuProvider, SyncUnitSchema, RocketDock {

    public static final int SLOT_DRIVE = 0;
    public static final int SLOT_FUEL_IN = 1;
    public static final int SLOT_FUEL_OUT = 2;
    public static final int SLOT_CAPSULE = 3;
    public static final int FIRST_PROGRAM = SLOT_CAPSULE + 1 + RocketStruct.MAX_STAGES * 3;
    public static final int SLOT_COUNT = FIRST_PROGRAM + RocketStruct.MAX_STAGES * 2;
    public static final int STACK_LIMIT = 8;
    private static final int EMPTY_TANK = 64_000;

    @SyncField(units = 1L << 0)
    public boolean hasDocked;

    @SyncField(units = 1L << 0)
    public boolean hasRider;

    @SyncField(units = 1L << 1)
    public int solidLevel;

    @SyncField(units = 1L << 1)
    public int solidMax;

    @SyncField(units = 1L << 2)
    public final FluidTankNTM[] tanks = new FluidTankNTM[BlockEntityLaunchPadRocket.TANK_COUNT];

    @SyncField(units = 1L << 3)
    public RocketStruct rocket = new RocketStruct();

    public int currentStage;
    public float rot;
    public float prevRot;

    private @Nullable EntityRideableRocket docked;

    public BlockEntityOrbitalStationLauncher(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.ORBITAL_STATION_LAUNCHER.get(), pos, state, SLOT_COUNT);
        for (int i = 0; i < tanks.length; i++) tanks[i] = new FluidTankNTM(EMPTY_TANK);
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
    public void tickServer() {
        if (!CelestialBody.inOrbit(level)) return;

        ItemVOTVdrive.getTarget(getItem(SLOT_DRIVE), level);
        ItemVOTVdrive.getTarget(getItem(programFrom(currentStage)), level);
        ItemVOTVdrive.getTarget(getItem(programTo(currentStage)), level);

        RocketStruct next = BlockEntityMachineRocketAssembly.assemble(this, SLOT_CAPSULE);
        if (!next.equals(rocket)) rocket = next;

        if (rocket.validate()) updateTanks();

        for (FluidTankNTM tank : tanks) tank.loadTank(SLOT_FUEL_IN, SLOT_FUEL_OUT, inventory);
        if (getItem(SLOT_FUEL_IN).is(ModItems.ROCKET_FUEL.get()) && solidLevel < solidMax) {
            removeItem(SLOT_FUEL_IN, 1);
            solidLevel = Math.min(solidLevel + 250, solidMax);
        }

        if (docked != null && (docked.isRemoved() || docked.getState() == RocketState.UNDOCKING))
            docked = null;

        hasDocked = docked != null;
        hasRider = hasDocked && docked.getFirstPassenger() != null;

        networkPackNT(250);
    }

    @Override
    public void tickClient() {
        if (!CelestialBody.inOrbit(level)) return;

        prevRot = rot;
        rot = hasDocked ? Math.min(rot + 2.25F, 90) : Math.max(rot - 2.25F, 0);
    }

    private void updateTanks() {
        TankPlan plan = BlockEntityLaunchPadRocket.planTanks(rocket, tanks, false);
        for (int i = 0; i < tanks.length; i++) {
            if (tanks[i] != plan.tanks().get(i)) tanks[i] = plan.tanks().get(i);
        }
        solidMax = plan.solid();
    }

    public void enterCapsule(Player player) {
        if (docked == null || docked.getFirstPassenger() != null) return;
        docked.interact(player, InteractionHand.MAIN_HAND, docked.position());
    }

    @Override
    public void dockRocket(EntityRideableRocket rocket) {
        docked = rocket;
    }

    public @Nullable EntityRideableRocket getDocked() {
        return docked;
    }

    private boolean hasDrive() {
        return getItem(SLOT_DRIVE).getItem() instanceof ItemVOTVdrive;
    }

    public boolean canLaunch() {
        return rocket.validate()
                && hasDrive()
                && BlockEntityLaunchPadRocket.areTanksFull(tanks, solidLevel, solidMax)
                && BlockEntityLaunchPadRocket.canReachDestination(
                        rocket, getItem(SLOT_DRIVE), level, worldPosition);
    }

    public void launch(@Nullable Player player) {
        if (!canLaunch()) return;

        EntityRideableRocket entity =
                new EntityRideableRocket(
                                level,
                                worldPosition.getX() + 0.5F,
                                worldPosition.getY() + 1.5F,
                                worldPosition.getZ() + 0.5F,
                                ItemCustomRocket.build(rocket))
                        .withProgram(getItem(SLOT_DRIVE))
                        .launchedBy(player);
        entity.setPos(entity.getX(), entity.getY() - entity.getBbHeight(), entity.getZ());
        level.addFreshEntity(entity);

        for (int i = 0; i < tanks.length; i++) tanks[i] = new FluidTankNTM(EMPTY_TANK);
        solidLevel = solidMax = 0;

        setItem(SLOT_DRIVE, ItemStack.EMPTY);
        for (int i = SLOT_CAPSULE; i < SLOT_COUNT; i++) setItem(i, ItemStack.EMPTY);

        dockRocket(entity);
    }

    public List<Component> findIssues() {
        List<Component> issues = new ArrayList<>();
        if (!rocket.validate()) return issues;

        BlockEntityLaunchPadRocket.findTankIssues(issues, tanks, solidLevel, solidMax);
        ItemStack drive = getItem(SLOT_DRIVE);
        if (BlockEntityLaunchPadRocket.findDriveIssues(issues, rocket, drive)) return issues;

        Target from = CelestialBody.getTarget(level, worldPosition.getX(), worldPosition.getZ());
        Target to = ItemVOTVdrive.getTarget(drive, level);
        BlockEntityLaunchPadRocket.findTravelIssues(issues, rocket, from, to);

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
            case SLOT_DRIVE -> stack.getItem() instanceof ItemVOTVdrive;
            case SLOT_FUEL_IN -> stack.is(ModItems.ROCKET_FUEL.get());
            default -> false;
        };
    }

    @Override
    public boolean hasPermission(Player player) {
        return stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {}

    @Override
    public void receiveControl(@Nullable Player player, CompoundTag data) {
        if (data.getBooleanOr("launch", false)) launch(player);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
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
        solidLevel = input.getIntOr("solid", 0);
        solidMax = input.getIntOr("maxSolid", 0);
        for (int i = 0; i < tanks.length; i++) {
            FluidTankNTM tank = new FluidTankNTM(EMPTY_TANK);
            input.child("t" + i).ifPresent(tank::deserialize);
            tanks[i] = tank;
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
        return new MenuOrbitalStationLauncher(containerId, playerInventory, this);
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
                hasDocked = input.readBoolean();
                hasRider = input.readBoolean();
            }
            case 1 -> {
                solidLevel = input.readInt();
                solidMax = input.readInt();
            }
            case 2 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            case 3 ->
                    rocket =
                            RocketStruct.STREAM_CODEC.decode(
                                    new RegistryFriendlyByteBuf(input, level.registryAccess()));
            default -> throw new IllegalArgumentException();
        }
    }
}
