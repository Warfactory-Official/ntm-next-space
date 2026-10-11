// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.handler.BobmazonSignHandler;
import com.hbmspace.handler.SpaceItemEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BobmazonSignHandler.class)
public abstract class MixinBobmazonSignHandler {
    @ModifyArg(
            method = "onSignUsed",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/item/ItemEntity;setPickUpDelay(I)V"),
            index = 0)
    private static int hbmspace$oneTickPickup(int delay) {
        return 1;
    }

    @Inject(method = "onSignUsed", at = @At("RETURN"))
    private static void hbmspace$catalogLog(
            Level level, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) SpaceItemEvents.hiddenCatalogObtained(player, pos);
    }
}
