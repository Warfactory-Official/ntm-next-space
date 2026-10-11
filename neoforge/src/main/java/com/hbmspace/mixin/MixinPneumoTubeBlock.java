// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.network.pneumatic.BlockEntityPneumoTube;
import com.hbm.blocks.network.pneumatic.PneumoTubeBlock;
import com.hbm.lib.Library;
import com.hbmspace.handler.PneumoIdentifierSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PneumoTubeBlock.class)
public abstract class MixinPneumoTubeBlock extends Block {
    protected MixinPneumoTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        return PneumoIdentifierSelection.handle(stack, level, pos, player)
                ? InteractionResult.SUCCESS
                : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Inject(method = "canConnectToAir", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$typedNozzle(
            LevelReader level, BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityPneumoTube tube)
                || !tube.isCompressor()
                || tube.ejectionDir == dir
                || tube.insertionDir == dir
                || level.getBlockState(pos.relative(dir)).getBlock() instanceof PneumoTubeBlock) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(
                Library.canConnectFluid(
                        level, pos.relative(dir), dir.getOpposite(), tube.compair.getFluid()));
    }
}
