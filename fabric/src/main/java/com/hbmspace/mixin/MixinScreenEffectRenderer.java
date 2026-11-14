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
