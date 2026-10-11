// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbm.blocks.multiblock.MultiblockMaskTable;
import com.hbm.capability.ICapabilityBlock;
import com.hbm.capability.MachineCaps;
import com.hbm.interfaces.ILookOverlay;
import com.hbm.items.ModItems;
import com.hbm.items.block.ItemBlockPlushie;
import com.hbm.sound.ModSounds;
import com.hbm.util.BobMathUtil;
import com.hbmspace.blockentity.machine.BlockEntityDysonLauncher;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.blocks.generic.PlushieTypeSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class MachineDysonLauncher extends BlockMultiblockCore
        implements ITickingBlock, ILookOverlay, ICapabilityBlock {
    private static final int[] DIMENSIONS = {16, 0, 8, 0, 0, 0};

    private static final Box[] BOXES = {
        new Box(6, 14, 0, 14, 1, 0, 6, -5),
        new Box(6, 14, 0, 14, 1, 0, -5, 6),
        new Box(2, 7, 2, 0, 0, 3, 2, 2),
        new Box(1, 6, 2, 0, 0, 3, 3, 3),
        new Box(0, 5, 2, 0, 0, 3, 3, 3),
        new Box(-1, 4, 2, 0, 0, 3, 3, 3),
        new Box(-2, 3, 2, 0, 0, 3, 2, 2),
        new Box(6, 14, 2, 0, 0, 0, 3, 3),
        new Box(6, 14, 0, 0, 0, 2, 3, 3),
        new Box(5, 13, 2, 0, 0, 2, 10, 5),
        new Box(4, 12, 2, 0, 0, 2, 10, 7),
        new Box(3, 11, 2, 0, 0, 2, 10, 8),
        new Box(2, 10, 2, 0, 0, 2, 10, 9),
        new Box(1, 9, 2, 0, 0, 2, 10, 10),
        new Box(0, 8, 2, 0, 0, 2, 10, 10),
        new Box(-1, 7, 2, 0, 0, 2, 10, 10),
        new Box(-2, 6, 2, 0, 0, 2, 10, 10),
        new Box(-3, 5, 2, 0, 0, 2, 10, 10),
        new Box(-4, 4, 2, 0, 0, 2, 9, 9),
        new Box(-3, 3, 2, 0, 2, 0, 8, 8),
        new Box(-4, 2, 2, 0, 2, 0, 7, 7),
        new Box(-7, 2, 1, 0, 0, 0, 6, 6),
        new Box(0, 2, 0, 0, 8, -8, 3, 3),
        new Box(0, 1, 1, 1, 8, -8, 1, 1),
        new Box(-7, 0, 1, 0, 0, 0, 4, 4),
        new Box(-6, 0, 1, 0, 0, 0, 5, 5),
        new Box(-5, 0, 1, 0, 0, 0, 6, 6),
        new Box(0, 0, 3, 0, 7, 0, 2, 2),
    };

    private record Box(int forward, int up, int... dimensions) {

        BlockPos origin(BlockPos core, Direction facing) {
            return core.offset(facing.getStepX() * forward, up, facing.getStepZ() * forward);
        }
    }

    public MachineDysonLauncher(Properties properties) {
        super(properties);
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public boolean checkRequirement(Level level, BlockPos placed, Direction dir, int o) {
        BlockPos core = placed.offset(dir.getStepX() * o, 0, dir.getStepZ() * o);
        for (Box box : BOXES) {
            if (!MultiblockHandlerXR.checkSpace(
                    level, box.origin(core, dir), box.dimensions(), placed, dir)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void visitCells(BlockPos core, Direction facing, CellVisitor visitor) {
        for (Box box : BOXES)
            MultiblockHandlerXR.visitBox(
                    box.origin(core, facing), box.dimensions(), facing, visitor);
        for (int back = 0; back <= 4; back++) {
            visitor.cell(local(core, 2, 0, -back, facing), MASK_EAST);
        }
    }

    @Override
    protected void visitPassiveCells(BlockPos core, Direction facing, PassiveCellVisitor visitor) {
        for (int back = 0; back <= 4; back++) {
            visitor.passiveCell(local(core, 2, 0, -back, facing), MASK_ALL, PASSIVE_POWER_IN);
        }
        visitor.passiveCell(local(core, 0, 0, -8, facing), MASK_ALL, PASSIVE_ITEMS);
    }

    private static BlockPos local(BlockPos core, int lx, int ly, int lz, Direction facing) {
        return core.offset(
                MultiblockMaskTable.worldX(lx, ly, lz, facing),
                ly,
                MultiblockMaskTable.worldZ(lx, ly, lz, facing));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDysonLauncher(pos, state);
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
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof BlockEntityDysonLauncher launcher))
            return InteractionResult.PASS;

        if (held.is(ModItems.SAT_CHIP.get())) {
            if (!launcher.getItem(BlockEntityDysonLauncher.SLOT_CHIP).isEmpty())
                return InteractionResult.PASS;
            launcher.setItem(BlockEntityDysonLauncher.SLOT_CHIP, held.copy());
            held.consume(held.getCount(), player);
            plug(level, hit.getBlockPos());
        } else if (!launcher.sunsetOverdrive
                && held.getItem() instanceof ItemBlockPlushie plushie
                && plushie.type == PlushieTypeSpace.FATO) {
            launcher.sunsetOverdrive = true;
            launcher.setChanged();
            held.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(core) instanceof BlockEntityDysonLauncher launcher))
            return InteractionResult.PASS;

        ItemStack chip = launcher.getItem(BlockEntityDysonLauncher.SLOT_CHIP);
        if (!chip.isEmpty()) {
            launcher.setItem(BlockEntityDysonLauncher.SLOT_CHIP, ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(chip);
            plug(level, hit.getBlockPos());
        }
        return InteractionResult.SUCCESS;
    }

    static void plug(Level level, BlockPos pos) {
        level.playSound(
                null,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                ModSounds.UPGRADE_PLUG.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F);
    }

    @Override
    public void buildLookOverlay(Level level, BlockPos pos, LookInfo info) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityDysonLauncher launcher)) return;

        info.title(getName(), 0xFFFF00, 0x404000);
        if (launcher.swarmId > 0) {
            info.line(Component.translatable("overlay.hbmspace.dyson.id", launcher.swarmId));
            info.line(Component.translatable("overlay.hbmspace.dyson.swarm", launcher.swarmCount));
            info.line(
                    Component.translatable(
                                    "overlay.hbmspace.dyson_launcher.power",
                                    BobMathUtil.getShortNumber(launcher.power))
                            .withStyle(
                                    launcher.power < BlockEntityDysonLauncher.MAX_POWER
                                            ? ChatFormatting.RED
                                            : ChatFormatting.GREEN));
        } else {
            info.line(Component.translatable("overlay.hbmspace.dyson.no_chip"));
        }
    }

    @Override
    public MachineCaps caps() {
        return MachineCaps.of(SkyBlockEntitiesSpace.DYSON_LAUNCHER)
                .powerIn()
                .items()
                .itemsAtCells()
                .fe();
    }
}
