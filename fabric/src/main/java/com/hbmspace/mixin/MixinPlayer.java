// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbmspace.handler.PlayerAuras;
import com.hbmspace.handler.ReentryFlames;
import com.hbmspace.handler.SpaceMultiblockEscape;
import com.hbmspace.handler.SpacePlacementEscape;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinPlayer implements SpacePlacementEscape {

    @Unique private @Nullable long[] hbmspace$escapeCores;
    @Unique private int hbmspace$escapeCount;
    @Unique private @Nullable ResourceKey<Level> hbmspace$escapeDimension;

    @Override
    public void hbmspace$grantPlacementEscape(ResourceKey<Level> dimension, long core) {
        if (hbmspace$escapeDimension != dimension) {
            hbmspace$escapeCount = 0;
            hbmspace$escapeDimension = dimension;
        }
        for (int i = 0; i < hbmspace$escapeCount; i++) {
            if (hbmspace$escapeCores[i] == core) return;
        }
        if (hbmspace$escapeCores == null) hbmspace$escapeCores = new long[2];
        else if (hbmspace$escapeCount == hbmspace$escapeCores.length) {
            hbmspace$escapeCores =
                    java.util.Arrays.copyOf(hbmspace$escapeCores, hbmspace$escapeCount * 2);
        }
        hbmspace$escapeCores[hbmspace$escapeCount++] = core;
    }

    @Override
    public void hbmspace$refreshPlacementEscape() {
        if (hbmspace$escapeCount == 0) return;
        Player player = (Player) (Object) this;
        if (hbmspace$escapeDimension != player.level().dimension()) {
            hbmspace$escapeCount = 0;
            hbmspace$escapeDimension = null;
            return;
        }
        BlockPos feet = BlockPos.containing(player.getX(), player.getY() + 0.1D, player.getZ());
        long feetOwner = SpaceMultiblockEscape.ownerAt(player.level(), feet);
        long headOwner = SpaceMultiblockEscape.ownerAt(player.level(), feet.above());
        if (feetOwner == MultiblockSurface.NO_CORE_TRUNCATED
                || headOwner == MultiblockSurface.NO_CORE_TRUNCATED) {
            return;
        }
        int kept = 0;
        for (int i = 0; i < hbmspace$escapeCount; i++) {
            long core = hbmspace$escapeCores[i];
            if (core == feetOwner || core == headOwner) hbmspace$escapeCores[kept++] = core;
        }
        hbmspace$escapeCount = kept;
        if (kept == 0) hbmspace$escapeDimension = null;
    }

    @Override
    public boolean hbmspace$hasPlacementEscape() {
        hbmspace$refreshPlacementEscape();
        return hbmspace$escapeCount != 0;
    }

    @Override
    public boolean hbmspace$ignoresPlacementCollision(Level level, BlockPos pos) {
        if (hbmspace$escapeCount == 0 || level != ((Player) (Object) this).level()) return false;
        hbmspace$refreshPlacementEscape();
        if (hbmspace$escapeCount == 0) return false;
        long owner = SpaceMultiblockEscape.ownerAt(level, pos);
        if (owner == MultiblockSurface.NO_CORE || owner == MultiblockSurface.NO_CORE_TRUNCATED)
            return false;
        for (int i = 0; i < hbmspace$escapeCount; i++) {
            if (hbmspace$escapeCores[i] == owner) return true;
        }
        return false;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void hbmspace$reentryPre(CallbackInfo ci) {
        hbmspace$refreshPlacementEscape();
        PlayerAuras.tick((Player) (Object) this);
        ReentryFlames.playerTick((Player) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void hbmspace$reentryPost(CallbackInfo ci) {
        ReentryFlames.playerTick((Player) (Object) this);
    }
}
