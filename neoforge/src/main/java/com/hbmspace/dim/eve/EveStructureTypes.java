// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public final class EveStructureTypes {

    public static final RegistryHandle<StructureType<ElectricVolcanoStructure>> ELECTRIC_VOLCANO =
            NeoForgeRegistrar.registerStructureType(
                    "hbmspace:electric_volcano", ElectricVolcanoStructure.CODEC);
    public static final RegistryHandle<StructurePieceType> ELECTRIC_VOLCANO_PIECE =
            NeoForgeRegistrar.registerStructurePieceType(
                    "hbmspace:electric_volcano", ElectricVolcanoPiece::new);

    private EveStructureTypes() {}
}
