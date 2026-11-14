/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

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
