// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
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
                || !(item.getBlock() instanceof BlockMultiblockCore block)) return;
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
