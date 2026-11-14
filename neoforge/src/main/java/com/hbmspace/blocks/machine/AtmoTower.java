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

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.DeuteriumTowerBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.AtmoTowerBlockEntity;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class AtmoTower extends DeuteriumTowerBlock {
    public AtmoTower(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AtmoTowerBlockEntity(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof AtmoTowerBlockEntity tower)) return;

        info.title(getName());
        info.row(
                Component.translatable(
                                "overlay.hbm.power", NumberFormatter.formatCompact(tower.energy))
                        .withStyle(
                                tower.energy < tower.getMaxPower() / 20
                                        ? ChatFormatting.RED
                                        : ChatFormatting.GREEN));
        for (int i = 0; i < tower.tankCount(); i++) {
            FluidTankNTM tank = tower.tank(i);
            info.row(
                    LookOverlay.flow(
                            i < 1,
                            Component.translatable(
                                    "overlay.hbm.tank",
                                    NTMFluidProperties.clientName(tank.getFluid()),
                                    tank.getFill(),
                                    tank.getMaxFill())));
        }
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.ATMO_TOWER)
                .powerIn()
                .fe()
                .fluidIn()
                .fluidOut();
    }
}
