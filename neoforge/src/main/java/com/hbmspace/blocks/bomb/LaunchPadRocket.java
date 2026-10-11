// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.bomb;

import com.hbm.blocks.ClimbBox;
import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.blocks.multiblock.MultiblockMaskTable;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import mov.movblock.tenon.strip.api.DropSafe;
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

public class LaunchPadRocket extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, ICapabilityBlock, ClimbBox {

    public static final EnumProperty<Cell> CELL = EnumProperty.create("cell", Cell.class);

    private static final int[] DIMENSIONS = {2, 0, 7, 6, 6, 6};
    private static final int[] BODY = {2, 0, 6, 6, 4, 4};
    private static final int[] BODY_BACK = {2, 0, 4, 0, 6, 6};
    private static final int[] BODY_FRONT = {2, 0, 0, 4, 6, 6};
    private static final int OFFSET = 6;

    private static final int[][] TOWER = {
        {2, -4}, {2, -5}, {2, -6}, {3, -4}, {4, -4}, {3, -6}, {4, -6}
    };
    private static final int LADDER_X = 2;
    private static final int SIDE_LADDER_X = 6;
    private static final int LADDER_Z = -5;
    private static final AABB LADDER = new AABB(0.75, 0, 0.25, 1.25, 1, 0.75);

