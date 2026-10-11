// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.interfaces.IToolable;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbmspace.blockentity.machine.BlockEntityAtmosphereEditor;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import com.hbmspace.util.AstronomyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BlockAtmosphereEditor extends Block implements ITickingBlock, IToolable, ILookOverlay {

    public BlockAtmosphereEditor(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAtmosphereEditor(pos, state);
    }

    @Override
    public boolean onScrew(
            Level level,
            Player player,
            BlockPos pos,
            @Nullable Direction side,
            Vec3 hit,
            ToolType tool) {
        if (tool != ToolType.SCREWDRIVER && tool != ToolType.HAND_DRILL && tool != ToolType.DEFUSER)
            return false;
        if (level.isClientSide()) return true;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAtmosphereEditor editor))
            return false;

        switch (tool) {
            case SCREWDRIVER -> editor.isOn = !editor.isOn;
            case HAND_DRILL -> editor.throughputFactor += player.isShiftKeyDown() ? -1 : 1;
            default -> editor.isEmitting = !editor.isEmitting;
        }
        editor.setChanged();
        return true;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        Fluid type = AtmosphereText.identified(stack);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (type == null
                || !(level.getBlockEntity(pos) instanceof BlockEntityAtmosphereEditor editor)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        editor.fluid = type;
        editor.setChanged();
        AtmosphereText.changedType(player, type);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (CelestialBody.inOrbit(level)) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAtmosphereEditor editor)) return;

        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        double pressure = atmosphere != null ? atmosphere.getPressure(editor.fluid) : 0;
        if (pressure < 0.0001) pressure = 0;
        pressure = Math.round(pressure * 1_000.0) / 1_000.0;

        info.title(getName().getString(), 0xFFFF00, 0x404000);
        info.line("State: " + (editor.isOn ? "RUNNING" : "OFF"));
        info.line("Current gas: " + NTMFluidProperties.clientName(editor.fluid) + " - " + pressure);
        info.line("Current mode: " + (editor.isEmitting ? "EMITTING" : "CAPTURING"));
        info.line(
                "Current throughput: "
                        + Math.pow(10, editor.throughputFactor) / AstronomyUtil.MB_PER_ATM);
    }
}
