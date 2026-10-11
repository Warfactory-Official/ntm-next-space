// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.api.environment.ISuffocationImmune;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toclient.GasFlamePayload;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.sound.ModSounds;
import com.hbmspace.entity.mob.ai.StepTowardsTargetGoal;
import com.hbmspace.entity.mob.ai.TankshellGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

public class EntityTankbot extends Monster implements ISuffocationImmune {
    private static final EntityDataAccessor<Integer> TARGET =
            SynchedEntityData.defineId(EntityTankbot.class, EntityDataSerializers.INT);

    private float headTargetYaw;
    private float headTargetYawO;

    public EntityTankbot(EntityType<? extends EntityTankbot> type, Level level) {
        super(type, level);
        setPathfindingMalus(PathType.WATER, -1.0F);
        deathTime = -50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 60.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MAX_HEALTH, 180.0D);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(
                1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.2D));
        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Mob.class,
                        0,
                        true,
                        true,
                        (target, level) -> !(target instanceof EntityTankbot)));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        goalSelector.addGoal(3, new StepTowardsTargetGoal(this, 50.0D, 0.2D, 200, 20, 0.6D));
        goalSelector.addGoal(4, new TankshellGoal(this, 2, 10, 30));
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
        super.tick();
        if (level().isClientSide()) aimTurret();
        setYRot(getYRot() + Mth.clamp(Mth.wrapDegrees(getYHeadRot() - getYRot()), -1.0F, 1.0F));
    }

    private void aimTurret() {
        headTargetYawO = headTargetYaw;
        if (!(level().getEntity(entityData.get(TARGET)) instanceof LivingEntity target)) return;
        double yaw =
                Math.atan2(target.getZ() - getZ(), target.getX() - getX()) * Mth.RAD_TO_DEG - 90.0D;
        headTargetYaw += (float) (Mth.wrapDegrees(yaw - headTargetYaw) * 0.1D);
    }

    public float headTargetYaw(float partialTick) {
        return Mth.lerp(partialTick, headTargetYawO, headTargetYaw);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownEgg && random.nextInt(10) == 0) {
            xpReward = 0;
            setHealth(0.0F);
            return true;
        }
        return super.hurtServer(level, source, plated(source, amount));
    }

    static float plated(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) amount = 0.0F;
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) amount = 0.0F;
        if (source.is(DamageTypeTags.IS_PROJECTILE)) amount *= 0.25F;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) amount *= 0.5F;
        if (amount > 50.0F) amount = 50.0F + (amount - 50.0F) * 0.25F;
        return amount;
    }

    @Override
    protected void tickDeath() {
        if (level() instanceof ServerLevel level) {
            if (deathTime == -30) playSound(ModSounds.CHOPPER_DAMAGE.get(), 10.0F, 1.0F);
            if (deathTime >= -30) {
                PacketWire.sendToAllAround(
                        new GasFlamePayload(
                                getX(),
                                getY() + 1.0D,
                                getZ(),
                                random.nextGaussian(),
                                0.4D,
                                random.nextGaussian(),
                                GasFlamePayload.DEFAULT_SCALE),
                        new TargetPoint(level, getX(), getY() + 1.0D, getZ(), 150));
            }
            if (deathTime == -5) {
                level.explode(
                        this, getX(), getY(), getZ(), 5.0F, true, Level.ExplosionInteraction.NONE);
                ExplosionCreator.composeEffectSmall(level, getX(), getY(), getZ());
                remove(RemovalReason.KILLED);
            }
        }
        super.tickDeath();
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
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
