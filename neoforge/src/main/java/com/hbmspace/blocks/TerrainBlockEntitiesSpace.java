// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityGeysir;
import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.deco.BlockEntityGeysirDCM;
import com.hbmspace.blockentity.deco.BlockEntityGeysirSpace;
import com.hbmspace.blockentity.deco.BlockEntityLightningVolcano;
import com.hbmspace.blockentity.machine.BlockEntityAlgaeFilm;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class TerrainBlockEntitiesSpace {

    public static final Reg.BeHandle<BlockEntityGeysirSpace> GEYSIR =
            HbmSpace.REG
                    .be(
                            "geysir",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityGeysirSpace::new,
                                            Set.of(TerrainBlocksSpace.GEYSIR_VOLCANIC.get())))
                    .ticksServerAt(BlockEntityGeysir::tick);

    public static final Reg.BeHandle<BlockEntityLightningVolcano> LIGHTNING_VOLCANO =
            HbmSpace.REG
                    .be(
                            "lightning_volcano",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityLightningVolcano::new,
                                            Set.of(TerrainBlocksSpace.GEYSIR_ELECTRIC.get())))
                    .ticksClientAt(BlockEntityLightningVolcano::tickClient)
                    .ticksServerAt(BlockEntityLightningVolcano::tickServer);

    public static final Reg.BeHandle<BlockEntityGeysirDCM> GEYSIR_DCM =
            HbmSpace.REG
                    .be(
                            "geysir_dcm",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityGeysirDCM::new,
                                            Set.of(TerrainBlocksSpace.GEYSIR_CHLORIC.get())))
                    .ticksServerAt(BlockEntityGeysirDCM::tickServer)
                    .ticksClientAt(BlockEntityGeysirDCM::tickClient);

    public static final Reg.BeHandle<BlockEntityAlgaeFilm> ALGAE_FILM =
            HbmSpace.REG
                    .be(
                            "algae_film",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityAlgaeFilm::new,
                                            Set.of(TerrainBlocksSpace.ALGAE_FILM.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    private TerrainBlockEntitiesSpace() {}
}
