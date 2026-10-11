// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbmspace.items.TerrainItemsSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityScutterfish extends EntityFish {

    public EntityScutterfish(EntityType<? extends EntityScutterfish> type, Level level) {
        super(type, level);
    }

    @Override
    protected double swimSpeed() {
        return 1.5D;
    }

    @Override
    protected float watchDistance() {
        return 6.0F;
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        MobDrops.dropFew(level, this, source, TerrainItemsSpace.SCUTTERTAIL.get());
    }
}
