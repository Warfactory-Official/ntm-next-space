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
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.SolarSystem;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class FT_Terraformer implements FluidTrait {

    private final SolarSystem.Body target;

    public FT_Terraformer(SolarSystem.Body target) {
        this.target = target;
    }

    public SolarSystem.Body getTarget() {
        return target;
    }

    public Block getTargetBlock() {
        if (target == SolarSystem.Body.DUNA) return ModBlocksSpace.DUNA_SANDS.get();
        if (target == SolarSystem.Body.LAYTHE) return TerrainBlocksSpace.LAYTHE_SILT.get();
        return Blocks.STONE;
    }

    @Override
    public void describe(Consumer<Component> info) {
        info.accept(
                Component.literal("[")
                        .append(Component.translatable("hbmfluid.trait.terraforming"))
                        .append("]")
                        .withStyle(ChatFormatting.AQUA));
    }
}
