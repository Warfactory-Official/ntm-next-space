// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.DeuteriumTower;
import com.hbm.capability.MachineCaps;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityAtmoTower;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class AtmoTower extends DeuteriumTower {

    private static final int[] DIMENSIONS = {6, 0, 1, 0, 0, 1};

    public AtmoTower(Properties props) {
        super(props);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAtmoTower(pos, state);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAtmoTower tower)) return;

        info.title(getName().getString(), 0xFFFF00, 0x404000);
        info.line(
                (tower.power < tower.getMaxPower() / 20 ? ChatFormatting.RED : ChatFormatting.GREEN)
                        + "Power: "
                        + BobMathUtil.getShortNumber(tower.power)
                        + "HE");
        for (int i = 0; i < tower.tanks.length; i++) {
            FluidTankNTM tank = tower.tanks[i];
            info.line(
                    (i < 1 ? ChatFormatting.GREEN + "-> " : ChatFormatting.RED + "<- ")
                            + ChatFormatting.RESET
                            + NTMFluidProperties.clientName(tank.getFluid())
                            + ": "
                            + tank.getFill()
                            + "/"
                            + tank.getMaxFill()
                            + "mB");
        }
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.ATMO_TOWER)
                .powerIn()
                .fluidIn()
                .fluidOut();
    }
}
