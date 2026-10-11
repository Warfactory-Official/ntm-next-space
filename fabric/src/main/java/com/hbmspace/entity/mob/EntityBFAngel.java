// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.api.environment.ISuffocationImmune;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toclient.MukePayload;
import com.hbm.sound.ModSounds;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.items.MobItemsSpace;
import com.hbmspace.sound.MobSoundsSpace;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityBFAngel extends Mob implements Enemy, ISuffocationImmune {
    private static final EntityDataAccessor<Integer> WAYPOINT_X =
            SynchedEntityData.defineId(EntityBFAngel.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WAYPOINT_Y =
            SynchedEntityData.defineId(EntityBFAngel.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WAYPOINT_Z =
            SynchedEntityData.defineId(EntityBFAngel.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    getUUID(),
                    getDisplayName(),
                    BossEvent.BossBarColor.PINK,
                    BossEvent.BossBarOverlay.PROGRESS);

    private final List<Entity> secondaries = new ArrayList<>();

    public int courseChangeCooldown;
    public int scanCooldown;
    public int hurtCooldown;
    public int gatlingCharge;
    public boolean isChargingGatling;

    private @Nullable Entity target;
    private double lockedX;
    private double lockedY;
    private double lockedZ;
    private int aoeCooldown = 0;
    private int aoeStage = 0;
    private final Vec3[] strikePositions = new Vec3[3];
    private final int[] strikeTimers = new int[3];
    private final boolean[] strikeArmed = new boolean[3];

    public EntityBFAngel(EntityType<? extends EntityBFAngel> type, Level level) {
        super(type, level);
        setNoGravity(true);
        this.xpReward = 500;
        this.deathTime = -30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10000.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WAYPOINT_X, 0);
        builder.define(WAYPOINT_Y, 0);
        builder.define(WAYPOINT_Z, 0);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {}

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 500000D;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.hurtCooldown > 0) return false;

        boolean hit = super.hurtServer(level, source, amount);
        if (hit) this.hurtCooldown = 5;
        return hit;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }

        if (this.hurtCooldown > 0) this.hurtCooldown--;
        if (this.courseChangeCooldown > 0) this.courseChangeCooldown--;
        if (this.scanCooldown > 0) this.scanCooldown--;

        if (this.target != null && !this.target.isAlive()) this.target = null;

        if (this.scanCooldown <= 0) scanForTargets();

        if (this.target != null && this.courseChangeCooldown <= 0) setCourse();

        tickWeapons(level);
        if (this.target != null) tickStrikes(level);
        dash();
        if (this.target != null) face();
    }

    private void scanForTargets() {
        this.secondaries.clear();
        this.target = null;

        for (Entity entity : level().getEntities(this, getBoundingBox().inflate(100D, 50D, 100D))) {
            if (!entity.isAlive() || !canAttackClass(entity)) continue;

            if (entity instanceof Player player) {
                if (player.isCreative()) continue;
                if (player.hasEffect(MobEffects.INVISIBILITY)) continue;

                if (this.target == null || distanceToSqr(player) < distanceToSqr(this.target))
                    this.target = player;
            }

            if (entity instanceof LivingEntity living
                    && distanceToSqr(living) < 100D * 100D
                    && getSensing().hasLineOfSight(living)
                    && living != this.target) {
                this.secondaries.add(living);
            }
        }

        if (this.target == null && !this.secondaries.isEmpty()) {
            this.target = this.secondaries.get(this.random.nextInt(this.secondaries.size()));
        }

        this.scanCooldown = 50;
    }

    private boolean canAttackClass(Entity entity) {
        return entity.getClass() != getClass();
    }

    private void setCourse() {
        Vec3 vec = new Vec3(getX() - this.target.getX(), 0D, getZ() - this.target.getZ());
        if (this.random.nextInt(3) > 0)
            vec = vec.yRot((float) Math.PI * 2F * this.random.nextFloat());

        double length = vec.length();
        double overshoot = 35D;

        int wX = (int) Math.floor(this.target.getX() - vec.x / length * overshoot);
        int wZ = (int) Math.floor(this.target.getZ() - vec.z / length * overshoot);

        int surface =
                level().getHeight(Heightmap.Types.WORLD_SURFACE, wX, wZ) + this.random.nextInt(15);
        setWaypoint(wX, Math.max(surface, (int) this.target.getY() - 5), wZ);

        this.courseChangeCooldown = 40 + this.random.nextInt(20);
    }

    private void tickWeapons(ServerLevel level) {
        if (this.tickCount % 300 < 200) {
            if (!this.secondaries.isEmpty()) {
                Entity e = this.secondaries.get(this.random.nextInt(this.secondaries.size()));
                if (!e.isAlive()) this.secondaries.remove(e);
            }

            if (this.target == null) return;

            if (!this.isChargingGatling) {
                this.isChargingGatling = true;
                this.gatlingCharge = 70;
                return;
            }

            if (this.gatlingCharge > 10) {
                this.lockedX = this.target.getX();
                this.lockedY = this.target.getY();
                this.lockedZ = this.target.getZ();
            }

            this.gatlingCharge--;

            if (this.gatlingCharge == 30) {
                level.playSound(
                        null, this, MobSoundsSpace.BFA_TALK.get(), getSoundSource(), 10.0F, 1.0F);
            }

            if (this.gatlingCharge <= 0) {
                fireBeamAt(level, this.lockedX, this.lockedY, this.lockedZ);
                this.isChargingGatling = false;
                this.gatlingCharge = 0;
            }

        } else if (this.tickCount % 20 == 0) {
            if (!this.secondaries.isEmpty()) {
                Entity e = this.secondaries.get(this.random.nextInt(this.secondaries.size()));
                if (e.isAlive()) rocketAttack(e);
                else this.secondaries.remove(e);
            } else if (this.target != null) {
                rocketAttack(this.target);
            }

        } else if (this.tickCount % 4 == 2 && this.target != null) {
            rocketAttack(this.target);
        }
    }

    private void tickStrikes(ServerLevel level) {
        if (this.aoeCooldown <= 0 && this.aoeStage == 0) {
            this.aoeStage = 1;

            for (int i = 0; i < 3; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 12;
                double offsetZ = (this.random.nextDouble() - 0.5) * 12;
                this.strikePositions[i] =
                        new Vec3(
                                this.target.getX() + offsetX,
                                this.target.getY(),
                                this.target.getZ() + offsetZ);
                this.strikeTimers[i] = -i * 4;
                this.strikeArmed[i] = true;
            }

            this.aoeCooldown = 200;
        }

        boolean allDone = true;
        for (int i = 0; i < 3; i++) {
            if (!this.strikeArmed[i]) continue;

            this.strikeTimers[i]++;
            if (this.strikeTimers[i] < 0) {
                allDone = false;
                continue;
            }

            Vec3 pos = this.strikePositions[i];

            if (this.strikeTimers[i] < 60 && this.strikeTimers[i] % 5 == 0) {
                level.playSound(
                        null,
                        pos.x,
                        pos.y,
                        pos.z,
                        MobSoundsSpace.STINGER_LOCK_ON.get(),
                        SoundSource.HOSTILE,
                        1.0F,
                        1F);
                PacketWire.sendToAllAround(
                        new RedDustBurstPayload(pos.x, pos.y, pos.z, 35, 0.2D),
                        new TargetPoint(level, pos.x, pos.y, pos.z, 100));
            }

            if (this.strikeTimers[i] >= 60) {
                fireDownwardBeamAt(level, pos.x, pos.y, pos.z);
                this.strikeArmed[i] = false;
            } else {
                allDone = false;
            }
        }

        if (allDone) this.aoeStage = 0;
        if (this.aoeCooldown > 0) this.aoeCooldown--;
    }

    private void dash() {
        setDeltaMovement(Vec3.ZERO);
        if (this.target == null) return;

        if (this.courseChangeCooldown <= 0) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double radius = 15 + this.random.nextInt(10);
            setWaypoint(
                    (int) (this.target.getX() + Math.cos(angle) * radius),
                    (int) (this.target.getY() + 5 + this.random.nextInt(5)),
                    (int) (this.target.getZ() + Math.sin(angle) * radius));
            this.courseChangeCooldown = 85;
        }

        if (this.courseChangeCooldown <= 10) {
            Vec3 dir =
                    new Vec3(
                                    getWaypointX() - getX(),
                                    getWaypointY() - getY(),
                                    getWaypointZ() - getZ())
                            .normalize();
            double dashSpeed = 2.0D * (this.courseChangeCooldown / 15.0);
            setDeltaMovement(dir.scale(dashSpeed));
        }

        this.courseChangeCooldown--;
    }

    private void face() {
        double dx = this.target.getX() - getX();
        double dy = this.target.getY() + this.target.getBbHeight() * 0.5 - (getY() + 1);
        double dz = this.target.getZ() - getZ();

        float targetYaw = (float) (Math.atan2(dz, dx) * 180D / Math.PI) + 90F;
        setYRot(getYRot() + Mth.wrapDegrees(targetYaw - getYRot()) * 0.15F);

        double dist = Math.sqrt(dx * dx + dz * dz);
        float targetPitch = (float) (Math.atan2(dy, dist) * 180D / Math.PI);
        setXRot(getXRot() + (targetPitch - getXRot()) * 0.1F);
    }

    private void fireBeamAt(ServerLevel level, double x, double y, double z) {
        double spawnX = getX() - 1;
        double spawnY = getY() + 4;
        double spawnZ = getZ();

        EntityBulletBeamBase bullet =
                new EntityBulletBeamBase(this, MobWeaponsSpace.TESLA_OVERCHARGE_STILL, 8F);
        bullet.setPos(spawnX, spawnY, spawnZ);
        bullet.setRotationsFromVector(new Vec3(x - spawnX, y - spawnY, z - spawnZ));
        bullet.performHitscanExternal(250D);
        level.addFreshEntity(bullet);
        playSound(MobSoundsSpace.BFA_SHOOT.get(), 5.0F, 1.0F);
    }

    private void fireDownwardBeamAt(ServerLevel level, double x, double y, double z) {
        EntityBulletBeamBase bullet =
                new EntityBulletBeamBase(this, MobWeaponsSpace.TESLA_OVERCHARGE_STILL, 6F);
        bullet.setPos(x, y + 40, z);
        bullet.setRotationsFromVector(new Vec3(0, -1, 0));
        bullet.performHitscanExternal(250D);
        level.addFreshEntity(bullet);
        playSound(MobSoundsSpace.BFA_SHOOT.get(), 5.0F, 1.0F);
    }

    private void rocketAttack(Entity e) {
        Vec3 heading =
                new Vec3(
                                e.getX() - getX(),
                                e.getY() + e.getBbHeight() / 2.0 - getY() - 0.5D,
                                e.getZ() - getZ())
                        .normalize();

        EntityBulletBaseMK4 bullet =
                new EntityBulletBaseMK4(
                        this, MobWeaponsSpace.FLAME_BALEFIRE_STILL, 0.5F, 1, 0, 0, 0);
        bullet.setDeltaMovement(heading);
        level().addFreshEntity(bullet);
        playSound(ModSounds.RICHARD_FIRE.get(), 5.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) this.bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void tickDeath() {
        setDeltaMovement(getDeltaMovement().subtract(0D, 0.05D, 0D));

        if (this.deathTime == -10) {
            level().playSound(
                            null, this, ModSounds.CHOPPER_DAMAGE.get(), getSoundSource(), 10F, 1F);
        }

        if (this.deathTime == 19 && level() instanceof ServerLevel server) {
            PacketWire.sendToAllAround(
                    new MukePayload(getX(), getY() + 0.5, getZ(), true, false),
                    new TargetPoint(server, getX(), getY(), getZ(), 250));
            server.playSound(
                    null,
                    getX(),
                    getY(),
                    getZ(),
                    ModSounds.GUN_MINI_NUKE_EXPLOSION.get(),
                    SoundSource.HOSTILE,
                    15.0F,
                    1.0F);

            spawnAtLocation(server, new ItemStack(MobItemsSpace.CORE_ANGEL.get()), 1F);

            Component flicker =
                    Component.translatable("chat.hbmspace.angel.flicker")
                            .withStyle(ChatFormatting.RED);
            for (Player player :
                    server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(200D))) {
                player.sendSystemMessage(flicker);
            }

            CelestialBody body = CelestialBody.getBody(server);
            if (body.getTrait(CBT_Invasion.class) == null)
                body.modifyTraits(new CBT_Invasion(0, 122, false));
        }

        super.tickDeath();
    }

    @Override
    protected float getSoundVolume() {
        return 10F;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    public void setWaypoint(int x, int y, int z) {
        this.entityData.set(WAYPOINT_X, x);
        this.entityData.set(WAYPOINT_Y, y);
        this.entityData.set(WAYPOINT_Z, z);
    }

    public int getWaypointX() {
        return this.entityData.get(WAYPOINT_X);
    }

    public int getWaypointY() {
        return this.entityData.get(WAYPOINT_Y);
    }

    public int getWaypointZ() {
        return this.entityData.get(WAYPOINT_Z);
    }
}
