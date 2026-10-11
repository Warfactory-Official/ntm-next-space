// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.container.SyncedData;
import com.hbm.inventory.recipes.ingredient.CountIngredient;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.inventory.slot.SlotRecipeOutput;
import com.hbm.inventory.slot.SlotUpgrade;
import com.hbm.items.machine.upgrade.ItemMachineUpgrade;
import com.hbmspace.blockentity.machine.BlockEntityMachineVacuumCircuit;
import com.hbmspace.inventory.recipes.VacuumCircuitRecipes;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MenuMachineVacuumCircuit extends BlockEntityMenu<BlockEntityMachineVacuumCircuit> {

    private final SyncedData data;

    public MenuMachineVacuumCircuit(
            int containerId, Inventory playerInv, BlockEntityMachineVacuumCircuit be) {
        this(
                containerId,
                playerInv,
                be,
                playerInv.player.level().isClientSide()
                        ? SyncedData.client(BlockEntityMachineVacuumCircuit.class)
                        : SyncedData.of(be),
                be);
    }

    private MenuMachineVacuumCircuit(
            int containerId,
            Inventory playerInv,
            Container container,
            SyncedData data,
            BlockEntityMachineVacuumCircuit be) {
        super(IndustryMenusSpace.MACHINE_VACUUM_CIRCUIT.get(), containerId, be, container);
        checkContainerSize(container, BlockEntityMachineVacuumCircuit.SLOT_COUNT);
        this.data = data;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++)
                addSlot(SlotFiltered.gated(container, i * 2 + j, 26 + j * 18, 18 + i * 18));
        }
        addSlot(
                new SlotRecipeOutput(
                        playerInv.player,
                        container,
                        BlockEntityMachineVacuumCircuit.SLOT_OUTPUT,
                        98,
                        27));
        addSlot(
                new SlotFiltered(
                        container,
                        BlockEntityMachineVacuumCircuit.SLOT_BATTERY,
                        143,
                        72,
                        IBatteryItem::isBattery));
        addSlot(
                new SlotUpgrade(
                        container, BlockEntityMachineVacuumCircuit.SLOT_UPGRADE_START, 89, 72));
        addSlot(
                new SlotUpgrade(
                        container, BlockEntityMachineVacuumCircuit.SLOT_UPGRADE_END, 107, 72));

        addStandardInventorySlots(playerInv, 8, 122);
        addDataSlots(data);
    }

    private static boolean matchesAny(List<CountIngredient> group, ItemStack stack) {
        for (CountIngredient ing : group) if (ing.ingredient().test(stack)) return true;
        return false;
    }

    public int getProgress() {
        return data.getInt("progress");
    }

    public int getProcessTime() {
        return data.getInt("processTime");
    }

    public long getPower() {
        return data.get("power");
    }

    public long getMaxPower() {
        return data.get("maxPower");
    }

    public long getConsumption() {
        return data.get("consumption");
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMove(
                player,
                index,
                true,
                stack -> {
                    int machineEnd = BlockEntityMachineVacuumCircuit.SLOT_COUNT;
                    if (index < machineEnd)
                        return moveItemStackTo(stack, machineEnd, slots.size(), true);
                    if (IBatteryItem.isBattery(stack))
                        return moveItemStackTo(
                                stack,
                                BlockEntityMachineVacuumCircuit.SLOT_BATTERY,
                                BlockEntityMachineVacuumCircuit.SLOT_BATTERY + 1,
                                false);
                    if (ItemMachineUpgrade.isUpgrade(stack))
                        return moveItemStackTo(
                                stack,
                                BlockEntityMachineVacuumCircuit.SLOT_UPGRADE_START,
                                BlockEntityMachineVacuumCircuit.SLOT_UPGRADE_END + 1,
                                false);
                    if (matchesAny(VacuumCircuitRecipes.wafer(), stack))
                        return moveItemStackTo(stack, 0, 2, false);
                    if (matchesAny(VacuumCircuitRecipes.pcb(), stack))
                        return moveItemStackTo(stack, 2, 4, false);
                    return false;
                });
    }
}
