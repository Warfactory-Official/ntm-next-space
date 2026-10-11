// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.entity.mob.BehemothYawTarget;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import net.minecraft.world.entity.InterpolationHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(InterpolationHandler.class)
public abstract class MixinInterpolationHandler implements BehemothYawTarget {
    @Shadow
    public abstract boolean hasActiveInterpolation();

    @Unique private static final VarHandle hbmspace$interpolationData;
    @Unique private static final VarHandle hbmspace$targetYaw;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> data =
                    Class.forName(
                            "net.minecraft.world.entity.InterpolationHandler$InterpolationData");
            hbmspace$interpolationData =
                    lookup.findVarHandle(InterpolationHandler.class, "interpolationData", data);
            hbmspace$targetYaw =
                    MethodHandles.privateLookupIn(data, lookup)
                            .findVarHandle(data, "yRot", float.class);
        } catch (ReflectiveOperationException failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }

    @Override
    public void hbmspace$pullYaw(double targetYaw) {
        if (!hasActiveInterpolation()) return;
        Object data = hbmspace$interpolationData.get(this);
        float yaw = (float) hbmspace$targetYaw.get(data);
        double delta = (targetYaw - yaw) % 360D;
        if (delta >= 180D) delta -= 360D;
        if (delta < -180D) delta += 360D;
        hbmspace$targetYaw.set(data, (float) (yaw + delta * 0.1D));
    }
}
