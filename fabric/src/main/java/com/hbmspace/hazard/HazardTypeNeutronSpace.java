// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2023 Pu-238
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.hazard;

import com.hbm.config.BalanceConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.hazard.helper.HazardHelper;
import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.hazard.type.IHazardType;
import com.hbm.util.ContaminationUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HazardTypeNeutronSpace implements IHazardType {
    @Override
    public void onUpdate(LivingEntity target, double level, ItemStack stack) {
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
    public void updateEntity(ItemEntity item, double level) {}

    @Override
    public void addHazardInformation(
            Player player,
            List<Component> lines,
            double level,
            ItemStack stack,
            List<IHazardModifier> modifiers) {
        float rate = (float) IHazardModifier.evalAllModifiers(stack, player, level, modifiers);
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
