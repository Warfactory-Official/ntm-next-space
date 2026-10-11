// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.special;

import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ItemMineralOreSpace extends Item {

    public final Clump clump;

    public ItemMineralOreSpace(Properties properties, Clump clump) {
        super(properties);
        this.clump = clump;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        adder.accept(
                Component.translatable(
                                "desc.hbmspace.clump." + clump.name().toLowerCase(Locale.ROOT))
                        .withStyle(clump.color));
    }

    public enum Clump {
        PEROXIDE(ChatFormatting.YELLOW),
        NITRIC(ChatFormatting.GOLD),
        SULFURIC(ChatFormatting.YELLOW),
        SOLVENT(ChatFormatting.RED),
        HYDROCHLORIC(ChatFormatting.GREEN),
        SCHRABIDIC(ChatFormatting.BLUE);

        private final ChatFormatting color;

        Clump(ChatFormatting color) {
            this.color = color;
        }
    }
}
