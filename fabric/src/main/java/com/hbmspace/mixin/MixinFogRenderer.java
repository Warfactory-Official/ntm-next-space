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

import com.hbm.api.client.ShaderPacks;
import com.hbm.platform.Platform;
import com.hbmspace.client.fog.SodiumFogBridge;
import com.hbmspace.client.fog.TerrainExpFog;
import com.hbmspace.client.fog.TerrainFogWriter;
import com.hbmspace.client.sky.CelestialFogTint;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FogRenderer.class, priority = 900)
public abstract class MixinFogRenderer implements TerrainFogWriter {
    @Shadow @Final @Mutable public static int FOG_UBO_SIZE;
    @Shadow private static boolean fogEnabled;

    @Shadow
    public abstract GpuBufferSlice getBuffer(FogRenderer.FogMode mode);

    @Unique private float hbmspace$writtenDensity;
    @Unique private int hbmspace$writtenFlags;

    @Override
    public boolean hbmspace$fogEnabled() {
        return fogEnabled;
    }

    @Override
    public GpuBufferSlice hbmspace$worldFog() {
        return getBuffer(FogRenderer.FogMode.WORLD);
    }

    @Override
    public float hbmspace$writtenDensity() {
        return hbmspace$writtenDensity;
    }

    @Override
    public int hbmspace$writtenFlags() {
        return hbmspace$writtenFlags;
    }

    @ModifyExpressionValue(
            method = "setupFog",
            at = @At(value = "NEW", target = "net/minecraft/client/renderer/fog/FogData"))
    private FogData hbmspace$celestialDensity(
            FogData data,
            Camera camera,
            int renderDistance,
            DeltaTracker delta,
            float darkenWorld,
            ClientLevel level) {
        float density = ShaderPacks.inUse() ? 0F : CelestialFogTint.density(level, camera);
        ((com.hbmspace.client.fog.TerrainFogPayload) (data))
                .hbmspace$payload(density, TerrainExpFog.flags(density, false));
        return data;
    }

    @WrapOperation(
            method = "setupFog",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V"))
    private void hbmspace$selectedEnvironment(
            FogEnvironment environment,
            FogData data,
            Camera camera,
            ClientLevel level,
            float renderDistance,
            DeltaTracker delta,
            Operation<Void> original) {
        original.call(environment, data, camera, level, renderDistance, delta);
        ((com.hbmspace.client.fog.TerrainFogPayload) (data))
                .hbmspace$payload(
                        ((com.hbmspace.client.fog.TerrainFogPayload) (data)).hbmspace$density(),
                        TerrainExpFog.flags(
                                ((com.hbmspace.client.fog.TerrainFogPayload) (data))
                                        .hbmspace$density(),
                                environment instanceof DarknessFogEnvironment));
    }

    @Inject(method = "setupFog", at = @At("RETURN"), order = 1100)
    private void hbmspace$sodiumSnapshot(
            Camera camera,
            int renderDistance,
            DeltaTracker delta,
            float darkenWorld,
            ClientLevel level,
            CallbackInfoReturnable<FogData> cir) {
        if (Platform.isModLoaded("sodium")) {
            SodiumFogBridge.captureStored((FogRenderer) (Object) this, cir.getReturnValue());
        }
    }

    @Inject(method = "updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V", at = @At("HEAD"))
    private void hbmspace$captureWriter(FogData data, CallbackInfo ci) {
        hbmspace$writtenDensity =
                fogEnabled
                        ? ((com.hbmspace.client.fog.TerrainFogPayload) (data)).hbmspace$density()
                        : 0F;
        hbmspace$writtenFlags =
                fogEnabled
                        ? ((com.hbmspace.client.fog.TerrainFogPayload) (data)).hbmspace$flags()
                        : 0;
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void hbmspace$appendFogSize(CallbackInfo ci) {
        assert FOG_UBO_SIZE == TerrainExpFog.NATIVE_PREFIX;
        FOG_UBO_SIZE = TerrainExpFog.NATIVE_SIZE;
    }

    @Inject(
            method = "updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("TAIL"))
    private void hbmspace$disabledFogTail(
            ByteBuffer bytes,
            int offset,
            Vector4f color,
            float environmentStart,
            float environmentEnd,
            float renderStart,
            float renderEnd,
            float skyEnd,
            float cloudEnd,
            CallbackInfo ci) {
        bytes.order(ByteOrder.nativeOrder())
                .putFloat(offset + TerrainExpFog.NATIVE_PREFIX, hbmspace$writtenDensity)
                .putInt(offset + TerrainExpFog.NATIVE_PREFIX + 4, hbmspace$writtenFlags);
        bytes.position(offset + TerrainExpFog.NATIVE_SIZE);
    }
}
