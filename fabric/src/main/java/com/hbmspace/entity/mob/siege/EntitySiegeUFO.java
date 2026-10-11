// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 George Paton
// SPDX-FileCopyrightText: 2026 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob.siege;

import com.hbm.entity.mob.EntityUFOBase;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbmspace.entity.mob.MobWeaponsSpace;
import com.hbmspace.sound.MobSoundsSpace;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntitySiegeUFO extends EntityUFOBase {
    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(EntitySiegeUFO.class, EntityDataSerializers.INT);
    private static final int MAX_LIFETIME = 60 * 2 * 20;

    private int attackCooldown;
    private double lastTargetX;
    private double lastTargetY;
    private double lastTargetZ;
    private int lifetime = 0;
    private boolean isRetreating = false;

    public EntitySiegeUFO(EntityType<? extends EntitySiegeUFO> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TIER, 0);
    }

    public void setTier(SiegeTier tier) {
        entityData.set(TIER, tier.id);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(tier.speedMod);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(tier.health * 0.25);
        setHealth(getMaxHealth());
    }

    public SiegeTier getTier() {
        return SiegeTier.byId(entityData.get(TIER));
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
            level.playSound(
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
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (isRemoved()) return;

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

        if (this.courseChangeCooldown > 0) this.courseChangeCooldown--;
        if (this.scanCooldown > 0) this.scanCooldown--;
        if (this.attackCooldown > 0) this.attackCooldown--;

        if (!isRetreating && this.target != null) {
            double eyeY = this.target.getY() + this.target.getEyeHeight();

            if (this.lastTargetX == 0 && this.lastTargetY == 0 && this.lastTargetZ == 0) {
                this.lastTargetX = this.target.getX();
                this.lastTargetY = eyeY;
                this.lastTargetZ = this.target.getZ();
            }

            if (random.nextInt(10) == 0) {
                this.lastTargetX = this.target.getX();
                this.lastTargetY = eyeY;
                this.lastTargetZ = this.target.getZ();
            }

            if (this.attackCooldown == 0) {
                this.attackCooldown = 20 + random.nextInt(25);
                fire(level);
            }
        }

        if (this.courseChangeCooldown > 0) {
            double speed =
                    this.target == null
                            ? 0.5D
                            : 1.0D + getAttributeValue(Attributes.MOVEMENT_SPEED);
            if (isRetreating) speed *= 2.0D;
            approachPosition(speed);
        }
    }

    private void fire(ServerLevel level) {
        EntityBulletBeamBase bullet =
                new EntityBulletBeamBase(this, MobWeaponsSpace.EMERALD_OVERCHARGE_STILL, 8F);
        bullet.setPos(getX(), getY(), getZ());
        bullet.setRotationsFromVector(
                new Vec3(lastTargetX - getX(), lastTargetY - getY(), lastTargetZ - getZ()));
        bullet.performHitscanExternal(250D);
        level.addFreshEntity(bullet);
        playSound(MobSoundsSpace.BFA_SHOOT.get(), 2.0F, 1.0F);
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

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level, DamageSource source, boolean killedByPlayer) {
        for (ItemStack drop : getTier().drops()) spawnAtLocation(level, drop, 0F);
    }

    public boolean isRetreating() {
        return isRetreating;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }
}
