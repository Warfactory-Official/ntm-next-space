// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.machine.MachineEPress;
import com.hbm.blocks.machine.MachinePress;
import com.hbm.blocks.multiblock.BlockMultiblockCell;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.interfaces.IToolable.ToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockMultiblockCell.class)
public abstract class MixinBlockMultiblockCell {
    @Inject(method = "onScrew", at = @At("HEAD"), cancellable = true)
    private void hbmspace$pressDummyDrill(
            Level level,
            Player player,
            BlockPos pos,
            @Nullable Direction side,
            Vec3 hit,
            ToolType tool,
            CallbackInfoReturnable<Boolean> cir) {
        if (tool != ToolType.HAND_DRILL) return;
        BlockPos core = MultiblockSurface.coreOfAny(level, pos);
        if (core == null || core.equals(pos)) return;
        var owner = level.getBlockState(core).getBlock();
        if (!(owner instanceof MachinePress || owner instanceof MachineEPress)) return;
        BlockMultiblockCore.withoutTeardown(() -> level.removeBlock(pos, false));
        cir.setReturnValue(true);
    }
}
