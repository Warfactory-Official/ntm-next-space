// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.machine;

import com.hbm.blocks.ITickingBlock;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.inventory.IGUIProvider;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.ItemReactorSensor;
import com.hbmspace.blockentity.machine.BlockEntityDishControl;
import com.hbmspace.blockentity.machine.BlockEntityMachineStardar;
import com.hbmspace.blocks.IndustryBlocksSpace;
import com.hbmspace.blocks.RocketryBlocksSpace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class MachineDishControl extends BlockMultiblockCore implements ITickingBlock {

    private static final int[] DIMENSIONS = {0, 0, 0, 0, 0, 1};

    public MachineDishControl(Properties props) {
        super(props);
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
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityDishControl(pos, state);
    }

    @Override
    protected InteractionResult useAtCore(
            BlockState coreState, Level level, BlockPos core, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(core) instanceof BlockEntityDishControl console))
            return InteractionResult.PASS;

        if (!console.isLinked) {
            complain(player, "chat.hbmspace.dishControl.notLinked");
            return InteractionResult.PASS;
        }
        BlockEntityMachineStardar stardar = console.linkedStardar();
        if (stardar == null) {
            complain(player, "chat.hbmspace.dishControl.missingTarget");
            return InteractionResult.PASS;
        }
        IGUIProvider.openBlockMenu(player, stardar, console.linkPosition);
        return InteractionResult.SUCCESS;
    }

    private void complain(Player player, String key) {
        player.sendSystemMessage(
                Component.literal("[")
                        .append(Component.translatable(getDescriptionId()))
                        .append("] ")
                        .withStyle(ChatFormatting.DARK_AQUA)
                        .append(Component.translatable(key).withStyle(ChatFormatting.RED)));
    }

    public static InteractionResult useSensor(UseOnContext context, @Nullable BlockPos core) {
        if (core == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockState state = level.getBlockState(core);
        if (state.is(RocketryBlocksSpace.MACHINE_STARDAR.get())) {
            return ItemReactorSensor.recordPosition(
                    context, Component.translatable("chat.hbmspace.reactorSensor.positionSet"));
        }
        if (!state.is(IndustryBlocksSpace.DISH_CONTROL.get())) return InteractionResult.PASS;

        ItemStack sensor = context.getItemInHand();
        if (sensor.get(ModDataComponents.REACTOR_POS.get()) == null) return InteractionResult.PASS;
        if (!(level.getBlockEntity(core) instanceof BlockEntityDishControl console))
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            Player player = context.getPlayer();
            if (player != null) {
                ItemReactorSensor.announce(
                        player,
                        Component.translatable("chat.hbmspace.reactorSensor.stardarLinked"));
            }
            console.tryLink(sensor);
        }
        ItemReactorSensor.confirm(context);
        return InteractionResult.SUCCESS;
    }
}
