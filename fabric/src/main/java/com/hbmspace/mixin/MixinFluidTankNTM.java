// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbmspace.HbmSpace;
import com.hbmspace.inventory.FluidContainersSpace;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidTankNTM.class)
public abstract class MixinFluidTankNTM {
    @Inject(method = "fillContainer", at = @At("HEAD"), cancellable = true)
    private void hbmspace$fillBucket(
            NonNullList<ItemStack> slots, int in, int out, CallbackInfo ci) {
        ItemStack empty = slots.get(in);
        if (!empty.is(Items.BUCKET)) return;
        FluidTankNTM tank = (FluidTankNTM) (Object) this;
        Fluid content = tank.getFluid();
        ItemStack full = FluidContainersSpace.filledBucket(content);
        if (full.isEmpty()) {
            if (content != null
                    && HbmSpace.MOD_ID.equals(
                            BuiltInRegistries.FLUID.getKey(content).getNamespace())) ci.cancel();
            return;
        }
        ci.cancel();
        if (tank.getFill() < 1000) return;

        Component name = empty.get(DataComponents.CUSTOM_NAME);
        if (name != null) full.set(DataComponents.CUSTOM_NAME, name);
        ItemStack output = slots.get(out);
        if (!output.isEmpty()
                && (!ItemStack.isSameItemSameComponents(output, full)
                        || output.getCount() + 1 > output.getMaxStackSize())) return;

        tank.setFill(tank.getFill() - 1000);
        empty.shrink(1);
        if (empty.isEmpty()) slots.set(in, ItemStack.EMPTY);
        if (output.isEmpty()) slots.set(out, full);
        else output.grow(1);
    }
}
