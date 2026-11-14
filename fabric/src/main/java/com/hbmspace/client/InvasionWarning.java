/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.client;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.WorldProviderCelestial;
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
        if (player == null || WorldProviderCelestial.of(player.level()) == null) return;
        CBT_Invasion invasion = CelestialBody.getBody(player.level()).getTrait(CBT_Invasion.class);
        if (invasion == null || invasion.isInvading || invasion.warningPlayed) return;
        invasion.warningPlayed = true;
        player.playSound(ModSoundsSpace.ALARM_PING.get(), 10F, 1F);
        player.sendSystemMessage(
                Component.translatable("invasion.hbmspace.incoming").withStyle(ChatFormatting.RED));
    }
}
