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

package com.hbmspace.neoforge;

import com.hbm.registration.Registrar;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.OrbitalStationBlock;
import com.hbmspace.commands.SpaceCommands;
import com.hbmspace.dim.CelestialFishing;
import com.hbmspace.dim.CityLights;
import com.hbmspace.items.VanillaCreativeSpace;
import com.hbmspace.items.armor.ArmorModsSpace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@Mod(HbmSpace.MOD_ID)
public final class HbmSpaceNeoForge {
    public HbmSpaceNeoForge(IEventBus modBus) {
        NeoForgeMod.enableMilkFluid();
        Registrar.addon(HbmSpace.MOD_ID, modBus, HbmSpace::register);
        modBus.addListener(
                (BuildCreativeModeTabContentsEvent event) -> {
                    for (VanillaCreativeSpace.Insertion insertion :
                            VanillaCreativeSpace.insertions()) {
                        if (!insertion.tab().equals(event.getTabKey())) continue;
                        ItemStack before = new ItemStack(insertion.before());
                        for (ItemStack stack : insertion.stacks()) {
                            event.insertBefore(
                                    before,
                                    stack,
                                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                    }
                });
        NeoForge.EVENT_BUS.addListener(
                (RegisterCommandsEvent event) -> SpaceCommands.register(event.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(
                (LivingIncomingDamageEvent event) ->
                        ArmorModsSpace.onHurt(
                                event.getEntity(), event.getSource(), event.getAmount()));
        NeoForge.EVENT_BUS.addListener(
                (BreakBlockEvent event) -> {
                    if (event.getLevel() instanceof Level level
                            && OrbitalStationBlock.refusesRemoval(
                                    level, event.getPos(), event.getState())) {
                        event.setCanceled(true);
                    }
                });
        NeoForge.EVENT_BUS.addListener(
                EventPriority.LOWEST,
                (BreakBlockEvent event) -> {
                    if (event.getLevel() instanceof ServerLevel level) {
                        CityLights.broken(level, event.getPos(), event.getState());
                    }
                });
        NeoForge.EVENT_BUS.addListener(
                (LootTableLoadEvent event) -> {
                    LootTable table = CelestialFishing.wrap(event.getKey(), event.getTable());
                    if (table == event.getTable()) return;
                    table.setLootTableId(event.getName());
                    event.setTable(table);
                });
    }
}
