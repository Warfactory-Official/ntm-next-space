// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.machine;

import com.hbm.items.machine.ItemRBMKRod.BurnFunction;
import com.hbm.items.machine.ItemRBMKRod.DepletionFunction;
import com.hbm.items.machine.ItemRBMKRod;

public final class RBMKFunctionsSpace {

    private RBMKFunctionsSpace() {}

    public enum Burn implements BurnFunction {
        SLOW_LINEAR;

        @Override
        public String title() {
            return "trait.hbmspace.rbmx.flux.slowlinear";
        }

        @Override
        public double reactivity(ItemRBMKRod rod, double flux, double enrichment) {
            return Math.sqrt(2 * flux + 30) / 10 * rod.reactivity / 2.5;
        }

        @Override
        public String formula(ItemRBMKRod rod) {
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
