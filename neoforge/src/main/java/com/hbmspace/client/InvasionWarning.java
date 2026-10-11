// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.sound.ModSoundsSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class InvasionWarning {

    private InvasionWarning() {}

    public static void install() {
        CBT_Invasion.CLIENT_WARNING = InvasionWarning::warn;
    }

    private static void warn() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        player.playSound(ModSoundsSpace.ALARM_PING.get(), 10F, 1F);
        player.sendSystemMessage(
                Component.translatable("invasion.hbmspace.incoming").withStyle(ChatFormatting.RED));
    }
}
