// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.missile;

import com.hbm.explosion.ExplosionLarge;
import com.hbmspace.blockentity.bomb.BlockEntityCombatDropPod;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.sound.RocketrySoundsSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class EntityCombatDropPod extends Entity {

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(EntityCombatDropPod.class, EntityDataSerializers.INT);

    private @Nullable CompoundTag entityType;
    private int amount;
    private int color;

    public EntityCombatDropPod(EntityType<? extends EntityCombatDropPod> type, Level level) {
        super(type, level);
    }

    public void setPayload(CompoundTag entityData, int amount, int color) {
        this.entityType = entityData;
        this.amount = amount;
        setColor(color);
    }

    public void setColor(int color) {
        this.color = color;
        entityData.set(COLOR, color);
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLOR, 0);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        setDeltaMovement(0, -1, 0);
        this.xo = this.xOld = getX();
        this.yo = this.yOld = getY();
        this.zo = this.zOld = getZ();

        for (int i = 0; i < 4; i++) {
            BlockPos pos = BlockPos.containing(getX(), getY() + 1, getZ());
            if (!level().isClientSide() && !level().getBlockState(pos).isAir()) {
                land(pos);
                return;
            }
            setPos(getX(), getY() - 1, getZ());
        }
    }

    private void land(BlockPos pos) {
        ExplosionLarge.spawnParticles(level(), getX(), getY() + 1, getZ(), 50);
        level().playSound(
                        null,
                        getX(),
                        getY(),
                        getZ(),
                        RocketrySoundsSpace.HATCH_IMPACT.get(),
                        SoundSource.BLOCKS,
                        10.0F,
                        0.5F + random.nextFloat() * 0.5F);

        BlockState pod = RocketryBlocksSpace.COMBAT_DROP.get().defaultBlockState();
        level().setBlockAndUpdate(pos, pod);
        if (entityType != null
                && level().getBlockEntity(pos) instanceof BlockEntityCombatDropPod capsule) {
            capsule.setPayload(entityType, amount, color);
        }
        discard();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 500000;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        entityType = input.read("entityType", CompoundTag.CODEC).orElse(null);
        amount = input.getIntOr("amount", 0);
        setColor(input.getIntOr("color", 0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("color", color);
        output.putInt("amount", amount);
        if (entityType != null) output.store("entityType", CompoundTag.CODEC, entityType);
    }
}
