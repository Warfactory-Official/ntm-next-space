// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.handler.SpaceMultiblockEscape;
import com.hbmspace.handler.SpacePlacementEscape;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenEffectRenderer.class)
public abstract class MixinScreenEffectRenderer {
    @Redirect(
            method = "getViewBlockingState",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/block/state/BlockState;isViewBlocking(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean hbmspace$placementView(
            BlockState state, BlockGetter view, BlockPos pos, Player player) {
        boolean blocking = state.isViewBlocking(view, pos);
        if (!blocking) return false;
        return !(view instanceof Level level
                && player instanceof SpacePlacementEscape escape
                && escape.hbmspace$ignoresPlacementCollision(level, pos)
                && SpaceMultiblockEscape.isCurrentState(level, pos, state));
    }
}
