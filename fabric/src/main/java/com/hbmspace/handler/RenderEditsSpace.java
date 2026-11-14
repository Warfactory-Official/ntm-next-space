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

package com.hbmspace.handler;

import com.hbm.render.util.InnerArmorModel;
import com.hbm.render.util.RiderTilt;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.hbmspace.items.armor.ArmorModsSpace;
import net.minecraft.util.Mth;

public final class RenderEditsSpace {

    private RenderEditsSpace() {}

    public static void install() {
        RiderTilt.POINT.install(
                HbmSpace.MOD_ID,
                (vehicle, partialTick) ->
                        vehicle instanceof RideableRocketEntity rocket
                                ? -Mth.lerp(partialTick, rocket.xRotO, rocket.getXRot())
                                : 0F);
        InnerArmorModel.POINT.install(
                HbmSpace.MOD_ID, stack -> stack.is(ArmorModsSpace.HEAVY_BOOTS.get()));
    }
}
