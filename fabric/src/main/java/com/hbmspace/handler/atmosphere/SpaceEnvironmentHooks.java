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

package com.hbmspace.handler.atmosphere;

import com.hbm.blockentity.machine.PumpBlockEntity;
import com.hbm.items.armor.ModArmorItem;
import com.hbm.items.armor.ModInsertItem;
import com.hbm.items.machine.FluidContainerInfiniteItem;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.items.armor.ArmorUtilSpace;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class SpaceEnvironmentHooks {

    private SpaceEnvironmentHooks() {}

    static void install() {
        ModArmorItem.EXTRA_LINES.install(HbmSpace.MOD_ID, SpaceEnvironmentHooks::armorLines);
        ModInsertItem.EXTRA_LINES.install(HbmSpace.MOD_ID, SpaceEnvironmentHooks::insertLines);
        FluidContainerInfiniteItem.CONDITION.install(HbmSpace.MOD_ID, new WaterTableCondition());

        PumpBlockEntity.addGround(() -> TerrainBlocksSpace.EVE_SILT.get());
        PumpBlockEntity.addGround(() -> TerrainBlocksSpace.EVE_ROCK.get());
        PumpBlockEntity.addGround(() -> TerrainBlocksSpace.IKE_REGOLITH.get());
        PumpBlockEntity.addGround(() -> ModBlocksSpace.IKE_STONE.get());
        PumpBlockEntity.addGround(() -> ModBlocksSpace.DUNA_SANDS.get());
        PumpBlockEntity.addGround(
                () -> BuiltInRegistries.BLOCK.getValue(RegistryUtil.id("moon_turf")));
        PumpBlockEntity.addGround(() -> TerrainBlocksSpace.LAYTHE_SILT.get());
        PumpBlockEntity.addGround(() -> ModBlocksSpace.MOHO_REGOLITH.get());
        PumpBlockEntity.addGround(() -> ModBlocksSpace.MINMUS_SMOOTH.get());
        PumpBlockEntity.addGround(() -> TerrainBlocksSpace.VINYL_SAND.get());
        PumpBlockEntity.addGround(() -> TerrainBlocksSpace.RUBBER_SILT.get());
        PumpBlockEntity.addGround(() -> FloraBlocksSpace.RUBBER_GRASS.get());
    }

    private static void armorLines(ItemStack stack, Consumer<Component> adder) {
        if (stack.getItem() instanceof ModArmorItem armor
                && armor.suit() == ModArmorItem.Suit.DNS) {
            adder.accept(Component.translatable("armor.canBreathe").withStyle(ChatFormatting.BLUE));
        } else if (ArmorUtilSpace.canSeal(stack)) {
            adder.accept(Component.translatable("armor.canSeal").withStyle(ChatFormatting.BLUE));
        }
    }

    private static void insertLines(ItemStack stack, Consumer<Component> adder) {
        if (ArmorUtilSpace.isCorrosionProtection(stack)) {
            adder.accept(
                    Component.translatable("desc.hbmspace.insert.corrosionProtection")
                            .withStyle(ChatFormatting.GOLD));
        }
    }
}
