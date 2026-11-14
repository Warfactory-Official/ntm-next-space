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

package com.hbmspace.client;

import com.hbm.blocks.ModBlockEntities;
import com.hbm.blocks.machine.CargoElevatorBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.render.MachinePlacementOutline;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.blocks.bomb.LaunchPadRocket;
import com.hbmspace.blocks.machine.RocketAssemblyBlock;
import com.hbmspace.handler.SpaceMultiblockPlacement;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SpaceMultiblockItemPreview {

    private SpaceMultiblockItemPreview() {}

    public static MachinePlacementOutline.State extract(
            Level level,
            BlockItem item,
            MultiblockCoreBlock block,
            BlockPlaceContext context,
            Player player) {
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        Direction direction = SpaceMultiblockPlacement.direction(block, face, player.getYRot());
        BlockPos core = SpaceMultiblockPlacement.core(block, clicked, face, player.getYRot());
        BlockState state = block.getStateForPlacement(context);
        boolean placeable =
                context.canPlace()
                        && state != null
                        && item.canPlace(context, state)
                        && block.placementClear(level, core, direction);
        VoxelShape shape = block.placementOutline(direction);
        var data = player.getMainHandItem().get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null || data.type().onlyOpCanSetNbt() && !player.canUseGameMasterBlocks()) {
            return new MachinePlacementOutline.State(
                    core, shape, block.placementExtraOutlines(direction), placeable);
        }
        BlockEntityType<?> type = data.type();
        CompoundTag tag = data.getUnsafe();
        LongOpenHashSet added = new LongOpenHashSet();
        MultiblockCoreBlock.FootprintVisitor cells = pos -> added.add(pos.asLong());
        if (block instanceof CargoElevatorBlock && type == ModBlockEntities.CARGO_ELEVATOR.get()) {
            int height = tag.getIntOr("height", 0);
            shape = Shapes.box(-1D, 0D, -1D, 2D, (double) height + 1D, 2D);
            if (height < 0 || (long) core.getY() + height > level.getMaxY()) {
                return new MachinePlacementOutline.State(
                        core, shape, block.placementExtraOutlines(direction), false);
            }
            for (int y = 1; y <= height; y++) {
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++) {
                        added.add(
                                BlockPos.asLong(core.getX() + x, core.getY() + y, core.getZ() + z));
                    }
            }
        } else if (block instanceof RocketAssemblyBlock
                && type == RocketryBlocksSpace.ROCKET_ASSEMBLY.get()) {
            int spire = tag.getIntOr("spire", 0);
            int[] platforms = tag.getIntArray("platforms").orElse(new int[0]);
            if ((long) core.getY() + spire > level.getMaxY()) {
                return new MachinePlacementOutline.State(
                        core, shape, block.placementExtraOutlines(direction), false);
            }
            for (int y : platforms) {
                if ((long) core.getY() + y < level.getMinY()
                        || (long) core.getY() + y > level.getMaxY()) {
                    return new MachinePlacementOutline.State(
                            core, shape, block.placementExtraOutlines(direction), false);
                }
            }
            RocketAssemblyBlock.visitRaised(core, spire, platforms, cells);
        } else if (block instanceof LaunchPadRocket
                && type == RocketryBlocksSpace.LAUNCH_PAD_ROCKET_BE.get()) {
            LaunchPadRocket.visitTower(
                    core, direction, tag.getIntOr("tower", 0), level.getMaxY(), cells);
        }
        for (long packed : added) {
            BlockPos world = BlockPos.of(packed);
            if (level.isOutsideBuildHeight(world) || !level.getBlockState(world).canBeReplaced())
                placeable = false;
            shape =
                    Shapes.or(
                            shape,
                            Shapes.block()
                                    .move(
                                            world.getX() - core.getX(),
                                            world.getY() - core.getY(),
                                            world.getZ() - core.getZ()));
        }
        return new MachinePlacementOutline.State(
                core, shape, block.placementExtraOutlines(direction), placeable);
    }
}
