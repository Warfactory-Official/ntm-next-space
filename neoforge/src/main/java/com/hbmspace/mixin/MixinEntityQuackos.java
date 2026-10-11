// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.mob.EntityQuackos;
import com.hbmspace.data.SpaceData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityQuackos.class)
public abstract class MixinEntityQuackos extends EntityDuck {
    @Unique private int hbmspace$killCounter;

    private MixinEntityQuackos(EntityType<? extends EntityDuck> type, Level level) {
        super(type, level);
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void hbmspace$cullable(RemovalReason reason, CallbackInfo ci) {
        if (level().isClientSide()
                || reason != RemovalReason.KILLED && reason != RemovalReason.DISCARDED
                || !SpaceData.SERVER_SAFETY.get()) return;
        hbmspace$killCounter += 2;
        if (hbmspace$killCounter > 20) super.remove(reason);
        ci.cancel();
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void hbmspace$cool(CallbackInfo ci) {
        if (hbmspace$killCounter > 0) hbmspace$killCounter--;
    }
}
