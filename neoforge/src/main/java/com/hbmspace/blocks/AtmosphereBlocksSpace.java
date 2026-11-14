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

import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.DescBlockItem;
import com.hbm.items.machine.FluidContainerInfiniteItem;
import com.hbm.registration.Reg;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.generic.MeltedFlesh;
import com.hbmspace.blocks.machine.AirPumpBlock;
import com.hbmspace.blocks.machine.AirScrubberBlock;
import com.hbmspace.blocks.machine.AtmoTower;
import com.hbmspace.blocks.machine.AtmosphereEditorBlock;
import com.hbmspace.blocks.machine.AtmosphericCompressorBlock;
import com.hbmspace.blocks.machine.AtmosphericEmitterBlock;
import com.hbmspace.items.tool.AtmosphereScannerItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class AtmosphereBlocksSpace {
    private static BlockBehaviour.Properties iron(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops();
    }

    public static final Reg.Handle<AirPumpBlock> AIR_VENT =
            HbmSpace.REG.blockItem("air_vent", AirPumpBlock::new, () -> iron(5.0F, 6.0F));

    public static final Reg.Handle<AirScrubberBlock> AIR_SCRUBBER =
            HbmSpace.REG.blockItem(
                    "air_scrubber",
                    AirScrubberBlock::new,
                    () -> iron(5.0F, 6.0F).noOcclusion(),
                    (block, props) -> new DescBlockItem(block, props, 1));

    public static final Reg.Handle<AtmoTower> MACHINE_ATMO_TOWER =
            HbmSpace.REG.blockItem(
                    "machine_atmo_tower", AtmoTower::new, () -> iron(10.0F, 12.0F).noOcclusion());

    public static final Reg.Handle<AtmosphericCompressorBlock> MACHINE_ATMO_VENT =
            HbmSpace.REG.blockItem(
                    "machine_atmo_vent",
                    AtmosphericCompressorBlock::new,
                    () -> iron(10.0F, 12.0F).noOcclusion());

    public static final Reg.Handle<AtmosphericEmitterBlock> MACHINE_ATMO_EMITTER =
            HbmSpace.REG.blockItem(
                    "machine_atmo_emitter",
                    AtmosphericEmitterBlock::new,
                    () -> iron(10.0F, 12.0F).noOcclusion());

    public static final Reg.Handle<AtmosphereEditorBlock> ATMOSPHERE_EDITOR =
            HbmSpace.REG.blockItem(
                    "atmosphere_editor",
                    AtmosphereEditorBlock::new,
                    () -> iron(1.0F, 1.0F),
                    (block, props) -> new DescBlockItem(block, props, 4, DescBlockItem.Desc.PLAIN));

    public static final Reg.Handle<AtmosphereScannerItem> ATMOSPHERE_SCANNER =
            HbmSpace.REG.item(
                    "atmosphere_scanner",
                    AtmosphereScannerItem::new,
                    () -> new Item.Properties().stacksTo(1));
    public static final Reg.Handle<FluidContainerInfiniteItem> OXY_PINWHEEL =
            HbmSpace.REG.item(
                    "oxy_pinwheel",
                    props -> new FluidContainerInfiniteItem(props, () -> NTMFluids.OXYGEN, 1),
                    Item.Properties::new);

    private static BlockBehaviour.Properties flesh() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .strength(0.1F, 0.3F)
                .sound(SoundType.GRAVEL)
                .noOcclusion()
                .replaceable()
                .randomTicks()
                .pushReaction(((PushReaction) (PushReaction.POPPED)));
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
                RegistryUtil.id("machine_deuterium_tower"),
                () -> new ItemStack(MACHINE_ATMO_TOWER.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                RegistryUtil.id("machine_deuterium_tower"),
                () -> new ItemStack(MACHINE_ATMO_VENT.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                RegistryUtil.id("machine_intake"),
                () -> new ItemStack(MACHINE_ATMO_EMITTER.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                RegistryUtil.id("gas_explosive"),
                () -> new ItemStack(AIR_VENT.get()));
        CreativeContents.insertAfter(
                Tab.MACHINES,
                RegistryUtil.id("gas_explosive"),
                () -> new ItemStack(AIR_SCRUBBER.get()));
        CreativeContents.insertBefore(
                Tab.MACHINES,
                RegistryUtil.id("drone_patrol"),
                () -> new ItemStack(ATMOSPHERE_EDITOR.get()));
        CreativeContents.insertAfter(
                Tab.PARTS,
                RegistryUtil.id("chlorine_pinwheel"),
                () -> new ItemStack(OXY_PINWHEEL.get()));
        CreativeContents.insertAfter(
                Tab.CONSUMABLES,
                RegistryUtil.id("plastic_bag"),
                () -> new ItemStack(ATMOSPHERE_SCANNER.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                RegistryUtil.id("trapdoor_steel"),
                () -> new ItemStack(FLESH_BLOCK.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                RegistryUtil.id("trapdoor_steel"),
                () -> new ItemStack(CHARRED_FLESH_BLOCK.get()));
        CreativeContents.insertAfter(
                Tab.BLOCKS,
                RegistryUtil.id("trapdoor_steel"),
                () -> new ItemStack(CARBONIZED_FLESH_BLOCK.get()));
    }

    private AtmosphereBlocksSpace() {}
}
