/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-FileCopyrightText: 2026 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
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

package com.hbmspace.entity.mob;

import com.hbm.api.environment.SuffocationImmune;
import com.hbm.entity.projectile.BulletBaseMK4Entity;
import com.hbm.entity.projectile.BulletBeamBaseEntity;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.packet.PacketWire;
import com.hbm.packet.layout.MukeTinyLayout;
import com.hbm.sound.ModSounds;
import com.hbm.sound.WorldSounds;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.packet.layout.SpaceRedDustBurst;
import com.hbmspace.sound.MobSoundsSpace;
import java.util.ArrayList;
import java.util.EnumSet;
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
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BFAngelEntity extends Mob implements Enemy, SuffocationImmune {
    private static final EntityDataAccessor<Integer> WAYPOINT_X =
            SynchedEntityData.defineId(BFAngelEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WAYPOINT_Y =
            SynchedEntityData.defineId(BFAngelEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WAYPOINT_Z =
            SynchedEntityData.defineId(BFAngelEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    getUUID(),
                    getDisplayName(),
                    BossEvent.BossBarColor.PINK,
                    BossEvent.BossBarOverlay.PROGRESS);

    private final List<LivingEntity> secondaries = new ArrayList<>();

    public int courseChangeCooldown;
    public int scanCooldown;
    public int hurtCooldown;
    public int gatlingCharge;
    public boolean isChargingGatling;

    private double lockedX;
    private double lockedY;
    private double lockedZ;
    private int aoeCooldown = 0;
    private int aoeStage = 0;
    private final Vec3[] strikePositions = new Vec3[3];
    private final int[] strikeTimers = new int[3];
    private final boolean[] strikeArmed = new boolean[3];
    private @Nullable DamageSource deathLoot;
    private @Nullable EntityReference<Entity> deathLootDirect;
    private @Nullable EntityReference<Entity> deathLootAttacker;
    private @Nullable EntityReference<Player> deathLootPlayer;
    private int deathLootPlayerMemoryTime;

    public BFAngelEntity(EntityType<? extends BFAngelEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        this.moveControl = new DashControl(this);
        this.lookControl = new EaseLookControl(this);
        this.xpReward = 500;
        this.deathTime = -30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10000.0D);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new ScanGoal(this));
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

        LivingEntity target = getTarget();
        if (target != null && this.courseChangeCooldown <= 0) setCourse(target);

        tickWeapons(level, target);
        if (target == null) return;

        tickStrikes(level, target);
        if (this.courseChangeCooldown <= 10) {
            this.moveControl.setWantedPosition(
                    getWaypointX(),
                    getWaypointY(),
                    getWaypointZ(),
                    2.0D * (this.courseChangeCooldown / 15.0));
        }
        this.courseChangeCooldown--;
        this.lookControl.setLookAt(
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
    }

    private void scan() {
        this.secondaries.clear();
        LivingEntity target = null;

        for (Entity entity : level().getEntities(this, getBoundingBox().inflate(100D, 50D, 100D))) {
            if (!entity.isAlive() || !canAttackClass(entity)) continue;

            if (entity instanceof Player player) {
                if (player.isCreative()) continue;
                if (player.hasEffect(MobEffects.INVISIBILITY)) continue;

                if (target == null || distanceToSqr(player) < distanceToSqr(target))
                    target = player;
            }

            if (entity instanceof LivingEntity living
                    && distanceToSqr(living) < 100D * 100D
                    && getSensing().hasLineOfSight(living)
                    && living != target) {
                this.secondaries.add(living);
            }
        }

        if (target == null && !this.secondaries.isEmpty()) {
            target = this.secondaries.get(this.random.nextInt(this.secondaries.size()));
        }

        setTarget(target);
        this.scanCooldown = 50;
    }

    private boolean canAttackClass(Entity entity) {
        return entity.getClass() != getClass();
    }

    private void setCourse(LivingEntity target) {
        Vec3 vec = new Vec3(getX() - target.getX(), 0D, getZ() - target.getZ());
        if (this.random.nextInt(3) > 0)
            vec = vec.yRot((float) Math.PI * 2F * this.random.nextFloat());

        double length = vec.length();
        double overshoot = 35D;

        int wX = (int) Math.floor(target.getX() - vec.x / length * overshoot);
        int wZ = (int) Math.floor(target.getZ() - vec.z / length * overshoot);

        int surface =
                level().getHeight(Heightmap.Types.WORLD_SURFACE, wX, wZ) + this.random.nextInt(15);
        setWaypoint(wX, Math.max(surface, (int) target.getY() - 5), wZ);

        this.courseChangeCooldown = 40 + this.random.nextInt(20);
    }

    private void tickWeapons(ServerLevel level, @Nullable LivingEntity target) {
        if (this.tickCount % 300 < 200) {
            if (!this.secondaries.isEmpty()) {
                LivingEntity e = this.secondaries.get(this.random.nextInt(this.secondaries.size()));
                if (!e.isAlive()) this.secondaries.remove(e);
            }

            if (target == null) return;

            if (!this.isChargingGatling) {
                this.isChargingGatling = true;
                this.gatlingCharge = 70;
                return;
            }

            if (this.gatlingCharge > 10) {
                this.lockedX = target.getX();
                this.lockedY = target.getY();
                this.lockedZ = target.getZ();
            }

            this.gatlingCharge--;

            if (this.gatlingCharge == 30) {
                WorldSounds.play(
                        level,
                        null,
                        this,
                        MobSoundsSpace.BFA_TALK.get(),
                        getSoundSource(),
                        10.0F,
                        1.0F);
            }

            if (this.gatlingCharge <= 0) {
                fireBeamAt(level, this.lockedX, this.lockedY, this.lockedZ);
                this.isChargingGatling = false;
                this.gatlingCharge = 0;
            }

        } else if (this.tickCount % 20 == 0) {
            if (!this.secondaries.isEmpty()) {
                LivingEntity e = this.secondaries.get(this.random.nextInt(this.secondaries.size()));
                if (e.isAlive()) rocketAttack(e);
                else this.secondaries.remove(e);
            } else if (target != null) {
                rocketAttack(target);
            }

        } else if (this.tickCount % 4 == 2 && target != null) {
            rocketAttack(target);
        }
    }

    private void tickStrikes(ServerLevel level, LivingEntity target) {
        if (this.aoeCooldown <= 0 && this.aoeStage == 0) {
            this.aoeStage = 1;

            for (int i = 0; i < 3; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 12;
                double offsetZ = (this.random.nextDouble() - 0.5) * 12;
                this.strikePositions[i] =
                        new Vec3(target.getX() + offsetX, target.getY(), target.getZ() + offsetZ);
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
                WorldSounds.play(
                        level,
                        null,
                        pos.x,
                        pos.y,
                        pos.z,
                        MobSoundsSpace.STINGER_LOCK_ON.get(),
                        SoundSource.HOSTILE,
                        1.0F,
                        1F);
                PacketWire.sendToAllAround(
                        SpaceRedDustBurst.encode(pos.x, pos.y, pos.z, 35, 0.2D),
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

    private void fireBeamAt(ServerLevel level, double x, double y, double z) {
        double spawnX = getX() - 1;
        double spawnY = getY() + 4;
        double spawnZ = getZ();

        BulletBeamBaseEntity bullet =
                new BulletBeamBaseEntity(this, MobWeaponsSpace.TESLA_OVERCHARGE_STILL, 8F);
        bullet.setPos(spawnX, spawnY, spawnZ);
        bullet.setRotationsFromVector(new Vec3(x - spawnX, y - spawnY, z - spawnZ));
        bullet.performHitscanExternal(250D);
        level.addFreshEntity(bullet);
        WorldSounds.play(this, MobSoundsSpace.BFA_SHOOT.get(), 5.0F, 1.0F);
    }

    private void fireDownwardBeamAt(ServerLevel level, double x, double y, double z) {
        BulletBeamBaseEntity bullet =
                new BulletBeamBaseEntity(this, MobWeaponsSpace.TESLA_OVERCHARGE_STILL, 6F);
        bullet.setPos(x, y + 40, z);
        bullet.setRotationsFromVector(new Vec3(0, -1, 0));
        bullet.performHitscanExternal(250D);
        level.addFreshEntity(bullet);
        WorldSounds.play(this, MobSoundsSpace.BFA_SHOOT.get(), 5.0F, 1.0F);
    }

    private void rocketAttack(Entity e) {
        Vec3 heading =
                new Vec3(
                                e.getX() - getX(),
                                e.getY() + e.getBbHeight() / 2.0 - getY() - 0.5D,
                                e.getZ() - getZ())
                        .normalize();

        BulletBaseMK4Entity bullet =
                new BulletBaseMK4Entity(
                        this, MobWeaponsSpace.FLAME_BALEFIRE_STILL, 0.5F, 1, 0, 0, 0);
        bullet.setFlightDirection(heading);
        level().addFreshEntity(bullet);
        WorldSounds.play(this, ModSounds.RICHARD_FIRE.get(), 5.0F, 1.0F);
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
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        deathLoot = source;
        deathLootPlayer = lastHurtByPlayer;
        deathLootPlayerMemoryTime = lastHurtByPlayerMemoryTime;
    }

    @Override
    protected void tickDeath() {
        setDeltaMovement(getDeltaMovement().subtract(0D, 0.05D, 0D));

        if (this.deathTime == -10) {
            WorldSounds.play(
                    level(), null, this, ModSounds.CHOPPER_DAMAGE.get(), getSoundSource(), 10F, 1F);
        }

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

            if (deathLoot != null) {
                var source = deathLoot;
                if (deathLootDirect != null || deathLootAttacker != null) {
                    source =
                            new DamageSource(
                                    source.typeHolder(),
                                    EntityReference.getEntity(deathLootDirect, server),
                                    EntityReference.getEntity(deathLootAttacker, server),
                                    source.sourcePositionRaw());
                }
                lastHurtByPlayer = deathLootPlayer;
                lastHurtByPlayerMemoryTime = deathLootPlayerMemoryTime;
                deathLoot = null;
                deathLootDirect = null;
                deathLootAttacker = null;
                deathLootPlayer = null;
                deathLootPlayerMemoryTime = 0;
                super.dropAllDeathLoot(server, source);
            }

            Component flicker =
                    Component.translatable("chat.hbmspace.angel.flicker")
                            .withStyle(ChatFormatting.RED);
            for (Player player :
                    server.getEntitiesOfClass(
                            Player.class,
                            getBoundingBox().inflate(200D),
                            EntitySelector.ENTITY_STILL_ALIVE)) {
                player.sendSystemMessage(flicker);
            }

            CelestialBody body = CelestialBody.getBody(server);
            if (body.getTrait(CBT_Invasion.class) == null)
                body.modifyTraits(new CBT_Invasion(0, 122, false));
        }

        super.tickDeath();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (deathLoot != null) {
            output.store("DeathLootType", DamageType.CODEC, deathLoot.typeHolder());
            EntityReference.store(
                    deathLootDirect == null
                            ? EntityReference.of(deathLoot.getDirectEntity())
                            : deathLootDirect,
                    output,
                    "DeathLootDirect");
            EntityReference.store(
                    deathLootAttacker == null
                            ? EntityReference.of(deathLoot.getEntity())
                            : deathLootAttacker,
                    output,
                    "DeathLootAttacker");
            EntityReference.store(deathLootPlayer, output, "DeathLootPlayer");
            output.putInt("DeathLootPlayerMemoryTime", deathLootPlayerMemoryTime);
            var position = deathLoot.sourcePositionRaw();
            if (position != null) output.store("DeathLootPosition", Vec3.CODEC, position);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        deathLoot =
                input.read("DeathLootType", DamageType.CODEC).map(DamageSource::new).orElse(null);
        input.read("DeathLootPosition", Vec3.CODEC)
                .ifPresent(
                        position -> deathLoot = new DamageSource(deathLoot.typeHolder(), position));
        deathLootDirect = EntityReference.read(input, "DeathLootDirect");
        deathLootAttacker = EntityReference.read(input, "DeathLootAttacker");
        deathLootPlayer = EntityReference.read(input, "DeathLootPlayer");
        deathLootPlayerMemoryTime = input.getIntOr("DeathLootPlayerMemoryTime", 0);
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

    private static final class ScanGoal extends Goal {

        private final BFAngelEntity angel;

        private ScanGoal(BFAngelEntity angel) {
            this.angel = angel;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.angel.scanCooldown > 0) this.angel.scanCooldown--;
            if (this.angel.scanCooldown <= 0) this.angel.scan();
        }
    }

    private static final class DashControl extends MoveControl<BFAngelEntity> {

        private DashControl(BFAngelEntity angel) {
            super(angel);
        }

        @Override
        public void tick() {
            Vec3 motion = Vec3.ZERO;
            if (this.operation == Operation.MOVE_TO) {
                this.operation = Operation.WAIT;
                motion =
                        new Vec3(
                                        this.wantedX - this.mob.getX(),
                                        this.wantedY - this.mob.getY(),
                                        this.wantedZ - this.mob.getZ())
                                .normalize()
                                .scale(this.speedModifier);
            }
            this.mob.setDeltaMovement(motion);
        }
    }

    private static final class EaseLookControl extends LookControl {

        private EaseLookControl(BFAngelEntity angel) {
            super(angel);
        }

        @Override
        public void tick() {
            if (!isLookingAtTarget()) return;
            this.lookAtCooldown = 0;

            double dx = this.wantedX - this.mob.getX();
            double dy = this.wantedY - (this.mob.getY() + 1);
            double dz = this.wantedZ - this.mob.getZ();

            float yaw = (float) (Math.atan2(dz, dx) * 180D / Math.PI) + 90F;
            this.mob.setYRot(
                    this.mob.getYRot() + Mth.wrapDegrees(yaw - this.mob.getYRot()) * 0.15F);

            float pitch = (float) (Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180D / Math.PI);
            this.mob.setXRot(this.mob.getXRot() + (pitch - this.mob.getXRot()) * 0.1F);
        }
    }
}
