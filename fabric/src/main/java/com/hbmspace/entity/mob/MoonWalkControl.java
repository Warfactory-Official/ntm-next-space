// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class MoonWalkControl extends MoveControl<Mob> {

    public MoonWalkControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (operation != Operation.MOVE_TO) {
            super.tick();
            return;
        }
        operation = Operation.WAIT;
        mob.setZza(0.0F);
        double dx = wantedX - mob.getX();
        double dz = wantedZ - mob.getZ();
        double dy = wantedY - Mth.floor(mob.getBoundingBox().minY + 0.5D);
        if (dx * dx + dy * dy + dz * dz < 2.500000277905201E-7D) return;

        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) + 90.0F;
        mob.setYRot(rotlerp(mob.getYRot(), yaw, 30.0F));
        float speed = (float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        mob.setSpeed(speed);
        mob.setZza(-speed);
        if (dy > 0.0D && dx * dx + dz * dz < 1.0D) mob.getJumpControl().jump();
    }
}
