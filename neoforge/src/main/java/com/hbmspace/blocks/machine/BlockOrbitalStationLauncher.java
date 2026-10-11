// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.capability.MachineCaps;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.dim.CelestialBody;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockOrbitalStationLauncher extends BlockOrbitalStation {

    public BlockOrbitalStationLauncher(Properties props) {
        super(props, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityOrbitalStationLauncher(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.ORBITAL_STATION_LAUNCHER)
                .fluidIn()
                .itemsAtCells();
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
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (!CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (!onPad(core, hit.getBlockPos())) return InteractionResult.PASS;
        if (!(level.getBlockEntity(core) instanceof BlockEntityOrbitalStationLauncher launcher)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (launcher.hasDocked) {
            if (!launcher.hasRider) launcher.enterCapsule(player);
        } else if (!player.isSecondaryUseActive()) {
            openCoreMenu(player, core, launcher);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (printsNoOrbit(level, info)) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityOrbitalStationLauncher launcher))
            return;

        Component title = getName();
        if (launcher.hasDocked) {
            info.title(title, 0xFFFF00, 0x404000);
            info.line(
                    launcher.hasRider
                            ? Component.translatable("station.occupiedRocket")
                                    .withStyle(ChatFormatting.YELLOW)
                            : Component.translatable("station.enterRocket"));
            return;
        }

        if (!launcher.rocket.validate()) return;

        List<Component> fuels = new ArrayList<>();
        for (FluidTankNTM tank : launcher.tanks) {
            if (tank.getTankType() == null) continue;
            fuels.add(
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
        if (launcher.solidMax > 0) {
            fuels.add(
                    Component.literal("-> ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(
                                    Component.translatable(
                                                    "overlay.hbmspace.launchPadRocket.solid",
                                                    ModItems.ROCKET_FUEL
                                                            .get()
                                                            .getDefaultInstance()
                                                            .getHoverName(),
                                                    launcher.solidLevel,
                                                    launcher.solidMax)
                                            .withStyle(ChatFormatting.WHITE)));
        }
        if (fuels.isEmpty()) return;

        info.title(title, 0xFFFF00, 0x404000);
        info.line(Component.translatable("overlay.hbmspace.launchPadRocket.fuels"));
        for (Component line : fuels) info.line(line);
    }
}
