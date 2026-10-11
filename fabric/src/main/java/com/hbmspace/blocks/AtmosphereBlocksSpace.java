// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.items.machine.ItemFluidContainerInfinite;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.generic.MeltedFlesh;
import com.hbmspace.blocks.machine.AtmoTower;
import com.hbmspace.blocks.machine.BlockAirPump;
import com.hbmspace.blocks.machine.BlockAirScrubber;
import com.hbmspace.blocks.machine.BlockAtmosphereEditor;
import com.hbmspace.blocks.machine.BlockAtmosphericCompressor;
import com.hbmspace.blocks.machine.MachineAtmosphericEmitter;
import com.hbmspace.items.tool.ItemAtmosphereScanner;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class AtmosphereBlocksSpace {

    private static final String CONCRETE = "hbm:block/concrete";

    private static BlockBehaviour.Properties iron(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops();
    }

    public static final Reg.Handle<BlockAirPump> AIR_VENT =
            HbmSpace.REG.blockItem("air_vent", BlockAirPump::new, () -> iron(5.0F, 6.0F));

    public static final Reg.Handle<BlockAirScrubber> AIR_SCRUBBER =
            HbmSpace.REG.blockItem(
                    "air_scrubber",
                    BlockAirScrubber::new,
                    () -> iron(5.0F, 6.0F).noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<AtmoTower> MACHINE_ATMO_TOWER =
            HbmSpace.REG.blockItem(
                    "machine_atmo_tower", AtmoTower::new, () -> iron(10.0F, 12.0F).noOcclusion());

    public static final Reg.Handle<BlockAtmosphericCompressor> MACHINE_ATMO_VENT =
            HbmSpace.REG.blockItem(
                    "machine_atmo_vent",
                    BlockAtmosphericCompressor::new,
                    () -> iron(10.0F, 12.0F).noOcclusion());

    public static final Reg.Handle<MachineAtmosphericEmitter> MACHINE_ATMO_EMITTER =
            HbmSpace.REG.blockItem(
                    "machine_atmo_emitter",
                    MachineAtmosphericEmitter::new,
                    () -> iron(10.0F, 12.0F).noOcclusion());

    public static final Reg.Handle<BlockAtmosphereEditor> ATMOSPHERE_EDITOR =
            HbmSpace.REG.blockItem(
                    "atmosphere_editor",
                    BlockAtmosphereEditor::new,
                    () -> iron(1.0F, 1.0F),
                    (block, props) -> new DescBlockItem(block, props, 4, DescBlockItem.Desc.PLAIN));

    public static final Reg.Handle<ItemAtmosphereScanner> ATMOSPHERE_SCANNER =
            HbmSpace.REG.item(
                    "atmosphere_scanner",
                    ItemAtmosphereScanner::new,
                    () -> new Item.Properties().stacksTo(1));
    public static final Reg.Handle<ItemFluidContainerInfinite> OXY_PINWHEEL =
            HbmSpace.REG.item(
                    "oxy_pinwheel",
                    props -> new ItemFluidContainerInfinite(props, () -> NTMFluids.OXYGEN, 1),
                    Item.Properties::new);

    private static BlockBehaviour.Properties flesh() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .strength(0.1F, 0.3F)
                .sound(SoundType.GRAVEL)
                .noOcclusion()
                .replaceable()
                .randomTicks()
                .pushReaction(PushReaction.DESTROY);
    }

    public static final Reg.Handle<MeltedFlesh> FLESH_BLOCK =
            HbmSpace.REG.blockItem(
                    "flesh_block",
                    props ->
                            new MeltedFlesh(
                                    props,
                                    () -> AtmosphereBlocksSpace.CHARRED_FLESH_BLOCK.get(),
                                    ParticleTypes.SMOKE,
                                    ParticleTypes.CLOUD),
                    AtmosphereBlocksSpace::flesh);
    public static final Reg.Handle<MeltedFlesh> CHARRED_FLESH_BLOCK =
            HbmSpace.REG.blockItem(
                    "charred_flesh_block",
                    props ->
                            new MeltedFlesh(
                                    props,
                                    () -> AtmosphereBlocksSpace.CARBONIZED_FLESH_BLOCK.get(),
                                    ParticleTypes.LARGE_SMOKE),
                    AtmosphereBlocksSpace::flesh);
    public static final Reg.Handle<MeltedFlesh> CARBONIZED_FLESH_BLOCK =
            HbmSpace.REG.blockItem(
                    "carbonized_flesh_block",
                    props -> new MeltedFlesh(props, null),
                    AtmosphereBlocksSpace::flesh);

    static {
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Library.id("machine_deuterium_tower"),
                () -> new ItemStack(MACHINE_ATMO_TOWER.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Library.id("machine_deuterium_tower"),
                () -> new ItemStack(MACHINE_ATMO_VENT.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                Library.id("machine_intake"),
                () -> new ItemStack(MACHINE_ATMO_EMITTER.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES, Library.id("gas_explosive"), () -> new ItemStack(AIR_VENT.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES, Library.id("gas_explosive"), () -> new ItemStack(AIR_SCRUBBER.get()));
        CreativeContents.insertBefore(
                Tab.MACHINES,
                Library.id("drone_patrol"),
                () -> new ItemStack(ATMOSPHERE_EDITOR.get()));
        CreativeContents.insertAfter(
                Tab.PARTS,
                Library.id("chlorine_pinwheel"),
                () -> new ItemStack(OXY_PINWHEEL.get()));
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                Library.id("plastic_bag"),
                () -> new ItemStack(ATMOSPHERE_SCANNER.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS, Library.id("trapdoor_steel"), () -> new ItemStack(FLESH_BLOCK.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Library.id("trapdoor_steel"),
                () -> new ItemStack(CHARRED_FLESH_BLOCK.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                Library.id("trapdoor_steel"),
                () -> new ItemStack(CARBONIZED_FLESH_BLOCK.get()));
    }

    private AtmosphereBlocksSpace() {}
}
