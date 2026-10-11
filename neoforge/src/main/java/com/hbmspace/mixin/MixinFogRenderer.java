// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.api.client.ShaderPacks;
import com.hbmspace.client.fog.SodiumFogBridge;
import com.hbmspace.client.fog.TerrainExpFog;
import com.hbmspace.client.fog.TerrainFogPayload;
import com.hbmspace.client.fog.TerrainFogWriter;
import com.hbmspace.client.sky.CelestialFogTint;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import java.nio.ByteBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.neoforged.fml.ModList;
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
        ((TerrainFogPayload) data).hbmspace$payload(density, TerrainExpFog.flags(density, false));
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
        TerrainFogPayload payload = (TerrainFogPayload) data;
        payload.hbmspace$payload(
                payload.hbmspace$density(),
                TerrainExpFog.flags(
                        payload.hbmspace$density(), environment instanceof DarknessFogEnvironment));
    }

    @Inject(method = "setupFog", at = @At("RETURN"), order = 1100)
    private void hbmspace$sodiumSnapshot(
            Camera camera,
            int renderDistance,
            DeltaTracker delta,
            float darkenWorld,
            ClientLevel level,
            CallbackInfoReturnable<FogData> cir) {
        if (ModList.get().isLoaded("sodium")) {
            SodiumFogBridge.captureStored((FogRenderer) (Object) this, cir.getReturnValue());
        }
    }

    @Inject(method = "updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V", at = @At("HEAD"))
    private void hbmspace$captureWriter(FogData data, CallbackInfo ci) {
        TerrainFogPayload payload = (TerrainFogPayload) data;
        hbmspace$writtenDensity = fogEnabled ? payload.hbmspace$density() : 0F;
        hbmspace$writtenFlags = fogEnabled ? payload.hbmspace$flags() : 0;
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
        bytes.order(java.nio.ByteOrder.nativeOrder())
                .putFloat(offset + TerrainExpFog.NATIVE_PREFIX, hbmspace$writtenDensity)
                .putInt(offset + TerrainExpFog.NATIVE_PREFIX + 4, hbmspace$writtenFlags);
        bytes.position(offset + TerrainExpFog.NATIVE_SIZE);
    }
}
