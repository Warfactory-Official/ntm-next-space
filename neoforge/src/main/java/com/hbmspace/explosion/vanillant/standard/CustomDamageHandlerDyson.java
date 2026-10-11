// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.ICustomDamageHandler;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class CustomDamageHandlerDyson implements ICustomDamageHandler {

    protected final long energy;

    public CustomDamageHandlerDyson(long energy) {
        this.energy = energy;
    }

    @Override
    public void handleAttack(ExplosionVNT explosion, Entity entity, double distanceScaled) {
        if (entity instanceof LivingEntity && entity.level() instanceof ServerLevel level) {
            entity.hurtServer(
                    level,
                    EntityProcessorStandard.setExplosionSource(explosion),
                    (float) energy / 1_000);
        }
    }
}
