// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.fluid.trait;

import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.util.BobMathUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class FT_Rocket extends FluidTrait {

    private final int isp;
    private final long thrust;

    public FT_Rocket(int isp, long thrust) {
        this.isp = isp;
        this.thrust = thrust;
    }

    public int getISP() {
        return isp;
    }

    public long getThrust() {
        return thrust;
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(
                Component.literal("[")
                        .append(Component.translatable("hbmfluid.trait.rocket"))
                        .append("]")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (isp > 0) {
            info.add(
                    Component.empty()
                            .append(
                                    Component.translatable("hbmfluid.trait.provides")
                                            .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
                            .append(
                                    Component.translatable(
                                                    "hbmfluid.trait.rocket.isp",
                                                    BobMathUtil.getShortNumber(isp))
                                            .withStyle(ChatFormatting.RED))
                            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
                            .append(
                                    Component.translatable("hbmfluid.trait.perBucket")
                                            .withStyle(ChatFormatting.YELLOW)));
        }
        info.add(
                Component.literal("[")
                        .append(Component.translatable("hbmfluid.trait.thrust"))
                        .append("]")
                        .withStyle(ChatFormatting.RED));
        if (thrust > 0) {
            info.add(
                    Component.empty()
                            .append(
                                    Component.translatable("hbmfluid.trait.provides")
                                            .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
                            .append(
                                    Component.translatable(
                                                    "hbmfluid.trait.thrust.newtons",
                                                    BobMathUtil.getShortNumber(thrust))
                                            .withStyle(ChatFormatting.RED))
                            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
                            .append(
                                    Component.translatable("hbmfluid.trait.thrust.perBucket")
                                            .withStyle(ChatFormatting.YELLOW)));
        }
    }
}
