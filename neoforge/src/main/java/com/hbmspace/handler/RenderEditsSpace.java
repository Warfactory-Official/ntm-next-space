// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.render.util.InnerArmorModel;
import com.hbm.render.util.RiderTilt;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.items.armor.ArmorModsSpace;
import net.minecraft.util.Mth;

public final class RenderEditsSpace {

    private RenderEditsSpace() {}

    public static void install() {
        RiderTilt.POINT.install(
                HbmSpace.MOD_ID,
                (vehicle, partialTick) ->
                        vehicle instanceof EntityRideableRocket rocket
                                ? -Mth.lerp(partialTick, rocket.xRotO, rocket.getXRot())
                                : 0F);
        InnerArmorModel.POINT.install(
                HbmSpace.MOD_ID, stack -> stack.is(ArmorModsSpace.HEAVY_BOOTS.get()));
    }
}
