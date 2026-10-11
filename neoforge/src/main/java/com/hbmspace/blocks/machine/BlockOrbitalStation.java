// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.client.ClientPlayerAccess;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStation;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.blocks.StationBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.handler.atmosphere.IBlockSealable;
import com.hbmspace.items.weapon.ItemCustomRocket;
import com.hbmspace.items.weapon.MissilePartsSpace;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockOrbitalStation extends BlockMultiblockCore
        implements ITickingBlock, ICapabilityBlock, ILookOverlay, IBlockSealable {

    private static final int[] DIMENSIONS = {1, 0, 2, 2, 2, 2};

    private static final int[][] RING = {
        {2, -1}, {2, 0}, {2, 1}, {-2, -1}, {-2, 0}, {-2, 1},
        {-1, 2}, {0, 2}, {1, 2}, {-1, -2}, {0, -2}, {1, -2}
    };

    private final boolean core;

    public BlockOrbitalStation(Properties props, boolean core) {
        super(props);
        this.core = core;
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 2;
    }

    private static int ringFace(int dx, int dz) {
        if (dx == 2) return MASK_EAST;
        if (dx == -2) return MASK_WEST;
        return dz == 2 ? MASK_SOUTH : MASK_NORTH;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        for (int[] cell : RING)
            visitor.cell(core.offset(cell[0], 1, cell[1]), ringFace(cell[0], cell[1]));
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        for (int[] cell : RING) {
            visitor.passiveCell(
                    core.offset(cell[0], 1, cell[1]), MASK_ALL, PASSIVE_ITEMS | PASSIVE_FLUID_IN);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityOrbitalStation(pos, state);
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(StationBlockEntitiesSpace.ORBITAL_STATION)
                .fluidIn()
                .itemsAtCells()
                .fluidFaces(BlockEntityOrbitalStation.class, (be, face) -> be.needsFuel);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return core ? null : super.getStateForPlacement(ctx);
    }

    @Override
    public boolean isSealed(Level level, BlockPos pos) {
        return true;
    }

    protected static boolean onPad(BlockPos core, BlockPos hit) {
        return Math.abs(core.getX() - hit.getX()) < 2 && Math.abs(core.getZ() - hit.getZ()) < 2;
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
        return use(level, core, player, hit);
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        return use(level, core, player, hit);
    }

    private static boolean placesRocket(ItemStack held) {
        return held.is(MissilePartsSpace.ROCKET_CUSTOM.get()) && ItemCustomRocket.hasFuel(held)
                || isCapsule(held);
    }

    private static boolean isCapsule(ItemStack held) {
        return held.is(MissilePartsSpace.RP_CAPSULE_20.get())
                || held.is(MissilePartsSpace.RP_POD_20.get());
    }

    private InteractionResult use(Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (!CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (!onPad(core, hit.getBlockPos())) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof BlockEntityOrbitalStation station))
            return InteractionResult.PASS;

        if (station.hasStoredItems()) {
            station.giveStoredItems(player);
        } else if (station.hasDocked) {
            if (!station.hasRider) {
                if (player.isShiftKeyDown()) {
                    if (player.getMainHandItem().isEmpty()) {
                        station.despawnRocket();
                        station.giveStoredItems(player);
                    }
                } else {
                    station.enterCapsule(player);
                }
            }
        } else {
            ItemStack held = player.getMainHandItem();
            if (placesRocket(held)) {
                station.spawnRocket(
                        isCapsule(held)
                                ? ItemCustomRocket.build(new RocketStruct(held))
                                : held.copyWithCount(1));
                held.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static boolean refusesRemoval(Level level, BlockPos pos, BlockState state) {
        if (state.is(StationBlocksSpace.ORBITAL_STATION.get())) return true;
        if (!MultiblockSurface.isSurface(state)) return false;
        BlockPos core = MultiblockSurface.coreOfAny(level, pos, state);
        return core != null
                && level.getBlockState(core).is(StationBlocksSpace.ORBITAL_STATION.get());
    }

    protected final boolean printsNoOrbit(Level level, LookInfo info) {
        if (CelestialBody.inOrbit(level)) return false;
        info.title(getName(), 0xFFFF00, 0x404000);
        info.line(
                Component.literal("! ! ! ")
                        .append(Component.translatable("atmosphere.noOrbit"))
                        .append(" ! ! !"),
                BobMathUtil.getBlink() ? 0xFF0000 : 0xFFFF00);
        return true;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (printsNoOrbit(level, info)) return;

        BlockPos hit = info.getHitPos();
        if (hit != null && !onPad(pos, hit)) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityOrbitalStation station)) return;

        List<Component> text = new ArrayList<>();
        for (ItemStack stack : station.inventory) {
            if (!stack.isEmpty()) {
                text.add(
                        Component.literal("<- ")
                                .withStyle(ChatFormatting.RED)
                                .append(
                                        stack.getHoverName()
                                                .copy()
                                                .withStyle(ChatFormatting.WHITE)));
            }
        }

        if (!text.isEmpty()) {
            text.add(Component.translatable("station.retrieveRocket"));
        } else if (station.hasDocked) {
            Player player = ClientPlayerAccess.player();
            if (!station.hasRider) {
                if (player != null && player.isShiftKeyDown()) {
                    if (player.getMainHandItem().isEmpty())
                        text.add(Component.translatable("station.removeRocket"));
                } else {
                    text.add(Component.translatable("station.enterRocket"));
                }
            } else {
                text.add(
                        Component.translatable("station.occupiedRocket")
                                .withStyle(ChatFormatting.YELLOW));
            }

            if (station.needsFuel) {
                for (FluidTankNTM tank : station.tanks) {
                    text.add(
                            Component.literal("-> ")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(
                                            NTMFluidProperties.getDisplayName(tank.getTankType())
                                                    .copy()
                                                    .withStyle(ChatFormatting.WHITE))
                                    .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                                    .append(
                                            Component.translatable(
                                                            "unit.millibuckets",
                                                            tank.getFill()
                                                                    + "/"
                                                                    + tank.getMaxFill())
                                                    .withStyle(ChatFormatting.WHITE)));
                }

                if (!station.hasFuel)
                    text.add(
                            Component.translatable("station.emptyRocket")
                                    .withStyle(ChatFormatting.RED));
            }
        } else {
            Player player = ClientPlayerAccess.player();
            if (player != null
                    && !player.isShiftKeyDown()
                    && placesRocket(player.getMainHandItem())) {
                text.add(Component.translatable("station.placeRocket"));
            }
        }

        if (text.isEmpty()) return;

        info.title(getName(), 0xFFFF00, 0x404000);
        for (Component line : text) info.line(line);
    }
}
