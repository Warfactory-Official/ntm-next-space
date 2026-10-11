// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.client.fog.TerrainFogPayload;
import java.nio.ByteBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
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
        bytes.order(java.nio.ByteOrder.nativeOrder())
                .putFloat(hbmspace$density)
                .putInt(hbmspace$flags);
    }
}
