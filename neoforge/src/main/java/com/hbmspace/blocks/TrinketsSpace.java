// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.BlockBobble;
import com.hbm.blocks.generic.BlockPlushie.PlushieType;
import com.hbm.blocks.generic.BlockPlushie;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.block.ItemBlockPlushie;
import com.hbm.items.block.ItemBlockTrinket;
import com.hbm.lib.Library;
import com.hbm.registration.ItemFamily;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.generic.BobbleTypeSpace;
import com.hbmspace.blocks.generic.PlushieTypeSpace;
import com.hbmspace.items.block.ItemBlockBobbleGwen;
import java.util.Locale;
import net.minecraft.world.item.Item;

public final class TrinketsSpace {

    public static final ItemFamily<PlushieTypeSpace, ItemBlockPlushie> PLUSHIES =
            HbmSpace.REG.family(
                    PlushieTypeSpace.values(),
                    type -> "plushie_" + type.name().toLowerCase(Locale.ROOT),
                    (props, type) -> new ItemBlockPlushie(ModBlocks.PLUSHIE.get(), props, type),
                    Item.Properties::new);

    public static final ItemFamily<BobbleTypeSpace, ItemBlockTrinket<BobbleTypeSpace>> BOBBLEHEADS =
            HbmSpace.REG.<BobbleTypeSpace, ItemBlockTrinket<BobbleTypeSpace>>family(
                    BobbleTypeSpace.values(),
                    type -> "bobblehead_" + type.name().toLowerCase(Locale.ROOT),
                    (props, type) ->
                            type == BobbleTypeSpace.GWEN
                                    ? new ItemBlockBobbleGwen(ModBlocks.BOBBLEHEAD.get(), props)
                                    : new ItemBlockTrinket<>(
                                            ModBlocks.BOBBLEHEAD.get(), props, type),
                    Item.Properties::new);

    static {
        BlockPlushie.TYPES.addAfter(PlushieType.HUNDUN, PlushieTypeSpace.TETO);
        BlockPlushie.TYPES.addAfter(PlushieType.HUNDUN, PlushieTypeSpace.MIKU);
        BlockPlushie.TYPES.addAfter(PlushieType.HUNDUN, PlushieTypeSpace.NERU);
        BlockPlushie.TYPES.addAfter(PlushieType.DERG, PlushieTypeSpace.FATO);
        BlockBobble.TYPES.addAfter(BobbleType.CIRNO, BobbleTypeSpace.GWEN);
        BlockBobble.TYPES.addAfter(BobbleType.CIRNO, BobbleTypeSpace.JUICE);
        BlockBobble.TYPES.addAfter(BobbleType.CIRNO, BobbleTypeSpace.JAMESH_2);
        BlockBobble.TYPES.addAfter(BobbleType.MELLOW, BobbleTypeSpace.MRKIMKIMORA);

        for (PlushieTypeSpace type :
                new PlushieTypeSpace[] {
                    PlushieTypeSpace.TETO, PlushieTypeSpace.MIKU, PlushieTypeSpace.NERU
                }) {
            CreativeContents.insertAfter(
                    Tab.BLOCKS, Library.id("plushie_hundun"), () -> PLUSHIES.stack(type));
        }
        for (BobbleTypeSpace type :
                new BobbleTypeSpace[] {
                    BobbleTypeSpace.GWEN, BobbleTypeSpace.JUICE, BobbleTypeSpace.JAMESH_2
                }) {
            CreativeContents.insertAfter(
                    Tab.BLOCKS, Library.id("bobblehead_cirno"), () -> BOBBLEHEADS.stack(type));
        }
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Library.id("bobblehead_mellow"),
                () -> BOBBLEHEADS.stack(BobbleTypeSpace.MRKIMKIMORA));
    }

    private TrinketsSpace() {}
}
