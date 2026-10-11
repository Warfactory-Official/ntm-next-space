// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.logic;

import com.hbm.entity.logic.EntityBalefire;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.ContaminationUtil;
import com.hbmspace.entity.BombEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityAntimatterBlast extends EntityBalefire {

    public EntityAntimatterBlast(EntityType<? extends EntityAntimatterBlast> type, Level level) {
        super(type, level);
    }

    public static EntityAntimatterBlast spawn(
            Level level, double x, double y, double z, int range) {
        EntityAntimatterBlast blast =
                new EntityAntimatterBlast(BombEntitiesSpace.ANTIMATTER_BLAST.get(), level);
        blast.setPos(x, y, z);
        blast.destructionRange = range;
        level.addFreshEntity(blast);
        return blast;
    }

    public void flash() {
        if (level() instanceof ServerLevel server && tickCount < 10) {
            radiate(server, 40000F * destructionRange, destructionRange * 2);
        }
    }

    private void radiate(ServerLevel level, float rads, double range) {
        Vec3 centre = position();
        for (LivingEntity e :
                level.getEntitiesOfClass(
                        LivingEntity.class, new AABB(centre, centre).inflate(range))) {
            Vec3 vec = new Vec3(e.getX() - centre.x, e.getEyeY() - centre.y, e.getZ() - centre.z);
            double len = vec.length();
            vec = vec.normalize();

            float res = 0;
            for (int i = 1; i < len; i++) {
                BlockPos pos =
                        BlockPos.containing(
                                centre.x + vec.x * i, centre.y + vec.y * i, centre.z + vec.z * i);
                res += level.getBlockState(pos).getBlock().getExplosionResistance();
            }
            if (res < 1) res = 1;

            float eRads = rads;
            eRads /= res;
            eRads /= (float) (len * len);

            ContaminationUtil.contaminate(
                    e, HazardType.RADIATION, ContaminationType.CREATIVE, eRads);
            if (eRads >= 500 && !(e instanceof Player player && player.getAbilities().instabuild)) {
                ExplosionLarge.spawnShock(level, e.getX(), e.getY(), e.getZ(), 10, 10);
                if (e instanceof Player) {
                    e.kill(level);
                } else {
                    e.setHealth(0);
                    e.discard();
                }
            }
        }
    }

    public static void stepEffects(EntityBalefire blast, boolean finished) {
        Level level = blast.level();
        RandomSource rand = blast.getRandom();
        if (rand.nextInt(5) == 0) {
            level.playSound(
                    null,
                    blast.getX(),
                    blast.getY(),
                    blast.getZ(),
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS,
                    10000.0F,
                    0.8F + rand.nextFloat() * 0.2F);
        }
        if (!finished) {
            level.playSound(
                    null,
                    blast.getX(),
                    blast.getY(),
                    blast.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER,
                    10000.0F,
                    0.8F + rand.nextFloat() * 0.2F);
        }
    }
}
