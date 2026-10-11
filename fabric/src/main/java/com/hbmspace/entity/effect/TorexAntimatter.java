// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.effect;

import com.hbm.entity.ModEntities;
import com.hbm.entity.effect.EntityNukeTorex.Cloudlet;
import com.hbm.entity.effect.EntityNukeTorex.TorexType;
import com.hbm.entity.effect.EntityNukeTorex;
import java.awt.Color;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class TorexAntimatter {

    public static final int TYPE = 2;

    private TorexAntimatter() {}

    public static void register() {
        EntityNukeTorex.registerPalette(TYPE, TorexAntimatter::colour);
    }

    public static EntityNukeTorex startFacAnti(
            Level level, double x, double y, double z, float scale) {
        EntityNukeTorex torex = new EntityNukeTorex(ModEntities.NUKE_TOREX.get(), level);
        torex.setScale(Mth.clamp(scale * 0.01F, 0.5F, 5F));
        torex.setTorexType(TYPE);
        torex.setPos(x, y, z);
        level.addFreshEntity(torex);
        return torex;
    }

    private static void colour(Cloudlet cloud, double col) {
        if (cloud.type == TorexType.RING) {
            double grey = Math.max(col, 0.25);
            cloud.colorR = grey;
            cloud.colorG = grey;
            cloud.colorB = grey;
        } else {
            int rgb = Color.HSBtoRGB(cloud.angle / 2F / (float) Math.PI, 1F, 1F);
            cloud.colorR = ((rgb >> 16) & 0xFF) / 255D;
            cloud.colorG = ((rgb >> 8) & 0xFF) / 255D;
            cloud.colorB = (rgb & 0xFF) / 255D;
        }
    }
}
