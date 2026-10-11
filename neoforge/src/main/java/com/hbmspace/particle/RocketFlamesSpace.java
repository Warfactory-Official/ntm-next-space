// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.particle;

import com.hbm.particle.RocketFlameColor;

public final class RocketFlamesSpace {
    public static final RocketFlameColor BALEFIRE =
            new RocketFlameColor(0F, 1F, 0F, false, false, false, 240);
    public static final RocketFlameColor FUSION =
            new RocketFlameColor(1F, 1F, 1F, false, false, true, 240);
    public static final RocketFlameColor MUD =
            new RocketFlameColor(1F, 1F, 0F, false, false, false, 20);
    public static final RocketFlameColor SCHRABIDIUM =
            new RocketFlameColor(1F, 1F, 1F, true, false, false, 240);
    public static final RocketFlameColor URANIUM_PLUTONIUM =
            new RocketFlameColor(0F, 1F, 0F, false, false, false, 80);

    private RocketFlamesSpace() {}

    public static RocketFlameColor contrail(String type) {
        return switch (type) {
            case "missileContrailf" -> FUSION;
            case "missileContrailbf" -> BALEFIRE;
            case "missileContrailMUD" -> MUD;
            case "missileContrailSCH" -> SCHRABIDIUM;
            case "missileContrailUP" -> URANIUM_PLUTONIUM;
            case "missileContrail" -> RocketFlameColor.FLAME;
            default -> throw new IllegalArgumentException(type);
        };
    }
}
