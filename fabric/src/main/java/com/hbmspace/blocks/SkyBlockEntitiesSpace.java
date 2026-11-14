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
import com.hbmspace.blockentity.deco.OrreryBlockEntity;
import com.hbmspace.blockentity.machine.DysonConverterAnatmogenesisBlockEntity;
import com.hbmspace.blockentity.machine.DysonConverterHEBlockEntity;
import com.hbmspace.blockentity.machine.DysonConverterTUBlockEntity;
import com.hbmspace.blockentity.machine.DysonLauncherBlockEntity;
import com.hbmspace.blockentity.machine.DysonReceiverBlockEntity;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class SkyBlockEntitiesSpace {
    public static final Reg.BeHandle<DysonLauncherBlockEntity> DYSON_LAUNCHER =
            (HbmSpace.REG.<DysonLauncherBlockEntity>be(
                            "dyson_launcher",
                            () ->
                                    new BlockEntityType<>(
                                            DysonLauncherBlockEntity::new,
                                            Set.of(SkyBlocksSpace.DYSON_LAUNCHER.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);
    public static final Reg.BeHandle<DysonReceiverBlockEntity> DYSON_RECEIVER =
            (HbmSpace.REG.<DysonReceiverBlockEntity>be(
                            "dyson_receiver",
                            () ->
                                    new BlockEntityType<>(
                                            DysonReceiverBlockEntity::new,
                                            Set.of(SkyBlocksSpace.DYSON_RECEIVER.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER_WITH_SOUND)
                    .ticksClientAt(BlockEntityTicks.CLIENT);
    public static final Reg.BeHandle<DysonConverterTUBlockEntity> DYSON_CONVERTER_TU =
            (HbmSpace.REG.<DysonConverterTUBlockEntity>be(
                    "dyson_converter_tu",
                    () ->
                            new BlockEntityType<>(
                                    DysonConverterTUBlockEntity::new,
                                    Set.of(SkyBlocksSpace.DYSON_CONVERTER_TU.get()))));
    public static final Reg.BeHandle<DysonConverterHEBlockEntity> DYSON_CONVERTER_HE =
            (HbmSpace.REG.<DysonConverterHEBlockEntity>be(
                            "dyson_converter_he",
                            () ->
                                    new BlockEntityType<>(
                                            DysonConverterHEBlockEntity::new,
                                            Set.of(SkyBlocksSpace.DYSON_CONVERTER_HE.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);
    public static final Reg.BeHandle<DysonConverterAnatmogenesisBlockEntity>
            DYSON_CONVERTER_ANATMOGENESIS =
                    (HbmSpace.REG.<DysonConverterAnatmogenesisBlockEntity>be(
                                    "dyson_converter_anatmogenesis",
                                    () ->
                                            new BlockEntityType<>(
                                                    DysonConverterAnatmogenesisBlockEntity::new,
                                                    Set.of(
                                                            SkyBlocksSpace
                                                                    .DYSON_CONVERTER_ANATMOGENESIS
                                                                    .get()))))
                            .ticksServerAt(BlockEntityTicks.SERVER)
                            .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<OrreryBlockEntity> ORRERY =
            (HbmSpace.REG.<OrreryBlockEntity>be(
                    "orrery",
                    () ->
                            new BlockEntityType<>(
                                    OrreryBlockEntity::new, Set.of(SkyBlocksSpace.ORRERY.get()))));

    private SkyBlockEntitiesSpace() {}
}
