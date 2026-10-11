// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.FluidIdentifierData;
import com.hbm.items.machine.FluidIdentifierItem;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTR3;
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

public class MachineHTR3 extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock, ILookOverlay {

    private static final int[] DIMENSIONS = {3, 3, 3, 3, 5, 5};

    public MachineHTR3(Properties props) {
        super(props);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 5;
    }

    @Override
    public int getHeightOffset() {
        return 3;
    }

    @Override
    public Direction getDirModified(Direction dir) {
        return dir.getCounterClockWise();
    }

    public static BlockPos feedCell(BlockPos core, Direction facing) {
        return core.relative(facing.getClockWise().getOpposite(), 5);
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        visitor.cell(feedCell(core, facing), MASK_EAST);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        visitor.passiveCell(feedCell(core, facing), MASK_ALL, PASSIVE_FLUID_IN);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityMachineHTR3(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.HTR3)
                .fluidIn()
                .fluidFaces(
                        BlockEntityMachineHTR3.class,
                        (be, face) -> CelestialBody.inOrbit(be.getLevel()));
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
        if (!(level.getBlockEntity(core) instanceof BlockEntityMachineHTR3 htr3))
            return InteractionResult.PASS;

        Fluid type =
                held.getOrDefault(
                                ModDataComponents.FLUID_IDENTIFIER.get(), FluidIdentifierData.EMPTY)
                        .primary();
        FT_Heatable heatable = NTMFluidProperties.getTrait(type, FT_Heatable.class);

        if (heatable != null
                && NTMFluidProperties.hasTrait(
                        heatable.getFirstStep().typeProduced(), FT_Rocket.class)) {
            htr3.tanks[0].setTankType(heatable.getFirstStep().typeProduced());
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
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineHTR3 thruster)) return;

        info.title(getName(), 0xFFFF00, 0x404000);

        if (!thruster.isFacingPrograde()) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.engineFacing"))
                            .append(" ! ! !"),
                    BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        for (FluidTankNTM tank : thruster.tanks) {
            info.line(
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

        if (feedCell(pos, coreFacing(level.getBlockState(pos))).equals(info.getHitPos())) {
            info.line(Component.translatable("overlay.hbmspace.htr3.feed"));
        }
    }
}
