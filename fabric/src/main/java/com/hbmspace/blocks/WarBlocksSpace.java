// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityMachineWarController;
import com.hbmspace.blocks.machine.WarController;
import java.util.Set;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class WarBlocksSpace {
    public static final Reg.Handle<WarController> WAR_CONTROLLER =
            HbmSpace.REG.blockItem(
                    "war_controller",
                    WarController::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(10.0F, 6.0F)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops());

    public static final Reg.BeHandle<BlockEntityMachineWarController> WAR_CONTROLLER_BE =
            HbmSpace.REG
                    .be(
                            "war_controller",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineWarController::new,
                                            Set.of(WAR_CONTROLLER.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    private WarBlocksSpace() {}
}
