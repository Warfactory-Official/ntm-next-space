// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.client.sky.CelestialLightmapTint;
import com.hbmspace.dim.SpaceDimensions;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteOrder;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public abstract class MixinLightmap {
    @Shadow @Final private GpuTextureView textureView;
    @Unique private @Nullable GpuTexture hbmspace$raw;
    @Unique private @Nullable GpuTextureView hbmspace$rawView;
    @Unique private @Nullable MappableRingBuffer hbmspace$tintInfo;
    @Unique private boolean hbmspace$redirected;

    @Inject(method = "render", at = @At("HEAD"))
    private void hbmspace$beginUpdate(LightmapRenderState state, CallbackInfo ci) {
        hbmspace$redirected = false;
    }

    @ModifyArg(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/systems/CommandEncoder;"
                                            + "createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/Optional;)Lcom/mojang/blaze3d/systems/RenderPass;"),
            index = 1)
    private GpuTextureView hbmspace$rawUpdate(GpuTextureView original) {
        var level = Minecraft.getInstance().level;
        if (level == null
                || level.dimension() != SpaceDimensions.IKE
                        && level.dimension() != SpaceDimensions.LAYTHE) return original;
        if (hbmspace$raw == null) {
            var device = RenderSystem.getDevice();
            hbmspace$raw =
                    device.createTexture(
                            "Space raw lightmap",
                            GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
                            GpuFormat.RGBA8_UNORM,
                            16,
                            16,
                            1,
                            1);
            hbmspace$rawView = device.createTextureView(hbmspace$raw);
            hbmspace$tintInfo =
                    new MappableRingBuffer(
                            () -> "Space lightmap tint",
                            GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_UNIFORM,
                            CelestialLightmapTint.INFO_SIZE);
        }
        hbmspace$redirected = true;
        return hbmspace$rawView;
    }

    @Inject(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/MappableRingBuffer;rotate()V"))
    private void hbmspace$finishUpdate(
            LightmapRenderState state,
            CallbackInfo ci,
            @Local(name = "commandEncoder") CommandEncoder encoder) {
        if (!hbmspace$redirected) return;
        var mc = Minecraft.getInstance();
        var level = mc.level;
        boolean ike = level.dimension() == SpaceDimensions.IKE;
        float sun =
                mc.gameRenderer
                        .mainCamera()
                        .attributeProbe()
                        .getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, 1F);
        try (var mapped = hbmspace$tintInfo.currentBuffer().map(false, true)) {
            var data = mapped.data().order(ByteOrder.nativeOrder());
            for (int sky = 0; sky < 16; sky++) {
                data.putFloat(
                        sky * Float.BYTES,
                        CelestialLightmapTint.bonus(
                                Lightmap.getBrightness(level.dimensionType(), sky), sun, ike));
            }
            data.putInt(64, ike ? 0 : 1);
        }
        try (var pass =
                encoder.createRenderPass(
                        () -> "Space lightmap tint", textureView, Optional.empty())) {
            pass.setPipeline(CelestialLightmapTint.PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("CelestialTint", hbmspace$tintInfo.currentBuffer());
            pass.bindTexture(
                    "LightmapRaw",
                    hbmspace$rawView,
                    RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
            pass.draw(3, 1, 0, 0);
        }
        hbmspace$tintInfo.rotate();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void hbmspace$closeTint(CallbackInfo ci) {
        if (hbmspace$raw == null) return;
        hbmspace$rawView.close();
        hbmspace$raw.close();
        hbmspace$tintInfo.close();
    }
}
