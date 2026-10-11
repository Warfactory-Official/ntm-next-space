// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityAirScrubber;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockAirScrubber extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, ICapabilityBlock {

    private static final int[] DIMENSIONS = {1, 0, 0, 0, 0, 0};

    public BlockAirScrubber(Properties props) {
        super(props);
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
        return MASK_ALL & ~MASK_UP;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAirScrubber(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAirScrubber scrubber)) return;

        info.title(getName().getString(), 0xFFFF00, 0x404000);
        info.line(
                (scrubber.canOperate() ? ChatFormatting.GREEN : ChatFormatting.RED)
                        + "Power: "
                        + BobMathUtil.getShortNumber(scrubber.getPower())
                        + "HE");
        info.line(
                ChatFormatting.RED
                        + "<- "
                        + ChatFormatting.RESET
                        + NTMFluidProperties.clientName(scrubber.tank.getFluid())
                        + ": "
                        + scrubber.tank.getFill()
                        + "/"
                        + scrubber.tank.getMaxFill()
                        + "mB");
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.AIR_SCRUBBER).powerIn().fluidOut().fe();
    }
}
