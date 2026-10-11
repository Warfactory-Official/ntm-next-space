// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.tool;

import com.hbmspace.util.AstronomyUtil;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

public final class ItemWandTimeSpace extends Item {

    private static final long MAX_MULTIPLIER = 1L << 62;

    public ItemWandTimeSpace(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            Player player = context.getPlayer();
            long current = AstronomyUtil.TIME_MULTIPLIER;
            long next =
                    player.isShiftKeyDown()
                            ? Math.max(1, current / 2)
                            : current < MAX_MULTIPLIER ? current * 2 : MAX_MULTIPLIER;
            AstronomyUtil.TIME_MULTIPLIER = next;
            player.sendSystemMessage(
                    Component.translatable("item.hbmspace.wand_time.multiplier", next));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        adder.accept(Component.translatable("item.hbmspace.wand_time.creative_only"));
        adder.accept(
                Component.translatable("item.hbmspace.wand_time.quote")
                        .withStyle(ChatFormatting.ITALIC));
    }
}
