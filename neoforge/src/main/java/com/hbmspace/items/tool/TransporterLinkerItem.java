/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.items.tool;

import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbmspace.blockentity.machine.TransporterBaseBlockEntity;
import com.hbmspace.inventory.container.TransporterLinkerMenu;
import com.hbmspace.items.StationItemsSpace;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class TransporterLinkerItem extends Item {

    public TransporterLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        adder.accept(Component.translatable("desc.item.transporterLinker.save"));
        adder.accept(Component.translatable("desc.item.transporterLinker.link"));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos core = MultiblockSurface.coreOfAny(level, ctx.getClickedPos());
        if (core == null
                || !(level.getBlockEntity(core)
                        instanceof TransporterBaseBlockEntity transporter)) {
            return InteractionResult.PASS;
        }

        Player player = ctx.getPlayer();
        if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel server) {
            ItemStack stack = ctx.getItemInHand();
            if (player.isShiftKeyDown()) {
                addTransporter(stack, server, transporter);
                player.sendSystemMessage(
                        Component.translatable("desc.item.transporterLinker.added"));
            } else {
                var opening =
                        new TransporterLinkerMenu.Opening(
                                ctx.getHand(),
                                TransporterInfo.from(level.dimension(), transporter),
                                getTransporters(stack, server.getServer()));
                final MenuProvider provider =
                        new SimpleMenuProvider(
                                (containerId, inventory, opener) ->
                                        new TransporterLinkerMenu(
                                                containerId, inventory, transporter, opening),
                                Component.empty());
                final StreamCodec<? super RegistryFriendlyByteBuf, TransporterLinkerMenu.Opening>
                        codec = TransporterLinkerMenu.Opening.STREAM_CODEC;
                serverPlayer.openMenu(provider, buf -> codec.encode(buf, opening));
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static List<GlobalPos> links(ItemStack stack) {
        return stack.getOrDefault(StationItemsSpace.TRANSPORTER_LINKS.get(), List.of());
    }

    private static @Nullable TransporterBaseBlockEntity resolve(
            MinecraftServer server, GlobalPos pos) {
        ServerLevel level = server.getLevel(pos.dimension());
        if (level == null || !isLoaded(level, pos.pos())) return null;
        return level.getBlockEntity(pos.pos()) instanceof TransporterBaseBlockEntity transporter
                ? transporter
                : null;
    }

    private static boolean isLoaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    private static void addTransporter(
            ItemStack stack, ServerLevel level, TransporterBaseBlockEntity transporter) {
        MinecraftServer server = level.getServer();
        List<GlobalPos> kept = new ArrayList<>();
        for (GlobalPos pos : links(stack)) {
            ServerLevel at = server.getLevel(pos.dimension());
            if (at != null && isLoaded(at, pos.pos()) && resolve(server, pos) == null) continue;
            kept.add(pos);
        }

        GlobalPos added = GlobalPos.of(level.dimension(), transporter.getBlockPos());
        if (!kept.contains(added)) kept.add(added);
        stack.set(StationItemsSpace.TRANSPORTER_LINKS.get(), List.copyOf(kept));
    }

    private static List<TransporterInfo> getTransporters(ItemStack stack, MinecraftServer server) {
        List<TransporterInfo> transporters = new ArrayList<>();
        for (GlobalPos pos : links(stack)) {
            TransporterBaseBlockEntity transporter = resolve(server, pos);
            if (transporter != null)
                transporters.add(TransporterInfo.from(pos.dimension(), transporter));
        }
        return transporters;
    }
}
