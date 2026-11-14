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

package com.hbmspace.fabric;

import com.hbm.registration.Addon;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.OrbitalStationBlock;
import com.hbmspace.commands.SpaceCommands;
import com.hbmspace.dim.CelestialFishing;
import com.hbmspace.dim.CityLights;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.items.VanillaCreativeSpace;
import com.hbmspace.items.armor.ArmorModsSpace;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;

public final class HbmSpaceFabric implements Addon {
    @Override
    public void register() {
        HbmSpace.register();
        for (ResourceKey<CreativeModeTab> tab :
                List.of(CreativeModeTabs.COMBAT, CreativeModeTabs.SPAWN_EGGS)) {
            CreativeModeTabEvents.modifyOutputEvent(tab)
                    .register(
                            output -> {
                                for (VanillaCreativeSpace.Insertion insertion :
                                        VanillaCreativeSpace.insertions()) {
                                    if (insertion.tab().equals(tab))
                                        output.insertBefore(insertion.before(), insertion.stacks());
                                }
                            });
        }
        FluidStorage.combinedItemApiProvider(Items.MILK_BUCKET)
                .register(
                        context ->
                                new FullItemFluidStorage(
                                        context,
                                        Items.BUCKET,
                                        FluidVariant.of(FluidsSpace.milk()),
                                        FluidConstants.BUCKET));
        FluidStorage.combinedItemApiProvider(Items.BUCKET)
                .register(
                        context ->
                                new EmptyItemFluidStorage(
                                        context,
                                        Items.MILK_BUCKET,
                                        FluidsSpace.milk(),
                                        FluidConstants.BUCKET));
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, access, env) -> SpaceCommands.register(dispatcher));
        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) ->
                        !OrbitalStationBlock.refusesRemoval(level, pos, state));
        PlayerBlockBreakEvents.AFTER.register(
                (level, player, pos, state, blockEntity) ->
                        CityLights.broken((ServerLevel) level, pos, state));
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> {
                    ArmorModsSpace.onHurt(entity, source, amount);
                    return true;
                });
        LootTableEvents.REPLACE.register(
                (key, original, source, registries) -> {
                    LootTable table = CelestialFishing.wrap(key, original);
                    return table == original ? null : table;
                });
    }
}
