// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityMachineMilkReformer;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntitiesSpace {
    public static final Reg.BeHandle<BlockEntityMachineMilkReformer> MILK_REFORMER =
            HbmSpace.REG
                    .be(
                            "milk_reformer",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityMachineMilkReformer::new,
                                            Set.of(ModBlocksSpace.MACHINE_MILK_REFORMER.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    private ModBlockEntitiesSpace() {}
}
