// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.container;

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.slot.SlotFiltered;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.SlotRocket.SlotCapsule;
import com.hbmspace.inventory.SlotRocket.SlotDrive;
import com.hbmspace.inventory.SlotRocket.SlotRocketPart;
import com.hbmspace.items.ItemVOTVdrive;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher.FIRST_PROGRAM;
import static com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher.SLOT_COUNT;

public class MenuOrbitalStationLauncher extends BlockEntityMenu<BlockEntityOrbitalStationLauncher> {

    private int layer;

    public MenuOrbitalStationLauncher(
            int containerId, Inventory playerInv, BlockEntityOrbitalStationLauncher be) {
        super(StationMenusSpace.ORBITAL_STATION_LAUNCHER.get(), containerId, be);
        checkContainerSize(be, SLOT_COUNT);

        addSlot(
                new SlotFiltered(
                        be,
                        BlockEntityOrbitalStationLauncher.SLOT_DRIVE,
                        42,
                        55,
                        stack -> stack.getItem() instanceof ItemVOTVdrive));
        addSlot(
                new SlotFiltered(
                        be,
                        BlockEntityOrbitalStationLauncher.SLOT_FUEL_IN,
                        42,
                        73,
                        stack ->
                                stack.is(ModItems.ROCKET_FUEL.get())
                                        || FluidTankNTM.isFluidContainer(stack)));
        addSlot(
                new SlotFiltered(
                        be,
                        BlockEntityOrbitalStationLauncher.SLOT_FUEL_OUT,
                        42,
                        91,
                        SlotFiltered.NONE));
        addSlot(new SlotCapsule(be, BlockEntityOrbitalStationLauncher.SLOT_CAPSULE, 18, 13));
        for (int i = 0; i < RocketStruct.MAX_STAGES; i++) {
            addSlot(
                    new SlotRocketPart(
                            be,
                            BlockEntityOrbitalStationLauncher.fuselage(i),
                            18,
                            44,
                            i,
                            this::layer,
                            PartType.FUSELAGE));
            addSlot(
                    new SlotRocketPart(
                            be,
                            BlockEntityOrbitalStationLauncher.fins(i),
                            18,
                            62,
                            i,
                            this::layer,
                            PartType.FINS));
            addSlot(
                    new SlotRocketPart(
                            be,
                            BlockEntityOrbitalStationLauncher.thruster(i),
                            18,
                            80,
                            i,
                            this::layer,
                            PartType.THRUSTER));
        }
        for (int i = 0; i < RocketStruct.MAX_STAGES; i++) {
            addSlot(
                    new SlotDrive(
                            be,
                            BlockEntityOrbitalStationLauncher.programFrom(i),
                            161,
                            54,
                            i,
                            this::layer));
            addSlot(
                    new SlotDrive(
                            be,
                            BlockEntityOrbitalStationLauncher.programTo(i),
                            170,
                            87,
                            i,
                            this::layer));
        }

        addStandardInventorySlots(playerInv, 8, 142);
    }

    public int layer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
        blockEntity().currentStage = layer;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= RocketStruct.MAX_STAGES) return false;
        setLayer(id);
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        if (slotId >= SLOT_COUNT && input == ContainerInput.QUICK_MOVE) return;

        if (slotId >= FIRST_PROGRAM && slotId < SLOT_COUNT) {
            Slot slot = slots.get(slotId);
            ItemStack held = getCarried();
            if (!held.isEmpty() && !slot.mayPlace(held)) return;
            slot.set(held.isEmpty() ? ItemStack.EMPTY : held.copyWithCount(1));
            slot.setChanged();
            return;
        }

        super.clicked(slotId, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMove(
                player,
                index,
                false,
                stack ->
                        index < SLOT_COUNT
                                && moveItemStackTo(stack, SLOT_COUNT, slots.size(), true));
    }
}
