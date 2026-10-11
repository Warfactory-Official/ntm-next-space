// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.handler.JoinGreeting;
import com.hbmspace.HbmSpace;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JoinGreeting.class)
public abstract class MixinJoinGreeting {
    @Inject(method = "message", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$message(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(
                Component.translatable(
                        "chat.hbmspace.loaded",
                        ModList.get()
                                .getModContainerById(HbmSpace.MOD_ID)
                                .orElseThrow()
                                .getModInfo()
                                .getVersion()
                                .toString(),
                        SharedConstants.getCurrentVersion().name()));
    }
}
