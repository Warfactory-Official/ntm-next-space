// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCell;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockCellShapes;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.blocks.multiblock.MultiblockMaskTable;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityHydroponic;
import com.hbmspace.blocks.BlockCrop;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.blocks.IndustryBlocksSpace;
import com.hbmspace.blocks.SpaceCellBuckets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class MachineHydroponic extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, ICapabilityBlock {

    private static final int[] DIMENSIONS = {2, 0, 0, 0, 2, 2};
    private static final int[] INTERIOR = {2, -1, 1, 1, 2, 2};
    private static final int LAMP_Y = 2;

    public MachineHydroponic(Properties props) {
        super(props);
        bounding.add(new AABB(-0.5, 0, -1.5, 0.5, 3, 1.5));
        bounding.add(new AABB(-1.125, 1, -2.5, 1.125, 3, -1.5));
        bounding.add(new AABB(-1.125, 1, 1.5, 1.125, 3, 2.5));
        bounding.add(new AABB(-0.75, 0, -2.5, 0.75, 1, -1.5));
        bounding.add(new AABB(-0.75, 0, 1.5, 0.75, 1, 2.5));
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
    public @Nullable VoxelShape cellShape(
            int lx, int ly, int lz, Direction facing, boolean open, boolean forCollision) {
        if (forCollision && lx == 0 && ly == 0 && lz == 0) return boundingOutline(facing);
        if (ly == 0 && lz == 0 && Math.abs(lx) == 2) return null;
        return super.cellShape(lx, ly, lz, facing, open, forCollision);
    }

    @Override
    protected BlockState cellStateFor(int lx, int ly, int lz, Direction facing, int shapeId) {
        if (ly == 0 && lz == 0 && Math.abs(lx) == 2) {
            return IndustryBlocksSpace.HYDROBAY_END_CELL
                    .get()
                    .defaultBlockState()
                    .setValue(HydrobayEndCell.ALONG_X, facing.getAxis() == Direction.Axis.Z)
                    .setValue(BlockMultiblockCell.SOUND, cellSound())
                    .setValue(BlockMultiblockCell.SEALED, cellsSealRadiation());
        }
        return super.cellStateFor(lx, ly, lz, facing, shapeId);
    }

    @Override
    public boolean checkRequirement(Level level, BlockPos placed, Direction dir, int o) {
        return super.checkRequirement(level, placed, dir, o)
                && MultiblockHandlerXR.checkSpace(
                        level, placed.relative(dir, o), INTERIOR, placed, dir);
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        Direction rot = facing.getClockWise();
        BlockPos top = core.above(2);
        MultiblockHandlerXR.visitBox(
                top.relative(rot, 2).relative(facing),
                new int[] {0, 1, 0, 0, 0, 4},
                facing,
                visitor);
        MultiblockHandlerXR.visitBox(
                top.relative(rot, 2).relative(facing, -1),
                new int[] {0, 1, 0, 0, 0, 4},
                facing,
                visitor);
        MultiblockHandlerXR.visitBox(
                top.relative(rot, 2), new int[] {0, 0, 0, 0, 0, 4}, facing, visitor);
        MultiblockHandlerXR.visitBox(
                core.above().relative(rot, 2), new int[] {1, 0, 1, 1, 0, 0}, facing, visitor);
        MultiblockHandlerXR.visitBox(
                core.above().relative(rot, -2), new int[] {1, 0, 1, 1, 0, 0}, facing, visitor);
        MultiblockHandlerXR.visitBox(core, new int[] {2, 0, 0, 0, -2, 2}, facing, visitor);
        MultiblockHandlerXR.visitBox(core, new int[] {2, 0, 0, 0, 2, -2}, facing, visitor);
        MultiblockHandlerXR.visitBox(
                top.relative(facing), new int[] {0, 0, 1, 0, 0, 0}, facing, visitor);
        MultiblockHandlerXR.visitBox(core, new int[] {0, 0, 0, 0, 2, 2}, facing, visitor);

        int ends = (1 << facing.ordinal()) | (1 << facing.getOpposite().ordinal());
        visitor.cell(core.relative(rot, 2), ends, ROLE_FLUID);
        visitor.cell(core.relative(rot, -2), ends, ROLE_FLUID);
        visitor.cell(top, MASK_UP, ROLE_POWER_IN);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        Direction rot = facing.getClockWise();
        int domains = PASSIVE_POWER_IN | PASSIVE_FLUID_IN | PASSIVE_ITEMS;
        visitor.passiveCell(core.relative(rot, 2), MASK_ALL, domains);
        visitor.passiveCell(core.relative(rot, -2), MASK_ALL, domains);
        visitor.passiveCell(core.above(2), MASK_ALL, domains);
    }

    @Override
    public boolean plantSoilAt(int lx, int ly, int lz) {
        return ly == 0 && lz == 0 && Math.abs(lx) <= 1;
    }

    @Override
    protected @Nullable BlockState swappedCellStateFor(int lx, int ly, int lz, Direction facing) {
        return ly == LAMP_Y && lz == 0 && Math.abs(lx) == 1 ? litLamp() : null;
    }

    private static BlockState litLamp() {
        return SpaceCellBuckets.H10_R12_METAL_LIT.get().defaultBlockState();
    }

    public void setLights(ServerLevel level, BlockPos core, Direction facing, boolean lit) {
        List<BlockPos> swapped = new ArrayList<>(2);
        BlockMultiblockCore.withoutTeardown(
                () -> {
                    for (int lx = -1; lx <= 1; lx += 2) {
                        BlockState state =
                                lit
                                        ? litLamp()
                                        : cellStateFor(
                                                lx,
                                                LAMP_Y,
                                                0,
                                                facing,
                                                MultiblockCellShapes.idAt(
                                                        this, lx, LAMP_Y, 0, facing));
                        BlockPos lamp =
                                core.offset(
                                        MultiblockMaskTable.worldX(lx, LAMP_Y, 0, facing),
                                        LAMP_Y,
                                        MultiblockMaskTable.worldZ(lx, LAMP_Y, 0, facing));
                        if (level.setBlock(lamp, state, Block.UPDATE_ALL)) swapped.add(lamp);
                    }
                });
        if (!swapped.isEmpty()) claimCells(level, core, swapped);
    }

    public static @Nullable CropBlock hydroponicCrop(ItemStack seed) {
        if (!(seed.getItem() instanceof BlockItem item)
                || !(item.getBlock() instanceof CropBlock crop)) return null;
        return crop instanceof BlockCrop space && !space.canHydro() ? null : crop;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityHydroponic(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityHydroponic hydro)) return;

        info.title(Component.translatable(getDescriptionId()), 0xFFFF00, 0x404000);
        info.line(
                (hydro.power <= BlockEntityHydroponic.LIGHT_POWER
                                ? ChatFormatting.RED
                                : ChatFormatting.GREEN)
                        + "Power: "
                        + BobMathUtil.getShortNumber(hydro.power)
                        + "HE");
        info.line(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + tankLine(hydro.tanks[0]));
        info.line(ChatFormatting.RED + "<- " + ChatFormatting.RESET + tankLine(hydro.tanks[1]));
    }

    private static String tankLine(FluidTankNTM tank) {
        return NTMFluidProperties.clientName(tank.getTankType())
                + ": "
                + tank.getFill()
                + "/"
                + tank.getMaxFill()
                + "mB";
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(IndustryBlockEntitiesSpace.HYDROBAY)
                .powerIn()
                .fluidIn()
                .fluidOut()
                .items()
                .itemsAtCells()
                .fe();
    }
}
