// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.potion;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.lib.ModDamageTypesSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class HbmPotionSpace {
    public static final RegistryHandle<MobEffect> RUN =
            NeoForgeRegistrar.registerMobEffect("hbmspace:run", RunEffect::new);
    public static final RegistryHandle<MobEffect> NITAN =
            NeoForgeRegistrar.registerMobEffect("hbmspace:nitan", NitanEffect::new);
    public static final RegistryHandle<MobEffect> FLASHBANG =
            NeoForgeRegistrar.registerMobEffect(
                    "hbmspace:flashbang",
                    () -> new SpaceMobEffect(MobEffectCategory.NEUTRAL, 0xD0D0D0));
    public static final RegistryHandle<MobEffect> SLIPPERY =
            NeoForgeRegistrar.registerMobEffect("hbmspace:slippery", SlipperyEffect::new);

    private HbmPotionSpace() {}

    private static class SpaceMobEffect extends MobEffect {
        private SpaceMobEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    private static final class RunEffect extends SpaceMobEffect {
        private static final float BLAST = 12F;

        private RunEffect() {
            super(MobEffectCategory.HARMFUL, 0x111111);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            entity.hurtServer(level, level.damageSources().source(ModDamageTypesSpace.RUN), 1000F);
            entity.setHealth(0.0F);
            new ExplosionVNT(level, entity.getX(), entity.getY(), entity.getZ(), BLAST)
                    .makeAmat()
                    .explode();
            EntitySounds.play(
                    entity,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    ModSounds.GUN_MINI_NUKE_EXPLOSION.get(),
                    SoundSource.PLAYERS,
                    100.0F,
                    1.0F);
            if (!(entity instanceof Player)) entity.discard();
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration <= 10;
        }
    }

    private static final class NitanEffect extends SpaceMobEffect {
        private static final int FATAL_COUNT = 3;

        private NitanEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x800080);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            if (entity instanceof Player player
                    && SpacePlayerProps.getNitanCount(player) == FATAL_COUNT) {
                entity.hurtServer(
                        level, level.damageSources().source(ModDamageTypesSpace.NITAN), 1000F);
            }
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }
    }

    private static final class SlipperyEffect extends SpaceMobEffect {
        private SlipperyEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xD0D0D0);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0D, 0.026D, 0D));
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }
    }
}
