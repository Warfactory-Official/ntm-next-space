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

package com.hbmspace.blockentity.machine;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class ThermalMachineDamage {
    public static final BooleanProperty DAMAGED = BooleanProperty.create("damaged");
    public static final String[] PERSISTENT_KEYS = {"damaged"};

    private ThermalMachineDamage() {}

    public static boolean damaged(BlockEntity be) {
        return be.getBlockState().getValue(DAMAGED);
    }

    public static void set(BlockEntity be, boolean damaged) {
        if (damaged(be) == damaged) return;
        var state = be.getBlockState().setValue(DAMAGED, damaged);
        var level = be.getLevel();
        if (level == null || level.getBlockEntity(be.getBlockPos()) != be) be.setBlockState(state);
        else
            level.setBlock(
                    be.getBlockPos(),
                    state,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SKIP_ON_PLACE);
        be.setChanged();
    }

    public static void write(BlockEntity be, DataComponentMap.Builder components) {
        if (damaged(be))
            components.set(
                    DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(DAMAGED, true));
    }

    public static void read(BlockEntity be, DataComponentGetter components) {
        BlockItemStateProperties properties = components.get(DataComponents.BLOCK_STATE);
        if (properties == null) return;
        Boolean damaged = properties.get(DAMAGED);
        if (damaged != null) set(be, damaged);
    }
}
