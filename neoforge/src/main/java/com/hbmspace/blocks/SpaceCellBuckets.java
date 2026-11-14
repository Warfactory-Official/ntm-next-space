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
import com.hbm.blocks.multiblock.CellBuckets;
import com.hbm.blocks.multiblock.GeometryCellPartition;
import com.hbm.blocks.multiblock.MultiblockCellBlock;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import java.util.List;
import net.minecraft.world.level.material.MapColor;

public final class SpaceCellBuckets {
    static {
        ModBlocks.addonPlainCell(
                HbmSpace.REG,
                "multiblock_cell_unbreakable_m6",
                CellBuckets.Plain.of(-1F, Float.POSITIVE_INFINITY, MapColor.METAL));
        for (CellBuckets.Plain key :
                List.of(
                        CellBuckets.Plain.of(1F, 1F, MapColor.METAL),
                        CellBuckets.Plain.of(1F, 1F, MapColor.COLOR_LIGHT_GRAY),
                        CellBuckets.Plain.of(1F, 1F, MapColor.TERRACOTTA_CYAN),
                        CellBuckets.Plain.of(5F, 60F, MapColor.TERRACOTTA_PURPLE),
                        CellBuckets.Plain.of(10F, 12F, MapColor.WOOL),
                        CellBuckets.Plain.of(10F, 12F, MapColor.METAL),
                        CellBuckets.Plain.of(10F, 12F, MapColor.COLOR_LIGHT_GRAY),
                        CellBuckets.Plain.of(10F, 12F, MapColor.PODZOL),
                        CellBuckets.Plain.of(10F, 12F, MapColor.TERRACOTTA_MAGENTA),
                        CellBuckets.Plain.of(10F, 12F, MapColor.TERRACOTTA_CYAN),
                        CellBuckets.Plain.of(10F, 12F, MapColor.TERRACOTTA_BLUE))) {
            ModBlocks.addonPlainCell(HbmSpace.REG, key.name(), key);
        }
        ModBlocks.sharedPlainCell(HbmSpace.REG, 0, CellBuckets.Plain.of(5F, 60F, MapColor.DIRT));
        ModBlocks.addonGeometryCell(
                HbmSpace.REG,
                CellBuckets.addAddonGeometry(
                        GeometryCellPartition.STATIC,
                        CellBuckets.Plain.of(5F, 6F, MapColor.TERRACOTTA_BLUE),
                        2));
    }

    public static final RegistryHandle<? extends MultiblockCellBlock> HYDROBAY_LIT_CELLS =
            hydrobayLitCells();

    private static RegistryHandle<? extends MultiblockCellBlock> hydrobayLitCells() {
        CellBuckets.Plain key =
                CellBuckets.Plain.of(
                        10F, 12F, HbmSpace.REG.constantMapColor("hydrobay", MapColor.METAL), 15);
        return ModBlocks.addonPlainCell(HbmSpace.REG, "multiblock_cell_hydrobay_lit", key);
    }

    private SpaceCellBuckets() {}
}
