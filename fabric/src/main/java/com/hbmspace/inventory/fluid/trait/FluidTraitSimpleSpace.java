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

package com.hbmspace.inventory.fluid.trait;

import com.hbm.inventory.fluid.trait.FluidTrait;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class FluidTraitSimpleSpace {

    private FluidTraitSimpleSpace() {}

    public static final class FT_ULTRAKILL implements FluidTrait {

        public static final FT_ULTRAKILL INSTANCE = new FT_ULTRAKILL();

        private FT_ULTRAKILL() {}

        @Override
        public void describeDetails(Consumer<Component> info) {
            info.accept(
                    Component.literal("[")
                            .append(Component.translatable("hbmfluid.trait.ultrakill"))
                            .append("]")
                            .withStyle(ChatFormatting.DARK_RED));
        }
    }

    public static final class FT_EXPLOSIVE implements FluidTrait {

        public static final FT_EXPLOSIVE INSTANCE = new FT_EXPLOSIVE();

        private FT_EXPLOSIVE() {}

        @Override
        public void describeDetails(Consumer<Component> info) {
            info.accept(
                    Component.literal("[")
                            .append(Component.translatable("hbmfluid.trait.explosive"))
                            .append("]")
                            .withStyle(ChatFormatting.RED));
        }
    }
}
