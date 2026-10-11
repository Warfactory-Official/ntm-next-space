// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class BodyReach {

    private BodyReach() {}

    public static boolean meteorsStrike(ResourceKey<Level> dimension) {
        return dimension != SpaceDimensions.ORBIT
                && (dimension == SpaceDimensions.THATMO
                        || CelestialBody.getBodyOrNull(dimension) != null);
    }

    public static @Nullable Component bobmazonRefusal(ResourceKey<Level> dimension) {
        return dimension == Level.OVERWORLD
                ? null
                : Component.translatable("chat.hbmspace.bobmazon.outOfRange");
    }
}
