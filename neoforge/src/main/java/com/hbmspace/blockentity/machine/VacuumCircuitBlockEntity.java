/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
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
import com.hbm.api.energymk2.EnergyItem;
import com.hbm.api.energymk2.ItemEnergyTransfer;
import com.hbm.blockentity.MachineBlockEntity;
import com.hbm.blockentity.UpgradeInfoProvider;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.inventory.recipes.ingredient.CountIngredient;
import com.hbm.inventory.recipes.loader.Outputs;
import com.hbm.items.machine.upgrade.MachineUpgradeItem;
import com.hbm.items.machine.upgrade.UpgradeInfo;
import com.hbm.items.machine.upgrade.UpgradeLimits;
import com.hbm.items.machine.upgrade.UpgradeManager;
import com.hbm.items.machine.upgrade.UpgradeType;
import com.hbm.packet.SyncUnitSchema;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.sound.ModSounds;
import com.hbm.sound.WorldSounds;
import com.hbm.util.TickPhase;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.inventory.container.VacuumCircuitMenu;
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

public class VacuumCircuitBlockEntity extends MachineBlockEntity
        implements EnergyHandler, MenuProvider, UpgradeInfoProvider, SyncUnitSchema {

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
    private static final UpgradeLimits VALID_UPGRADES =
            UpgradeLimits.of(UpgradeType.SPEED, 3).and(UpgradeType.POWER, 3);

    private final UpgradeManager upgradeManager = new UpgradeManager(this);
    public long power;
    public long maxPower;

    {
        hbm$syncSet$VacuumCircuitBlockEntity$maxPower(DEFAULT_MAX_POWER);
    }

    public long consumption;
    public int progress;
    public int processTime;

    {
        hbm$syncSet$VacuumCircuitBlockEntity$processTime(1);
    }

    public boolean canOperate;

    {
        hbm$syncSet$VacuumCircuitBlockEntity$canOperate(true);
    }

    public ItemStack display = ItemStack.EMPTY;
    private @Nullable VacuumCircuitRecipe recipe;

    public VacuumCircuitBlockEntity(BlockPos pos, BlockState state) {
        super(IndustryBlockEntitiesSpace.VACUUM_CIRCUIT.get(), pos, state, SLOT_COUNT);
    }

    @Override
    public void tickServer() {
        ServerLevel server = (ServerLevel) level;
        CBT_Atmosphere atmosphere =
                ChunkAtmosphereManager.proxy.getAtmosphere(server, worldPosition);
        hbm$syncSet$VacuumCircuitBlockEntity$canOperate(
                atmosphere == null || atmosphere.getPressure() <= MAX_PRESSURE);

        hbm$syncSet$VacuumCircuitBlockEntity$power(
                (long)
                        (power
                                + (ItemEnergyTransfer.extract(
                                        this, SLOT_BATTERY, maxPower - power, false))));
        hbm$syncSet$VacuumCircuitBlockEntity$recipe(
                VacuumCircuitRecipes.INSTANCE.getRecipe(this, WAFER_SLOTS, PCB_SLOTS));

        upgradeManager.scan(this, SLOT_UPGRADE_START, SLOT_UPGRADE_END);
        int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
        int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);

        long intendedMaxPower;
        if (recipe != null) {
            int duration = recipe.duration;
            hbm$syncSet$VacuumCircuitBlockEntity$processTime(
                    duration - duration * speedLevel / 6 + duration * powerLevel / 3);
            hbm$syncSet$VacuumCircuitBlockEntity$consumption(
                    recipe.power + recipe.power * speedLevel - recipe.power * powerLevel / 6);
            intendedMaxPower = recipe.power * 20L;

            if (canProcess(recipe)) {
                hbm$syncSet$VacuumCircuitBlockEntity$progress((int) (progress + 1));
                hbm$syncSet$VacuumCircuitBlockEntity$power((long) (power - (consumption)));
                if (progress >= processTime) {
                    hbm$syncSet$VacuumCircuitBlockEntity$progress(0);
                    consumeAndProduce(recipe);
                }

                if (TickPhase.every(this, SharedConstants.TICKS_PER_SECOND)) {
                    Direction dir = MultiblockCoreBlock.coreFacing(getBlockState());
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
                hbm$syncSet$VacuumCircuitBlockEntity$progress(0);
            }
        } else {
            hbm$syncSet$VacuumCircuitBlockEntity$progress(0);
            hbm$syncSet$VacuumCircuitBlockEntity$consumption(IDLE_CONSUMPTION);
            intendedMaxPower = DEFAULT_MAX_POWER;
        }

        hbm$syncSet$VacuumCircuitBlockEntity$maxPower(Math.max(intendedMaxPower, power));
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
                && MachineUpgradeItem.isUpgrade(stack)) {
            WorldSounds.machine(
                    level,
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
        if (slot == SLOT_BATTERY) return EnergyItem.matches(stack);
        if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END)
            return MachineUpgradeItem.isUpgrade(stack);
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
        hbm$syncSet$VacuumCircuitBlockEntity$power(p);
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public UpgradeLimits upgradeCaps() {
        return VALID_UPGRADES;
    }

    @Override
    public void appendUpgradeInfo(UpgradeType type, int level, List<Component> tooltip) {
        UpgradeInfo.soldering(type, level, tooltip);
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
        return new VacuumCircuitMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("power").ifPresent(v -> hbm$syncSet$VacuumCircuitBlockEntity$power(v));
        input.getLong("maxPower").ifPresent(v -> hbm$syncSet$VacuumCircuitBlockEntity$maxPower(v));
        input.getInt("progress").ifPresent(v -> hbm$syncSet$VacuumCircuitBlockEntity$progress(v));
        input.getInt("processTime")
                .ifPresent(v -> hbm$syncSet$VacuumCircuitBlockEntity$processTime(v));
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
            case 0 -> hbm$syncSet$VacuumCircuitBlockEntity$power(input.readLong());
            case 1 -> hbm$syncSet$VacuumCircuitBlockEntity$maxPower(input.readLong());
            case 2 -> hbm$syncSet$VacuumCircuitBlockEntity$consumption(input.readLong());
            case 3 -> hbm$syncSet$VacuumCircuitBlockEntity$progress(input.readInt());
            case 4 -> hbm$syncSet$VacuumCircuitBlockEntity$processTime(input.readInt());
            case 5 -> hbm$syncSet$VacuumCircuitBlockEntity$canOperate(input.readBoolean());
            case 6 -> readDisplay(input);
            default -> throw new IllegalArgumentException();
        }
    }

    public final void hbm$syncSet$VacuumCircuitBlockEntity$power(long next) {
        if (this.power != next) {
            this.power = next;
            if ((1L << 0) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 0);
        }
    }

    public final void hbm$syncSet$VacuumCircuitBlockEntity$maxPower(long next) {
        if (this.maxPower != next) {
            this.maxPower = next;
            if ((1L << 1) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 1);
        }
    }

    public final void hbm$syncSet$VacuumCircuitBlockEntity$consumption(long next) {
        if (this.consumption != next) {
            this.consumption = next;
            if ((1L << 2) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 2);
        }
    }

    public final void hbm$syncSet$VacuumCircuitBlockEntity$progress(int next) {
        if (this.progress != next) {
            this.progress = next;
            if ((1L << 3) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 3);
        }
    }

    public final void hbm$syncSet$VacuumCircuitBlockEntity$processTime(int next) {
        if (this.processTime != next) {
            this.processTime = next;
            if ((1L << 4) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 4);
        }
    }

    public final void hbm$syncSet$VacuumCircuitBlockEntity$canOperate(boolean next) {
        if (this.canOperate != next) {
            this.canOperate = next;
            if ((1L << 5) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 5);
        }
    }

    public final void hbm$syncSet$VacuumCircuitBlockEntity$recipe(VacuumCircuitRecipe next) {
        if (this.recipe != next) {
            boolean bound = syncBound();
            if (bound) com.hbm.packet.SyncBindings.unbind(this, this.recipe);
            this.recipe = next;
            if (bound) bindSyncValue(next, 3, 1L << 6);
            if ((1L << 6) == 0L) syncChanged(3);
            else syncUnitsChanged(3, 1L << 6);
        }
    }

    static {
        com.hbm.packet.SyncBindings.field(VacuumCircuitBlockEntity.class, "recipe", 3, 1L << 6);
        com.hbm.inventory.container.SyncedData.fields(
                VacuumCircuitBlockEntity.class,
                "consumption",
                "maxPower",
                "power",
                "processTime",
                "progress");
    }
}
