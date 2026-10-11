// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.world.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public final class MartianBaseTypes {

    public static RegistryHandle<StructureType<MartianBaseStructure>> STRUCTURE;
    public static RegistryHandle<StructurePieceType> PIECE;

    static {
        STRUCTURE =
                NeoForgeRegistrar.registerStructureType(
                        "hbmspace:martian_base", MartianBaseStructure.CODEC);
        PIECE =
                NeoForgeRegistrar.registerStructurePieceType(
                        "hbmspace:martian_base",
                        (context, tag) -> new TemplateStructurePiece(PIECE.get(), context, tag));
    }

    private MartianBaseTypes() {}
}
