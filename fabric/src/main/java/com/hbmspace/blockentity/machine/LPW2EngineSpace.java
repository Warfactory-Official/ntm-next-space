// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.blockentity.machine.BlockEntityMachineLPW2;
import com.hbm.blocks.ModBlockEntities;
import com.hbm.registration.Reg;

public final class LPW2EngineSpace {
    private LPW2EngineSpace() {}

    public static void install() {
        var handle = (Reg.BeHandle<BlockEntityMachineLPW2>) ModBlockEntities.LPW2;
        handle.ticks(
                be -> ((BlockEntityMachineLPW2Space) be).tickClient(),
                be -> ((BlockEntityMachineLPW2Space) be).tickServer());
    }
}
