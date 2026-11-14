/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-FileCopyrightText: 2023 Pu-238
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * Copyright (C) 2023 Pu-238. All rights reserved.
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

package com.hbmspace.hazard;

import com.hbm.config.BalanceConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.hazard.helper.HazardHelper;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.hazard.type.HazardKind;
import com.hbm.util.ContaminationUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HazardTypeNeutronSpace implements HazardKind {
    @Override
    public void onUpdate(LivingEntity target, float base, ItemStack stack) {
        double level = base;
        if (!NeutronExposureSpace.enabled()) return;
        float total = (float) level * stack.getCount();
        if (total <= 0F) return;
        float rad = total / 20F * (float) ContaminationUtil.calculateRadiationMod(target);
        if (HazardHelper.isHoldingReacher(target)) {
            if (BalanceConfig.enable528) rad /= 49F;
            else rad = (float) Math.sqrt(rad + 1F / ((rad + 2F) * (rad + 2F))) - 1F / (rad + 2F);
        }
        rad *= RadiationConfig.hazardRate;
        if (target instanceof Player player) NeutronExposureSpace.applyInventory(player, rad);
        NeutronExposureSpace.contaminate(target, ContaminationUtil.ContaminationType.CREATIVE, rad);
    }

    @Override
    public void onDropped(ItemEntity item, float level) {}

    @Override
    public void appendTooltip(
            Player player,
            List<Component> lines,
            float base,
            ItemStack stack,
            List<HazardModifier> modifiers) {
        double level = base;
        float rate = (float) HazardModifier.evalAllModifiers(stack, player, level, modifiers);
        if (rate < 1E-5D || !NeutronExposureSpace.enabled()) return;
        lines.add(
                Component.literal("[")
                        .append(Component.translatable("trait.neutron"))
                        .append("]")
                        .withStyle(ChatFormatting.BLUE));
        lines.add(
                Component.translatable(
                                "desc.hbmspace.neutron.rate", Math.floor(rate * 1000F) / 1000D)
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (stack.getCount() > 1) {
            lines.add(
                    Component.translatable(
                                    "desc.hbmspace.neutron.stack_rate",
                                    Math.floor(rate * 1000F * stack.getCount()) / 1000D)
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
