// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna;

import com.hbm.world.structure.TemplateStructurePiece;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.WorldTypeTeleport;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class MartianBaseStructure extends Structure {

    public static final MapCodec<MartianBaseStructure> CODEC =
            simpleCodec(MartianBaseStructure::new);
    private static final Identifier TEMPLATE =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "duna_martian_base");

    public MartianBaseStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!WorldTypeTeleport.isMartian()
                || !(context.chunkGenerator() instanceof ChunkProviderDuna)
                || !context.chunkPos().equals(new ChunkPos(0, 0))) return Optional.empty();
        Optional<StructureTemplate> template = context.structureTemplateManager().get(TEMPLATE);
        if (template.isEmpty()) return Optional.empty();

        int height =
                context.chunkGenerator()
                        .getBaseHeight(
                                0,
                                0,
                                Heightmap.Types.WORLD_SURFACE_WG,
                                context.heightAccessor(),
                                context.randomState());
        Vec3i size = template.get().getSize();
        BlockPos center = new BlockPos(0, height - 1, 0);
        BlockPos origin = center.offset(-size.getX() / 2, 0, -size.getZ() / 2);
        return Optional.of(
                new GenerationStub(
                        center,
                        builder ->
                                builder.addPiece(
                                        new TemplateStructurePiece(
                                                MartianBaseTypes.PIECE.get(),
                                                TEMPLATE,
                                                template.get(),
                                                origin))));
    }

    @Override
    public StructureType<?> type() {
        return MartianBaseTypes.STRUCTURE.get();
    }
}
