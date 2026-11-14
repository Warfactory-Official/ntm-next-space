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

import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.NumberFormatter;
import com.hbmspace.blockentity.machine.AlkylationBlockEntity;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class AlkylationBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, LookOverlayProvider, CapabilityBlock {
    public AlkylationBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlkylationBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOnAtCore(
            ItemStack stack,
            BlockState coreState,
            Level level,
            BlockPos core,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        Fluid fluid = AtmosphereText.identified(stack);
        if (fluid == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()
                && level.getBlockEntity(core) instanceof AlkylationBlockEntity alkylation) {
            alkylation.tanks[0].setTankTypeByIdentifier(fluid);
            alkylation.setChanged();
            AtmosphereText.changedType(player, fluid);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!(level.getBlockEntity(pos) instanceof AlkylationBlockEntity alkylation)) return;

        info.title(getName());
        info.row(
                Component.translatable(
                                "overlay.hbm.power",
                                NumberFormatter.formatCompact(alkylation.power))
                        .withStyle(
                                alkylation.power < AlkylationBlockEntity.MAX_POWER / 20
                                        ? ChatFormatting.RED
                                        : ChatFormatting.GREEN));
        for (int i = 0; i < alkylation.tanks.length; i++) {
            FluidTankNTM tank = alkylation.tanks[i];
            if (tank.getTankType() == null) continue;
            info.row(
                    LookOverlay.flow(
                            i < 2,
                            Component.translatable(
                                    "overlay.hbm.tank",
                                    NTMFluidProperties.clientName(tank.getFluid()),
                                    tank.getFill(),
                                    tank.getMaxFill())));
        }
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.ALKYLATION)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .fe()
                .faces(AlkylationBlockEntity.class, (be, side) -> side != Direction.DOWN)
                .fluidFaces(
                        AlkylationBlockEntity.class, (be, face) -> face.side() != Direction.DOWN);
    }
}
