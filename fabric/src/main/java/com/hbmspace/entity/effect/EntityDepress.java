// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.effect;

import com.hbm.client.ClientEffects;
import com.hbmspace.entity.AtmosphereEntitiesSpace;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityDepress extends Entity {

    private static final EntityDataAccessor<Byte> DIR =
            SynchedEntityData.defineId(EntityDepress.class, EntityDataSerializers.BYTE);

    public static final double RANGE = 8;

    public int timeToLive;

    public EntityDepress(EntityType<? extends EntityDepress> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.timeToLive = 100;
    }

    public EntityDepress(Level level, Direction dir, int timeToLive) {
        this(AtmosphereEntitiesSpace.DEPRESS.get(), level);
        this.timeToLive = timeToLive;
        setDir(dir);
    }

    protected void setDir(Direction dir) {
        this.entityData.set(DIR, (byte) dir.get3DDataValue());
    }

    protected Direction getDir() {
        return Direction.from3DDataValue(this.entityData.get(DIR));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DIR, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();

        Direction dir = getDir();

        if (level().isClientSide()) {
            RandomSource rand = level().getRandom();
            double x = getX() + rand.nextGaussian() * 0.25;
            double y = getY() + rand.nextGaussian() * 0.25;
            double z = getZ() + rand.nextGaussian() * 0.25;
            double moX = dir.getStepX() * 0.5 + rand.nextGaussian() * 0.25;
            double moY = dir.getStepY() * 0.5 + rand.nextGaussian() * 0.25;
            double moZ = dir.getStepZ() * 0.5 + rand.nextGaussian() * 0.25;
            ClientEffects.spawnRocketFlame(
                    level(), x, y, z, 0.5F, moX, moY, moZ, 100 + rand.nextInt(20), 1F, 1F, 1F);
        }

        for (Entity entity :
                level().getEntities(
                                this,
                                new AABB(
                                        getX() - RANGE,
                                        getY() - RANGE,
                                        getZ() - RANGE,
                                        getX() + RANGE,
                                        getY() + RANGE,
                                        getZ() + RANGE))) {
            Vec3 vec =
                    new Vec3(
                            getX() - entity.getX(), getY() - entity.getY(), getZ() - entity.getZ());

            if (entity instanceof Player player && SpaceLivingProps.hasGravity(player)) continue;

            double dist = vec.length();

            if (dist > RANGE) continue;

            boolean succ =
                    switch (dir) {
                        case UP -> entity.getY() < getY();
                        case DOWN -> entity.getY() > getY();
                        case EAST -> entity.getX() < getX();
                        case WEST -> entity.getX() > getX();
                        case SOUTH -> entity.getZ() < getZ();
                        case NORTH -> entity.getZ() > getZ();
                    };

            vec = vec.normalize();

            double speed = succ ? 0.1D : -0.1D;
            entity.setDeltaMovement(
                    entity.getDeltaMovement().add(vec.x * speed, vec.y * speed, vec.z * speed));
        }

        timeToLive--;

        if (timeToLive <= 0) {
            discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        timeToLive = input.getIntOr("ttl", 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("ttl", timeToLive);
    }
}
