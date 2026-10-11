// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.render.ArmorSocketRenderer;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorSocketRenderer.class)
public abstract class MixinArmorSocketRenderer {
    @ModifyReturnValue(method = "wingsGrounded", at = @At("RETURN"))
    private static boolean hbmspace$relaxRiding(boolean grounded, HumanoidRenderState state) {
        return grounded || state.isPassenger;
    }
}
