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

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BobbleBlock.BobbleType;
import com.hbm.blocks.generic.BobbleBlock;
import com.hbm.blocks.generic.PlushieBlock.PlushieType;
import com.hbm.blocks.generic.PlushieBlock;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.block.PlushieBlockItem;
import com.hbm.items.block.TrinketBlockItem;
import com.hbm.registration.ItemFamily;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.generic.BobbleTypeSpace;
import com.hbmspace.blocks.generic.PlushieTypeSpace;
import com.hbmspace.items.block.BobbleGwenBlockItem;
import java.util.Locale;
import net.minecraft.world.item.Item;

public final class TrinketsSpace {

    public static final ItemFamily<PlushieTypeSpace, PlushieBlockItem> PLUSHIES =
            HbmSpace.REG.family(
                    PlushieTypeSpace.values(),
                    type -> "plushie_" + type.name().toLowerCase(Locale.ROOT),
                    (props, type) -> new PlushieBlockItem(ModBlocks.PLUSHIE.get(), props, type),
                    Item.Properties::new);

    public static final ItemFamily<BobbleTypeSpace, TrinketBlockItem<BobbleTypeSpace>> BOBBLEHEADS =
            HbmSpace.REG.<BobbleTypeSpace, TrinketBlockItem<BobbleTypeSpace>>family(
                    BobbleTypeSpace.values(),
                    type -> "bobblehead_" + type.name().toLowerCase(Locale.ROOT),
                    (props, type) ->
                            type == BobbleTypeSpace.GWEN
                                    ? new BobbleGwenBlockItem(ModBlocks.BOBBLEHEAD.get(), props)
                                    : new TrinketBlockItem<>(
                                            ModBlocks.BOBBLEHEAD.get(), props, type),
                    Item.Properties::new);

    static {
        PlushieBlock.TYPES.addAfter(PlushieType.HUNDUN, PlushieTypeSpace.TETO);
        PlushieBlock.TYPES.addAfter(PlushieType.HUNDUN, PlushieTypeSpace.MIKU);
        PlushieBlock.TYPES.addAfter(PlushieType.HUNDUN, PlushieTypeSpace.NERU);
        PlushieBlock.TYPES.addAfter(PlushieType.DERG, PlushieTypeSpace.FATO);
        BobbleBlock.TYPES.addAfter(BobbleType.CIRNO, BobbleTypeSpace.GWEN);
        BobbleBlock.TYPES.addAfter(BobbleType.CIRNO, BobbleTypeSpace.JUICE);
        BobbleBlock.TYPES.addAfter(BobbleType.CIRNO, BobbleTypeSpace.JAMESH_2);
        BobbleBlock.TYPES.addAfter(BobbleType.MELLOW, BobbleTypeSpace.MRKIMKIMORA);

        for (PlushieTypeSpace type :
                new PlushieTypeSpace[] {
                    PlushieTypeSpace.TETO, PlushieTypeSpace.MIKU, PlushieTypeSpace.NERU
                }) {
            CreativeContents.insertAfter(
                    Tab.BLOCKS, RegistryUtil.id("plushie_hundun"), () -> PLUSHIES.stack(type));
        }
        for (BobbleTypeSpace type :
                new BobbleTypeSpace[] {
                    BobbleTypeSpace.GWEN, BobbleTypeSpace.JUICE, BobbleTypeSpace.JAMESH_2
                }) {
            CreativeContents.insertAfter(
                    Tab.BLOCKS, RegistryUtil.id("bobblehead_cirno"), () -> BOBBLEHEADS.stack(type));
        }
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                RegistryUtil.id("bobblehead_mellow"),
                () -> BOBBLEHEADS.stack(BobbleTypeSpace.MRKIMKIMORA));
    }

    private TrinketsSpace() {}
}
