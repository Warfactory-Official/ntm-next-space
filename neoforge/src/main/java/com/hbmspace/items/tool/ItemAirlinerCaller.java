// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.tool;

import com.hbm.entity.logic.EntityBomber;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ItemAirlinerCaller extends Item {

    public ItemAirlinerCaller(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = player.pick(500D, 1F, false);
        if (!(hit instanceof BlockHitResult blockHit)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            double x = blockHit.getBlockPos().getX();
            double y = blockHit.getBlockPos().getY();
            double z = blockHit.getBlockPos().getZ();
            level.addFreshEntity(EntityBomber.variant(level, x, y + 16D, z, 75, 125, 90000, 8, 9));
            player.sendSystemMessage(
                    Component.translatable("desc.item.bombCaller.reroutedCivilianTraffic"));
            EntitySounds.play(
                    player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.TECH_BLEEP.get(),
                    SoundSource.PLAYERS,
                    1F,
                    1F);
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        adder.accept(Component.translatable("desc.item.bombCaller.aimClickToCall"));
        adder.accept(Component.translatable("desc.item.bombCaller.civilianAirliner"));
    }
}
