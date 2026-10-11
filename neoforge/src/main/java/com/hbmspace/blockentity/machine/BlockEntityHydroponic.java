// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.api.fluidmk2.FluidFlushOutputs;
import com.hbm.api.fluidmk2.FluidTankEndpoint;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.inventory.container.ContainerSync;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.util.InventoryUtil;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.blocks.machine.MachineHydroponic;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereHandler;
import com.hbmspace.inventory.container.MenuHydroponic;
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

public class BlockEntityHydroponic extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, FluidTankEndpoint, MenuProvider, SyncUnitSchema {

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

    @SyncField(units = 1L << 1)
    public final FluidTankNTM[] tanks = new FluidTankNTM[2];

    private final FluidTankNTM[] receiving;
    private final FluidTankNTM[] sending;

    @SyncField(units = 1L << 0)
    @ContainerSync
    public long power;

    @ContainerSync public int fertilizer;

    private boolean lightsOn;
    private final int[] prevAge = new int[3];
    private final FluidFlushOutputs flush = new FluidFlushOutputs();

    public BlockEntityHydroponic(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.HYDROBAY.get(), pos, state, SLOT_COUNT);
        tanks[0] = new FluidTankNTM(NTMFluids.CARBONDIOXIDE, 16_000);
        tanks[1] = new FluidTankNTM(NTMFluids.OXYGEN, 16_000);
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
        power = Math.max(0L, Math.min(p, MAX_POWER));
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
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public void tickServer() {
        ServerLevel server = (ServerLevel) level;
        Direction facing = getBlockState().getValue(BlockMultiblockCore.FACING);
        Direction rot = facing.getClockWise();
        long prevPower = power;
        int prevFertilizer = fertilizer;
        int prevCo2 = tanks[0].getFill();

        flush.provide(server, this);

        if (power > 0) power = Math.max(power - DRAIN, 0);

        ItemStack feed = inventory.get(SLOT_FERTILIZER);
        int strength = fertilizerStrength(feed);
        if (strength > 0 && fertilizer <= MAX_FERTILIZER - strength) {
            feed.shrink(1);
            fertilizer += strength;
        }

        power += ItemEnergyTransfer.extract(this, SLOT_BATTERY, MAX_POWER - power, false);

        boolean lit = power >= LIGHT_POWER;
        if (lit != lightsOn) {
            ((MachineHydroponic) getBlockState().getBlock())
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
            CropBlock planted = MachineHydroponic.hydroponicCrop(seed);
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
        return new MenuHydroponic(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < tanks.length; i++)
            input.child(TANK_KEYS[i]).ifPresent(tanks[i]::deserialize);
        for (int i = 0; i < prevAge.length; i++) prevAge[i] = input.getIntOr("p" + i, 0);
        power = input.getLongOr("power", 0L);
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
            case 0 -> power = input.readLong();
            case 1 -> {
                for (FluidTankNTM tank : tanks) tank.packetDeserialize(input);
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
