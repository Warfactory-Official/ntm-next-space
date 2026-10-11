// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.util;

public final class AstronomyUtil {

    public static final float GRAVITATIONAL_CONSTANT = 6.6743015e-11F;

    public static final float DEFAULT_ALTITUDE_KM = 100;

    public static final long SECONDS_IN_DAY = 20 * 60;
    public static final long SECONDS_IN_KSP_DAY = 6 * 60 * 60;
    public static final long TICKS_IN_DAY = SECONDS_IN_DAY * 20;

    public static final double DAY_FACTOR = (double) SECONDS_IN_DAY / (double) SECONDS_IN_KSP_DAY;

    public static final float STANDARD_GRAVITY = 1.6F;
    public static final float PLAYER_GRAVITY_MODIFIER = STANDARD_GRAVITY / 9.81F;

    public static final float KM_IN_AU = 13_599_840F;

    public static volatile long TIME_MULTIPLIER = 1;

    public static final double MB_PER_ATM = 1_000_000_000D * 1_000D;

    private AstronomyUtil() {}
}
