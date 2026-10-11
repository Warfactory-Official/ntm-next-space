// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntitySawmill;
import com.hbmspace.blocks.machine.PolymerSawmillSpace;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntitySawmill.class)
public abstract class MixinBlockEntitySawmill {
    @Unique private boolean hbmspace$polymerStickInput;

    @Inject(
            method = "getOutput",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z",
                            ordinal = 0),
            cancellable = true)
    private void hbmspace$polymerOutput(ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack output = PolymerSawmillSpace.output(input);
        if (output != null) cir.setReturnValue(output);
    }

    @Inject(method = "sawdustChance", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$polymerResult(
            ItemStack result, CallbackInfoReturnable<Float> cir) {
        if (PolymerSawmillSpace.polymer(result)) cir.setReturnValue(0F);
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void hbmspace$rememberInput(CallbackInfo ci) {
        hbmspace$polymerStickInput =
                PolymerSawmillSpace.stick(((BlockEntitySawmill) (Object) this).getItem(0));
    }

    @ModifyExpressionValue(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blockentity/machine/BlockEntitySawmill;sawdustChance(Lnet/minecraft/world/item/ItemStack;)F"))
    private float hbmspace$polymerStickByproduct(float chance) {
        return hbmspace$polymerStickInput ? 0F : chance;
    }
}
