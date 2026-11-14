/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.blocks.machine;

import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.platform.Platform;
import com.hbm.util.Blink;
import com.hbm.util.BlockReadBounds;
import com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity;
import com.hbmspace.dim.CelestialBody;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class RocketAssemblyBlock extends MultiblockCoreBlock
        implements TickingEntityBlock, LookOverlayProvider {

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    private static final int RADIUS = 4;
    private static final int SPAN = 2 * RADIUS + 1;

    public RocketAssemblyBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PART, Part.CORE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    @Override
    public boolean hasVariableFootprint() {
        return true;
    }

    @Override
    public boolean usesSharedCells() {
        return false;
    }

    @Override
    public boolean isCellState(BlockState state) {
        return state.getValue(PART) != Part.CORE;
    }

    @Override
    protected void visitPlacedCells(
            LevelAccessor level, BlockPos core, Direction facing, FootprintVisitor visitor) {
        footprint(facing).visit(core, visitor);
        if (level.getBlockEntity(core) instanceof RocketAssemblyBlockEntity vab) {
            visitRaised(core, vab.spireHeight(), vab.platforms(), visitor);
        }
    }

    @Override
    protected void visitTeardownCells(
            LevelAccessor level, BlockPos core, Direction facing, FootprintVisitor visitor) {
        footprint(facing).visit(core, visitor);
        int top = level.getMaxY() - core.getY();
        int[] every = new int[top];
        for (int i = 0; i < top; i++) every[i] = i + 1;
        visitRaised(core, top, every, visitor);
    }

    public static void visitRaised(
            BlockPos core, int spire, int[] platforms, FootprintVisitor visitor) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 1; y <= spire; y++) {
            visitor.cell(pos.setWithOffset(core, 0, y, 0));
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    visitor.cell(pos.setWithOffset(core, sx * RADIUS, y, sz * RADIUS));
                }
            }
        }
        for (int y : platforms) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (!isColumn(x, z)) visitor.cell(pos.setWithOffset(core, x, y, z));
                }
            }
        }
    }

    private static boolean isColumn(int x, int z) {
        return x == 0 && z == 0 || Math.abs(x) == RADIUS && Math.abs(z) == RADIUS;
    }

    private static boolean inRaised(int x, int y, int z, int spire, int[] platforms) {
        if (y < 1 || Math.abs(x) > RADIUS || Math.abs(z) > RADIUS) return false;
        if (isColumn(x, z)) return y <= spire;
        for (int platform : platforms) {
            if (platform == y) return true;
        }
        return false;
    }

    @Override
    protected BlockState cellStateFor(int lx, int ly, int lz, Direction facing, int shapeId) {
        return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.CELL);
    }

    @Override
    public @Nullable BlockPos findClientCore(BlockGetter level, BlockPos cell) {
        BlockPos.MutableBlockPos pos = cell.mutable();
        BlockPos core = descend(level, pos);
        if (core != null) return MultiblockSurface.coreClaims(level, core, cell) ? core : null;
        int foot = pos.getY();
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                pos.set(cell.getX() + sx * RADIUS, foot + 2, cell.getZ() + sz * RADIUS);
                if (MultiblockSurface.coreClaims(level, pos, cell)) return pos.immutable();
            }
        }
        int x = runStart(level, cell, Direction.WEST), z = runStart(level, cell, Direction.NORTH);
        pos.set(
                x + RADIUS + Math.floorDiv(cell.getX() - x, SPAN) * SPAN,
                cell.getY(),
                z + RADIUS + Math.floorDiv(cell.getZ() - z, SPAN) * SPAN);
        core = descend(level, pos);
        return core != null && MultiblockSurface.coreClaims(level, core, cell) ? core : null;
    }

    private @Nullable BlockPos descend(BlockGetter level, BlockPos.MutableBlockPos pos) {
        while (true) {
            BlockState state = read(level, pos);
            if (state == null || !state.is(this)) {
                pos.move(Direction.UP);
                return null;
            }
            if (!isCellState(state)) return pos.immutable();
            pos.move(Direction.DOWN);
        }
    }

    private int runStart(BlockGetter level, BlockPos cell, Direction toward) {
        BlockPos.MutableBlockPos pos = cell.mutable();
        do {
            pos.move(toward);
        } while (read(level, pos) instanceof BlockState state && state.is(this));
        pos.move(toward.getOpposite());
        return toward.getAxis().choose(pos.getX(), pos.getY(), pos.getZ());
    }

    private static @Nullable BlockState read(BlockGetter level, BlockPos pos) {
        return BlockReadBounds.canRead(level, pos) && canReadWithoutLoading(level, pos)
                ? level.getBlockState(pos)
                : null;
    }

    @Override
    public boolean claimsCell(BlockGetter level, BlockPos core, BlockPos cell, Direction facing) {
        if (super.claimsCell(level, core, cell, facing)) return true;
        return level.getBlockEntity(core) instanceof RocketAssemblyBlockEntity vab
                && inRaised(
                        cell.getX() - core.getX(),
                        cell.getY() - core.getY(),
                        cell.getZ() - core.getZ(),
                        vab.spireHeight(),
                        vab.platforms());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCellState(state) ? null : new RocketAssemblyBlockEntity(pos, state);
    }

    private static @Nullable BlockPos core(Level level, BlockPos pos) {
        return MultiblockSurface.coreOfAny(level, pos);
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        BlockPos resolved = core(level, core);
        if (resolved == null) return InteractionResult.PASS;
        return super.useAtCore(level.getBlockState(resolved), level, resolved, player, hit);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack held,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected void removedAt(
            BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (isCellState(state)) foldedCellRemoved(level, pos, movedByPiston);
        else super.removedAt(state, level, pos, movedByPiston);
    }

    @Override
    public BlockState playerWillDestroy(
            Level level, BlockPos pos, BlockState state, Player player) {
        if (isCellState(state)
                && level instanceof ServerLevel server
                && !player.preventsBlockDrops()
                && Platform.canHarvestBlock(level, pos, state, player)) {
            BlockPos core = core(level, pos);
            if (core != null) {
                Block.dropResources(
                        level.getBlockState(core),
                        server,
                        pos,
                        level.getBlockEntity(core),
                        player,
                        player.getMainHandItem());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return isCellState(state) ? List.of() : super.getDrops(state, params);
    }

    @Override
    protected void onExplosionHit(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            Explosion explosion,
            BiConsumer<ItemStack, BlockPos> onHit) {
        BlockPos core = isCellState(state) ? core(level, pos) : pos;
        if (core == null) return;
        super.onExplosionHit(level.getBlockState(core), level, core, explosion, onHit);
    }

    @Override
    public @Nullable BlockPos rigidStructureCore(Level level, BlockPos member) {
        return core(level, member);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        if (!CelestialBody.inOrbit(level)) return;
        info.title(Component.translatable(getDescriptionId()));
        info.row(
                Component.literal("! ! ! ")
                        .append(Component.translatable("atmosphere.yesOrbit"))
                        .append(" ! ! !"),
                Blink.lit() ? 0xFF0000 : 0xFFFF00);
    }

    public enum Part implements StringRepresentable {
        CORE("core"),
        CELL("cell");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
