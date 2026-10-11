// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.items.machine.ItemCassette;
import com.hbmspace.items.SirenItemsSpace;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCassette.class)
public abstract class MixinItemCassette {

    @Inject(method = "typeOf", at = @At("RETURN"), cancellable = true)
    private static void hbmspace$typeOf(ItemStack stack, CallbackInfoReturnable<TrackType> cir) {
        if (cir.getReturnValue() != TrackType.NULL || !(stack.getItem() instanceof ItemCassette))
            return;
        TrackType track = SirenItemsSpace.TRACKS.typeOf(stack);
        if (track != null) cir.setReturnValue(track);
    }
}
