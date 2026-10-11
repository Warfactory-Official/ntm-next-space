// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.tool;

import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toclient.PlayerInformPayload;
import com.hbm.util.TickPhase;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemAtmosphereScanner extends Item {
    public static final int INFO_ID = 969;
    private static final int MILLIS = 4_000;

    public ItemAtmosphereScanner(Properties props) {
        super(props);
    }

    @Override
    public void inventoryTick(
            ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!(owner instanceof ServerPlayer player) || !TickPhase.every(owner, 5)) return;

        CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(player);

        boolean hasAtmosphere = false;
        if (atmosphere != null) {
            for (int i = 0; i < atmosphere.fluids.size(); i++) {
                CBT_Atmosphere.FluidEntry entry = atmosphere.fluids.get(i);
                if (entry.pressure > 0.0001) {
                    String pressure =
                            String.format(
                                    Locale.ROOT,
                                    "%.4f",
                                    AtmosphereText.roundDecimal(entry.pressure, 4));
                    Component line =
                            NTMFluidProperties.getDisplayName(entry.fluid)
                                    .copy()
                                    .withStyle(ChatFormatting.AQUA)
                                    .append(
                                            Component.literal(": " + pressure + "atm")
                                                    .withStyle(ChatFormatting.RESET));
                    PacketWire.sendTo(new PlayerInformPayload(line, INFO_ID + i, MILLIS), player);
                    hasAtmosphere = true;
                }
            }
        }

        if (!hasAtmosphere) {
            Component vacuum =
                    Component.translatable("desc.hbmspace.atmosphereScanner.nearVacuum")
                            .withStyle(ChatFormatting.YELLOW);
            PacketWire.sendTo(new PlayerInformPayload(vacuum, INFO_ID, MILLIS), player);
        }
    }
}
