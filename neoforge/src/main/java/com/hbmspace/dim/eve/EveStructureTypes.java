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

package com.hbmspace.dim.eve;

import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public final class EveStructureTypes {

    public static final RegistryHandle<StructureType<ElectricVolcanoStructure>> ELECTRIC_VOLCANO =
            Registrar.registerStructureType(
                    "hbmspace:electric_volcano", ElectricVolcanoStructure.CODEC);
    public static final RegistryHandle<StructurePieceType> ELECTRIC_VOLCANO_PIECE =
            Registrar.registerStructurePieceType(
                    "hbmspace:electric_volcano", ElectricVolcanoPiece::new);

    private EveStructureTypes() {}
}
