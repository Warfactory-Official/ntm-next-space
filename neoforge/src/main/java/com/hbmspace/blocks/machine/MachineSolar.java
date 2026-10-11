// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.CapabilityDependentShape;
import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.lib.Library;
import com.hbm.uninos.graph.EndpointRegistry;
import com.hbmspace.blockentity.machine.BlockEntityMachineSolarPanel;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class MachineSolar extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock, CapabilityDependentShape.Endpoint {

    private static final int[] DIMENSIONS = {0, 0, 2, 2, 0, 0};

    public MachineSolar(Properties props) {
        super(props);
        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) state = state.setValue(arm(dir), false);
        registerDefaultState(state);
    }

    private static BooleanProperty arm(Direction dir) {
        return PipeBlock.PROPERTY_BY_DIRECTION.get(dir);
    }

    public static int arms(BlockState state) {
        int mask = 0;
        for (Direction dir : Direction.values()) {
            if (state.getValue(arm(dir))) mask |= 1 << dir.ordinal();
        }
        return mask;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        for (Direction dir : Direction.values()) builder.add(arm(dir));
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public int coreMask() {
        return MASK_ALL;
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineSolarPanel(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.SOLAR_PANEL).powerOut();
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server)) return;
        FoldedOwner owner = ownerOf(level, pos);
        if (owner != null)
            Library.redrawArms(server.getBlockState(owner.pos()), server, owner.pos());
    }

    @Override
    public void reindexLoadedCells(ServerLevel level, BlockPos core) {
        super.reindexLoadedCells(level, core);
        BlockEntity be = level.getBlockEntity(core);
        assert be instanceof BlockEntityMachineSolarPanel;
        EndpointRegistry.declareSelfIfEndpoint(level, be);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction dir,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        BlockState updated =
                super.updateShape(
                        state, level, ticks, pos, dir, neighbourPos, neighbourState, random);
        boolean joins = Library.canConnect(level, neighbourPos, dir.getOpposite());
        if (level.isClientSide())
            return Library.predictArm(updated, arm(dir), neighbourState, joins);
        return updated.setValue(arm(dir), joins);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            @Nullable Orientation orientation,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level instanceof ServerLevel server) {
            Library.redrawArms(state, server, pos);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState turned = super.rotate(state, rotation);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            turned = turned.setValue(arm(rotation.rotate(dir)), state.getValue(arm(dir)));
        }
        return turned;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = state.getValue(FACING);
        BlockState mirrored = state.setValue(FACING, mirror.getRotation(facing).rotate(facing));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            mirrored = mirrored.setValue(arm(mirror.mirror(dir)), state.getValue(arm(dir)));
        }
        return mirrored;
    }
}
