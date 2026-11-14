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

import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.render.MachinePlacementOutline;
import com.hbmspace.client.SpaceMultiblockItemPreview;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MachinePlacementOutline.class)
public abstract class MixinMachinePlacementOutline {
    @Inject(method = "extract", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$placementPreview(
            Level level,
            BlockHitResult hit,
            @Nullable Player player,
            CallbackInfoReturnable<MachinePlacementOutline.State> cir) {
        if (player == null
                || hit.getType() != HitResult.Type.BLOCK
                || !(player.getMainHandItem().getItem() instanceof BlockItem item)
                || !(item.getBlock() instanceof MultiblockCoreBlock block)) return;
        BlockPlaceContext context =
                new BlockPlaceContext(
                        player, InteractionHand.MAIN_HAND, player.getMainHandItem(), hit);
        context = item.updatePlacementContext(context);
        if (context == null) {
            cir.setReturnValue(null);
            return;
        }
        cir.setReturnValue(SpaceMultiblockItemPreview.extract(level, item, block, context, player));
    }
}
