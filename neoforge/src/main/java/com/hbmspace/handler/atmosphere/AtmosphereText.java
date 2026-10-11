// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.FluidIdentifierData;
import com.hbm.items.machine.FluidIdentifierItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class AtmosphereText {

    private AtmosphereText() {}

    public static double roundDecimal(double num, int digits) {
        return new BigDecimal(num).setScale(digits, RoundingMode.HALF_UP).doubleValue();
    }

    public static boolean isGas(@Nullable Fluid fluid) {
        return NTMFluidProperties.hasTrait(fluid, FT_Gaseous.class)
                || NTMFluidProperties.hasTrait(fluid, FT_Gaseous_ART.class);
    }

    public static @Nullable Fluid identified(ItemStack stack) {
        if (!(stack.getItem() instanceof FluidIdentifierItem)) return null;
        Fluid fluid =
                stack.getOrDefault(
                                ModDataComponents.FLUID_IDENTIFIER.get(), FluidIdentifierData.EMPTY)
                        .primary();
        return fluid == Fluids.EMPTY ? null : fluid;
    }

    public static void changedType(Player player, Fluid fluid) {
        player.sendSystemMessage(
                Component.translatable("desc.shared.changedTypeTo")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(NTMFluidProperties.getDisplayName(fluid))
                        .append("!"));
    }

    public static void retypeGaseous(
            ItemStack stack, Player player, FluidTankNTM tank, BlockEntity be) {
        Fluid fluid = identified(stack);
        if (fluid == null || !isGas(fluid)) return;
        tank.setTankTypeByIdentifier(fluid);
        be.setChanged();
        changedType(player, fluid);
    }
}
