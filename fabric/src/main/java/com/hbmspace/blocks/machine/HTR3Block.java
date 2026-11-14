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
import com.hbm.inventory.fluid.trait.HeatableTrait;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.FluidIdentifierData;
import com.hbm.items.machine.FluidIdentifierItem;
import com.hbm.util.Blink;
import com.hbmspace.blockentity.machine.HTR3BlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.inventory.fluid.trait.FT_Rocket;
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

public class HTR3Block extends MultiblockCoreBlock
        implements TickingEntityBlock, CapabilityBlock, LookOverlayProvider {
    public HTR3Block(Properties props) {
        super(props);
    }

    @Override
    public Direction getDirModified(Direction dir) {
        return dir.getCounterClockWise();
    }

    public static BlockPos feedCell(BlockPos core, Direction facing) {
        return core.relative(facing.getClockWise().getOpposite(), 5);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HTR3BlockEntity(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.HTR3)
                .fluidIn()
                .fluidFaces(
                        HTR3BlockEntity.class, (be, face) -> CelestialBody.inOrbit(be.getLevel()));
    }

    @Override
    protected InteractionResult useItemOnAtCore(
            ItemStack held,
            BlockState coreState,
            Level level,
            BlockPos core,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(held.getItem() instanceof FluidIdentifierItem)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(core) instanceof HTR3BlockEntity htr3))
            return InteractionResult.PASS;

        Fluid type =
                held.getOrDefault(
                                ModDataComponents.FLUID_IDENTIFIER.get(), FluidIdentifierData.EMPTY)
                        .primary();
        HeatableTrait heatable = NTMFluidProperties.getTrait(type, HeatableTrait.class);

        if (heatable != null
                && NTMFluidProperties.hasTrait(heatable.first().product(), FT_Rocket.class)) {
            htr3.tanks[0].setTankType(heatable.first().product());
            htr3.setChanged();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        return level.isClientSide() || player.isShiftKeyDown()
                ? InteractionResult.SUCCESS
                : InteractionResult.PASS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof HTR3BlockEntity thruster)) return;

        info.title(getName());

        if (!thruster.isFacingPrograde()) {
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.engineFacing"))
                            .append(" ! ! !"),
                    Blink.lit() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        for (FluidTankNTM tank : thruster.tanks) {
            info.row(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(
                                    NTMFluidProperties.getDisplayName(tank.getTankType())
                                            .copy()
                                            .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                            .append(
                                    Component.translatable(
                                                    "unit.millibuckets",
                                                    tank.getFill() + "/" + tank.getMaxFill())
                                            .withStyle(ChatFormatting.WHITE)));
        }

        if (feedCell(pos, coreFacing(level.getBlockState(pos))).equals(info.hit())) {
            info.row(Component.translatable("overlay.hbmspace.htr3.feed"));
        }
    }
}
