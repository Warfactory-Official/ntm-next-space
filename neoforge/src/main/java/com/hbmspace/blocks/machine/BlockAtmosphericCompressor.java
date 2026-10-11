// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.DeuteriumTower;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.IToolable;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityAtmosphericCompressor;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.AtmosphereText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class BlockAtmosphericCompressor extends DeuteriumTower implements IToolable {

    private static final int[] DIMENSIONS = {3, 0, 1, 0, 0, 1};

    public BlockAtmosphericCompressor(Properties props) {
        super(props);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAtmosphericCompressor(pos, state);
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
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAtmosphericCompressor compressor))
            return false;

        compressor.cycleGas();
        return true;
    }

    @Override
    protected InteractionResult useItemOnAtCore(
            ItemStack stack,
            BlockState coreState,
            Level level,
            BlockPos core,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        Fluid type = AtmosphereText.identified(stack);
        if (player.isShiftKeyDown() || type == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(core) instanceof BlockEntityAtmosphericCompressor compressor
                && compressor.switchGas(type)) {
            AtmosphereText.changedType(player, type);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAtmosphericCompressor compressor))
            return;

        info.title(getName().getString(), 0xFFFF00, 0x404000);
        if (!CelestialBody.hasTrait(level, CBT_Atmosphere.class)) {
            info.line(
                    ChatFormatting.RED
                            + "ERROR: "
                            + ChatFormatting.RESET
                            + "CANNOT COLLECT IN VACUUM");
            return;
        }
        info.line(
                (compressor.power < BlockEntityAtmosphericCompressor.MAX_POWER / 20
                                ? ChatFormatting.RED
                                : ChatFormatting.GREEN)
                        + "Power: "
                        + BobMathUtil.getShortNumber(compressor.power)
                        + "HE");
        info.line(
                ChatFormatting.RED
                        + "<- "
                        + ChatFormatting.RESET
                        + NTMFluidProperties.clientName(compressor.tank.getFluid())
                        + ": "
                        + compressor.tank.getFill()
                        + "/"
                        + compressor.tank.getMaxFill()
                        + "mB");
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_COMPRESSOR)
                .powerIn()
                .fluidOut()
                .fe();
    }
}
