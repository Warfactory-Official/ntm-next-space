// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.ModBlockEntities;
import com.hbm.blocks.machine.MachineLPW2;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityMachineLPW2Space;
import com.hbmspace.dim.CelestialBody;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MachineLPW2.class)
public abstract class MixinMachineLPW2 extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock, ILookOverlay {

    protected MixinMachineLPW2(Properties properties) {
        super(properties);
    }

    @Inject(method = "getOffset", at = @At("HEAD"), cancellable = true)
    private void hbmspace$placeAtNozzle(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(9);
    }

    @Override
    public Direction getDirModified(Direction dir) {
        return dir.getCounterClockWise();
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        Direction rot = facing.getClockWise();
        visitor.cell(core.relative(facing, 3).relative(rot.getOpposite()).above(3), MASK_SOUTH);
        visitor.cell(core.relative(facing, -3).relative(rot.getOpposite()).above(3), MASK_NORTH);
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        Direction rot = facing.getClockWise();
        visitor.passiveCell(
                core.relative(facing, 3).relative(rot.getOpposite()).above(3),
                MASK_ALL,
                PASSIVE_FLUID_IN);
        visitor.passiveCell(
                core.relative(facing, -3).relative(rot.getOpposite()).above(3),
                MASK_ALL,
                PASSIVE_FLUID_IN);
    }

    @Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true)
    private void hbmspace$engine(
            BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        cir.setReturnValue(new BlockEntityMachineLPW2Space(pos, state));
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(ModBlockEntities.LPW2)
                .fluidIn()
                .fluidFaces(
                        BlockEntityMachineLPW2Space.class,
                        (be, face) -> CelestialBody.inOrbit(be.getLevel()));
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityMachineLPW2Space thruster)) return;
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
                                    Component.translatable(
                                                    "overlay.hbmspace.launchPadRocket.tank",
                                                    NTMFluidProperties.getDisplayName(
                                                            tank.getTankType()),
                                                    tank.getFill(),
                                                    tank.getMaxFill())
                                            .withStyle(ChatFormatting.WHITE)));
        }
    }
}
