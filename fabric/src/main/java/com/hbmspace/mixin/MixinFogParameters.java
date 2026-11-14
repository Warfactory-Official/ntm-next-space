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

import com.hbmspace.client.fog.BoundTerrainFog;
import com.hbmspace.client.fog.TerrainFogWriter;
import com.mojang.blaze3d.buffers.GpuBuffer;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FogParameters.class, remap = false)
public abstract class MixinFogParameters implements BoundTerrainFog {
    @Shadow @Final @Mutable private float cullDistance;
    @Unique private float hbmspace$density;
    @Unique private int hbmspace$flags;
    @Unique private TerrainFogWriter hbmspace$writer;
    @Unique private GpuBuffer hbmspace$buffer;
    @Unique private long hbmspace$offset;
    @Unique private long hbmspace$length;
    @Unique private boolean hbmspace$cullOverridden;

    @Override
    public float hbmspace$density() {
        return hbmspace$density;
    }

    @Override
    public int hbmspace$flags() {
        return hbmspace$flags;
    }

    @Override
    public TerrainFogWriter hbmspace$writer() {
        return hbmspace$writer;
    }

    @Override
    public void hbmspace$writer(TerrainFogWriter writer) {
        hbmspace$writer = writer;
    }

    @Override
    public GpuBuffer hbmspace$buffer() {
        return hbmspace$buffer;
    }

    @Override
    public long hbmspace$offset() {
        return hbmspace$offset;
    }

    @Override
    public long hbmspace$length() {
        return hbmspace$length;
    }

    @Override
    public void hbmspace$cullDistance(float distance) {
        cullDistance = distance;
        hbmspace$cullOverridden = true;
    }

    @Override
    public boolean hbmspace$cullOverridden() {
        return hbmspace$cullOverridden;
    }

    @Override
    public void hbmspace$binding(GpuBuffer buffer, long offset, long length) {
        hbmspace$buffer = buffer;
        hbmspace$offset = offset;
        hbmspace$length = length;
    }

    @Override
    public void hbmspace$payload(float density, int flags) {
        hbmspace$density = density;
        hbmspace$flags = flags;
    }

    @Inject(
            method =
                    "<init>(Lorg/joml/Vector4f;Lnet/caffeinemc/mods/sodium/client/util/FogParameters;)V",
            at = @At("RETURN"))
    private void hbmspace$copyFrame(Vector4f color, FogParameters previous, CallbackInfo ci) {
        hbmspace$payload(
                ((com.hbmspace.client.fog.TerrainFogPayload) (java.lang.Object) (previous))
                        .hbmspace$density(),
                ((com.hbmspace.client.fog.TerrainFogPayload) (java.lang.Object) (previous))
                        .hbmspace$flags());
        hbmspace$writer =
                ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (previous))
                        .hbmspace$writer();
        hbmspace$binding(
                ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (previous))
                        .hbmspace$buffer(),
                ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (previous))
                        .hbmspace$offset(),
                ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (previous))
                        .hbmspace$length());
        hbmspace$cullOverridden =
                ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (previous))
                        .hbmspace$cullOverridden();
        if (hbmspace$cullOverridden) cullDistance = previous.cullDistance();
    }
}
