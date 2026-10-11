// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.inventory.material.MaterialShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MaterialShapes.class)
public abstract class MixinMaterialShapes {
    @ModifyVariable(
            method = "autogenItemId",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1,
            require = 1)
    private String hbmspace$deshAutogenName(String segment) {
        return segment.equals("desh") ? "workers_alloy" : segment;
    }
}
