// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory;

import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbmspace.handler.RocketPart;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.weapon.ItemCustomRocket;
import com.hbmspace.items.weapon.MissilePartsSpace;
import java.util.function.IntSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotRocket extends Slot {

    public SlotRocket(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof ItemCustomRocket;
    }

    public static class SlotCapsule extends Slot {

        public SlotCapsule(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            RocketPart part = RocketPart.of(stack);
            if (part == null || part.type != PartType.WARHEAD) return false;
            if (stack.is(MissilePartsSpace.RP_POD_20.get())) return false;
            return part.isPayload();
        }
    }

    public static class SlotLayer extends Slot {

        private final int layer;
        private final IntSupplier current;

        public SlotLayer(
                Container container, int slot, int x, int y, int layer, IntSupplier current) {
            super(container, slot, x, y);
            this.layer = layer;
            this.current = current;
        }

        @Override
        public boolean isActive() {
            return current.getAsInt() == layer;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isActive();
        }
    }

    public static class SlotRocketPart extends SlotLayer {

        private final PartType type;

        public SlotRocketPart(
                Container container,
                int slot,
                int x,
                int y,
                int layer,
                IntSupplier current,
                PartType type) {
            super(container, slot, x, y, layer, current);
            this.type = type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!super.mayPlace(stack)) return false;
            RocketPart part = RocketPart.of(stack);
            return part != null && part.type == type;
        }
    }

    public static class SlotDrive extends SlotLayer {

        public SlotDrive(
                Container container, int slot, int x, int y, int layer, IntSupplier current) {
            super(container, slot, x, y, layer, current);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return super.mayPlace(stack) && stack.getItem() instanceof ItemVOTVdrive;
        }
    }
}
