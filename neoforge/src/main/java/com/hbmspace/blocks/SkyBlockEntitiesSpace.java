// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.deco.BlockEntityOrrery;
import com.hbmspace.blockentity.machine.BlockEntityDysonConverterAnatmogenesis;
import com.hbmspace.blockentity.machine.BlockEntityDysonConverterHE;
import com.hbmspace.blockentity.machine.BlockEntityDysonConverterTU;
import com.hbmspace.blockentity.machine.BlockEntityDysonLauncher;
import com.hbmspace.blockentity.machine.BlockEntityDysonReceiver;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class SkyBlockEntitiesSpace {
    public static final Reg.BeHandle<BlockEntityDysonLauncher> DYSON_LAUNCHER =
            HbmSpace.REG
                    .be(
                            "dyson_launcher",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityDysonLauncher::new,
                                            Set.of(SkyBlocksSpace.DYSON_LAUNCHER.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);
    public static final Reg.BeHandle<BlockEntityDysonReceiver> DYSON_RECEIVER =
            HbmSpace.REG
                    .be(
                            "dyson_receiver",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityDysonReceiver::new,
                                            Set.of(SkyBlocksSpace.DYSON_RECEIVER.get())))
                    .ticks(BlockEntityMachineBase::tickClient, BlockEntityMachineBase::tickServer);
    public static final Reg.BeHandle<BlockEntityDysonConverterTU> DYSON_CONVERTER_TU =
            HbmSpace.REG.be(
                    "dyson_converter_tu",
                    () ->
                            new BlockEntityType<>(
                                    BlockEntityDysonConverterTU::new,
                                    Set.of(SkyBlocksSpace.DYSON_CONVERTER_TU.get())));
    public static final Reg.BeHandle<BlockEntityDysonConverterHE> DYSON_CONVERTER_HE =
            HbmSpace.REG
                    .be(
                            "dyson_converter_he",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityDysonConverterHE::new,
                                            Set.of(SkyBlocksSpace.DYSON_CONVERTER_HE.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);
    public static final Reg.BeHandle<BlockEntityDysonConverterAnatmogenesis>
            DYSON_CONVERTER_ANATMOGENESIS =
                    HbmSpace.REG
                            .be(
                                    "dyson_converter_anatmogenesis",
                                    () ->
                                            new BlockEntityType<>(
                                                    BlockEntityDysonConverterAnatmogenesis::new,
                                                    Set.of(
                                                            SkyBlocksSpace
                                                                    .DYSON_CONVERTER_ANATMOGENESIS
                                                                    .get())))
                            .ticks(
                                    BlockEntityMachineBase::tickClient,
                                    BlockEntityMachineBase::tickServer);

    public static final Reg.BeHandle<BlockEntityOrrery> ORRERY =
            HbmSpace.REG.be(
                    "orrery",
                    () ->
                            new BlockEntityType<>(
                                    BlockEntityOrrery::new, Set.of(SkyBlocksSpace.ORRERY.get())));

    private SkyBlockEntitiesSpace() {}
}
