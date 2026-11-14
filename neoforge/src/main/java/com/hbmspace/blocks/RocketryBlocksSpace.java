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
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.CombatDropPodBlockEntity;
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity;
import com.hbmspace.blockentity.machine.DriveProcessorBlockEntity;
import com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity;
import com.hbmspace.blockentity.machine.StardarBlockEntity;
import com.hbmspace.blocks.bomb.CombatDropPod;
import com.hbmspace.blocks.bomb.LaunchPadRocket;
import com.hbmspace.blocks.machine.DriveProcessorBlock;
import com.hbmspace.blocks.machine.RocketAssemblyBlock;
import com.hbmspace.blocks.machine.StardarBlock;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class RocketryBlocksSpace {
    public static final Reg.Handle<RocketAssemblyBlock> MACHINE_ROCKET_ASSEMBLY =
            HbmSpace.REG.blockItem(
                    "machine_rocket_assembly",
                    RocketAssemblyBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 3));

    public static final Reg.Handle<LaunchPadRocket> LAUNCH_PAD_ROCKET =
            HbmSpace.REG.blockItem(
                    "launch_pad_rocket",
                    LaunchPadRocket::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<StardarBlock> MACHINE_STARDAR =
            HbmSpace.REG.blockItem(
                    "machine_stardar",
                    StardarBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 3));

    public static final Reg.Handle<DriveProcessorBlock> MACHINE_DRIVE_PROCESSOR =
            HbmSpace.REG.blockItem(
                    "machine_drive_processor",
                    DriveProcessorBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion());

    public static final Reg.Handle<CombatDropPod> COMBAT_DROP =
            HbmSpace.REG.blockItem(
                    "combat_drop",
                    CombatDropPod::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion());

    public static final Reg.BeHandle<RocketAssemblyBlockEntity> ROCKET_ASSEMBLY =
            (HbmSpace.REG.<RocketAssemblyBlockEntity>be(
                            "machine_rocket_assembly",
                            () ->
                                    new BlockEntityType<>(
                                            RocketAssemblyBlockEntity::new,
                                            Set.of(MACHINE_ROCKET_ASSEMBLY.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<LaunchPadRocketBlockEntity> LAUNCH_PAD_ROCKET_BE =
            (HbmSpace.REG.<LaunchPadRocketBlockEntity>be(
                            "launch_pad_rocket",
                            () ->
                                    new BlockEntityType<>(
                                            LaunchPadRocketBlockEntity::new,
                                            Set.of(LAUNCH_PAD_ROCKET.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.BeHandle<StardarBlockEntity> STARDAR_BE =
            (HbmSpace.REG.<StardarBlockEntity>be(
                            "machine_stardar",
                            () ->
                                    new BlockEntityType<>(
                                            StardarBlockEntity::new,
                                            Set.of(MACHINE_STARDAR.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<CombatDropPodBlockEntity> COMBAT_DROP_BE =
            (HbmSpace.REG.<CombatDropPodBlockEntity>be(
                            "combat_drop",
                            () ->
                                    new BlockEntityType<>(
                                            CombatDropPodBlockEntity::new,
                                            Set.of(COMBAT_DROP.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER)
                    .ticksClientAt(BlockEntityTicks.CLIENT);

    public static final Reg.BeHandle<DriveProcessorBlockEntity> DRIVE_PROCESSOR_BE =
            (HbmSpace.REG.<DriveProcessorBlockEntity>be(
                            "machine_drive_processor",
                            () ->
                                    new BlockEntityType<>(
                                            DriveProcessorBlockEntity::new,
                                            Set.of(MACHINE_DRIVE_PROCESSOR.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    static {
        CreativeContents.insertAfter(
                Tab.MISSILES,
                RegistryUtil.id("machine_missile_assembly"),
                () -> new ItemStack(MACHINE_ROCKET_ASSEMBLY.get()));
        CreativeContents.insertAfter(
                Tab.MISSILES,
                RegistryUtil.id("launch_pad"),
                () -> new ItemStack(LAUNCH_PAD_ROCKET.get()));
        CreativeContents.insertAfter(
                Tab.MISSILES,
                RegistryUtil.id("soyuz_capsule"),
                () -> new ItemStack(COMBAT_DROP.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "machine_solar"),
                () -> new ItemStack(MACHINE_STARDAR.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "machine_stardar"),
                () -> new ItemStack(MACHINE_DRIVE_PROCESSOR.get()));
    }

    private RocketryBlocksSpace() {}
}
