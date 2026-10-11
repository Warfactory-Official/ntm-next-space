// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityDysonReceiver;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class MachineDysonReceiver extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay {

    private static final int[] DIMENSIONS = {2, 0, 4, 2, 2, 2};
    private static final int[] BEAM_BASE = {1, 0, 8, -4, 1, 1};
    private static final int[] BASE = {4, 0, 4, 0, 1, 1};
    private static final int[] DISH = {1, 0, 6, 6, 6, 6};
    private static final int[] TOWER = {17, 0, 1, 1, 1, 1};
    private static final int DISH_HEIGHT = 10;

    public MachineDysonReceiver(Properties properties) {
        super(properties);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 2;
    }

    @Override
    public boolean checkRequirement(Level level, BlockPos placed, Direction dir, int o) {
        if (!super.checkRequirement(level, placed, dir, o)) return false;
        BlockPos core = placed.offset(dir.getStepX() * o, 0, dir.getStepZ() * o);
        return MultiblockHandlerXR.checkSpace(level, core, BEAM_BASE, placed, dir)
                && MultiblockHandlerXR.checkSpace(level, core, BASE, placed, dir)
                && MultiblockHandlerXR.checkSpace(level, core.above(DISH_HEIGHT), DISH, placed, dir)
                && MultiblockHandlerXR.checkSpace(level, core, TOWER, placed, dir);
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        super.visitCells(core, facing, visitor);
        MultiblockHandlerXR.visitBox(core, BEAM_BASE, facing, visitor);
        MultiblockHandlerXR.visitBox(core, BASE, facing, visitor);
        MultiblockHandlerXR.visitBox(core.above(DISH_HEIGHT), DISH, facing, visitor);
        MultiblockHandlerXR.visitBox(core, TOWER, facing, visitor);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDysonReceiver(pos, state);
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
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof BlockEntityDysonReceiver receiver))
            return InteractionResult.PASS;

        if (held.is(ModItems.SAT_CHIP.get())) {
            if (!receiver.getItem(0).isEmpty()) return InteractionResult.PASS;
            receiver.setItem(0, held.copy());
            held.consume(held.getCount(), player);
            MachineDysonLauncher.plug(level, hit.getBlockPos());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof BlockEntityDysonReceiver receiver))
            return InteractionResult.PASS;

        ItemStack chip = receiver.getItem(0);
        if (!chip.isEmpty()) {
            receiver.setItem(0, ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(chip);
            MachineDysonLauncher.plug(level, hit.getBlockPos());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityDysonReceiver receiver)) return;

        long energyOutput =
                receiver.isReceiving
                        ? BlockEntityDysonReceiver.getEnergyOutput(receiver.swarmCount)
                                / receiver.swarmConsumers
                                * 20
                        : 0;

        info.title(getName(), 0xFFFF00, 0x404000);
        if (receiver.swarmId > 0) {
            info.line(Component.translatable("overlay.hbmspace.dyson.id", receiver.swarmId));
            info.line(Component.translatable("overlay.hbmspace.dyson.swarm", receiver.swarmCount));
            info.line(
                    Component.translatable(
                            "overlay.hbmspace.dyson_receiver.consumers", receiver.swarmConsumers));
            info.line(
                    Component.translatable(
                            "overlay.hbmspace.dyson_receiver.power",
                            BobMathUtil.getShortNumber(energyOutput)));
        } else {
            info.line(Component.translatable("overlay.hbmspace.dyson.no_chip"));
        }
    }
}
