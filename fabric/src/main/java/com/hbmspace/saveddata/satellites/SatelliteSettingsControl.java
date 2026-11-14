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

package com.hbmspace.saveddata.satellites;

import com.hbm.inventory.container.HeldItemMenu;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.inventory.container.RocketryMenusSpace;
import com.hbmspace.items.ModDataComponentsSpace;
import io.netty.handler.codec.DecoderException;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class SatelliteSettingsControl {

    private SatelliteSettingsControl() {}

    public static void apply(
            ServerPlayer player,
            int containerId,
            String owner,
            float altitude,
            float inclination,
            float phaseOffset,
            boolean blinking,
            float blinkPeriod,
            int red,
            int green,
            int blue) {
        if (!Float.isFinite(altitude)
                || !Float.isFinite(inclination)
                || !Float.isFinite(phaseOffset)
                || !Float.isFinite(blinkPeriod)) {
            throw new DecoderException("Nonfinite satellite setting");
        }
        HeldItemMenu menu =
                HeldItemMenu.current(player, containerId, RocketryMenusSpace.SATELLITE_SETTINGS);
        if (menu == null) return;
        ItemStack stack = menu.stack();
        SatelliteType type = SatelliteType.fromStack(stack);
        if (!SatellitesSpace.isSpace(type)) return;

        SatelliteOrbit old =
                stack.getOrDefault(
                        ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                        SatellitesSpace.defaults(type));
        String resolvedOwner =
                owner.equals(old.owner()) ? old.owner() : player.getName().getString();
        stack.set(
                ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                new SatelliteOrbit(
                        Mth.clamp(
                                inclination,
                                SatelliteOrbit.MIN_INCLINATION,
                                SatelliteOrbit.MAX_INCLINATION),
                        Mth.clamp(
                                altitude,
                                SatelliteOrbit.MIN_ALTITUDE_KM,
                                SatelliteOrbit.MAX_ALTITUDE_KM),
                        phaseOffset,
                        blinking,
                        blinkPeriod,
                        resolvedOwner,
                        Mth.clamp(red, 0, 255) / 255F,
                        Mth.clamp(green, 0, 255) / 255F,
                        Mth.clamp(blue, 0, 255) / 255F));
    }
}
