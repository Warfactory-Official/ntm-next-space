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

import com.hbm.blocks.generic.FalloutBlock;
import com.hbm.inventory.material.MaterialTagRegistry;
import com.hbm.inventory.material.MaterialTags;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.registration.Reg;
import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.bomb.DetSaltBlock;
import com.hbmspace.blocks.generic.FenceGateBlock;
import com.hbmspace.blocks.generic.SaltedFalloutBlock;
import com.hbmspace.inventory.OreDictManagerSpace;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class MaterialBlocksSpace {
    public static final Reg.Handle<Block> BLOCK_NICKEL =
            storage("block_nickel", OreDictManagerSpace.NI);
    public static final Reg.Handle<Block> BLOCK_OSMIRIDIUM =
            storage("block_osmiridium", MaterialTagRegistry.OSMIRIDIUM);
    public static final Reg.Handle<SaltedFalloutBlock> SALTED_FALLOUT =
            HbmSpace.REG.blockItem(
                    "salted_fallout",
                    SaltedFalloutBlock::new,
                    () -> FalloutBlock.defaultProperties().randomTicks());
    public static final Reg.Handle<DetSaltBlock> DET_SALT =
            HbmSpace.REG.blockItem(
                    "det_salt",
                    DetSaltBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(0.1F, 0.0F)
                                    .sound(SoundType.STONE)
                                    .requiresCorrectToolForDrops());

    public static final Reg.Handle<LadderBlock> LADDER_ALUMINIUM =
            HbmSpace.REG.blockItem(
                    "ladder_aluminium",
                    LadderBlock::new,
                    () ->
                            BlockBehaviour.Properties.of()
                                    .strength(0.25F, 1.2F)
                                    .sound(SoundType.STONE)
                                    .noOcclusion()
                                    .pushReaction(((PushReaction) (PushReaction.DESTROY))));

    public static final RegistryHandle<SoundEvent> FENCE_GATE_SOUND =
            Registrar.registerSound(HbmSpace.MOD_ID + ":block.fencegate");
    public static final BlockSetType FENCE_GATE_SET =
            BlockSetType.register(
                    new BlockSetType(
                            HbmSpace.MOD_ID + "_fence_gate",
                            true,
                            true,
                            false,
                            BlockSetType.PressurePlateSensitivity.EVERYTHING,
                            SoundType.STONE,
                            gateSound(),
                            gateSound(),
                            gateSound(),
                            gateSound(),
                            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
                            SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
                            SoundEvents.STONE_BUTTON_CLICK_OFF,
                            SoundEvents.STONE_BUTTON_CLICK_ON));
    public static final Reg.Handle<FenceGateBlock> FENCE_GATE =
            HbmSpace.REG.blockItem(
                    "fence_gate",
                    props -> new FenceGateBlock(FENCE_GATE_SET, props),
                    () ->
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 0.6F)
                                    .noOcclusion()
                                    .requiresCorrectToolForDrops()
                                    .pushReaction(((PushReaction) (PushReaction.DESTROY))));

    static {
        after(Tab.BLOCKS, "block_niter", BLOCK_NICKEL);
        after(Tab.BLOCKS, "block_steel", BLOCK_OSMIRIDIUM);
        after(Tab.BLOCKS, "fallout", SALTED_FALLOUT);
        after(Tab.NUKES, "det_nuke", DET_SALT);
        after(Tab.BLOCKS, "door_bunker", FENCE_GATE);
    }

    private MaterialBlocksSpace() {}

    private static Reg.Handle<Block> storage(String name, MaterialTags dict) {
        return HbmSpace.REG.blockItem(
                name,
                Block::new,
                () ->
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.METAL)
                                .strength(5.0F, 6.0F)
                                .sound(SoundType.METAL)
                                .requiresCorrectToolForDrops());
    }

    private static SoundEvent gateSound() {
        return SoundEvent.createVariableRangeEvent(
                Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "block.fencegate"));
    }

    private static String gateModel(Direction facing, String half, boolean right, boolean open) {
        return "_"
                + facing.getSerializedName()
                + "_"
                + half
                + "_"
                + (right ? "right" : "left")
                + (open ? "_open" : "");
    }

    private static void after(Tab tab, String anchor, Reg.Handle<? extends Block> block) {
        CreativeContents.insertAfter(
                tab, RegistryUtil.id(anchor), () -> new ItemStack(block.get()));
    }
}
