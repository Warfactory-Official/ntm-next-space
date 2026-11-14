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

package com.hbmspace.client;

import com.hbm.client.ClientRegistry;
import com.hbm.integration.recipeviewer.RecipePages;
import com.hbmspace.client.gui.CryoDistillScreen;
import com.hbmspace.client.gui.HydroponicScreen;
import com.hbmspace.client.gui.VacuumCircuitScreen;
import com.hbmspace.client.render.IndustryRenderTypes;
import com.hbmspace.client.render.IndustryRenders;
import com.hbmspace.integration.recipeviewer.CryoPage;
import com.hbmspace.integration.recipeviewer.VacuumCircuitPage;
import com.hbmspace.inventory.container.IndustryMenusSpace;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.function.Consumer;

final class IndustryClient {

    private IndustryClient() {}

    static void init() {
        RecipePages.addon(new CryoPage());
        VacuumCircuitPage vacuumCircuit = new VacuumCircuitPage();
        RecipePages.addon(vacuumCircuit);
        RecipePages.addonClickArea(VacuumCircuitScreen.class, 63, 28, 32, 14, vacuumCircuit);
    }

    static void registerMenuScreens(ClientRegistry.MenuScreenRegistrar reg) {
        reg.accept(IndustryMenusSpace.MACHINE_CRYO_DISTILL.get(), CryoDistillScreen::new);
        reg.accept(IndustryMenusSpace.MACHINE_VACUUM_CIRCUIT.get(), VacuumCircuitScreen::new);
        reg.accept(IndustryMenusSpace.HYDROBAY.get(), HydroponicScreen::new);
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
