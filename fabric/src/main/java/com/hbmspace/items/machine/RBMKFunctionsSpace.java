/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023 TheNewGuy
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023 TheNewGuy. All rights reserved.
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

package com.hbmspace.items.machine;

import com.hbm.items.machine.RBMKRodItem.BurnFunction;
import com.hbm.items.machine.RBMKRodItem.DepletionFunction;
import com.hbm.items.machine.RBMKRodItem;

public final class RBMKFunctionsSpace {

    private RBMKFunctionsSpace() {}

    public enum Burn implements BurnFunction {
        SLOW_LINEAR;

        @Override
        public String title() {
            return "trait.hbmspace.rbmx.flux.slowlinear";
        }

        @Override
        public double reactivity(RBMKRodItem rod, double flux, double enrichment) {
            return Math.sqrt(2 * flux + 30) / 10 * rod.reactivity / 2.5;
        }

        @Override
        public String formula(RBMKRodItem rod) {
            return "sqrt(2 * %1$s + 30) / 10 * %2$s / 2.5";
        }
    }

    public enum Depletion implements DepletionFunction {
        CF_SLOPE;

        @Override
        public double modify(double enrichment) {
            return enrichment + (Math.sin(enrichment * Math.PI)) * 1.4;
        }
    }
}
