// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.inventory.container.ContainerSync;
import com.hbm.inventory.recipes.ingredient.CountIngredient;
import com.hbm.inventory.recipes.loader.Outputs;
import com.hbm.items.machine.upgrade.ItemMachineUpgrade;
import com.hbm.items.machine.upgrade.UpgradeManager;
import com.hbm.items.machine.upgrade.UpgradeType;
import com.hbm.packet.SyncField;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.sound.ModSounds;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.inventory.container.MenuMachineVacuumCircuit;
import com.hbmspace.inventory.recipes.VacuumCircuitRecipe;
import com.hbmspace.inventory.recipes.VacuumCircuitRecipes;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityMachineVacuumCircuit extends BlockEntityMachineBase
        implements IEnergyHandlerMK2, MenuProvider, IUpgradeInfoProvider, SyncUnitSchema {

    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_BATTERY = 5;
    public static final int SLOT_UPGRADE_START = 6;
    public static final int SLOT_UPGRADE_END = 7;
    public static final int SLOT_COUNT = 8;

    public static final long DEFAULT_MAX_POWER = 2_000L;
    public static final long IDLE_CONSUMPTION = 100L;
    public static final double MAX_PRESSURE = 0.001D;

    private static final int[] WAFER_SLOTS = {0, 1};
    private static final int[] PCB_SLOTS = {2, 3};
    private static final int[] ACCESSIBLE_SLOTS = {0, 1, 2, 3, SLOT_OUTPUT};
    private static final int[] VALID_UPGRADES =
            IUpgradeInfoProvider.upgradeCaps(UpgradeType.SPEED, 3, UpgradeType.POWER, 3);

    private final UpgradeManager upgradeManager = new UpgradeManager(this);

    @SyncField(units = 1L << 0)
    @ContainerSync
    public long power;

    @SyncField(units = 1L << 1)
    @ContainerSync
    public long maxPower = DEFAULT_MAX_POWER;

    @SyncField(units = 1L << 2)
    @ContainerSync
    public long consumption;

    @SyncField(units = 1L << 3)
    @ContainerSync
    public int progress;

    @SyncField(units = 1L << 4)
    @ContainerSync
    public int processTime = 1;

    @SyncField(units = 1L << 5)
    public boolean canOperate = true;

    @SyncField(units = 1L << 6)
    private @Nullable VacuumCircuitRecipe recipe;

    public ItemStack display = ItemStack.EMPTY;

    public BlockEntityMachineVacuumCircuit(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.VACUUM_CIRCUIT.get(), pos, state, SLOT_COUNT);
    }

    @Override
    public void tickServer() {
        ServerLevel server = (ServerLevel) level;
        CBT_Atmosphere atmosphere =
                ChunkAtmosphereManager.proxy.getAtmosphere(server, worldPosition);
        canOperate = atmosphere == null || atmosphere.getPressure() <= MAX_PRESSURE;

        power += ItemEnergyTransfer.extract(this, SLOT_BATTERY, maxPower - power, false);
        recipe = VacuumCircuitRecipes.INSTANCE.getRecipe(this, WAFER_SLOTS, PCB_SLOTS);

        upgradeManager.scan(this, SLOT_UPGRADE_START, SLOT_UPGRADE_END);
        int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
        int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);

        long intendedMaxPower;
        if (recipe != null) {
            int duration = recipe.duration;
            processTime = duration - duration * speedLevel / 6 + duration * powerLevel / 3;
            consumption = recipe.power + recipe.power * speedLevel - recipe.power * powerLevel / 6;
            intendedMaxPower = recipe.power * 20L;

            if (canProcess(recipe)) {
                progress++;
                power -= consumption;
                if (progress >= processTime) {
                    progress = 0;
                    consumeAndProduce(recipe);
                }

                if (TickPhase.every(this, SharedConstants.TICKS_PER_SECOND)) {
                    Direction dir = BlockMultiblockCore.coreFacing(getBlockState());
                    Direction rot = dir.getClockWise();
                    ParticleCreators.sparks(
                            server,
                            worldPosition.getX()
                                    + 0.5
                                    + dir.getStepX() * 0.625
                                    + rot.getStepX() * 0.5,
                            worldPosition.getY() + 1.25,
                            worldPosition.getZ()
                                    + 0.5
                                    + dir.getStepZ() * 0.625
                                    + rot.getStepZ() * 0.5,
                            3,
                            false,
                            25);
                }
            } else {
                progress = 0;
            }
        } else {
            progress = 0;
            consumption = IDLE_CONSUMPTION;
            intendedMaxPower = DEFAULT_MAX_POWER;
        }

        maxPower = Math.max(intendedMaxPower, power);
        networkPackNT(25);
    }

    private boolean canProcess(VacuumCircuitRecipe recipe) {
        if (!canOperate) return false;
        if (power < consumption) return false;

        return Outputs.fits(inventory.get(SLOT_OUTPUT), recipe.outputItems(), 0);
    }

    private void consumeAndProduce(VacuumCircuitRecipe recipe) {
        consumeGroup(WAFER_SLOTS, recipe.wafer());
        consumeGroup(PCB_SLOTS, recipe.pcb);

        ItemStack out = inventory.get(SLOT_OUTPUT);
        ItemStack made = recipe.output();
        if (out.isEmpty()) {
            inventory.set(SLOT_OUTPUT, made.copy());
        } else {
            out.grow(made.getCount());
        }
        setChanged();
    }

    private void consumeGroup(int[] slots, CountIngredient[] group) {
        for (CountIngredient ing : group) {
            for (int slot : slots) {
                ItemStack stack = inventory.get(slot);
                if (ing.test(stack)) {
                    stack.shrink(ing.count());
                    break;
                }
            }
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (level != null
                && !level.isClientSide()
                && slot >= SLOT_UPGRADE_START
                && slot <= SLOT_UPGRADE_END
                && ItemMachineUpgrade.isUpgrade(stack)) {
            level.playSound(
                    null,
                    worldPosition,
                    ModSounds.UPGRADE_PLUG.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_BATTERY) return IBatteryItem.isBattery(stack);
        if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END)
            return ItemMachineUpgrade.isUpgrade(stack);
        if (slot < 2)
            return isValidForGroup(WAFER_SLOTS, slot, stack, VacuumCircuitRecipes.wafer());
        if (slot < 4) return isValidForGroup(PCB_SLOTS, slot, stack, VacuumCircuitRecipes.pcb());
        return false;
    }

    private boolean isValidForGroup(
            int[] groupSlots, int slot, ItemStack stack, List<CountIngredient> known) {
        for (int i : groupSlots) {
            if (i != slot
                    && !inventory.get(i).isEmpty()
                    && ItemStack.isSameItemSameComponents(inventory.get(i), stack)) return false;
        }
        for (CountIngredient ing : known) if (ing.ingredient().test(stack)) return true;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ACCESSIBLE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot != SLOT_OUTPUT && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT;
    }

    @Override
    public long getPower() {
        return Math.max(0L, Math.min(power, maxPower));
    }

    @Override
    public void setPower(long p) {
        power = p;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public int[] getValidUpgrades() {
        return VALID_UPGRADES;
    }

    private void writeDisplay(ByteBuf output) {
        boolean hasRecipe = recipe != null;
        output.writeBoolean(hasRecipe);
        if (hasRecipe) output.writeInt(BuiltInRegistries.ITEM.getId(recipe.previewItem()));
    }

    private void readDisplay(ByteBuf input) {
        display =
                input.readBoolean()
                        ? new ItemStack(BuiltInRegistries.ITEM.byId(input.readInt()))
                        : ItemStack.EMPTY;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineVacuumCircuit");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, Inventory playerInventory, Player player) {
        return new MenuMachineVacuumCircuit(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("power").ifPresent(v -> power = v);
        input.getLong("maxPower").ifPresent(v -> maxPower = v);
        input.getInt("progress").ifPresent(v -> progress = v);
        input.getInt("processTime").ifPresent(v -> processTime = v);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("power", power);
        output.putLong("maxPower", maxPower);
        output.putInt("progress", progress);
        output.putInt("processTime", processTime);
    }

    @Override
    public long syncUnitMask() {
        return 0x7fL;
    }

    @Override
    public void writeSyncUnit(int unit, ByteBuf output) {
        switch (unit) {
            case 0 -> output.writeLong(power);
            case 1 -> output.writeLong(maxPower);
            case 2 -> output.writeLong(consumption);
            case 3 -> output.writeInt(progress);
            case 4 -> output.writeInt(processTime);
            case 5 -> output.writeBoolean(canOperate);
            case 6 -> writeDisplay(output);
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public void readSyncUnit(int unit, ByteBuf input) {
        switch (unit) {
            case 0 -> power = input.readLong();
            case 1 -> maxPower = input.readLong();
            case 2 -> consumption = input.readLong();
            case 3 -> progress = input.readInt();
            case 4 -> processTime = input.readInt();
            case 5 -> canOperate = input.readBoolean();
            case 6 -> readDisplay(input);
            default -> throw new IllegalArgumentException();
        }
    }
}
