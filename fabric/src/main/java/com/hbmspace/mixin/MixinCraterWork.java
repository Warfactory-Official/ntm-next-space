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

import com.hbm.explosion.ExplosionTom;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.hbm.explosion.ExplosionTom$CraterWork")
public abstract class MixinCraterWork {
    @Shadow @Final ExplosionTom this$0;

    @Shadow
    private void haloColumn(
            LevelChunkSection[] sections,
            Long2ObjectOpenHashMap<BlockState> writes,
            int lx,
            int lz,
            int x,
            int z) {
        throw new AssertionError();
    }

    @Inject(method = "carveColumn", at = @At("TAIL"))
    private void hbmspace$haloPastReach(
            LevelChunkSection[] sections,
            Long2ObjectOpenHashMap<BlockState> writes,
            int lx,
            int lz,
            int x,
            int z,
            CallbackInfo ci) {
        if (!((com.hbmspace.explosion.TomLavaReach) (java.lang.Object) (this$0))
                .hbmspace$floods(x, z)) haloColumn(sections, writes, lx, lz, x, z);
    }

    @WrapOperation(
            method = "carveColumn",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lit/unimi/dsi/fastutil/longs/Long2ObjectOpenHashMap;put(JLjava/lang/Object;)Ljava/lang/Object;",
                            ordinal = 2))
    private Object hbmspace$lavaInReach(
            Long2ObjectOpenHashMap<BlockState> writes,
            long pos,
            Object lava,
            Operation<Object> original,
            @Local(argsOnly = true, ordinal = 2) int x,
            @Local(argsOnly = true, ordinal = 3) int z) {
        return ((com.hbmspace.explosion.TomLavaReach) (java.lang.Object) (this$0))
                        .hbmspace$floods(x, z)
                ? original.call(writes, pos, lava)
                : null;
    }

    @WrapOperation(
            method = "haloColumn",
            at = @At(value = "INVOKE", target = "Lcom/hbm/explosion/ExplosionTom;floor(II)I"))
    private int hbmspace$floodingNeighbours(
            ExplosionTom tom, int nx, int nz, Operation<Integer> original) {
        return ((com.hbmspace.explosion.TomLavaReach) (java.lang.Object) (tom))
                        .hbmspace$floods(nx, nz)
                ? original.call(tom, nx, nz)
                : Integer.MAX_VALUE;
    }

    @ModifyExpressionValue(
            method = "haloColumn",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
    private int hbmspace$aboveOwnFloor(
            int to,
            @Local(argsOnly = true, ordinal = 2) int x,
            @Local(argsOnly = true, ordinal = 3) int z) {
        return ((com.hbmspace.explosion.TomLavaReach) (java.lang.Object) (this$0))
                        .hbmspace$inside(x, z)
                ? Math.max(
                        to,
                        ((com.hbmspace.explosion.TomLavaReach) (java.lang.Object) (this$0))
                                .hbmspace$floor(x, z))
                : to;
    }
}
