// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;
import com.hbm.items.weapon.sedna.factory.XFactoryEnergy;
import com.hbm.items.weapon.sedna.factory.XFactoryFlamer;
import com.hbm.particle.SpentCasing;

public final class MobWeaponsSpace {
    public static final BulletConfig R762_AP_STILL = XFactory762mm.r762_ap.clone().setKnockback(0);
    public static final BulletConfig EMERALD_OVERCHARGE_STILL =
            XFactoryEnergy.energy_emerald_overcharge.clone().setKnockback(0);
    public static final BulletConfig TESLA_OVERCHARGE_STILL =
            XFactoryEnergy.energy_tesla_overcharge.clone().setKnockback(0);
    public static final BulletConfig FLAME_BALEFIRE_STILL =
            XFactoryFlamer.flame_balefire.clone().setKnockback(0);

    public static final SpentCasing TANK_CASING =
            new SpentCasing(SpentCasing.CasingType.SHOTGUN)
                    .setColor(0xE5DD00, SpentCasing.COLOR_CASE_12GA)
                    .setScale(10F)
                    .register("TankGa")
                    .setMaxAge(60);

    private MobWeaponsSpace() {}
}
