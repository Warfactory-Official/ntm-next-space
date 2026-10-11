// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.api.environment.ISuffocationImmune;
import com.hbm.sound.ModSounds;
import com.hbmspace.entity.mob.ai.BehemothGunGoal;
import com.hbmspace.entity.mob.ai.StepTowardsTargetGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

public class EntityWarBehemoth extends Monster implements ISuffocationImmune {
    private static final EntityDataAccessor<Integer> TARGET =
            SynchedEntityData.defineId(EntityWarBehemoth.class, EntityDataSerializers.INT);

    private float aimPitch;
    private float aimPitchO;

    public EntityWarBehemoth(EntityType<? extends EntityWarBehemoth> type, Level level) {
        super(type, level);
        setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 50.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.STEP_HEIGHT, 5.0D);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(
                1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Mob.class,
                        0,
                        true,
                        true,
                        (target, level) -> !(target instanceof EntityWarBehemoth)));
        goalSelector.addGoal(3, new StepTowardsTargetGoal(this, 50.0D, 0.3D, 50, 10, 0.6D));
        goalSelector.addGoal(4, new BehemothGunGoal(this, 3, 35, 30));
        targetSelector.addGoal(5, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET, 0);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        entityData.set(TARGET, target != null ? target.getId() : 0);
    }

    @Override
    public void tick() {
        if (level().isClientSide()) aimBodyYaw();
        super.tick();
        if (level().isClientSide()) {
            aimBody();
        } else {
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.1D, 0.0D));
        }
    }

    private void aimBodyYaw() {
        if (!getInterpolation().hasActiveInterpolation()
                || !(level().getEntity(entityData.get(TARGET)) instanceof LivingEntity target))
            return;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double yaw = Math.atan2(dz, dx) * (180D / Math.PI) - 90D;
        ((BehemothYawTarget) getInterpolation()).hbmspace$pullYaw(yaw);
    }

    private void aimBody() {
        aimPitchO = aimPitch;
        if (!(level().getEntity(entityData.get(TARGET)) instanceof LivingEntity target)) return;
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - getEyeY();
        double dz = target.getZ() - getZ();
        double pitch = Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * -Mth.RAD_TO_DEG;
        aimPitch += (float) (Mth.wrapDegrees(pitch - aimPitch) * 0.1D);
    }

    public float aimPitch(float partialTick) {
        return Mth.lerp(partialTick, aimPitchO, aimPitch);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownEgg && random.nextInt(10) == 0) {
            xpReward = 0;
            setHealth(0.0F);
            return true;
        }
        return super.hurtServer(level, source, EntityTankbot.plated(source, amount));
    }

    @Override
    protected float getFlyingSpeed() {
        return 0.0F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_CYBERCRAB.get();
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        return spawnReason == EntitySpawnReason.CHUNK_GENERATION
                || super.checkSpawnRules(level, spawnReason);
    }
}
