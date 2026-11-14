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

import com.hbm.api.energymk2.EnergyHandler;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankHandler;
import com.hbm.api.fluidmk2.FlushFaces;
import com.hbm.api.fluidmk2.FlushLanes;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankAccess;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.InventoryUtil;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.blocks.machine.HydroponicBlock;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereHandler;
import com.hbmspace.inventory.container.HydroponicMenu;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HydroponicBlockEntity extends MachineBlockEntity
        implements FluidTankAccess, EnergyHandler, FluidTankHandler, MenuProvider, SyncUnitSchema {

    public static final int SLOT_PLANT = 0;
    public static final int SLOT_FERTILIZER = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_OUTPUT_START = 3;
    public static final int SLOT_COUNT = 6;
    private static final int[] ACCESSIBLE_SLOTS = {0, 1, 2, 3, 4, 5};

    public static final long MAX_POWER = 2_000L;
    public static final long DRAIN = 25L;
    public static final long LIGHT_POWER = 200L;
    public static final int MIN_CO2 = 100;
    public static final int MAX_FERTILIZER = 200;
    private static final String[] TANK_KEYS = {"t0", "t1"};

    public final FluidTankNTM[] tanks = new FluidTankNTM[2];
    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;
    public long power;
    public int fertilizer;

    private boolean lightsOn;
    private final int[] prevAge = new int[3];
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public HydroponicBlockEntity(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.HYDROBAY.get(), pos, state, SLOT_COUNT);
        hbm$syncArraySet$HydroponicBlockEntity$tanks(
                tanks, 0, new FluidTankNTM(NTMFluids.CARBONDIOXIDE, 16_000));
        hbm$syncArraySet$HydroponicBlockEntity$tanks(
                tanks, 1, new FluidTankNTM(NTMFluids.OXYGEN, 16_000));
        receiving = new FluidTankNTM[] {tanks[0]};
        sending = new FluidTankNTM[] {tanks[1]};
    }

    public static boolean isPlantable(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item
                && item.getBlock() instanceof VegetationBlock;
    }

    public static int fertilizerStrength(ItemStack stack) {
        if (stack.is(Items.BONE_MEAL)) return 1;
        if (stack.is(ModItems.POWDER_FERTILIZER.get())) return 9;
        return 0;
    }

    public static List<ItemStack> validFertilizers() {
        return List.of(
                new ItemStack(Items.BONE_MEAL), new ItemStack(ModItems.POWDER_FERTILIZER.get()));
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long p) {
        hbm$syncSet$HydroponicBlockEntity$power(Math.max(0L, Math.min(p, MAX_POWER)));
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return receiving;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return sending;
    }

    @Override
    public void declareFlush(FlushLanes out) {
        for (FluidTankNTM tank : getSendingTanks()) out.add(tank, FlushFaces.own());
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
    public void tickServer() {
        ServerLevel server = (ServerLevel) level;
        Direction facing = getBlockState().getValue(MultiblockCoreBlock.FACING);
        Direction rot = facing.getClockWise();
        long prevPower = power;
        int prevFertilizer = fertilizer;
        int prevCo2 = tanks[0].getFill();

        flush.provide(server, this);

        if (power > 0) hbm$syncSet$HydroponicBlockEntity$power(Math.max(power - DRAIN, 0));

        ItemStack feed = inventory.get(SLOT_FERTILIZER);
        int strength = fertilizerStrength(feed);
        if (strength > 0 && fertilizer <= MAX_FERTILIZER - strength) {
            feed.shrink(1);
            fertilizer += strength;
        }

        hbm$syncSet$HydroponicBlockEntity$power(
                (long)
                        (power
                                + (ItemEnergyTransfer.extract(
                                        this, SLOT_BATTERY, MAX_POWER - power, false))));

        boolean lit = power >= LIGHT_POWER;
        if (lit != lightsOn) {
            ((HydroponicBlock) getBlockState().getBlock())
                    .setLights(server, worldPosition, facing, lit);
            lightsOn = lit;
            setChanged();
        }

        for (int i = 0; i < prevAge.length; i++)
            grow(server, i, worldPosition.relative(rot, i - 1).above());

        if (power != prevPower || fertilizer != prevFertilizer || tanks[0].getFill() != prevCo2)
            setChanged();
        networkPackNT(15);
    }

    private void grow(ServerLevel server, int i, BlockPos spot) {
        BlockState state = server.getBlockState(spot);
        if (power < LIGHT_POWER || tanks[0].getFill() < MIN_CO2) {
            if (state.getBlock() instanceof CropBlock crop) {
                server.setBlock(spot, crop.getStateForAge(prevAge[i]), Block.UPDATE_CLIENTS);
            }
            return;
        }

        if (!(state.getBlock() instanceof CropBlock)) {
            ItemStack seed = inventory.get(SLOT_PLANT);
            CropBlock planted = HydroponicBlock.hydroponicCrop(seed);
            if (planted == null) return;
            BlockState sown = planted.defaultBlockState();
            server.setBlock(spot, sown, Block.UPDATE_ALL);
            prevAge[i] = 0;
            seed.shrink(1);
            setChanged();
        }

        RandomSource random = server.getRandom();
        if (random.nextInt(120) == 0) server.getBlockState(spot).randomTick(server, spot, random);

        state = server.getBlockState(spot);
        if (!(state.getBlock() instanceof CropBlock crop)) return;
        boolean grown = false;
        if (crop.isValidBonemealTarget(server, spot, state)) {
            if (fertilizer > 0 && random.nextInt(60) == 0) {
                if (crop.isBonemealSuccess(server, random, spot, state)) {
                    crop.performBonemeal(server, random, spot, state);
                    server.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, spot, 15);
                }
                fertilizer--;
            }
        } else {
            grown = true;
        }

        state = server.getBlockState(spot);
        int age = crop.getAge(state);
        if (age != prevAge[i]) {
            int converted =
                    Math.max(age - prevAge[i], 0) * ChunkAtmosphereHandler.CROP_GROWTH_CONVERSION;
            tanks[0].setFill(Math.max(tanks[0].getFill() - converted, 0));
            tanks[1].setFill(Math.min(tanks[1].getFill() + converted, tanks[1].getMaxFill()));
            prevAge[i] = age;
        }

        if (grown && harvest(Block.getDrops(state, server, spot, null))) {
            server.setBlock(spot, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            setChanged();
        }
    }

    private boolean harvest(List<ItemStack> drops) {
        int last = SLOT_COUNT - 1;
        if (!InventoryUtil.doesArrayHaveSpace(
                inventory, SLOT_OUTPUT_START, last, drops.toArray(ItemStack[]::new))) {
            return false;
        }
        for (ItemStack drop : drops)
            InventoryUtil.tryAddItemToInventory(inventory, SLOT_OUTPUT_START, last, drop);
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_PLANT) return isPlantable(stack);
        if (slot == SLOT_FERTILIZER) return fertilizerStrength(stack) > 0;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ACCESSIBLE_SLOTS;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot >= SLOT_OUTPUT_START;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.hydrobay");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new HydroponicMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < tanks.length; i++)
            input.child(TANK_KEYS[i]).ifPresent(tanks[i]::deserialize);
        for (int i = 0; i < prevAge.length; i++) prevAge[i] = input.getIntOr("p" + i, 0);
        hbm$syncSet$HydroponicBlockEntity$power(input.getLongOr("power", 0L));
        lightsOn = input.getBooleanOr("lights", false);
        fertilizer = input.getIntOr("fertilizer", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < tanks.length; i++) tanks[i].serialize(output.child(TANK_KEYS[i]));
        for (int i = 0; i < prevAge.length; i++) output.putInt("p" + i, prevAge[i]);
        output.putLong("power", power);
        output.putBoolean("lights", lightsOn);
        output.putInt("fertilizer", fertilizer);
    }

    @Override
    public long syncUnitMask() {
        return 0x3L;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetSerialize(output);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> hbm$syncSet$HydroponicBlockEntity$power(input.readLong());
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public final FluidTankNTM hbm$syncArraySet$HydroponicBlockEntity$tanks(
            FluidTankNTM[] array, int index, FluidTankNTM next) {
        if (array[index] != next) {
            if (array == this.tanks && syncBound())
                com.hbm.packet.SyncBindings.unbind(this, array[index]);
            array[index] = next;
            if (array == this.tanks) {
                syncArrayChanged(array, index, 3, 1L << 1);
            }
        }
        return next;
    }

    public final void hbm$syncSet$HydroponicBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(HydroponicBlockEntity.class, "tanks", 3, 1L << 1);
        com.hbm.inventory.container.SyncedData.fields(
                HydroponicBlockEntity.class, "fertilizer", "power");
    }
}
