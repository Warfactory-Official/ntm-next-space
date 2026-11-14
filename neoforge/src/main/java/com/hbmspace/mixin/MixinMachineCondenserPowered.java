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

import com.hbm.blocks.machine.PoweredCondenserBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.RepairLookOverlay;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.RepairHandler;
import com.hbm.interfaces.ToolInteractionHandler;
import com.hbm.interfaces.ToolType;
import com.hbmspace.blockentity.machine.CondenserPoweredSpaceBlockEntity;
import com.hbmspace.blockentity.machine.ThermalMachineDamage;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoweredCondenserBlock.class)
abstract class MixinMachineCondenserPowered extends MultiblockCoreBlock
        implements ToolInteractionHandler {
    protected MixinMachineCondenserPowered(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ThermalMachineDamage.DAMAGED);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hbmspace$intactDefault(Properties properties, CallbackInfo ci) {
        registerDefaultState(defaultBlockState().setValue(ThermalMachineDamage.DAMAGED, false));
    }

    @Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true)
    private void hbmspace$repairable(
            BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        cir.setReturnValue(new CondenserPoweredSpaceBlockEntity(pos, state));
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void onExplosionHit(
            BlockState state,
            ServerLevel level,
            BlockPos core,
            Explosion explosion,
            BiConsumer<ItemStack, BlockPos> onHit) {
        var be = (CondenserPoweredSpaceBlockEntity) level.getBlockEntity(core);
        if (be.lastExplosion == explosion) return;
        be.lastExplosion = explosion;
        if (be.isDamaged()) level.removeBlock(core, false);
        else be.damage();
    }

    @Override
    public boolean onToolUse(
            Level level,
            Player player,
            BlockPos core,
            @Nullable Direction side,
            Vec3 hit,
            ToolType tool) {
        if (tool != ToolType.TORCH) return false;
        return RepairHandler.tryRepair(level, core, player);
    }

    @Inject(method = "buildLookOverlay", at = @At("HEAD"), cancellable = true)
    private void hbmspace$repairOverlay(
            Level level, BlockPos core, LookOverlay info, CallbackInfo ci) {
        if (level.getBlockEntity(core) instanceof CondenserPoweredSpaceBlockEntity be
                && be.isDamaged()) {
            RepairLookOverlay.build(level, core, info);
            ci.cancel();
        }
    }
}
