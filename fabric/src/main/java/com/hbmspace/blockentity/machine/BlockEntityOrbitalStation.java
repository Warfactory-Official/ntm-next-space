// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncSlots;
import com.hbm.packet.SyncUnitSchema;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.blocks.StationBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.entity.missile.EntityRideableRocket.RocketState;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.entity.missile.RocketDock;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.inventory.fluid.trait.FT_Rocket;
import com.hbmspace.items.weapon.ItemCustomRocket;
import io.netty.buffer.ByteBuf;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Utf8String;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

@SyncSlots(all = true, units = 1L << 2, components = false)
public class BlockEntityOrbitalStation extends BlockEntityMachineBase
        implements FluidTankEndpoint, SyncUnitSchema, RocketDock {

    private static final long STATE_UNIT = 1L << 0;
    private static final long STATION_UNIT = 1L << 1;
    private static final long SLOTS_UNIT = 1L << 2;
    private static final long TANKS_UNIT = 1L << 3;

    private @Nullable OrbitalStation station;
    private @Nullable EntityRideableRocket docked;

    @SyncField(units = TANKS_UNIT)
    public final FluidTankNTM[] tanks = {new FluidTankNTM(FluidsSpace.hydrazine(), 16_000)};

    public boolean isReserved = false;

    @SyncField(units = STATE_UNIT)
    public boolean hasDocked = false;

    @SyncField(units = STATE_UNIT)
    public boolean hasRider = false;

    @SyncField(units = STATE_UNIT)
    public boolean needsFuel = false;

    @SyncField(units = STATE_UNIT)
    public boolean hasFuel = false;

    @SyncField(units = STATION_UNIT)
    private @Nullable StationView view;

    public float rot;
    public float prevRot;

    public BlockEntityOrbitalStation(BlockPos pos, BlockState state) {
        super(StationBlockEntitiesSpace.ORBITAL_STATION.get(), pos, state, 16);
    }

    @Override
    public void tickServer() {
        if (!CelestialBody.inOrbit(level)) return;

        station = OrbitalStation.getStationFromPosition(worldPosition.getX(), worldPosition.getZ());

        if (isCore()) station.update(level);
        station.addPort(this);

        if (docked != null && docked.isReusable()) {
            int fillRequirement = getFillRequirement(false);
            for (FluidTankNTM tank : tanks)
                tank.changeTankSize(Math.max(fillRequirement, tank.getFill()));
        }

        if (docked != null && (docked.isRemoved() || docked.getState() == RocketState.UNDOCKING)) {
            if (!docked.isRemoved() && docked.isReusable()) {
                boolean toOrbit = docked.getTarget().inOrbit();
                for (FluidTankNTM tank : tanks) {
                    tank.changeTankSize(Math.max(0, tank.getFill() - getFillRequirement(toOrbit)));
                }
            }

            undockRocket();
        }

        if (docked != null && docked.isReusable()) {
            boolean fuelled = hasSufficientFuel(docked.getTarget().inOrbit());

            if (fuelled && docked.getState() == RocketState.NEEDSFUEL) {
                docked.setState(
                        !docked.navDrive.isEmpty() ? RocketState.AWAITING : RocketState.LANDED);
            } else if (!fuelled && docked.getState() != RocketState.NEEDSFUEL) {
                docked.setState(RocketState.NEEDSFUEL);
            }
        }

        hasDocked = docked != null;
        hasRider = hasDocked && docked.getFirstPassenger() != null;
        needsFuel = hasDocked && docked.isReusable();
        hasFuel = needsFuel && hasSufficientFuel(docked.getTarget().inOrbit());

        if (hasDocked) isReserved = false;

        if (isCore()) {
            StationView next = StationView.of(station);
            if (!next.equals(view)) view = next;
        }

        networkPackNT(OrbitalStation.STATION_SIZE / 2);
    }

    @Override
    public void tickClient() {
        if (!CelestialBody.inOrbit(level)) return;

        if (isCore() && station != null) station.update(level);

        prevRot = rot;
        if (hasDocked) {
            rot += 2.25F;
            if (rot > 90) rot = 90;
        } else {
            rot -= 2.25F;
            if (rot < 0) rot = 0;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (level != null && !level.isClientSide() && station != null && !isCore())
            station.removePort(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (docked != null) docked.dropNDie();
    }

    public boolean isCore() {
        return getBlockState().is(StationBlocksSpace.ORBITAL_STATION.get());
    }

    public void reservePort() {
        isReserved = true;
    }

    public void enterCapsule(Player player) {
        if (docked == null || docked.getFirstPassenger() != null) return;
        docked.interact(player, InteractionHand.MAIN_HAND, docked.position());
    }

    @Override
    public void dockRocket(EntityRideableRocket rocket) {
        despawnRocket();

        docked = rocket;
    }

    public void undockRocket() {
        docked = null;
    }

    public void despawnRocket() {
        if (docked == null) return;

        Deque<ItemStack> itemsToStuff = new ArrayDeque<>();

        RocketStruct rocket = docked.getRocket();
        if (!rocket.stages.isEmpty()) {
            itemsToStuff.push(ItemCustomRocket.build(rocket, true));
        } else {
            itemsToStuff.push(new ItemStack(rocket.capsule.item));
        }

        if (!docked.navDrive.isEmpty()) itemsToStuff.push(docked.navDrive.copy());

        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).isEmpty()) {
                inventory.set(i, itemsToStuff.pop());
                if (itemsToStuff.isEmpty()) break;
            }
        }
        setChanged();

        docked.discard();
        docked = null;
    }

    public void spawnRocket(ItemStack stack) {
        EntityRideableRocket rocket =
                new EntityRideableRocket(
                        level,
                        worldPosition.getX() + 0.5F,
                        worldPosition.getY() + 1.5F,
                        worldPosition.getZ() + 0.5F,
                        stack);
        rocket.setPos(rocket.getX(), rocket.getY() - rocket.getBbHeight(), rocket.getZ());
        rocket.setState(rocket.isReusable() ? RocketState.NEEDSFUEL : RocketState.LANDED);
        level.addFreshEntity(rocket);

        dockRocket(rocket);
    }

    public @Nullable EntityRideableRocket getDocked() {
        return docked;
    }

    public boolean hasSufficientFuel(boolean toOrbit) {
        int fillRequirement = getFillRequirement(toOrbit);
        for (FluidTankNTM tank : tanks) {
            if (tank.getFill() < fillRequirement) return false;
        }

        return true;
    }

    public int getFillRequirement(boolean toOrbit) {
        if (toOrbit) return 500;

        int mass = docked != null ? docked.getRocket().getLaunchMass() : 4_000;

        FT_Rocket fuelTrait = NTMFluidProperties.getTrait(tanks[0].getTankType(), FT_Rocket.class);
        int thrust = fuelTrait != null ? (int) fuelTrait.getThrust() : 600_000;
        int isp = fuelTrait != null ? fuelTrait.getISP() : 300;

        return SolarSystem.getCostBetween(
                station.orbiting, station.orbiting, mass, thrust, isp, false, true);
    }

    public boolean hasStoredItems() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }

    public void giveStoredItems(Player player) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                inventory.set(i, ItemStack.EMPTY);
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
        setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] slots = new int[inventory.size()];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("t0").ifPresent(tanks[0]::deserialize);
        tanks[0].setTankType(FluidsSpace.hydrazine());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tanks[0].serialize(output.child("t0"));
    }

    @Override
    public long syncUnitMask() {
        return super.syncUnitMask() | STATE_UNIT | STATION_UNIT | SLOTS_UNIT | TANKS_UNIT;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> {
                output.writeBoolean(hasDocked);
                output.writeBoolean(hasRider);
                output.writeBoolean(needsFuel);
                output.writeBoolean(hasFuel);
            }
            case 1 -> {
                output.writeBoolean(view != null);
                if (view != null) view.write(output);
            }
            case 2 -> {
                for (ItemStack stack : inventory) {
                    output.writeInt(
                            stack.isEmpty() ? -1 : BuiltInRegistries.ITEM.getId(stack.getItem()));
                }
            }
            case 3 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> super.writeSyncUnit(unit, output);
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> {
                hasDocked = input.readBoolean();
                hasRider = input.readBoolean();
                needsFuel = input.readBoolean();
                hasFuel = input.readBoolean();
            }
            case 1 -> {
                if (input.readBoolean())
                    OrbitalStation.clientStation = station = StationView.read(input).toStation();
            }
            case 2 -> {
                for (int i = 0; i < inventory.size(); i++) {
                    int id = input.readInt();
                    inventory.set(
                            i,
                            id < 0
                                    ? ItemStack.EMPTY
                                    : new ItemStack(BuiltInRegistries.ITEM.byId(id)));
                }
            }
            case 3 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> super.readSyncUnit(unit, input);
        }
    }

    private record StationView(
            String orbiting,
            String target,
            int state,
            int stateTimer,
            int maxStateTimer,
            boolean hasEngines,
            float gravityMultiplier,
            String name,
            List<BlockPos> errorsAt) {

        static StationView of(OrbitalStation station) {
            boolean orbit = station.state == StationState.ORBIT;
            return new StationView(
                    station.orbiting.name,
                    station.target.name,
                    station.state.ordinal(),
                    orbit ? 0 : station.stateTimer,
                    orbit ? 0 : station.maxStateTimer,
                    station.hasEngines,
                    station.gravityMultiplier,
                    station.name,
                    List.copyOf(station.errorsAt));
        }

        void write(ByteBuf buf) {
            Utf8String.write(buf, orbiting, Short.MAX_VALUE);
            Utf8String.write(buf, target, Short.MAX_VALUE);
            buf.writeInt(state);
            buf.writeInt(stateTimer);
            buf.writeInt(maxStateTimer);
            buf.writeBoolean(hasEngines);
            buf.writeFloat(gravityMultiplier);
            Utf8String.write(buf, name, Short.MAX_VALUE);
            buf.writeInt(errorsAt.size());
            for (BlockPos error : errorsAt) buf.writeLong(error.asLong());
        }

        static StationView read(ByteBuf buf) {
            String orbiting = Utf8String.read(buf, Short.MAX_VALUE);
            String target = Utf8String.read(buf, Short.MAX_VALUE);
            int state = buf.readInt();
            int stateTimer = buf.readInt();
            int maxStateTimer = buf.readInt();
            boolean hasEngines = buf.readBoolean();
            float gravity = buf.readFloat();
            String name = Utf8String.read(buf, Short.MAX_VALUE);
            int count = buf.readInt();
            BlockPos[] errors = new BlockPos[count];
            for (int i = 0; i < count; i++) errors[i] = BlockPos.of(buf.readLong());
            return new StationView(
                    orbiting,
                    target,
                    state,
                    stateTimer,
                    maxStateTimer,
                    hasEngines,
                    gravity,
                    name,
                    List.of(errors));
        }

        OrbitalStation toStation() {
            OrbitalStation station = new OrbitalStation(CelestialBody.getBody(orbiting));
            station.target = CelestialBody.getBody(target);
            station.state = StationState.values()[state];
            station.stateTimer = stateTimer;
            station.maxStateTimer = maxStateTimer;
            station.hasEngines = hasEngines;
            station.gravityMultiplier = gravityMultiplier;
            station.name = name;
            station.errorsAt = new ArrayList<>(errorsAt);
            return station;
        }
    }
}
