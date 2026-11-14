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

package com.hbmspace.blocks.bomb;

import com.hbm.blocks.ClimbBox;
import com.hbm.blocks.TickingEntityBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.blocks.multiblock.MultiblockMaskTable;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.capability.CapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.LookOverlay;
import com.hbm.interfaces.LookOverlayProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.platform.Platform;
import com.hbm.util.Blink;
import com.hbm.util.BlockReadBounds;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class LaunchPadRocket extends MultiblockCoreBlock
        implements TickingEntityBlock, LookOverlayProvider, CapabilityBlock, ClimbBox {

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final int[][] TOWER = {
        {2, -4}, {2, -5}, {2, -6}, {3, -4}, {4, -4}, {3, -6}, {4, -6}
    };
    private static final int[][] WALKWAY = walkway();
    private static final int TOWER_BASE = 3;
    private static final int LADDER_X = 2;
    private static final int SIDE_LADDER_X = 6;
    private static final int LADDER_Z = -5;
    private static final AABB[] LADDER = new AABB[4];

    static {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            LADDER[facing.get2DDataValue()] =
                    Facing.rotateAabb(new AABB(-0.75, 0, -0.25, -0.25, 1, 0.25), facing)
                            .move(0.5, 0, 0.5);
        }
    }

    public LaunchPadRocket(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PART, Part.CORE));
    }

    private static int[][] walkway() {
        int[][] cells = new int[19][];
        cells[0] = new int[] {2, -3};
        int i = 1;
        for (int k = 1; k <= 6; k++) {
            for (int x = -1; x <= 1; x++) cells[i++] = new int[] {x, -k};
        }
        return cells;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    private static BlockPos at(BlockPos core, Direction facing, int lx, int ly, int lz) {
        return core.offset(
                MultiblockMaskTable.worldX(lx, ly, lz, facing),
                ly,
                MultiblockMaskTable.worldZ(lx, ly, lz, facing));
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
        if (level.getBlockEntity(core) instanceof LaunchPadRocketBlockEntity pad) {
            visitTower(core, facing, pad.towerHeight(), level.getMaxY(), visitor);
        }
    }

    @Override
    protected void visitTeardownCells(
            LevelAccessor level, BlockPos core, Direction facing, FootprintVisitor visitor) {
        footprint(facing).visit(core, visitor);
        for (int height = 9; height + 2 <= level.getMaxY() - core.getY(); height++) {
            visitWalkway(core, facing, height, level.getMaxY(), visitor);
        }
        visitTower(core, facing, level.getMaxY() - core.getY(), level.getMaxY(), visitor);
    }

    public static void visitTower(
            BlockPos core, Direction facing, int height, int maxY, FootprintVisitor visitor) {
        if (height <= 8) return;
        for (int oy = TOWER_BASE; oy < height + 3 && core.getY() + oy <= maxY; oy++) {
            for (int[] column : TOWER) visitor.cell(at(core, facing, column[0], oy, column[1]));
        }
        visitWalkway(core, facing, height, maxY, visitor);
    }

    private static void visitWalkway(
            BlockPos core, Direction facing, int height, int maxY, FootprintVisitor visitor) {
        int oy = height + 2;
        if (core.getY() + oy > maxY) return;
        for (int[] cell : WALKWAY) visitor.cell(at(core, facing, cell[0], oy, cell[1]));
    }

    private static boolean inTower(int lx, int ly, int lz, int height) {
        if (height <= 8 || ly < TOWER_BASE || ly > height + 2) return false;
        return contains(TOWER, lx, lz) || ly == height + 2 && contains(WALKWAY, lx, lz);
    }

    private static boolean contains(int[][] cells, int lx, int lz) {
        for (int[] cell : cells) {
            if (cell[0] == lx && cell[1] == lz) return true;
        }
        return false;
    }

    @Override
    protected BlockState cellStateFor(int lx, int ly, int lz, Direction facing, int shapeId) {
        boolean ladder = lz == LADDER_Z && lx == (ly >= TOWER_BASE ? LADDER_X : SIDE_LADDER_X);
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, ladder ? Part.LADDER : Part.CELL);
    }

    @Override
    public @Nullable BlockPos findClientCore(BlockGetter level, BlockPos cell) {
        Direction facing = level.getBlockState(cell).getValue(FACING);
        BlockPos.MutableBlockPos ladder = new BlockPos.MutableBlockPos();
        for (int[][] layer : new int[][][] {TOWER, WALKWAY}) {
            for (int[] xz : layer) {
                int lx = LADDER_X - xz[0], lz = LADDER_Z - xz[1];
                ladder.setWithOffset(
                        cell,
                        MultiblockMaskTable.worldX(lx, 0, lz, facing),
                        0,
                        MultiblockMaskTable.worldZ(lx, 0, lz, facing));
                if (!isLadder(level, ladder)) continue;
                int foot = ladder.getY();
                while (isLadder(level, ladder.setY(foot - 1))) foot--;
                BlockPos core =
                        new BlockPos(
                                ladder.getX()
                                        - MultiblockMaskTable.worldX(LADDER_X, 0, LADDER_Z, facing),
                                foot - TOWER_BASE,
                                ladder.getZ()
                                        - MultiblockMaskTable.worldZ(
                                                LADDER_X, 0, LADDER_Z, facing));
                if (MultiblockSurface.coreClaims(level, core, cell)) return core;
            }
        }
        return null;
    }

    private boolean isLadder(BlockGetter level, BlockPos pos) {
        if (!BlockReadBounds.canRead(level, pos) || !canReadWithoutLoading(level, pos))
            return false;
        BlockState state = level.getBlockState(pos);
        return state.is(this) && state.getValue(PART) == Part.LADDER;
    }

    @Override
    public boolean claimsCell(BlockGetter level, BlockPos core, BlockPos cell, Direction facing) {
        if (super.claimsCell(level, core, cell, facing)) return true;
        int dx = cell.getX() - core.getX(),
                dy = cell.getY() - core.getY(),
                dz = cell.getZ() - core.getZ();
        return level.getBlockEntity(core) instanceof LaunchPadRocketBlockEntity pad
                && inTower(
                        MultiblockMaskTable.localX(dx, dy, dz, facing),
                        dy,
                        MultiblockMaskTable.localZ(dx, dy, dz, facing),
                        pad.towerHeight());
    }

    @Override
    public @Nullable AABB climbBox(BlockState state) {
        return state.getValue(PART) == Part.LADDER
                ? LADDER[state.getValue(FACING).get2DDataValue()]
                : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCellState(state) ? null : new LaunchPadRocketBlockEntity(pos, state);
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
    public MachineCaps caps() {
        return MachineCaps.of(RocketryBlocksSpace.LAUNCH_PAD_ROCKET_BE)
                .powerIn()
                .fluidIn()
                .items()
                .fe();
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookOverlay info) {
        Component title = Component.translatable(getDescriptionId());
        if (CelestialBody.inOrbit(level)) {
            info.title(title);
            info.row(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.yesOrbit"))
                            .append(" ! ! !"),
                    Blink.lit() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        BlockPos core = core(level, pos);
        if (core == null || !(level.getBlockEntity(core) instanceof LaunchPadRocketBlockEntity pad))
            return;
        if (pad.rocket == null) return;
        if (pos.getY() - core.getY() > 2) return;

        info.row(Component.translatable("overlay.hbmspace.launchPadRocket.fuels"));
        int before = info.rowCount();
        for (FluidTankNTM tank : pad.tanks) {
            if (tank.getTankType() == null) continue;
            info.row(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(
                                    Component.translatable(
                                                    "overlay.hbmspace.launchPadRocket.tank",
                                                    NTMFluidProperties.getDisplayName(
                                                            tank.getTankType()),
                                                    tank.getFill(),
                                                    tank.getMaxFill())
                                            .withStyle(ChatFormatting.WHITE)));
        }
        if (pad.solidMax > 0) {
            info.row(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(
                                    Component.translatable(
                                                    "overlay.hbmspace.launchPadRocket.solid",
                                                    ModItems.ROCKET_FUEL
                                                            .get()
                                                            .getDefaultInstance()
                                                            .getHoverName(),
                                                    pad.solidLevel,
                                                    pad.solidMax)
                                            .withStyle(ChatFormatting.WHITE)));
        }
        if (info.rowCount() == before) return;

        info.title(title);
    }

    public enum Part implements StringRepresentable {
        CORE("core"),
        CELL("cell"),
        LADDER("ladder");

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
