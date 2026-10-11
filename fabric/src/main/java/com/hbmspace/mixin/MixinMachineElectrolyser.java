// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.machine.MachineElectrolyser;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MachineElectrolyser.class)
public abstract class MixinMachineElectrolyser {
    @Inject(method = "visitCells", at = @At("TAIL"))
    private void hbmspace$sideConnections(
            BlockPos core,
            Direction facing,
            BlockMultiblockCore.CellVisitor visitor,
            CallbackInfo ci) {
        Direction rot = facing.getClockWise();
        int dx = facing.getStepX(), dz = facing.getStepZ();
        int rx = rot.getStepX(), rz = rot.getStepZ();
        int side = 1 << rot.ordinal();
        int opposite = 1 << rot.getOpposite().ordinal();
        int back = 1 << facing.getOpposite().ordinal();
        int front = 1 << facing.ordinal();

        visitor.cell(core.offset(-rx * 3, 0, -rz * 3), opposite);
        visitor.cell(core.offset(rx, 0, rz), side);
        visitor.cell(core.offset(-dx * 5 + rx, 0, -dz * 5 + rz), back | side);
        visitor.cell(core.offset(dx * 5 + rx, 0, dz * 5 + rz), front | side);
    }

    @Inject(method = "visitPassiveCells", at = @At("TAIL"))
    private void hbmspace$sideProxies(
            BlockPos core,
            Direction facing,
            BlockMultiblockCore.PassiveCellVisitor visitor,
            CallbackInfo ci) {
        Direction rot = facing.getClockWise();
        int rx = rot.getStepX(), rz = rot.getStepZ();
        int domains =
                BlockMultiblockCore.PASSIVE_POWER_IN
                        | BlockMultiblockCore.PASSIVE_FLUID_IN
                        | BlockMultiblockCore.PASSIVE_ITEMS;
        visitor.passiveCell(
                core.offset(-rx * 3, 0, -rz * 3), BlockMultiblockCore.MASK_ALL, domains);
        visitor.passiveCell(core.offset(rx, 0, rz), BlockMultiblockCore.MASK_ALL, domains);
    }
}
