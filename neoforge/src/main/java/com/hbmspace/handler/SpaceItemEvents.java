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

import com.hbm.NuclearTech;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.HbmConfig;
import com.hbm.config.RuntimeConfig;
import com.hbmspace.items.PartsItemsSpace;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.apache.logging.log4j.LogManager;

public final class SpaceItemEvents {
    private static final UUID MELLOW = UUID.fromString("99b9efa5-8c38-4b22-86c2-baf97789d2fb");

    private SpaceItemEvents() {}

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (entity.level() instanceof ServerLevel level && entity.getUUID().equals(MELLOW)) {
            entity.spawnAtLocation(
                    level,
                    new ItemStack(ModBlocks.BLOCK_METEOR.get(), 1 + entity.getRandom().nextInt(10)),
                    0F);
        }
    }

    public static void killedByPlayer(LivingEntity entity) {
        if (entity instanceof Villager
                && entity.level() instanceof ServerLevel level
                && level.getGameRules().get(GameRules.MOB_DROPS)) {
            entity.spawnAtLocation(level, new ItemStack(PartsItemsSpace.FLESH.get(), 5), 0F);
        }
    }

    public static void hiddenCatalogObtained(Player player, BlockPos pos) {
        LogManager.getLogger("NTM")
                .fatal(
                        "THE HIDDENCAT HAS BEEN OBTAINED  x: {} /  y: {} / z: {} by {}!",
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        player.getDisplayName().getString());
    }

    public static void leverUsed(Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !((RuntimeConfig) HbmConfig.RUNTIME).extendedLogging()) return;
        NuclearTech.LOGGER.info(
                "[DET] pulled lever at {} / {} / {} by {}!",
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                player.getDisplayName().getString());
    }
}
