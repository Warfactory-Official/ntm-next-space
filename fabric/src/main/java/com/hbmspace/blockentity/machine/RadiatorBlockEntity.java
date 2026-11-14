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

import com.hbm.blockentity.machine.thermal.SteamCondenserBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.data.SpaceData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RadiatorBlockEntity extends SteamCondenserBlockEntity {

    public RadiatorBlockEntity(BlockPos pos, BlockState state) {
        super(
                StationBlockEntitiesSpace.RADIATOR.get(),
                pos,
                state,
                SpaceData.RADIATOR_INPUT_TANK_SIZE.get(),
                SpaceData.RADIATOR_OUTPUT_TANK_SIZE.get());
    }

    @Override
    protected boolean vacuumProof() {
        return true;
    }
}
