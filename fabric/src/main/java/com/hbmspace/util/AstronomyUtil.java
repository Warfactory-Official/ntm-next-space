/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

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
