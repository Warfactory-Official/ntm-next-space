// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.fabric;

import com.hbm.registration.Addon;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.BlockOrbitalStation;
import com.hbmspace.commands.SpaceCommands;
import com.hbmspace.dim.CelestialFishing;
import com.hbmspace.dim.CityLights;
import com.hbmspace.dim.SpaceChunkGenerators;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.handler.SpaceItemEvents;
import com.hbmspace.handler.atmosphere.EveDeath;
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
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
        SpaceChunkGenerators.forEach(
                (id, codec) -> Registry.register(BuiltInRegistries.CHUNK_GENERATOR, id, codec));
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
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, access, env) -> SpaceCommands.register(dispatcher));
        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, pos, state, blockEntity) ->
                        !BlockOrbitalStation.refusesRemoval(level, pos, state));
        PlayerBlockBreakEvents.AFTER.register(
                (level, player, pos, state, blockEntity) ->
                        CityLights.broken((ServerLevel) level, pos, state));
        ServerLivingEntityEvents.AFTER_DEATH.register(EveDeath::onDeath);
        ServerLivingEntityEvents.AFTER_DEATH.register(SpaceItemEvents::onDeath);
        ServerLivingEntityEvents.AFTER_DEATH.register(ArmorModsSpace::onDeath);
        ServerLivingEntityEvents.AFTER_DEATH.register(CBT_Invasion::onDeath);
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
