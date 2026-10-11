// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.food;

import com.hbm.config.VersatileConfig;
import com.hbm.items.special.ItemCustomLore;
import com.hbm.packet.toclient.VomitPayload;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import com.hbmspace.potion.HbmPotionSpace;
import com.hbmspace.sound.ModSoundsSpace;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemAniman extends ItemCustomLore {

    private static final int HUNGER_TICKS = 60;
    private static final int HUNGER_AMPLIFIER = 19;
    private static final int VOMIT_COUNT = 25;

    public ItemAniman(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (VersatileConfig.hasPotionSickness(player)) return InteractionResult.PASS;
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel server))
            return result;

        VersatileConfig.applyPotionSickness(player, 5);
        EntitySounds.play(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSoundsSpace.PLAYER_OIL.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        player.addEffect(
                new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HbmPotionSpace.RUN.get()),
                        10 * SharedConstants.TICKS_PER_SECOND,
                        0));
        ParticleCreators.vomit(server, player, VomitPayload.MODE_NORMAL, VOMIT_COUNT);
        EntitySounds.play(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.PLAYER_VOMIT.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, HUNGER_TICKS, HUNGER_AMPLIFIER));
        return result;
    }
}
