// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.machine.IFluidContainerItem;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import com.hbm.util.TooltipStyle;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class ItemLoxTank extends Item {

    private static final int CHARGE = 1000;

    public ItemLoxTank(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        for (Component line :
                TooltipStyle.lines(Component.translatable(getDescriptionId() + ".desc"))) {
            adder.accept(line);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!ArmorModHandler.hasMods(helmet)) return InteractionResult.PASS;
        ItemStack tank = ArmorModHandler.pryMod(helmet, ArmorModHandler.PLATE_ONLY);
        if (!(tank.getItem() instanceof IFluidContainerItem container)
                || !container.machineFillable()) {
            return InteractionResult.PASS;
        }
        if (container.fill(tank, NTMFluids.OXYGEN, CHARGE, 0) <= 0) return InteractionResult.PASS;
        ArmorModHandler.applyMod(helmet, tank);

        player.getItemInHand(hand).consume(1, player);
        EntitySounds.play(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.JETPACK_TANK.get(),
                SoundSource.PLAYERS,
                1F,
                1F);
        return InteractionResult.SUCCESS;
    }
}
