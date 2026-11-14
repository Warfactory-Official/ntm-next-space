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

package com.hbmspace.inventory.container;

import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.CryoDistillBlockEntity;
import com.hbmspace.blockentity.machine.HydroponicBlockEntity;
import com.hbmspace.blockentity.machine.VacuumCircuitBlockEntity;
import net.minecraft.world.inventory.MenuType;

public final class IndustryMenusSpace {
    public static final RegistryHandle<MenuType<CryoDistillMenu>> MACHINE_CRYO_DISTILL =
            Registrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_cryo_distill", CryoDistillBlockEntity.class);
    public static final RegistryHandle<MenuType<VacuumCircuitMenu>> MACHINE_VACUUM_CIRCUIT =
            Registrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":machine_vacuum_circuit", VacuumCircuitBlockEntity.class);
    public static final RegistryHandle<MenuType<HydroponicMenu>> HYDROBAY =
            Registrar.registerBlockEntityMenu(
                    HbmSpace.MOD_ID + ":hydrobay", HydroponicBlockEntity.class);

    private IndustryMenusSpace() {}
}
