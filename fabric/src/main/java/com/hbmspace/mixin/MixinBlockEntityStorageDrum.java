// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityStorageDrum;
import com.hbmspace.hazard.NeutronExposureSpace;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityStorageDrum.class)
public abstract class MixinBlockEntityStorageDrum {
    @Redirect(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/util/ContaminationUtil;"
                                            + "neutronActivateItem(Lnet/minecraft/world/item/ItemStack;FF)Z"))
    private boolean hbmspace$replaceLegacyDecay(ItemStack stack, float rad, float factor) {
        return false;
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void hbmspace$sourceDecay(CallbackInfo ci) {
        BlockEntityStorageDrum drum = (BlockEntityStorageDrum) (Object) this;
        for (int slot = 0; slot < drum.getContainerSize(); slot++) {
            ItemStack stack = drum.getItem(slot);
            float before = NeutronExposureSpace.activation(stack);
            NeutronExposureSpace.decay(stack, 0.9899916F);
            if (before != NeutronExposureSpace.activation(stack)) drum.setChanged();
        }
    }
}
