// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.fluid.trait;

import com.hbm.inventory.fluid.trait.FluidTrait;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class FluidTraitSimpleSpace {

    private FluidTraitSimpleSpace() {}

    public static final class FT_ULTRAKILL extends FluidTrait {

        public static final FT_ULTRAKILL INSTANCE = new FT_ULTRAKILL();

        private FT_ULTRAKILL() {}

        @Override
        public void addInfoHidden(List<Component> info) {
            info.add(
                    Component.literal("[")
                            .append(Component.translatable("hbmfluid.trait.ultrakill"))
                            .append("]")
                            .withStyle(ChatFormatting.DARK_RED));
        }
    }

    public static final class FT_EXPLOSIVE extends FluidTrait {

        public static final FT_EXPLOSIVE INSTANCE = new FT_EXPLOSIVE();

        private FT_EXPLOSIVE() {}

        @Override
        public void addInfoHidden(List<Component> info) {
            info.add(
                    Component.literal("[")
                            .append(Component.translatable("hbmfluid.trait.explosive"))
                            .append("]")
                            .withStyle(ChatFormatting.RED));
        }
    }
}
