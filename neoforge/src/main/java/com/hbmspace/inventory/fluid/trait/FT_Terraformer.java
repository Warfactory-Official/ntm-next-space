// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.fluid.trait;

import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.SolarSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class FT_Terraformer extends FluidTrait {

    private final SolarSystem.Body target;

    public FT_Terraformer(SolarSystem.Body target) {
        this.target = target;
    }

    public SolarSystem.Body getTarget() {
        return target;
    }

    public Block getTargetBlock() {
        if (target == SolarSystem.Body.DUNA) return ModBlocksSpace.DUNA_SANDS.get();
        if (target == SolarSystem.Body.LAYTHE) return TerrainBlocksSpace.LAYTHE_SILT.get();
        return Blocks.STONE;
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(
                Component.literal("[")
                        .append(Component.translatable("hbmfluid.trait.terraforming"))
                        .append("]")
                        .withStyle(ChatFormatting.AQUA));
    }
}
