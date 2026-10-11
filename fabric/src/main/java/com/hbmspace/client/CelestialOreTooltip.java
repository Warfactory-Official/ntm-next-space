// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbmspace.blocks.ore.CelestialOre;
import com.hbmspace.config.SpaceConfig;
import com.hbmspace.dim.SolarSystem.Body;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class CelestialOreTooltip {

    private static @Nullable Map<Item, CelestialOre> ores;

    private CelestialOreTooltip() {}

    public static void append(ItemStack stack, List<Component> lines) {
        if (!SpaceConfig.showOreLocations) return;
        if (stack.is(Items.COAL_ORE)) {
            lines.add(
                    Component.translatable("desc.hbmspace.ore.foundOn")
                            .withStyle(ChatFormatting.GOLD));
            lines.add(body(Body.KERBIN, ChatFormatting.AQUA));
            return;
        }
        CelestialOre ore = ores().get(stack.getItem());
        if (ore == null) return;
        Set<Body> on = ore.spawnsOn();
        int bodies = Body.values().length;
        if (on.isEmpty()) return;
        if (on.size() == bodies) {
            lines.add(
                    Component.translatable("desc.hbmspace.ore.anywhere")
                            .withStyle(ChatFormatting.GOLD));
        } else if (on.size() == bodies - 1) {
            lines.add(
                    Component.translatable("desc.hbmspace.ore.anywhereExcept")
                            .withStyle(ChatFormatting.GOLD));
            for (Body body : Body.values()) {
                if (!on.contains(body)) lines.add(body(body, ChatFormatting.RED));
            }
        } else {
            lines.add(
                    Component.translatable("desc.hbmspace.ore.foundOn")
                            .withStyle(ChatFormatting.GOLD));
            for (Body body : on) lines.add(body(body, ChatFormatting.AQUA));
        }
    }

    private static Component body(Body body, ChatFormatting colour) {
        return Component.translatable(
                        "desc.hbmspace.ore.body", Component.translatable("body." + body.name))
                .withStyle(colour);
    }

    private static Map<Item, CelestialOre> ores() {
        Map<Item, CelestialOre> map = ores;
        if (map != null) return map;
        map = new HashMap<>();
        for (CelestialOre ore : CelestialOre.values()) {
            map.put(ore.host().asItem(), ore);
            for (Body body : ore.bodies()) map.put(ore.block(body).asItem(), ore);
        }
        map.put(Items.NETHER_QUARTZ_ORE, CelestialOre.QUARTZ);
        ores = map;
        return map;
    }
}
