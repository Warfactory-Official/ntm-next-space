/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 HbmMods (The Bobcat)
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 HbmMods (The Bobcat). All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.entity.effect;

import com.hbm.entity.ModEntities;
import com.hbm.entity.effect.NukeTorexEntity.Cloudlet;
import com.hbm.entity.effect.NukeTorexEntity.TorexType;
import com.hbm.entity.effect.NukeTorexEntity;
import java.awt.Color;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class TorexAntimatter {

    public static final int TYPE = 2;

    private TorexAntimatter() {}

    public static void register() {
        NukeTorexEntity.registerPalette(TYPE, TorexAntimatter::colour);
    }

    public static NukeTorexEntity startFacAnti(
            Level level, double x, double y, double z, float scale) {
        NukeTorexEntity torex = new NukeTorexEntity(ModEntities.NUKE_TOREX.get(), level);
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
