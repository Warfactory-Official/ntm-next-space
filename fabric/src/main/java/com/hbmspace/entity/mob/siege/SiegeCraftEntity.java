/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2022 HbmMods (The Bobcat)
 * SPDX-FileCopyrightText: 2026 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2022 HbmMods (The Bobcat). All rights reserved.
 * Copyright (C) 2026 JamesH_2. All rights reserved.
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

import com.hbm.entity.ModEntities;
import com.hbm.entity.mob.UFOBaseEntity;
import com.hbm.entity.projectile.SiegeLaserEntity;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.lib.ModDamageTypes;
import com.hbm.packet.PacketWire;
import com.hbm.packet.layout.MukeTinyLayout;
import com.hbm.packet.layout.PlasmaBlastLayout;
import com.hbm.packet.layout.SparkBurstLayout;
import com.hbm.sound.ModSounds;
import com.hbm.sound.WorldSounds;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.ContaminationUtil;
import com.hbmspace.packet.layout.SpaceRedDustBurst;
import com.hbmspace.sound.MobSoundsSpace;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SiegeCraftEntity extends UFOBaseEntity {
    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(SiegeCraftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LOCKON_X =
            SynchedEntityData.defineId(SiegeCraftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LOCKON_Y =
            SynchedEntityData.defineId(SiegeCraftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LOCKON_Z =
            SynchedEntityData.defineId(SiegeCraftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> BEAM =
            SynchedEntityData.defineId(SiegeCraftEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int MAX_LIFETIME = 60 * 4 * 20;

    private int attackCooldown;
    private int beamCountdown;
    private int lifetime = 0;
    private boolean isRetreating = false;

    public SiegeCraftEntity(EntityType<? extends SiegeCraftEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TIER, 0);
        builder.define(LOCKON_X, 0F);
        builder.define(LOCKON_Y, 0F);
        builder.define(LOCKON_Z, 0F);
        builder.define(BEAM, false);
    }

    public void setTier(SiegeTier tier) {
        entityData.set(TIER, tier.id);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(tier.speedMod);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(tier.health * 25);
        setHealth(getMaxHealth());
    }

    public SiegeTier getTier() {
        return SiegeTier.byId(entityData.get(TIER));
    }

    public void setBeam(boolean beam) {
        entityData.set(BEAM, beam);
    }

    public boolean getBeam() {
        return entityData.get(BEAM);
    }

    public void setLockon(double x, double y, double z) {
        entityData.set(LOCKON_X, (float) x);
        entityData.set(LOCKON_Y, (float) y);
        entityData.set(LOCKON_Z, (float) z);
    }

    public Vec3 getLockon() {
        return new Vec3(
                entityData.get(LOCKON_X), entityData.get(LOCKON_Y), entityData.get(LOCKON_Z));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isInvulnerableTo(level, source)) return false;

        SiegeTier tier = getTier();

        if (tier.fireProof && source.is(DamageTypeTags.IS_FIRE)) {
            clearFire();
            return false;
        }

        if (tier.noFriendlyFire
                && source.getEntity() != null
                && !(source.getEntity() instanceof Player)) {
            return false;
        }

        damage -= tier.dt;

        if (damage < 0) {
            WorldSounds.play(
                    level,
                    null,
                    this,
                    SoundEvents.ITEM_BREAK.value(),
                    getSoundSource(),
                    5F,
                    1.0F + random.nextFloat() * 0.5F);
            return false;
        }

        damage *= 1F - tier.dr;

        return super.hurtServer(level, source, damage);
    }

    @Override
    protected void tickDeath() {
        this.beamCountdown = 200;
        setBeam(false);
        setDeltaMovement(getDeltaMovement().subtract(0D, 0.05D, 0D));

        if (this.deathTime == 19 && level() instanceof ServerLevel server) {
            PacketWire.sendToAllAround(
                    MukeTinyLayout.encode(getX(), getY() + 0.5, getZ()),
                    new TargetPoint(server, getX(), getY(), getZ(), 250));
            WorldSounds.explosion(
                    server,
                    null,
                    getX(),
                    getY(),
                    getZ(),
                    ModSounds.GUN_MINI_NUKE_EXPLOSION.get(),
                    SoundSource.HOSTILE,
                    15.0F,
                    1.0F);
        }

        super.tickDeath();
    }

    @Override
    protected int getScanRange() {
        return 100;
    }

    @Override
    protected int targetHeightOffset() {
        return 7 + random.nextInt(5);
    }

    @Override
    protected int wanderHeightOffset() {
        return 10 + random.nextInt(2);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (isRemoved()) return;

        if (this.courseChangeCooldown > 0) this.courseChangeCooldown--;
        if (this.scanCooldown > 0) this.scanCooldown--;

        lifetime++;
        if (lifetime > MAX_LIFETIME) isRetreating = true;

        if (isRetreating) {
            this.target = null;
            setWaypoint((int) getX(), 255, (int) getZ());
            approachPosition(20);
            if (getY() > 250 || lifetime > MAX_LIFETIME + 300) {
                discard();
                return;
            }
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.beamCountdown > 0) this.beamCountdown--;

        if (random.nextInt(50) == 0) {
            PacketWire.sendToAllAround(
                    SparkBurstLayout.encode(
                            getX() + random.nextGaussian() * 2,
                            getY() + random.nextGaussian(),
                            getZ() + random.nextGaussian() * 2,
                            2 + random.nextInt(3),
                            false),
                    new TargetPoint(level, getX(), getY(), getZ(), 50));
        }

        setBeam(tickBeam(level));

        if (this.attackCooldown == 0 && this.target != null) {
            this.attackCooldown = 30 + random.nextInt(10);
            fireLasers(level);
        }

        if (this.courseChangeCooldown > 0) {
            double speed =
                    this.target == null
                            ? 0.5D
                            : 1.0D + getAttributeValue(Attributes.MOVEMENT_SPEED) * 3.0D;
            if (isRetreating) speed *= 2.0D;
            approachPosition(speed);
        }
    }

    private boolean tickBeam(ServerLevel level) {
        if (this.target == null || this.beamCountdown <= 0) {
            this.beamCountdown = 300;
            return false;
        }

        if (this.beamCountdown >= 60 && this.beamCountdown < 120) {
            setLockon(
                    this.target.getX(),
                    this.target.getY() + this.target.getBbHeight() * 0.5,
                    this.target.getZ());
            if (this.beamCountdown == 110) {
                WorldSounds.play(
                        level,
                        null,
                        this.target,
                        MobSoundsSpace.STINGER_LOCK_ON.get(),
                        SoundSource.HOSTILE,
                        2F,
                        0.75F);
            }
        }

        Vec3 lockon = getLockon();

        if (this.beamCountdown >= 40 && this.beamCountdown < 100) {
            PacketWire.sendToAllAround(
                    SpaceRedDustBurst.encode(lockon.x, lockon.y, lockon.z, 5, 0.2D),
                    new TargetPoint(level, lockon.x, lockon.y, lockon.z, 100));
        }

        if (this.beamCountdown >= 40) return false;

        if (this.beamCountdown == 39) {
            WorldSounds.play(
                    level,
                    null,
                    lockon.x,
                    lockon.y,
                    lockon.z,
                    ModSounds.UFO_BLAST.get(),
                    SoundSource.HOSTILE,
                    5.0F,
                    0.9F + level.getRandom().nextFloat() * 0.2F);
        }

        for (Entity e : level.getEntities(this, new AABB(lockon, lockon).inflate(2))) {
            if (!canAttackClass(e)) continue;
            e.hurtServer(level, level.damageSources().source(ModDamageTypes.COMBINE, this), 1000F);
            e.igniteForSeconds(5);
            if (e instanceof LivingEntity living) {
                ContaminationUtil.contaminate(
                        living, HazardType.RADIATION, ContaminationType.CREATIVE, 5F);
            }
        }

        PacketWire.sendToAllAround(
                PlasmaBlastLayout.encode(
                        lockon.x,
                        lockon.y,
                        lockon.z,
                        0.0F,
                        0.75F,
                        1.0F,
                        -90 + random.nextFloat() * 180,
                        random.nextFloat() * 180F,
                        5F),
                new TargetPoint(level, lockon.x, lockon.y, lockon.z, 150));
        return true;
    }

    private static boolean canAttackClass(Entity e) {
        return e.getClass() != Creeper.class && e.getClass() != Ghast.class;
    }

    private void fireLasers(ServerLevel level) {
        Vec3 vec =
                new Vec3(
                                this.target.getX() - getX(),
                                this.target.getY() + this.target.getBbHeight() * 0.5 - getY(),
                                this.target.getZ() - getZ())
                        .normalize();
        SiegeTier tier = getTier();

        float health = getHealth() / getMaxHealth();
        int r = (int) (0xff * (1 - health));
        int g = (int) (0xff * health);
        int color = (r << 16) | (g << 8);

        for (int i = 0; i < 7; i++) {
            Vec3 copy = vec.yRot((float) Math.PI / 180F * (i - 3) * 5F);

            SiegeLaserEntity laser =
                    new SiegeLaserEntity(ModEntities.SIEGE_LASER.get(), level, this);
            laser.setPos(getX(), getY(), getZ());
            laser.setThrowableHeading(copy.x, copy.y, copy.z, 1F, 0.0F);
            laser.setColor(color);
            laser.setDamage(tier.damageMod);
            laser.setBreakChance(tier.laserBreak * 2);
            if (tier.laserIncendiary) laser.setIncendiary();
            level.addFreshEntity(laser);
        }

        WorldSounds.play(this, ModSounds.BALLS_LASER.get(), 2.0F, 1.0F);
    }

    @Override
    protected void setCourseWithoutTarget() {
        int x = (int) Math.floor(getX() + random.nextGaussian() * 15);
        int z = (int) Math.floor(getZ() + random.nextGaussian() * 15);
        setWaypoint(x, surfaceAt(x, z) + 5 + random.nextInt(6), z);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData) {
        setTier(SiegeTier.byId(random.nextInt(SiegeTier.TIERS.size())));
        return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("siegeTier", getTier().id);
        output.putInt("lifetime", lifetime);
        output.putBoolean("isRetreating", isRetreating);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(TIER, SiegeTier.byId(input.getIntOr("siegeTier", 0)).id);
        lifetime = input.getIntOr("lifetime", 0);
        isRetreating = input.getBooleanOr("isRetreating", false);
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public void setBeamCountdown(int beamCountdown) {
        this.beamCountdown = beamCountdown;
    }
}
