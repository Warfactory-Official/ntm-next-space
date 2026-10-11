// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2021-2022 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob.siege;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.MaterialShapeRoster;
import com.hbm.items.ModItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class SiegeTier {

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
                    .drop(ingot(Mats.MAT_COBALT), 2)
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
                    .drop(ingot(Mats.MAT_STAR), 1)
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
    }

    public static SiegeTier byId(int id) {
        return id >= 0 && id < REGISTRY.size() ? REGISTRY.get(id) : CLAY;
    }

    public List<ItemStack> drops() {
        List<ItemStack> out = new ArrayList<>(drops.size());
        for (Drop drop : drops) out.add(new ItemStack(drop.item().get(), drop.count()));
        return out;
    }

    private static Supplier<ItemLike> steel() {
        return ingot(Mats.MAT_STEEL);
    }

    private static Supplier<ItemLike> ingot(NTMMaterial material) {
        return () -> MaterialShapeRoster.require(MaterialShapes.INGOT, material);
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

    private record Drop(Supplier<? extends ItemLike> item, int count) {}
}
