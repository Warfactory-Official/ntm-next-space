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

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.generic.OrreryBlock;
import com.hbmspace.blocks.machine.DysonConverterAnatmogenesisBlock;
import com.hbmspace.blocks.machine.DysonConverterHEBlock;
import com.hbmspace.blocks.machine.DysonConverterTUBlock;
import com.hbmspace.blocks.machine.DysonLauncherBlock;
import com.hbmspace.blocks.machine.DysonReceiverBlock;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class SkyBlocksSpace {
    public static final Reg.Handle<DysonLauncherBlock> DYSON_LAUNCHER =
            HbmSpace.REG.blockItem(
                    "dyson_launcher",
                    DysonLauncherBlock::new,
                    SkyBlocksSpace::machine,
                    (block, props) -> new DescBlockItem(block, props, 2));

    public static final Reg.Handle<DysonReceiverBlock> DYSON_RECEIVER =
            HbmSpace.REG.blockItem(
                    "dyson_receiver",
                    DysonReceiverBlock::new,
                    SkyBlocksSpace::machine,
                    (block, props) -> new DescBlockItem(block, props, 2));

    public static final Reg.Handle<DysonConverterTUBlock> DYSON_CONVERTER_TU =
            HbmSpace.REG.blockItem(
                    "dyson_converter_tu", DysonConverterTUBlock::new, SkyBlocksSpace::machine);

    public static final Reg.Handle<DysonConverterHEBlock> DYSON_CONVERTER_HE =
            HbmSpace.REG.blockItem(
                    "dyson_converter_he", DysonConverterHEBlock::new, SkyBlocksSpace::machine);

    public static final Reg.Handle<DysonConverterAnatmogenesisBlock> DYSON_CONVERTER_ANATMOGENESIS =
            HbmSpace.REG.blockItem(
                    "dyson_converter_anatmogenesis",
                    DysonConverterAnatmogenesisBlock::new,
                    SkyBlocksSpace::machine);

    public static final Reg.Handle<OrreryBlock> ORRERY =
            HbmSpace.REG.blockItem("orrery", OrreryBlock::new, SkyBlocksSpace::machine);

    static {
        for (ItemLike block :
                List.of(
                        DYSON_LAUNCHER,
                        DYSON_RECEIVER,
                        DYSON_CONVERTER_TU,
                        DYSON_CONVERTER_HE,
                        DYSON_CONVERTER_ANATMOGENESIS,
                        ORRERY)) {
            CreativeContents.insertBefore(
                    Tab.MACHINES, RegistryUtil.id("drone_patrol"), () -> new ItemStack(block));
        }
    }

    private SkyBlocksSpace() {}

    private static BlockBehaviour.Properties machine() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(10.0F, 12.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }
}
