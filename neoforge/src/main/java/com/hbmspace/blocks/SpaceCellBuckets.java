// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.multiblock.BlockMultiblockCell;
import com.hbm.blocks.multiblock.CellBuckets;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import net.minecraft.world.level.material.MapColor;

public final class SpaceCellBuckets {
    public static final RegistryHandle<? extends BlockMultiblockCell> UNBREAKABLE_METAL =
            ModBlocks.addonPlainCell(
                    HbmSpace.REG,
                    "multiblock_cell_unbreakable_m6",
                    CellBuckets.Plain.of(-1F, Float.POSITIVE_INFINITY, MapColor.METAL));
    public static final RegistryHandle<? extends BlockMultiblockCell> H1_R1_METAL =
            ModBlocks.addonPlainCell(
                    HbmSpace.REG,
                    "multiblock_cell_h1_r1_m6",
                    CellBuckets.Plain.of(1F, 1F, MapColor.METAL));
    public static final RegistryHandle<? extends BlockMultiblockCell> H5_R60_METAL =
            ModBlocks.addonPlainCell(
                    HbmSpace.REG,
                    "multiblock_cell_h5_r60_m6",
                    CellBuckets.Plain.of(5F, 60F, MapColor.METAL));
    public static final RegistryHandle<? extends BlockMultiblockCell> H5_R18_METAL =
            ModBlocks.addonPlainCell(
                    HbmSpace.REG,
                    "multiblock_cell_h5_r18_m6",
                    CellBuckets.Plain.of(5F, 18F, MapColor.METAL));
    public static final RegistryHandle<? extends BlockMultiblockCell> H10_R12_METAL_LIT =
            ModBlocks.addonPlainCell(
                    HbmSpace.REG,
                    "multiblock_cell_h10_r12_m6_l15",
                    CellBuckets.Plain.of(10F, 12F, MapColor.METAL, 15));

    private SpaceCellBuckets() {}
}
