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
import com.hbm.client.model.BarrelModel;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity;
import com.hbmspace.blocks.bomb.NukeAntimatter;
import com.hbmspace.blocks.generic.BrownBarrelBlock;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class BombBlocksSpace {
    public static final Reg.Handle<NukeAntimatter> NUKE_ANTIMATTER =
            HbmSpace.REG.blockItem(
                    "nuke_antimatter",
                    NukeAntimatter::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 3600.0F)
                                    .noOcclusion()
                                    .requiresCorrectToolForDrops());

    public static final Reg.BeHandle<NukeAntimatterBlockEntity> NUKE_ANTIMATTER_BE =
            (HbmSpace.REG.<NukeAntimatterBlockEntity>be(
                            "nuke_antimatter",
                            () ->
                                    new BlockEntityType<>(
                                            NukeAntimatterBlockEntity::new,
                                            Set.of(NUKE_ANTIMATTER.get()))))
                    .ticksServerAt(BlockEntityTicks.SERVER);

    public static final Reg.Handle<BrownBarrelBlock> OIL_BARREL =
            HbmSpace.REG
                    .blockItem(
                            "oil_barrel",
                            BrownBarrelBlock::new,
                            () ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.METAL)
                                            .strength(0.5F, 1.5F)
                                            .noOcclusion()
                                            .requiresCorrectToolForDrops())
                    .bakedBy(() -> new BarrelModel());

    static {
        CreativeContents.insertBefore(
                Tab.NUKES,
                RegistryUtil.id("nuke_fstbmb"),
                () -> new ItemStack(NUKE_ANTIMATTER.get()));
        CreativeContents.insertAfter(
                Tab.NUKES,
                RegistryUtil.id("vitrified_barrel"),
                () -> new ItemStack(OIL_BARREL.get()));
    }

    private BombBlocksSpace() {}
}
