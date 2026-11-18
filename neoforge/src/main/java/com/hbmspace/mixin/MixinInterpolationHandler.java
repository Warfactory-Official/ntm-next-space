/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.mixin;

import com.hbmspace.entity.mob.BehemothYawTarget;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.LinkedList;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.world.entity.SteppedInterpolationHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SteppedInterpolationHandler.class)
public abstract class MixinInterpolationHandler implements BehemothYawTarget {
    @Shadow
    public abstract boolean hasActiveInterpolation();

    @Unique private static final VarHandle hbmspace$interpolationData;
    @Unique private static final VarHandle hbmspace$remainingSteps;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> data =
                    Class.forName(
                            "net.minecraft.world.entity.SteppedInterpolationHandler$InterpolationData");
            hbmspace$interpolationData =
                    lookup.findVarHandle(
                            SteppedInterpolationHandler.class, "interpolationData", data);
            hbmspace$remainingSteps =
                    MethodHandles.privateLookupIn(data, lookup)
                            .findVarHandle(data, "remainingSteps", LinkedList.class);
        } catch (ReflectiveOperationException failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }

    @Override
    public void hbmspace$pullYaw(double targetYaw) {
        if (!hasActiveInterpolation()) return;
        Object data = hbmspace$interpolationData.get(this);
        for (Object step : (LinkedList<?>) hbmspace$remainingSteps.get(data)) {
            hbmspace$easeTargetYaw((PositionAndRotation.Mutable) step, targetYaw);
        }
        hbmspace$easeTargetYaw((PositionAndRotation.Mutable) data, targetYaw);
    }

    @Unique
    private static void hbmspace$easeTargetYaw(
            PositionAndRotation.Mutable endpoint, double targetYaw) {
        float yaw = endpoint.yRot();
        double delta = (targetYaw - yaw) % 360D;
        if (delta >= 180D) delta -= 360D;
        if (delta < -180D) delta += 360D;
        endpoint.set(endpoint.position(), (float) (yaw + delta * 0.1D), endpoint.xRot());
    }
}
