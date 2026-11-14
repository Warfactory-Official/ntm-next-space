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

package com.hbmspace.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ComposableExplosion;
import com.hbm.explosion.vanillant.ExplosionHitListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class CustomDamageHandlerDyson implements ExplosionHitListener {

    protected final long energy;

    public CustomDamageHandlerDyson(long energy) {
        this.energy = energy;
    }

    @Override
    public void afterHit(ComposableExplosion blast, Entity entity, double distance) {
        if (entity instanceof LivingEntity && entity.level() instanceof ServerLevel level) {
            entity.hurtServer(level, blast.getDamageSource(), (float) energy / 1_000);
        }
    }
}
