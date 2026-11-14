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

package com.hbmspace.blockentity.deco;

import com.hbm.blockentity.BlockEntityTicks;
import com.hbm.lib.ModDamageTypes;
import com.hbm.sound.EntitySounds;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class GeysirDCMBlockEntity extends BlockEntity implements BlockEntityTicks {

    public static Consumer<GeysirDCMBlockEntity> CLIENT_EFFECTS = be -> {};

    public GeysirDCMBlockEntity(BlockPos pos, BlockState state) {
        super(TerrainBlockEntitiesSpace.GEYSIR_DCM.get(), pos, state);
    }

    @Override
    public void tickClient() {
        CLIENT_EFFECTS.accept(this);
    }

    @Override
    public void tickServer() {
        var pos = worldPosition;
        AABB column =
                new AABB(
                        pos.getX() - 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() - 0.5,
                        pos.getX() + 1.5,
                        pos.getY() + 60,
                        pos.getZ() + 1.5);
        ServerLevel server = (ServerLevel) level;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, column)) {
            float damage = Mth.clamp(living.getMaxHealth() * 0.1F, 3, 20);
            if (living.hurtServer(
                    server, level.damageSources().source(ModDamageTypes.ACID), damage)) {
                EntitySounds.play(
                        living,
                        living.getX(),
                        living.getY(),
                        living.getZ(),
                        SoundEvents.FIRE_EXTINGUISH,
                        living.getSoundSource(),
                        1.0F,
                        1.0F);
            }
        }
    }
}
