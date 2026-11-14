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

package com.hbmspace.client;

import com.hbm.api.environment.Environment;
import com.hbm.client.ClientEffects;
import com.hbmspace.blockentity.deco.GeysirDCMBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class FloraClientEffects {

    private FloraClientEffects() {}

    public static void install() {
        GeysirDCMBlockEntity.CLIENT_EFFECTS = FloraClientEffects::dcmContrail;
    }

    private static void dcmContrail(GeysirDCMBlockEntity be) {
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        double x = pos.getX() + 0.5D, y = pos.getY(), z = pos.getZ() + 0.5D;
        Player player = Minecraft.getInstance().player;
        if (player == null || player.distanceToSqr(x, y, z) > 350D * 350D) return;
        ClientEffects.spawnRocketFlameInAir(
                level,
                x,
                y,
                z,
                1.5F,
                0D,
                4D,
                0D,
                100 + level.getRandom().nextInt(20),
                Environment.particlePressure(level),
                0xA4 / 255F,
                0xD7 / 255F,
                0xDD / 255F);
    }
}
