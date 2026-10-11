// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.effect;

import com.hbm.entity.effect.EntityNukeTorex.Cloudlet;
import com.hbm.entity.effect.EntityNukeTorex.TorexType;
import com.hbm.entity.effect.EntityNukeTorex;
import net.minecraft.util.Mth;

public final class TorexTypesSpace {
    public static final TorexType STARFISH =
            TorexType.of(
                    new EntityNukeTorex.Behavior() {
                        @Override
                        public void move(Cloudlet cloud) {
                            EntityNukeTorex torex = cloud.torex();
                            double factor =
                                    Mth.clamp((cloud.posY - torex.getY()) / torex.coreHeight, 0, 1);
                            cloud.motionX = Mth.cos(cloud.angle) * factor;
                            cloud.motionY = 0;
                            cloud.motionZ = -Mth.sin(cloud.angle) * factor;
                        }

                        @Override
                        public void colour(Cloudlet cloud) {
                            EntityNukeTorex torex = cloud.torex();
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
