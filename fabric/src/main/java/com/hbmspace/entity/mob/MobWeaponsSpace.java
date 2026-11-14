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

package com.hbmspace.entity.mob;

import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;
import com.hbm.items.weapon.sedna.factory.XFactoryEnergy;
import com.hbm.items.weapon.sedna.factory.XFactoryFlamer;
import com.hbm.particle.casing.Casing;
import com.hbm.particle.casing.CasingShape;

public final class MobWeaponsSpace {
    public static final BulletConfig R762_AP_STILL = XFactory762mm.r762_ap.clone().setKnockback(0);
    public static final BulletConfig EMERALD_OVERCHARGE_STILL =
            XFactoryEnergy.energy_emerald_overcharge.clone().setKnockback(0);
    public static final BulletConfig TESLA_OVERCHARGE_STILL =
            XFactoryEnergy.energy_tesla_overcharge.clone().setKnockback(0);
    public static final BulletConfig FLAME_BALEFIRE_STILL =
            XFactoryFlamer.flame_balefire.clone().setKnockback(0);

    public static final Casing TANK_CASING =
            Casing.template(CasingShape.SHOTGUN)
                    .tints(0xE5DD00, Casing.GAUGE_12)
                    .scale(10F)
                    .lifetime(60)
                    .register("TankGa");

    private MobWeaponsSpace() {}
}
