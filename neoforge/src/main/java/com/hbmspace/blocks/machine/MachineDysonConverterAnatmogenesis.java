// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.interfaces.IToolable;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbmspace.blockentity.machine.BlockEntityDysonConverterAnatmogenesis;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import com.hbmspace.util.AstronomyUtil;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MachineDysonConverterAnatmogenesis extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, IToolable {

    private static final int[] DIMENSIONS = {2, 0, 5, 5, 1, 1};

    public MachineDysonConverterAnatmogenesis(Properties properties) {
        super(properties);
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
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDysonConverterAnatmogenesis(pos, state);
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
        Fluid fluid = AtmosphereText.identified(held);
        if (fluid == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()
                && AtmosphereText.isGas(fluid)
                && level.getBlockEntity(core)
                        instanceof BlockEntityDysonConverterAnatmogenesis converter) {
            converter.fluid = fluid;
            converter.setChanged();
            AtmosphereText.changedType(player, fluid);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onScrew(
            Level level,
            Player player,
            BlockPos pos,
            @Nullable Direction side,
            Vec3 hit,
            ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) return false;
        if (level.isClientSide()) return true;
        if (!(level.getBlockEntity(pos)
                instanceof BlockEntityDysonConverterAnatmogenesis converter)) return false;

        converter.isEmitting = !converter.isEmitting;
        converter.setChanged();
        return true;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos)
                instanceof BlockEntityDysonConverterAnatmogenesis converter)) return;

        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        double pressure = atmosphere != null ? atmosphere.getPressure(converter.fluid) : 0;
        if (pressure < 0.0001) pressure = 0;
        pressure = Math.round(pressure * 1_000.0) / 1_000.0;

        info.title(getName(), 0xFFFF00, 0x404000);
        info.line(
                Component.translatable(
                        "overlay.hbmspace.anatmogenesis.rate",
                        (double) converter.gasProduced * 20 * 60 * 60 / AstronomyUtil.MB_PER_ATM));
        info.line(
                Component.translatable(
                        "overlay.hbmspace.anatmogenesis.gas",
                        NTMFluidProperties.getDisplayName(converter.fluid),
                        pressure));
        info.line(
                Component.translatable(
                        converter.isEmitting
                                ? "overlay.hbmspace.anatmogenesis.emitting"
                                : "overlay.hbmspace.anatmogenesis.capturing"));
    }
}
