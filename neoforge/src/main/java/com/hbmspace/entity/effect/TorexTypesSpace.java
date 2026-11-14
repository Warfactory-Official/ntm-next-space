/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2022 HbmMods (The Bobcat)
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2022 HbmMods (The Bobcat). All rights reserved.
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

import com.hbm.entity.effect.NukeTorexEntity.Cloudlet;
import com.hbm.entity.effect.NukeTorexEntity.TorexType;
import com.hbm.entity.effect.NukeTorexEntity;
import net.minecraft.util.Mth;

public final class TorexTypesSpace {
    public static final TorexType STARFISH =
            TorexType.of(
                    new NukeTorexEntity.Behavior() {
                        @Override
                        public void move(Cloudlet cloud) {
                            NukeTorexEntity torex = cloud.torex();
                            double factor =
                                    Mth.clamp((cloud.posY - torex.getY()) / torex.coreHeight, 0, 1);
                            cloud.motionX = Mth.cos(cloud.angle) * factor;
                            cloud.motionY = 0;
                            cloud.motionZ = -Mth.sin(cloud.angle) * factor;
                        }

                        @Override
                        public void colour(Cloudlet cloud) {
                            NukeTorexEntity torex = cloud.torex();
                            double distX = torex.getX() - cloud.posX;
                            double distY = torex.getY() + torex.coreHeight - cloud.posY;
                            double distZ = torex.getZ() - cloud.posZ;
                            double dist =
                                    Math.max(
                                            Math.sqrt(
                                                    (distX * distX + distY * distY + distZ * distZ)
                                                            / torex.heat),
                                            1);
                            double col = 2D / dist;
                            cloud.colorR = Math.max(col * 8, 0.5);
                            cloud.colorG = Math.max(col * 1.5, 0.1);
                            cloud.colorB = Math.max(col * 0.5, 0.05);
                        }
                    });

    private TorexTypesSpace() {}
}
