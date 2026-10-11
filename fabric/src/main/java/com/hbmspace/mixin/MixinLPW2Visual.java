// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityMachineLPW2;
import com.hbm.client.render.flywheel.HbmDynamicBlockEntityVisual;
import com.hbm.client.render.flywheel.LPW2Visual;
import com.hbm.util.GameTime;
import com.hbmspace.blockentity.machine.BlockEntityMachineLPW2Space;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LPW2Visual.class)
public abstract class MixinLPW2Visual extends HbmDynamicBlockEntityVisual<BlockEntityMachineLPW2> {
    protected MixinLPW2Visual(
            VisualizationContext context, BlockEntityMachineLPW2 be, float partialTicks) {
        super(context, be, partialTicks);
    }

    @ModifyVariable(method = "writeFrame", at = @At("STORE"), ordinal = 0)
    private double hbmspace$engineTime(double time, float partialTicks) {
        return ((BlockEntityMachineLPW2Space) blockEntity).renderTime(partialTicks);
    }

    @ModifyVariable(method = "writeFrame", at = @At("STORE"), name = "errorTimer")
    private double hbmspace$terminalTime(double timer, float partialTicks) {
        return ((GameTime.millis(blockEntity.getLevel()) / 50L) % 1_000_000L + partialTicks) / 3D;
    }
}
