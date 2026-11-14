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

import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbmspace.handler.SpacePlacementEscape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void hbmspace$grantedCoreSuffocation(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof SpacePlacementEscape escape)
                || !escape.hbmspace$hasPlacementEscape()) return;
        Entity entity = (Entity) (Object) this;
        if (entity.noPhysics) {
            cir.setReturnValue(false);
            return;
        }
        float width = entity.getBbWidth() * 0.8F;
        AABB eye = AABB.ofSize(entity.getEyePosition(), width, 1.0E-6D, width);
        for (BlockPos pos : BlockPos.betweenClosed(eye)) {
            var state = entity.level().getBlockState(pos);
            if (state.isAir() || !state.isSuffocating(entity.level(), pos)) continue;
            if ((MultiblockSurface.foldedCore(state) != null
                            || MultiblockSurface.isFoldedCell(state))
                    && escape.hbmspace$ignoresPlacementCollision(entity.level(), pos)) continue;
            if (Shapes.joinIsNotEmpty(
                    state.getCollisionShape(entity.level(), pos).move(pos),
                    Shapes.create(eye),
                    BooleanOp.AND)) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }
}
