// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace;

import com.hbm.api.entity.LivingHooks;
import com.hbm.compat.computercraft.ComputerCraft;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBomber;
import com.hbm.inventory.recipes.ModRecipes;
import com.hbm.items.machine.ItemReactorSensor;
import com.hbm.registration.Reg;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.blocks.AtmosphereBlocksSpace;
import com.hbmspace.blocks.BombBlocksSpace;
import com.hbmspace.blocks.CapacitorBlocksSpace;
import com.hbmspace.blocks.FloraBlocksSpace;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.blocks.IndustryBlocksSpace;
import com.hbmspace.blocks.MaterialBlocksSpace;
import com.hbmspace.blocks.ModBlockEntitiesSpace;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.blocks.SkyBlocksSpace;
import com.hbmspace.blocks.SpaceCellBuckets;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.blocks.StationBlocksSpace;
import com.hbmspace.blocks.TerrainBlockEntitiesSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.blocks.TrinketsSpace;
import com.hbmspace.blocks.WarBlocksSpace;
import com.hbmspace.blocks.machine.MachineDishControl;
import com.hbmspace.blocks.ore.CelestialOresSpace;
import com.hbmspace.compat.computercraft.RocketryPeripherals;
import com.hbmspace.compat.computercraft.SpacePeripherals;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.CelestialHandler;
import com.hbmspace.dim.FloraBodies;
import com.hbmspace.dim.SkyHandler;
import com.hbmspace.dim.duna.MartianBaseTypes;
import com.hbmspace.dim.eve.EveStructureTypes;
import com.hbmspace.entity.AtmosphereEntitiesSpace;
import com.hbmspace.entity.BombEntitiesSpace;
import com.hbmspace.entity.MobEntitiesSpace;
import com.hbmspace.entity.MobsSpace;
import com.hbmspace.entity.RocketryEntitiesSpace;
import com.hbmspace.entity.effect.TorexAntimatter;
import com.hbmspace.entity.effect.TorexVacuum;
import com.hbmspace.entity.mob.MobWeaponsSpace;
import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.handler.RenderEditsSpace;
import com.hbmspace.handler.SlipperyMotion;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.inventory.FluidContainersSpace;
import com.hbmspace.inventory.container.BombMenusSpace;
import com.hbmspace.inventory.container.IndustryMenusSpace;
import com.hbmspace.inventory.container.ModMenusSpace;
import com.hbmspace.inventory.container.RocketryMenusSpace;
import com.hbmspace.inventory.container.StationMenusSpace;
import com.hbmspace.inventory.container.WarMenusSpace;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.inventory.fluid.trait.FluidTraitsSpace;
import com.hbmspace.inventory.material.MaterialShapeItemsSpace;
import com.hbmspace.inventory.material.MatsSpace;
import com.hbmspace.inventory.recipes.AlkylationRecipes;
import com.hbmspace.inventory.recipes.AtmosphereRecipes;
import com.hbmspace.inventory.recipes.CryoRecipes;
import com.hbmspace.inventory.recipes.VacuumCircuitRecipes;
import com.hbmspace.items.ComponentItemsSpace;
import com.hbmspace.items.ConsumableItemsSpace;
import com.hbmspace.items.FoodItemsSpace;
import com.hbmspace.items.FuelItemsSpace;
import com.hbmspace.items.HiddenItemsSpace;
import com.hbmspace.items.MobItemsSpace;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.PartsItemsSpace;
import com.hbmspace.items.RecordsSpace;
import com.hbmspace.items.SirenItemsSpace;
import com.hbmspace.items.SkyItemsSpace;
import com.hbmspace.items.StationItemsSpace;
import com.hbmspace.items.TerrainItemsSpace;
import com.hbmspace.items.WarItemsSpace;
import com.hbmspace.items.WasteItemsSpace;
import com.hbmspace.items.armor.ArmorModsSpace;
import com.hbmspace.items.armor.ModArmorItemsSpace;
import com.hbmspace.items.machine.WavelengthsSpace;
import com.hbmspace.items.special.CelestialBedrockOre;
import com.hbmspace.items.tool.BombCallerItemsSpace;
import com.hbmspace.items.tool.WandTimeItemsSpace;
import com.hbmspace.items.weapon.MissileEditsSpace;
import com.hbmspace.items.weapon.MissileKindsSpace;
import com.hbmspace.items.weapon.MissilePartsSpace;
import com.hbmspace.main.ChunkLoaderManager;
import com.hbmspace.particle.SpaceParticles;
import com.hbmspace.potion.HbmPotionSpace;
import com.hbmspace.saveddata.satellites.SatellitesSpace;
import com.hbmspace.sound.BodySoundsSpace;
import com.hbmspace.sound.MobSoundsSpace;
import com.hbmspace.sound.ModSoundsSpace;
import com.hbmspace.sound.RocketrySoundsSpace;
import com.hbmspace.sound.SkySoundsSpace;
import com.hbmspace.world.OverworldFeaturesSpace;
import com.hbmspace.world.loot.WandLootScaledNumberProvider;

