// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blockentity.BlockEntityMachineBase;
import com.hbm.client.model.BarrelModel;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter;
import com.hbmspace.blocks.bomb.NukeAntimatter;
import com.hbmspace.blocks.generic.BlockBrownBarrel;
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

    public static final Reg.BeHandle<BlockEntityNukeAntimatter> NUKE_ANTIMATTER_BE =
            HbmSpace.REG
                    .be(
                            "nuke_antimatter",
                            () ->
                                    new BlockEntityType<>(
                                            BlockEntityNukeAntimatter::new,
                                            Set.of(NUKE_ANTIMATTER.get())))
                    .ticksServer(BlockEntityMachineBase::tickServer);

    public static final Reg.Handle<BlockBrownBarrel> OIL_BARREL =
            HbmSpace.REG
                    .blockItem(
                            "oil_barrel",
                            BlockBrownBarrel::new,
                            () ->
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.METAL)
                                            .strength(0.5F, 1.5F)
                                            .noOcclusion()
                                            .requiresCorrectToolForDrops())
                    .bakedBy(() -> new BarrelModel());

    static {
        CreativeContents.insertBefore(
                Tab.NUKES, Library.id("nuke_fstbmb"), () -> new ItemStack(NUKE_ANTIMATTER.get()));
        CreativeContents.insertAfter(
                Tab.NUKES, Library.id("vitrified_barrel"), () -> new ItemStack(OIL_BARREL.get()));
    }

    private BombBlocksSpace() {}
}
