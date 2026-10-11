// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.api.fluidmk2.FluidPipeTintData;
import com.hbm.blockentity.machine.BlockEntityRefueler;
import com.hbm.blocks.machine.BlockRefueler;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockRefueler.class)
public abstract class MixinBlockRefueler implements ILookOverlay {

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityRefueler refueler)) return;
        Fluid type = refueler.tank.getTankType();
        info.title(((Block) (Object) this).getName().getString(), 0xFFFF00, 0x404000);
        info.line(
                NTMFluidProperties.getDisplayName(type).getString(),
                FluidPipeTintData.colorFor(type == null ? Fluids.EMPTY : type) & 0xFFFFFF);
    }
}
