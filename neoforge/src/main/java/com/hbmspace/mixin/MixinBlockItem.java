// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbmspace.handler.SpaceBlockItemPlacement;
import com.hbmspace.handler.SpaceMultiblockEscape;
import com.hbmspace.handler.SpaceMultiblockPlacement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem implements SpaceBlockItemPlacement {
    @Invoker("canPlace")
    public abstract boolean hbmspace$canPlace(BlockPlaceContext context, BlockState state);

    @WrapOperation(
            method = "place",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;"
                                            + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;"
                                            + "Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void hbmspace$clickedFace(
            Block block,
            Level level,
            BlockPos pos,
            BlockState state,
            LivingEntity placer,
            ItemStack stack,
            Operation<Void> original,
            @Local(ordinal = 1) BlockPlaceContext context) {
        if (!(block instanceof BlockMultiblockCore machine)) {
            original.call(block, level, pos, state, placer, stack);
            return;
        }
        int previous = SpaceMultiblockPlacement.begin(context.getClickedFace());
        try {
            original.call(block, level, pos, state, placer, stack);
            if (SpaceMultiblockPlacement.didAssemble()) {
                BlockPos core =
                        SpaceMultiblockPlacement.core(
                                machine, pos, context.getClickedFace(), placer.getYRot());
                SpaceMultiblockEscape.placed(level, core, machine, placer);
            }
        } finally {
            SpaceMultiblockPlacement.end(previous);
        }
    }
}
