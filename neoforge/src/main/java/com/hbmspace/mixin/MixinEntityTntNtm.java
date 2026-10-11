// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.entity.item.EntityTntNtm;
import com.hbmspace.entity.SpinningCharge;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTntNtm.class)
public abstract class MixinEntityTntNtm extends PrimedTnt implements SpinningCharge {

    @Unique private float hbmspace$momentumYaw;
    @Unique private float hbmspace$momentumPitch;
    @Unique private float hbmspace$yaw;
    @Unique private float hbmspace$yawO;
    @Unique private float hbmspace$pitch;
    @Unique private float hbmspace$pitchO;

    private MixinEntityTntNtm(EntityType<? extends PrimedTnt> type, Level level) {
        super(type, level);
    }

    @Inject(
            method =
                    "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL"))
    private void hbmspace$momentum(EntityType<?> type, Level level, CallbackInfo ci) {
        if (!level.isClientSide()) return;
        hbmspace$momentumPitch = (float) (level.getRandom().nextGaussian() * 5F);
        hbmspace$momentumYaw = (float) (level.getRandom().nextGaussian() * 10F);
    }

    @Override
    public void tick() {
        super.tick();
        hbmspace$yawO = hbmspace$yaw;
        hbmspace$pitchO = hbmspace$pitch;
        hbmspace$yaw += hbmspace$momentumYaw;
        hbmspace$pitch += hbmspace$momentumPitch;
    }

    @Override
    public float hbmspace$spinYaw(float partialTicks) {
        return Mth.lerp(partialTicks, hbmspace$yawO, hbmspace$yaw);
    }

    @Override
    public float hbmspace$spinPitch(float partialTicks) {
        return Mth.lerp(partialTicks, hbmspace$pitchO, hbmspace$pitch);
    }
}
