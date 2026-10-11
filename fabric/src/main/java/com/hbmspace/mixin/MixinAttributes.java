// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Attributes.class)
public abstract class MixinAttributes {
    @ModifyConstant(
            method = "<clinit>",
            slice =
                    @Slice(
                            from =
                                    @At(
                                            value = "CONSTANT",
                                            args = "stringValue=attribute.name.max_health"),
                            to =
                                    @At(
                                            value = "FIELD",
                                            target =
                                                    "Lnet/minecraft/world/entity/ai/attributes/Attributes;"
                                                            + "MAX_HEALTH:Lnet/minecraft/core/Holder;",
                                            opcode = Opcodes.PUTSTATIC)),
            constant = @Constant(doubleValue = 1024.0),
            require = 1,
            allow = 1)
    private static double hbmspace$siegeHealthCeiling(double original) {
        return 12_500.0;
    }
}
