// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityCondenserPowered;
import com.hbm.client.render.flywheel.CondenserPoweredVisual;
import com.hbm.client.render.flywheel.HbmDynamicBlockEntityVisual;
import com.hbmspace.blockentity.machine.ThermalMachineDamage;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CondenserPoweredVisual.class)
public abstract class MixinCondenserPoweredVisual
        extends HbmDynamicBlockEntityVisual<BlockEntityCondenserPowered> {
    @Shadow @Final private TransformedInstance[] instances;

    protected MixinCondenserPoweredVisual(
            VisualizationContext context, BlockEntityCondenserPowered be, float partialTicks) {
        super(context, be, partialTicks);
    }

    @Inject(method = "updateMovingParts", at = @At("HEAD"), cancellable = true)
    private void hbmspace$damagedParts(float partialTicks, CallbackInfo ci) {
        boolean damaged = ThermalMachineDamage.damaged(blockEntity);
        for (TransformedInstance instance : instances) instance.setVisible(!damaged);
        if (damaged) ci.cancel();
    }
}