public final class HbmSpace {
    public static final String MOD_ID = "hbmspace";
    public static final Reg.Namespace REG = Reg.in(MOD_ID);

    private HbmSpace() {}

    public static void register() {
        WandLootScaledNumberProvider.register();
        com.hbmspace.hazard.NeutronExposureSpace.register();
        com.hbmspace.handler.SpaceMultiblockEscape.register();
        com.hbmspace.blockentity.machine.LPW2EngineSpace.install();
        load(
                FluidsSpace.class,
                ModBlocksSpace.class,
                CapacitorBlocksSpace.class,
                ModBlockEntitiesSpace.class,
                ModMenusSpace.class,
                FluidTraitsSpace.class,
                SpaceData.class,
                CelestialHandler.class,
                SkyHandler.class,
                ModArmorItemsSpace.class,
                MatsSpace.class,
                MaterialShapeItemsSpace.class,
                PartsItemsSpace.class,
                HiddenItemsSpace.class);
        ChunkAtmosphereManager.init();
        EntityNukeTorex.registerSimulation(new TorexVacuum());
        load(
                AtmosphereBlocksSpace.class,
                AtmosphereBlockEntitiesSpace.class,
                AtmosphereEntitiesSpace.class);
        ModRecipes.addon(AtmosphereRecipes.INSTANCE);
        if (ComputerCraft.LOADED) SpacePeripherals.register();
        load(
                WavelengthsSpace.class,
                ModSoundsSpace.class,
                SirenItemsSpace.class,
                ConsumableItemsSpace.class,
                WandTimeItemsSpace.class,
                TrinketsSpace.class,
                FoodItemsSpace.class,
                BombCallerItemsSpace.class,
                HbmPotionSpace.class,
                FuelItemsSpace.class,
                ComponentItemsSpace.class,
                ArmorModsSpace.class,
                MaterialBlocksSpace.class,
                RecordsSpace.class,
                WasteItemsSpace.class);
        EntityBomber.addonLoop(9, ModSoundsSpace.ENTITY_JETLOOPBOMB::get);
        SpacePlayerProps.register();
        load(
                ModDataComponentsSpace.class,
                MissileKindsSpace.class,
                MissilePartsSpace.class,
                RocketryBlocksSpace.class,
                RocketryMenusSpace.class,
                RocketrySoundsSpace.class,
                RocketryEntitiesSpace.class);
        MissileEditsSpace.install();
        SatellitesSpace.install();
        if (ComputerCraft.LOADED) RocketryPeripherals.register();
        load(
                SpaceCellBuckets.class,
                StationBlocksSpace.class,
                StationBlockEntitiesSpace.class,
                StationMenusSpace.class,
                StationItemsSpace.class,
                ChunkLoaderManager.class);
        load(
                TerrainBlocksSpace.class,
                FloraBlocksSpace.class,
                TerrainItemsSpace.class,
                TerrainBlockEntitiesSpace.class,
                CelestialOresSpace.class,
                EveStructureTypes.class,
                MartianBaseTypes.class,
                FloraBodies.class,
                OverworldFeaturesSpace.class,
                FluidContainersSpace.class,
                MobSoundsSpace.class,
                MobWeaponsSpace.class,
                MobEntitiesSpace.class,
                BodySoundsSpace.class);
        load(
                SkyBlocksSpace.class,
                SkyBlockEntitiesSpace.class,
                SkyItemsSpace.class,
                SkySoundsSpace.class,
                SpaceParticles.class);
        load(IndustryBlocksSpace.class, IndustryBlockEntitiesSpace.class, IndustryMenusSpace.class);
        ItemReactorSensor.addonTarget(MachineDishControl::useSensor);
        load(WarItemsSpace.class, WarBlocksSpace.class, WarMenusSpace.class);
        load(BombEntitiesSpace.class, BombBlocksSpace.class, BombMenusSpace.class);
        load(MobsSpace.class, MobItemsSpace.class);
        TorexAntimatter.register();
        ModRecipes.addon(AlkylationRecipes.INSTANCE);
        ModRecipes.addon(CryoRecipes.INSTANCE);
        ModRecipes.addon(VacuumCircuitRecipes.INSTANCE);
        CelestialBedrockOre.install();
        BaseEditsSpace.install();
        LivingHooks.onTick(SlipperyMotion::tick);
        RenderEditsSpace.install();
    }

    private static void load(Class<?>... holders) {
        for (Class<?> holder : holders) {
            try {
                Class.forName(holder.getName(), true, holder.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "content holder is missing: " + holder.getName(), e);
            }
        }
    }
}