    public LaunchPadRocket(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CELL, Cell.CORE));
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
        return OFFSET;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        MultiblockHandlerXR.visitBox(core, BODY, facing, visitor);
        MultiblockHandlerXR.visitBox(core.relative(facing, -2), BODY_BACK, facing, visitor);
        MultiblockHandlerXR.visitBox(core.relative(facing, 2), BODY_FRONT, facing, visitor);
        int face = 1 << facing.getOpposite().get3DDataValue();
        visitInputs(core, facing, pos -> visitor.cell(pos, face));
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        visitInputs(
                core,
                facing,
                pos ->
                        visitor.passiveCell(
                                pos,
                                MASK_ALL,
                                PASSIVE_POWER_IN | PASSIVE_FLUID_IN | PASSIVE_ITEMS));
    }

    private static void visitInputs(BlockPos core, Direction facing, Consumer<BlockPos> input) {
        for (int or = 1; or < 5; or++) {
            for (int oy = 0; oy < 3; oy++) input.accept(at(core, facing, or, oy, -7));
        }
        input.accept(at(core, facing, -3, 0, -7));
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
        return state.getValue(CELL) != Cell.CORE;
    }

    @Override
    protected void visitPlacedCells(
            LevelAccessor level, BlockPos core, Direction facing, CellVisitor visitor) {
        visitCells(core, facing, visitor);
        if (level.getBlockEntity(core) instanceof BlockEntityLaunchPadRocket pad) {
            visitTower(core, facing, pad.towerHeight(), level.getMaxY(), visitor);
        }
    }

    @Override
    protected void visitTeardownCells(
            LevelAccessor level, BlockPos core, Direction facing, CellVisitor visitor) {
        visitCells(core, facing, visitor);
        for (int height = 9; height + 2 <= level.getMaxY() - core.getY(); height++) {
            visitWalkway(core, facing, height, level.getMaxY(), visitor);
        }
        visitTower(core, facing, level.getMaxY() - core.getY(), level.getMaxY(), visitor);
    }

    public static void visitTower(
            BlockPos core, Direction facing, int height, int maxY, CellVisitor visitor) {
        if (height <= 8) return;
        for (int oy = 3; oy < height + 3 && core.getY() + oy <= maxY; oy++) {
            for (int[] column : TOWER)
                visitor.cell(at(core, facing, column[0], oy, column[1]), MASK_NONE);
        }
        visitWalkway(core, facing, height, maxY, visitor);
    }

    private static void visitWalkway(
            BlockPos core, Direction facing, int height, int maxY, CellVisitor visitor) {
        int oy = height + 2;
        if (core.getY() + oy > maxY) return;
        visitor.cell(at(core, facing, 2, oy, -3), MASK_NONE);
        for (int k = 1; k <= 6; k++) {
            for (int x = -1; x <= 1; x++) visitor.cell(at(core, facing, x, oy, -k), MASK_NONE);
        }
    }

    @Override
    protected BlockState cellStateFor(int lx, int ly, int lz, Direction facing, int shapeId) {
        return defaultBlockState().setValue(FACING, facing).setValue(CELL, chain(lx, ly, lz));
    }

    private static Cell chain(int x, int y, int z) {
        if (y >= 3) {
            if (isTower(x, z)) return x == LADDER_X && z == LADDER_Z ? Cell.LADDER : Cell.DOWN;
            if (z != -4) return z < -4 ? Cell.FORWARD : Cell.BACKWARD;
            return Cell.COUNTER;
        }
        if (x == SIDE_LADDER_X && z == LADDER_Z) return y > 0 ? Cell.LADDER : Cell.LADDER_CLOCKWISE;
        if (y > 0) return Cell.DOWN;
        if (z == -7) return Cell.FORWARD;
        if (x != 0) return x > 0 ? Cell.CLOCKWISE : Cell.COUNTER;
        return z > 0 ? Cell.BACKWARD : Cell.FORWARD;
    }

    private static boolean isTower(int x, int z) {
        for (int[] column : TOWER) {
            if (column[0] == x && column[1] == z) return true;
        }
        return false;
    }

    @Override
    public @Nullable BlockPos findClientCore(BlockGetter level, BlockPos cell) {
        BlockPos.MutableBlockPos pos = cell.mutable();
        for (int steps = 0; steps <= level.getHeight() + 4 * OFFSET; steps++) {
            if (!canReadWithoutLoading(level, pos)) return null;
            BlockState state = level.getBlockState(pos);
            if (!state.is(this)) return null;
            Cell chain = state.getValue(CELL);
            if (chain == Cell.CORE) return pos.immutable();
            pos.move(chain.step(state.getValue(FACING)));
        }
        return null;
    }

    @Override
    public boolean claimsCell(BlockGetter level, BlockPos core, BlockPos cell, Direction facing) {
        return core.equals(findClientCore(level, cell));
    }

    @Override
    public @Nullable AABB climbBox(BlockState state) {
        if (!state.getValue(CELL).ladder) return null;
        Direction facing = state.getValue(FACING);
        double x0 = rotatedX(LADDER.minX, LADDER.minZ, facing),
                z0 = rotatedZ(LADDER.minX, LADDER.minZ, facing);
        double x1 = rotatedX(LADDER.maxX, LADDER.maxZ, facing),
                z1 = rotatedZ(LADDER.maxX, LADDER.maxZ, facing);
        return new AABB(
                Math.min(x0, x1),
                LADDER.minY,
                Math.min(z0, z1),
                Math.max(x0, x1),
                LADDER.maxY,
                Math.max(z0, z1));
    }

    private static double rotatedX(double x, double z, Direction facing) {
        double lx = x - 0.5, lz = z - 0.5;
        return 0.5
                + switch (facing) {
                    case WEST -> -lz;
                    case NORTH -> -lx;
                    case EAST -> lz;
                    default -> lx;
                };
    }

    private static double rotatedZ(double x, double z, Direction facing) {
        double lx = x - 0.5, lz = z - 0.5;
        return 0.5
                + switch (facing) {
                    case WEST -> lx;
                    case NORTH -> -lz;
                    case EAST -> -lx;
                    default -> lz;
                };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCellState(state) ? null : new BlockEntityLaunchPadRocket(pos, state);
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
                && state.canHarvestBlock((BlockGetter) level, pos, player)) {
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
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        Component title = Component.translatable(getDescriptionId());
        if (CelestialBody.inOrbit(level)) {
            info.title(title, 0xFFFF00, 0x404000);
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.yesOrbit"))
                            .append(" ! ! !"),
                    BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        BlockPos core = core(level, pos);
        if (core == null || !(level.getBlockEntity(core) instanceof BlockEntityLaunchPadRocket pad))
            return;
        if (pad.rocket == null) return;
        if (pos.getY() - core.getY() > 2) return;

        info.line(Component.translatable("overlay.hbmspace.launchPadRocket.fuels"));
        int before = info.size();
        for (FluidTankNTM tank : pad.tanks) {
            if (tank.getTankType() == null) continue;
            info.line(
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
            info.line(
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
        if (info.size() == before) return;

        info.title(title, 0xFFFF00, 0x404000);
    }

    public enum Cell implements StringRepresentable {
        CORE("core", Turn.NONE, false),
        DOWN("down", Turn.DOWN, false),
        FORWARD("forward", Turn.FORWARD, false),
        BACKWARD("backward", Turn.BACKWARD, false),
        CLOCKWISE("clockwise", Turn.CLOCKWISE, false),
        COUNTER("counter", Turn.COUNTER, false),
        LADDER("ladder", Turn.DOWN, true),
        LADDER_CLOCKWISE("ladder_clockwise", Turn.CLOCKWISE, true);

        private final String name;
        private final Turn turn;
        public final boolean ladder;

        Cell(String name, Turn turn, boolean ladder) {
            this.name = name;
            this.turn = turn;
            this.ladder = ladder;
        }

        public Direction step(Direction facing) {
            return switch (turn) {
                case DOWN -> Direction.DOWN;
                case FORWARD -> facing;
                case BACKWARD -> facing.getOpposite();
                case CLOCKWISE -> facing.getClockWise();
                case COUNTER -> facing.getCounterClockWise();
                case NONE -> throw new IllegalStateException("the core takes no step");
            };
        }

        @DropSafe
        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private enum Turn {
        NONE,
        DOWN,
        FORWARD,
        BACKWARD,
        CLOCKWISE,
        COUNTER
    }
}
