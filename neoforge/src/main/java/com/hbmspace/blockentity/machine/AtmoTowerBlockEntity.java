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

import com.hbm.blockentity.machine.DeuteriumExtractorBlockEntity;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.inventory.fluid.FluidsSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AtmoTowerBlockEntity extends DeuteriumExtractorBlockEntity {

    public static final long MAX_POWER = 1_000_000L;

    public AtmoTowerBlockEntity(BlockPos pos, BlockState state) {
        super(AtmosphereBlockEntitiesSpace.ATMO_TOWER.get(), pos, state, 50_000, 5_000);
        feed.setTankType(FluidsSpace.earthair());
        product.setTankType(FluidsSpace.nitrogen());
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }
}
