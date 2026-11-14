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

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityTicks;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.WarControllerBlockEntity;
import com.hbmspace.blocks.machine.WarController;
import java.util.Set;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class WarBlocksSpace {
    public static final Reg.Handle<WarController> WAR_CONTROLLER =
            HbmSpace.REG.blockItem(
                    "war_controller",
                    WarController::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(10.0F, 6.0F)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops());

    public static final Reg.BeHandle<WarControllerBlockEntity> WAR_CONTROLLER_BE =
            (HbmSpace.REG.<WarControllerBlockEntity>be(
                            "war_controller",
                            () ->
                                    new BlockEntityType<>(
                                            WarControllerBlockEntity::new,
                                            Set.of(WAR_CONTROLLER.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    private WarBlocksSpace() {}
}
