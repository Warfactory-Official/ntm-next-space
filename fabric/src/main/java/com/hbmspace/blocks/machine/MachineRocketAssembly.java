// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockMaskTable;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityMachineRocketAssembly;
import com.hbmspace.dim.CelestialBody;
import java.util.List;
import java.util.function.BiConsumer;
import mov.movblock.tenon.strip.api.DropSafe;
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

public class MachineRocketAssembly extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay {

    public static final EnumProperty<Chain> CELL = EnumProperty.create("cell", Chain.class);
    private static final int[] DIMENSIONS = {0, 2, 4, 4, 4, 4};
    private static final int RADIUS = 4;

    public MachineRocketAssembly(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CELL, Chain.CORE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CELL);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return RADIUS;
    }

    @Override
    public int getHeightOffset() {
        return 2;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x != 0 || z != 0) visitor.cell(pos.setWithOffset(core, x, 0, z), MASK_NONE);
            }
        }
        for (int y = -2; y <= 0; y++) {
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    if (y < 0)
                        visitor.cell(
                                pos.setWithOffset(core, sx * RADIUS, y, sz * RADIUS), MASK_NONE);
                    if (y < 0)
                        visitor.cell(
                                pos.setWithOffset(core, sx * (RADIUS - 1), y, sz * RADIUS),
                                MASK_NONE);
                    if (y < 0)
                        visitor.cell(
                                pos.setWithOffset(core, sx * RADIUS, y, sz * (RADIUS - 1)),
                                MASK_NONE);
                }
            }
        }
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
        return state.getValue(CELL) != Chain.CORE;
    }

    @Override
    protected void visitPlacedCells(
            LevelAccessor level, BlockPos core, Direction facing, CellVisitor visitor) {
        visitCells(core, facing, visitor);
        if (level.getBlockEntity(core) instanceof BlockEntityMachineRocketAssembly vab) {
            visitRaised(core, vab.spireHeight(), vab.platforms(), visitor);
        }
    }

    @Override
    protected void visitTeardownCells(
            LevelAccessor level, BlockPos core, Direction facing, CellVisitor visitor) {
        visitCells(core, facing, visitor);
        int top = level.getMaxY() - core.getY();
        int[] every = new int[top];
        for (int i = 0; i < top; i++) every[i] = i + 1;
        visitRaised(core, top, every, visitor);
    }

    public static void visitRaised(BlockPos core, int spire, int[] platforms, CellVisitor visitor) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 1; y <= spire; y++) {
            visitor.cell(pos.setWithOffset(core, 0, y, 0), MASK_NONE);
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    visitor.cell(pos.setWithOffset(core, sx * RADIUS, y, sz * RADIUS), MASK_NONE);
                }
            }
        }
        for (int y : platforms) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (x == 0 && z == 0) continue;
                    if (Math.abs(x) == RADIUS && Math.abs(z) == RADIUS) continue;
                    visitor.cell(pos.setWithOffset(core, x, y, z), MASK_NONE);
                }
            }
        }
    }

    @Override
    protected BlockState cellStateFor(int lx, int ly, int lz, Direction facing, int shapeId) {
        int x = MultiblockMaskTable.worldX(lx, ly, lz, facing);
        int z = MultiblockMaskTable.worldZ(lx, ly, lz, facing);
        return defaultBlockState().setValue(FACING, facing).setValue(CELL, chain(x, ly, z));
    }

    private static Chain chain(int x, int y, int z) {
        if (y < 0) return Chain.UP;
        boolean column = x == 0 && z == 0 || Math.abs(x) == RADIUS && Math.abs(z) == RADIUS;
        if (y > 0 && column) return Chain.DOWN;
        if (x != 0) return x > 0 ? Chain.WEST : Chain.EAST;
        return z > 0 ? Chain.NORTH : Chain.SOUTH;
    }

    @Override
    public @Nullable BlockPos findClientCore(BlockGetter level, BlockPos cell) {
        BlockPos.MutableBlockPos pos = cell.mutable();
        for (int steps = 0; steps <= level.getHeight() + 2 * RADIUS; steps++) {
            if (!canReadWithoutLoading(level, pos)) return null;
            BlockState state = level.getBlockState(pos);
            if (!state.is(this)) return null;
            Chain chain = state.getValue(CELL);
            if (chain == Chain.CORE) return pos.immutable();
            pos.move(chain.step);
        }
        return null;
    }

    @Override
    public boolean claimsCell(BlockGetter level, BlockPos core, BlockPos cell, Direction facing) {
        return core.equals(findClientCore(level, cell));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCellState(state) ? null : new BlockEntityMachineRocketAssembly(pos, state);
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
                && player.hasCorrectToolForDrops(state)) {
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
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!CelestialBody.inOrbit(level)) return;
        info.title(Component.translatable(getDescriptionId()), 0xFFFF00, 0x404000);
        info.line(
                Component.literal("! ! ! ")
                        .append(Component.translatable("atmosphere.yesOrbit"))
                        .append(" ! ! !"),
                BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
    }

    public enum Chain implements StringRepresentable {
        CORE("core", Direction.UP),
        UP("up", Direction.UP),
        DOWN("down", Direction.DOWN),
        NORTH("north", Direction.NORTH),
        SOUTH("south", Direction.SOUTH),
        WEST("west", Direction.WEST),
        EAST("east", Direction.EAST);

        private final String name;
        public final Direction step;

        Chain(String name, Direction step) {
            this.name = name;
            this.step = step;
        }

        @DropSafe
        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
