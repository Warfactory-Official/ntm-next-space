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

import com.hbm.blockentity.machine.storage.FileCabinetBlockEntity;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.SlagBlock;
import com.hbm.explosion.vanillant.ComposableExplosion;
import com.hbm.explosion.vanillant.standard.AttenuatedRayBlockSelector;
import com.hbm.explosion.vanillant.standard.BlockMutatorDebris;
import com.hbm.explosion.vanillant.standard.CellDestruction;
import com.hbm.explosion.vanillant.standard.MultiPointExplosionEntityProcessor;
import com.hbm.items.ModItems;
import com.hbm.particle.helper.ExplosionCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FileCabinetBlockEntity.class)
public abstract class MixinFilingCabinet {

    @Inject(method = "tickViewed", at = @At("HEAD"))
    private void hbmspace$igniteRockets(CallbackInfo _) {
        var cabinet = (FileCabinetBlockEntity) (Object) this;
        var level = cabinet.getLevel();
        var pos = cabinet.getBlockPos();
        var rockets = 0;
        for (var slot = 0; slot < cabinet.getContainerSize(); slot++) {
            var stack = cabinet.getItem(slot);
            if (stack.is(ModItems.MISSILE_SOYUZ_0.get())
                    || stack.is(ModItems.MISSILE_SOYUZ_1.get())
                    || stack.is(ModItems.MISSILE_SOYUZ_2.get())) {
                rockets++;
            }
        }
        for (var rocket = 0; rocket < rockets; rocket++) {
            new ComposableExplosion(level, pos, 15F)
                    .blockSelector(new AttenuatedRayBlockSelector(48))
                    .blockProcessor(
                            CellDestruction.noDrops()
                                    .transform(
                                            new BlockMutatorDebris(
                                                    ModBlocks.BLOCK_SLAG
                                                            .get()
                                                            .defaultBlockState()
                                                            .setValue(SlagBlock.CRACKED, true))))
                    .entityProcessor(new MultiPointExplosionEntityProcessor(7.5D).range(3F))
                    .explode();
            ExplosionCreator.composeEffectStandard(
                    level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        }
    }
}
