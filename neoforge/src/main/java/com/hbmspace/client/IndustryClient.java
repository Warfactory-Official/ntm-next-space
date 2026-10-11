// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.client.ClientRegistry;
import com.hbm.integration.recipeviewer.RecipePages;
import com.hbmspace.client.gui.ScreenHydroponic;
import com.hbmspace.client.gui.ScreenMachineCryoDistill;
import com.hbmspace.client.gui.ScreenMachineVacuumCircuit;
import com.hbmspace.client.render.IndustryRenderTypes;
import com.hbmspace.client.render.IndustryRenders;
import com.hbmspace.integration.recipeviewer.CryoPage;
import com.hbmspace.integration.recipeviewer.VacuumCircuitPage;
import com.hbmspace.inventory.container.IndustryMenusSpace;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.function.Consumer;

final class IndustryClient {

    private IndustryClient() {}

    static void init() {
        RecipePages.addon(new CryoPage());
        VacuumCircuitPage vacuumCircuit = new VacuumCircuitPage();
        RecipePages.addon(vacuumCircuit);
        RecipePages.addonClickArea(ScreenMachineVacuumCircuit.class, 63, 28, 32, 14, vacuumCircuit);
    }

    static void registerMenuScreens(ClientRegistry.MenuScreenRegistrar reg) {
        reg.accept(IndustryMenusSpace.MACHINE_CRYO_DISTILL.get(), ScreenMachineCryoDistill::new);
        reg.accept(
                IndustryMenusSpace.MACHINE_VACUUM_CIRCUIT.get(), ScreenMachineVacuumCircuit::new);
        reg.accept(IndustryMenusSpace.HYDROBAY.get(), ScreenHydroponic::new);
    }

    static void registerBlockEntityRenderers(ClientRegistry.BlockEntityRendererRegistrar reg) {
        IndustryRenders.registerBlockEntityRenderers(reg);
    }

    static void registerVisualizers() {
        IndustryRenders.registerVisualizers();
    }

    static void registerRenderPipelines(Consumer<RenderPipeline> reg) {
        IndustryRenderTypes.forEach(reg);
    }
}
