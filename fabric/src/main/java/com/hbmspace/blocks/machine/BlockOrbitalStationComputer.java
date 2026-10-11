// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.machine.BlockMachineHorizontal;
import com.hbm.client.ClientPlayerAccess;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.IGUIProvider;
import com.hbm.sound.ModSounds;
import com.hbm.util.BobMathUtil;
import com.hbmspace.api.tile.IPropulsion;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationComputer;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.orbit.OrbitalStation.StationState;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.items.ItemVOTVdrive.Destination;
import com.hbmspace.items.ItemVOTVdrive;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockOrbitalStationComputer extends BlockMachineHorizontal implements ILookOverlay {

    public BlockOrbitalStationComputer(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityOrbitalStationComputer(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack held,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (held.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityOrbitalStationComputer computer)) {
            return InteractionResult.PASS;
        }
        if (computer.isTravelling()) return InteractionResult.PASS;

        if (held.getItem() instanceof ItemVOTVdrive) {
            if (!computer.getItem(0).isEmpty()) return InteractionResult.PASS;

            Destination destination = ItemVOTVdrive.getDestination(held);
            if (destination.body() == SolarSystem.Body.ORBIT) return InteractionResult.PASS;
            if (!computer.travelTo(destination.body().getBody(), held.copy()))
                return InteractionResult.PASS;

            held.setCount(0);
            level.playSound(
                    null, pos, ModSounds.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        IGUIProvider.openBlockMenu(player, computer, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityOrbitalStationComputer computer)) {
            return InteractionResult.PASS;
        }
        if (computer.isTravelling()) return InteractionResult.PASS;

        ItemStack drive = computer.getItem(0);
        if (!drive.isEmpty()) {
            player.getInventory().placeItemBackInInventory(drive.copy());
            computer.setItem(0, ItemStack.EMPTY);
        } else {
            IGUIProvider.openBlockMenu(player, computer, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        info.title(getName(), 0xFFFF00, 0x404000);

        if (!CelestialBody.inOrbit(level)) {
            info.line(
                    Component.literal("! ! ! ")
                            .append(Component.translatable("atmosphere.noOrbit"))
                            .append(" ! ! !"),
                    BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof BlockEntityOrbitalStationComputer computer))
            return;

        OrbitalStation station = OrbitalStation.clientStation;
        double progress = station.getUnscaledProgress(0);

        if (!station.hasEngines) {
            info.line(
                    Component.translatable("overlay.hbmspace.station.no_engines")
                            .withStyle(ChatFormatting.RED));
        } else if (!station.errorsAt.isEmpty()) {
            for (BlockPos errorAt : station.errorsAt) {
                if (!(level.getBlockEntity(errorAt) instanceof IPropulsion engine)) continue;

                List<Component> errors = new ArrayList<>();
                engine.addErrors(errors);

                if (!errors.isEmpty()) {
                    info.line(
                            engine.getBlockEntity()
                                    .getBlockState()
                                    .getBlock()
                                    .getName()
                                    .copy()
                                    .withStyle(ChatFormatting.RED));
                    for (Component error : errors) info.line(error);
                }
            }
        } else if (progress > 0) {
            Component target = Component.translatable("body." + station.target.name);
            if (station.state == StationState.LEAVING) {
                info.line(
                        Component.translatable("station.engage")
                                .append(": ")
                                .withStyle(ChatFormatting.AQUA)
                                .append(target.copy().withStyle(ChatFormatting.WHITE)));
            } else if (station.state == StationState.ARRIVING) {
                info.line(
                        Component.translatable("station.disengage").withStyle(ChatFormatting.AQUA));
            } else {
                info.line(
                        Component.translatable("station.travelling")
                                .append(": ")
                                .withStyle(ChatFormatting.AQUA)
                                .append(target.copy().withStyle(ChatFormatting.WHITE)));
            }
            info.line(
                    Component.translatable("station.progress")
                            .append(": ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(
                                    Component.literal(Math.round(progress * 100) + "%")
                                            .withStyle(ChatFormatting.WHITE)));
        } else if (computer.hasDrive) {
            Player player = ClientPlayerAccess.player();
            boolean holding = player != null && !player.getMainHandItem().isEmpty();
            info.line(
                    Component.translatable(
                            holding ? "station.removeDrive" : "station.interactDrive"));
        } else {
            info.line(Component.translatable("station.insertDrive"));
        }
    }
}
