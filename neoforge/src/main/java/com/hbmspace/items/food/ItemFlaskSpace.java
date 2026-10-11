// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.food;

import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.potion.HbmPotionSpace;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemFlaskSpace extends Item {

    private static final int DURATION = 3 * SharedConstants.TICKS_PER_MINUTE;

    public final EnumInfusion type;

    public ItemFlaskSpace(Properties properties, EnumInfusion type) {
        super(properties);
        this.type = type;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()
                && entity instanceof Player player
                && type == EnumInfusion.NITAN) {
            SpacePlayerProps.addNitanDose(player);
            player.addEffect(
                    new MobEffectInstance(
                            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HbmPotionSpace.NITAN.get()),
                            DURATION,
                            0));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, DURATION, 3));
        }
        return result;
    }

    public enum EnumInfusion {
        NITAN
    }
}
