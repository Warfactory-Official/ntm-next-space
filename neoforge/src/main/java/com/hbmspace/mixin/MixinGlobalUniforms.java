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

import com.hbmspace.client.fog.TerrainFogPayload;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        targets =
                "net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager$GlobalUniforms",
        remap = false)
public abstract class MixinGlobalUniforms implements TerrainFogPayload {
    @Unique private float hbmspace$density;
    @Unique private int hbmspace$flags;

    @Override
    public float hbmspace$density() {
        return hbmspace$density;
    }

    @Override
    public int hbmspace$flags() {
        return hbmspace$flags;
    }

    @Override
    public void hbmspace$payload(float density, int flags) {
        hbmspace$density = density;
        hbmspace$flags = flags;
    }

    @Inject(method = "equals", at = @At("RETURN"), cancellable = true)
    private void hbmspace$includeFrame(Object other, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            TerrainFogPayload frame = (TerrainFogPayload) other;
            cir.setReturnValue(
                    Float.floatToIntBits(hbmspace$density)
                                    == Float.floatToIntBits(frame.hbmspace$density())
                            && hbmspace$flags == frame.hbmspace$flags());
        }
    }

    @Inject(method = "hashCode", at = @At("RETURN"), cancellable = true)
    private void hbmspace$includeFrameHash(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(
                31 * (31 * cir.getReturnValueI() + Float.floatToIntBits(hbmspace$density))
                        + hbmspace$flags);
    }

    @Inject(
            method = "write",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/buffers/Std140Builder;get()Ljava/nio/ByteBuffer;"),
            remap = false)
    private void hbmspace$disabledFogTail(ByteBuffer bytes, CallbackInfo ci) {
        bytes.order(ByteOrder.nativeOrder()).putFloat(hbmspace$density).putInt(hbmspace$flags);
    }
}
