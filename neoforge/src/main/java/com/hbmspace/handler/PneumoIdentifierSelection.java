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

package com.hbmspace.handler;

import com.hbm.blockentity.network.pneumatic.PneumaticTubeBlockEntity;
import com.hbm.blocks.network.pneumatic.PneumoTubeBlock;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.trait.CorrosiveTrait;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Amat;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.FluidIdentifierData;
import com.hbm.items.machine.FluidIdentifierItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public final class PneumoIdentifierSelection {

    private PneumoIdentifierSelection() {}

    public static boolean handle(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (player.isSecondaryUseActive()
                || !(stack.getItem() instanceof FluidIdentifierItem)
                || !(level.getBlockEntity(pos) instanceof PneumaticTubeBlockEntity tube)
                || !tube.hasIntake()) {
            return false;
        }
        if (!level.isClientSide()) {
            Fluid type =
                    stack.getOrDefault(
                                    ModDataComponents.FLUID_IDENTIFIER.get(),
                                    FluidIdentifierData.EMPTY)
                            .primary();
            boolean valid =
                    !NTMFluidProperties.hasTrait(type, FT_Amat.class)
                            && !NTMFluidProperties.hasTrait(type, CorrosiveTrait.class)
                            && (NTMFluidProperties.hasTrait(type, FT_Gaseous.class)
                                    || NTMFluidProperties.hasTrait(type, FT_Gaseous_ART.class));
            if (valid) {
                tube.gasTank.setTankTypeByIdentifier(type);
                tube.setChanged();
                PneumoTubeBlock.refreshConnections(level, pos);
                player.sendSystemMessage(
                        Component.translatable(
                                        "desc.fluidtank.changed_type",
                                        NTMFluidProperties.getDisplayName(type))
                                .withStyle(ChatFormatting.YELLOW));
            } else {
                player.sendSystemMessage(
                        Component.translatable("chat.hbmspace.pneumo.invalid_gas")
                                .withStyle(ChatFormatting.RED));
            }
        }
        return true;
    }
}
