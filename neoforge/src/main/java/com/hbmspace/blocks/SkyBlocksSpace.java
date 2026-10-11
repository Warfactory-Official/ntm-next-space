// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.generic.BlockOrrery;
import com.hbmspace.blocks.machine.MachineDysonConverterAnatmogenesis;
import com.hbmspace.blocks.machine.MachineDysonConverterHE;
import com.hbmspace.blocks.machine.MachineDysonConverterTU;
import com.hbmspace.blocks.machine.MachineDysonLauncher;
import com.hbmspace.blocks.machine.MachineDysonReceiver;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class SkyBlocksSpace {

    private static final String STEEL_MACHINE = "hbmspace:block/block_steel_machine";

    public static final Reg.Handle<MachineDysonLauncher> DYSON_LAUNCHER =
            HbmSpace.REG.blockItem(
                    "dyson_launcher",
                    MachineDysonLauncher::new,
                    SkyBlocksSpace::machine,
                    (block, props) -> new DescBlockItem(block, props, 2));

    public static final Reg.Handle<MachineDysonReceiver> DYSON_RECEIVER =
            HbmSpace.REG.blockItem(
                    "dyson_receiver",
                    MachineDysonReceiver::new,
                    SkyBlocksSpace::machine,
                    (block, props) -> new DescBlockItem(block, props, 2));

    public static final Reg.Handle<MachineDysonConverterTU> DYSON_CONVERTER_TU =
            HbmSpace.REG.blockItem(
                    "dyson_converter_tu", MachineDysonConverterTU::new, SkyBlocksSpace::machine);

    public static final Reg.Handle<MachineDysonConverterHE> DYSON_CONVERTER_HE =
            HbmSpace.REG.blockItem(
                    "dyson_converter_he", MachineDysonConverterHE::new, SkyBlocksSpace::machine);

    public static final Reg.Handle<MachineDysonConverterAnatmogenesis>
            DYSON_CONVERTER_ANATMOGENESIS =
                    HbmSpace.REG.blockItem(
                            "dyson_converter_anatmogenesis",
                            MachineDysonConverterAnatmogenesis::new,
                            SkyBlocksSpace::machine);

    public static final Reg.Handle<BlockOrrery> ORRERY =
            HbmSpace.REG.blockItem("orrery", BlockOrrery::new, SkyBlocksSpace::machine);

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
                    Tab.MACHINES, Library.id("drone_patrol"), () -> new ItemStack(block));
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
