// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.explosion.ExplosionTom;
import com.hbmspace.explosion.TomLavaReach;
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
public abstract class MixinExplosionTomCraterWork {
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
        if (!((TomLavaReach) (Object) this$0).hbmspace$floods(x, z))
            haloColumn(sections, writes, lx, lz, x, z);
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
        return ((TomLavaReach) (Object) this$0).hbmspace$floods(x, z)
                ? original.call(writes, pos, lava)
                : null;
    }

    @WrapOperation(
            method = "haloColumn",
            at = @At(value = "INVOKE", target = "Lcom/hbm/explosion/ExplosionTom;floor(II)I"))
    private int hbmspace$floodingNeighbours(
            ExplosionTom tom, int nx, int nz, Operation<Integer> original) {
        return ((TomLavaReach) (Object) tom).hbmspace$floods(nx, nz)
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
        TomLavaReach tom = (TomLavaReach) (Object) this$0;
        return tom.hbmspace$inside(x, z) ? Math.max(to, tom.hbmspace$floor(x, z)) : to;
    }
}
