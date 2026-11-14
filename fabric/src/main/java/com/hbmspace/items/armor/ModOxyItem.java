/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.items.ModDataComponents;
import com.hbm.items.armor.ArmorModItem;
import com.hbm.items.machine.FluidContainerItem;
import com.hbm.sound.AudioSystem;
import com.hbm.sound.SoundHandle;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import com.hbmspace.sound.ModSoundsSpace;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public class ModOxyItem extends ArmorModItem implements FluidContainerItem {

    private final int maxFuel;
    private final int rate;
    private final int consumption;

    private @Nullable SoundHandle audioBreathing;

    public ModOxyItem(Properties properties, int maxFuel, int rate, int consumption) {
        super(properties, ArmorModHandler.PLATE_ONLY, true, false, false, false);
        this.maxFuel = maxFuel;
        this.rate = rate;
        this.consumption = consumption;
    }

    private static Fluid fuel() {
        return NTMFluids.OXYGEN;
    }

    private Component gauge(ItemStack stack) {
        return Component.translatable(
                "item.hbm.jetpack.fuel",
                NTMFluidProperties.getDisplayName(fuel()),
                getFuel(stack),
                maxFuel);
    }

    @Override
    protected void addSpecificTooltip(ItemStack stack, Consumer<Component> adder) {
        adder.accept(gauge(stack).copy().withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, display, adder, flag);
        adder.accept(Component.translatable("armor.mustSeal").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void addDescription(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(
                Component.literal("  ")
                        .append(stack.getHoverName())
                        .append(" (")
                        .append(gauge(stack))
                        .append(")")
                        .withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean ticksClient() {
        return true;
    }

    @Override
    public void modUpdateClient(LivingEntity entity, ItemStack armor) {
        if (!(entity instanceof Player player) || !player.isLocalPlayer()) return;

        if (SpaceLivingProps.isTankInUse(player) && !player.isCreative()) {
            if (audioBreathing == null || !audioBreathing.isPlaying()) {
                audioBreathing =
                        AudioSystem.getLoopedSound(
                                ModSoundsSpace.PLAYER_PLSS_BREATHING.get(),
                                SoundSource.PLAYERS,
                                (float) player.getX(),
                                (float) player.getY(),
                                (float) player.getZ(),
                                0.1F,
                                5.0F,
                                1.0F,
                                10);
                if (audioBreathing == null) return;
                audioBreathing.follow(player);
                audioBreathing.start();
            }

            audioBreathing.refresh();
        } else if (audioBreathing != null) {
            audioBreathing.stop();
            audioBreathing = null;
        }
    }

    public boolean attemptBreathing(
            LivingEntity entity,
            ItemStack stack,
            @Nullable CBT_Atmosphere atmosphere,
            boolean forceConsume) {
        if (!forceConsume && ChunkAtmosphereManager.proxy.canBreathe(atmosphere)) {
            SpaceLivingProps.setTankInUse(entity, false);
            return true;
        }

        if (entity.tickCount % rate == 0) setFuel(stack, Math.max(getFuel(stack) - consumption, 0));

        boolean hasFuel = getFuel(stack) > 0;

        SpaceLivingProps.setTankInUse(entity, hasFuel);

        return hasFuel;
    }

    @Override
    public boolean canStore(Fluid type) {
        return false;
    }

    @Override
    public boolean machineFillable() {
        return true;
    }

    @Override
    public int capacity(ItemStack stack) {
        return maxFuel;
    }

    @Override
    public FluidStackNTM getContent(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FLUID_CONTENT.get(), EMPTY_CONTENT);
    }

    @Override
    public void setContent(ItemStack stack, FluidStackNTM content) {
        if (content == null || content.type() == Fluids.EMPTY || content.amount() <= 0) {
            stack.remove(ModDataComponents.FLUID_CONTENT.get());
            return;
        }
        stack.set(
                ModDataComponents.FLUID_CONTENT.get(),
                new FluidStackNTM(
                        content.type(), Math.min(content.amount(), maxFuel), content.pressure()));
    }

    @Override
    public int fill(ItemStack stack, Fluid type, int amount, int pressure) {
        if (type != fuel() || amount <= 0) return 0;
        FluidStackNTM held = getContent(stack);
        if (held.type() != Fluids.EMPTY && held.pressure() != pressure) return 0;
        int have = held.type() == Fluids.EMPTY ? 0 : (int) held.amount();
        int accepted = Math.min(amount, maxFuel - have);
        if (accepted <= 0) return 0;
        setContent(stack, new FluidStackNTM(type, have + accepted, pressure));
        return accepted;
    }

    @Override
    public int drain(ItemStack stack, Fluid type, int amount, int pressure) {
        return 0;
    }

    public int getFuel(ItemStack stack) {
        FluidStackNTM held = getContent(stack);
        return held.type() == fuel() ? (int) held.amount() : 0;
    }

    public void setFuel(ItemStack stack, int fuel) {
        setContent(stack, new FluidStackNTM(fuel(), fuel));
    }

    public int getMaxFuel() {
        return maxFuel;
    }
}
