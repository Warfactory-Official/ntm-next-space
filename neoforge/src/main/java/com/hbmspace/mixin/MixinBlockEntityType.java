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

import com.hbm.blocks.ModBlockEntities;
import com.hbm.blocks.machine.storage.CapacitorBlock;
import com.hbmspace.blockentity.machine.ChungusSpaceBlockEntity;
import com.hbmspace.blockentity.machine.CondenserPoweredSpaceBlockEntity;
import com.hbmspace.blockentity.machine.LPW2SpaceBlockEntity;
import com.hbmspace.blocks.CapacitorBlocksSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class MixinBlockEntityType {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void hbmspace$complexCapacitor(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof CapacitorBlock
                && (Object) this == ModBlockEntities.CAPACITOR.get()
                && state.getBlock() == CapacitorBlocksSpace.CAPACITOR_COMPLEX.get()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void hbmspace$lpw2Engine(
            BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        if ((Object) this == ModBlockEntities.LPW2.get()) {
            cir.setReturnValue(new LPW2SpaceBlockEntity(pos, state));
        }
    }

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void hbmspace$thermalDamage(
            BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        if ((Object) this == ModBlockEntities.CHUNGUS.get()) {
            cir.setReturnValue(new ChungusSpaceBlockEntity(pos, state));
        } else if ((Object) this == ModBlockEntities.CONDENSER_POWERED.get()) {
            cir.setReturnValue(new CondenserPoweredSpaceBlockEntity(pos, state));
        }
    }
}
