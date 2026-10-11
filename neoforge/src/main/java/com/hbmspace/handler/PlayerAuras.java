// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import java.util.function.Consumer;
import net.minecraft.world.entity.player.Player;

public final class PlayerAuras {
    public static Consumer<Player> CLIENT_START;

    private PlayerAuras() {}

    public static void tick(Player player) {
        if (player.level().isClientSide()) CLIENT_START.accept(player);
    }
}
