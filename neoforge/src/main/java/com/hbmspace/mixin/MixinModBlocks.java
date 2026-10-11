// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.fluid.ClassicFluid;
import com.hbm.registration.Reg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModBlocks.class)
public abstract class MixinModBlocks {
    @Redirect(
            method = "<clinit>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/registration/Reg;"
                                            + "classicFluid(Ljava/lang/String;Lcom/hbm/blocks/fluid/ClassicFluid$Spec;)"
                                            + "Lcom/hbm/blocks/fluid/ClassicFluid$Pair;"))
    private static ClassicFluid.Pair hbmspace$schrabidicQuanta(
            String name, ClassicFluid.Spec spec) {
        if (name.equals("schrabidic_fluid")) {
            spec =
                    new ClassicFluid.Spec(
                            ClassicFluid.Physics.WATER,
                            8,
                            spec.tickDelay(),
                            spec.density(),
                            spec.displacesLiquids(),
                            spec.sprite(),
                            spec.block(),
                            spec.bucket(),
                            spec.kind());
        }
        return Reg.classicFluid(name, spec);
    }
}
