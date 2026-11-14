/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2021-2022 HbmMods (The Bobcat)
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2021-2022 HbmMods (The Bobcat). All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.entity.mob.siege;

import com.hbm.inventory.material.Material;
import com.hbm.inventory.material.MaterialShape;
import com.hbm.inventory.material.Materials;
import com.hbm.items.MaterialShapeRoster;
import com.hbm.items.ModItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.ItemLike;

public final class SiegeTier {

    private static final SiegeTier[] BY_ID = new SiegeTier[100];
    private static final List<SiegeTier> REGISTRY = new ArrayList<>();
    public static final List<SiegeTier> TIERS = Collections.unmodifiableList(REGISTRY);

    public static final SiegeTier DEFAULT_BUFF =
            new SiegeTier(20, "buff").drop(steel(), 1).dr(0.2F).dmg(2F).laser(0F, 0F, false);
    public static final SiegeTier CLAY =
            new SiegeTier(30, "clay").drop(steel(), 1).dr(0.2F).dmg(3F).laser(0F, 0F, false);
    public static final SiegeTier STONE =
            new SiegeTier(40, "stone")
                    .drop(steel(), 4)
                    .dr(0.3F)
                    .dt(1F)
                    .fp()
                    .dmg(5F)
                    .laser(0F, 0F, true);
    public static final SiegeTier IRON =
            new SiegeTier(50, "iron")
                    .drop(steel(), 6)
                    .dr(0.3F)
                    .dt(2F)
                    .fp()
                    .dmg(7.5F)
                    .ff()
                    .laser(0F, 1F, true);
    public static final SiegeTier SILVER =
            new SiegeTier(70, "silver")
                    .drop(steel(), 8)
                    .dr(0.5F)
                    .dt(3F)
                    .nf()
                    .fp()
                    .dmg(10F)
                    .sp(0.5F)
                    .ff()
                    .laser(0.01F, 1F, true);
    public static final SiegeTier GOLD =
            new SiegeTier(100, "gold")
                    .drop(steel(), 8)
                    .drop(ModItems.CIRCUIT_CAPACITOR, 2)
                    .dr(0.5F)
                    .dt(5F)
                    .nf()
                    .fp()
                    .dmg(15F)
                    .sp(0.5F)
                    .ff()
                    .laser(0.02F, 1.5F, true);
    public static final SiegeTier DESH =
            new SiegeTier(150, "desh")
                    .drop(steel(), 16)
                    .drop(ModItems.CIRCUIT_CAPACITOR, 3)
                    .dr(0.7F)
                    .dt(7F)
                    .nf()
                    .fp()
                    .dmg(25F)
                    .sp(0.5F)
                    .ff()
                    .laser(0.05F, 1.5F, true);
    public static final SiegeTier SCHRAB =
            new SiegeTier(250, "schrab")
                    .drop(steel(), 32)
                    .drop(ingot(Materials.COBALT), 2)
                    .drop(ModItems.CIRCUIT_ADVANCED, 3)
                    .dr(0.7F)
                    .dt(10F)
                    .nf()
                    .fp()
                    .dmg(50F)
                    .sp(1F)
                    .ff()
                    .laser(0.1F, 2F, true);
    public static final SiegeTier DNT =
            new SiegeTier(500, "dnt")
                    .drop(ingot(Materials.STARMETAL), 1)
                    .dr(0.9F)
                    .dt(20F)
                    .nf()
                    .fp()
                    .dmg(100F)
                    .sp(1F)
                    .ff()
                    .laser(0.2F, 2F, true);

    public final int id;
    public final String name;
    public final float health;
    public float dt = 0F;
    public float dr = 0F;
    public float damageMod = 1F;
    public float speedMod = 0F;
    public boolean fireProof = false;
    public boolean noFall = false;
    public boolean noFriendlyFire = false;
    public float laserBreak = 0F;
    public float laserExplosive = 0F;
    public boolean laserIncendiary = false;
    private final List<Drop> drops = new ArrayList<>();

    private SiegeTier(float baseHealth, String name) {
        this.id = REGISTRY.size();
        this.health = baseHealth;
        this.name = name;
        REGISTRY.add(this);
        BY_ID[id] = this;
    }

    public static SiegeTier byId(int id) {
        var tier = BY_ID[id];
        return tier != null ? tier : CLAY;
    }

    public List<Drop> drops() {
        return Collections.unmodifiableList(drops);
    }

    private static Supplier<ItemLike> steel() {
        return ingot(Materials.STEEL);
    }

    private static Supplier<ItemLike> ingot(Material material) {
        return () -> MaterialShapeRoster.require(MaterialShape.INGOT, material);
    }

    private SiegeTier drop(Supplier<? extends ItemLike> item, int count) {
        drops.add(new Drop(item, count));
        return this;
    }

    private SiegeTier dt(float dt) {
        this.dt = dt;
        return this;
    }

    private SiegeTier dr(float dr) {
        this.dr = dr;
        return this;
    }

    private SiegeTier dmg(float dmg) {
        this.damageMod = dmg;
        return this;
    }

    private SiegeTier sp(float sp) {
        this.speedMod = sp;
        return this;
    }

    private SiegeTier fp() {
        this.fireProof = true;
        return this;
    }

    private SiegeTier nf() {
        this.noFall = true;
        return this;
    }

    private SiegeTier ff() {
        this.noFriendlyFire = true;
        return this;
    }

    private SiegeTier laser(float breaking, float explosive, boolean incendiary) {
        this.laserBreak = breaking;
        this.laserExplosive = explosive;
        this.laserIncendiary = incendiary;
        return this;
    }

    public record Drop(Supplier<? extends ItemLike> item, int count) {}
}
