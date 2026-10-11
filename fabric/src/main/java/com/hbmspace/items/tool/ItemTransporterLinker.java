// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.tool;

import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.packet.PacketWire;
import com.hbmspace.blockentity.machine.BlockEntityTransporterBase;
import com.hbmspace.items.StationItemsSpace;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ItemTransporterLinker extends Item {

    public static BiConsumer<TransporterInfo, List<TransporterInfo>> OPEN_SCREEN =
            (from, transporters) -> {};

    public ItemTransporterLinker(Properties properties) {
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
                        instanceof BlockEntityTransporterBase transporter)) {
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
                PacketWire.sendTo(
                        new TransporterLinkerPayload(
                                TransporterInfo.from(level.dimension(), transporter),
                                getTransporters(stack, server.getServer())),
                        serverPlayer);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static List<GlobalPos> links(ItemStack stack) {
        return stack.getOrDefault(StationItemsSpace.TRANSPORTER_LINKS.get(), List.of());
    }

    private static @Nullable BlockEntityTransporterBase resolve(
            MinecraftServer server, GlobalPos pos) {
        ServerLevel level = server.getLevel(pos.dimension());
        if (level == null || !isLoaded(level, pos.pos())) return null;
        return level.getBlockEntity(pos.pos()) instanceof BlockEntityTransporterBase transporter
                ? transporter
                : null;
    }

    private static boolean isLoaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    private static void addTransporter(
            ItemStack stack, ServerLevel level, BlockEntityTransporterBase transporter) {
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
            BlockEntityTransporterBase transporter = resolve(server, pos);
            if (transporter != null)
                transporters.add(TransporterInfo.from(pos.dimension(), transporter));
        }
        return transporters;
    }
}
