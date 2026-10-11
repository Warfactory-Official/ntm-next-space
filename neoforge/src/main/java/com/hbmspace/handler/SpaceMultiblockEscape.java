// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.blockentity.Synced;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.blocks.multiblock.MultiblockSurface;
import com.hbm.packet.PacketWire;
import com.hbm.packet.PayloadHandlerThread;
import com.hbm.platform.NeoForgeNetwork;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.Nullable;

public final class SpaceMultiblockEscape {

    private SpaceMultiblockEscape() {}

    public static void register() {
        NeoForgeNetwork.clientbound.add(
                new NeoForgeNetwork.Registration<>(
                        PlacementEscapePayload.TYPE,
                        PlacementEscapePayload.STREAM_CODEC,
                        PlacementEscapePayload::handle,
                        PayloadHandlerThread.MAIN));
    }

    public static long ownerAt(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return MultiblockSurface.NO_CORE;
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk == null) return MultiblockSurface.NO_CORE_TRUNCATED;
        BlockState state = chunk.getBlockState(pos);
        if (MultiblockSurface.foldedCore(state) != null) return pos.asLong();
        if (!MultiblockSurface.isFoldedCell(state)) return MultiblockSurface.NO_CORE;
        if (level instanceof ServerLevel server) {
            return MultiblockSurface.recordedCorePacked(server, pos.getX(), pos.getY(), pos.getZ());
        }
        BlockPos core = MultiblockSurface.clientCoreOf(level, pos);
        return core == null ? MultiblockSurface.NO_CORE_TRUNCATED : core.asLong();
    }

    public static boolean isCurrentState(Level level, BlockPos pos, BlockState state) {
        LevelChunk chunk = loadedChunk(level, pos);
        return chunk != null && chunk.getBlockState(pos) == state;
    }

    private static @Nullable LevelChunk loadedChunk(Level level, BlockPos pos) {
        return level instanceof ServerLevel server
                ? BlockMultiblockCore.readableChunk(server, pos.getX(), pos.getZ())
                : (LevelChunk)
                        level.getChunkSource()
                                .getChunk(
                                        pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
    }

    public static void placed(
            Level level, BlockPos core, BlockMultiblockCore block, LivingEntity placer) {
        if (!(level instanceof ServerLevel)
                || !(placer instanceof Player player)
                || !level.getBlockState(core).is(block)) return;
        BlockPos feet = BlockPos.containing(player.getX(), player.getY() + 0.1D, player.getZ());
        long packed = core.asLong();
        if (ownerAt(level, feet) != packed && ownerAt(level, feet.above()) != packed) return;
        ((SpacePlacementEscape) player).hbmspace$grantPlacementEscape(level.dimension(), packed);
        if (!(player instanceof ServerPlayer recipient)) return;

        Long2ObjectOpenHashMap<ShortOpenHashSet> changes = new Long2ObjectOpenHashMap<>();
        for (BlockPos pos : block.rigidStructureBlocks(level, core)) {
            changes.computeIfAbsent(
                            SectionPos.asLong(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4),
                            ignored -> new ShortOpenHashSet())
                    .add(SectionPos.sectionRelativePos(pos));
        }
        for (var entry : changes.long2ObjectEntrySet()) {
            SectionPos section = SectionPos.of(entry.getLongKey());
            LevelChunk chunk = loadedChunk(level, section.origin());
            if (chunk == null) continue;
            recipient.connection.send(
                    new ClientboundSectionBlocksUpdatePacket(
                            section,
                            entry.getValue(),
                            chunk.getSection(level.getSectionIndex(section.minBlockY()))));
        }
        BlockEntity entity = level.getBlockEntity(core);
        if (entity instanceof Synced synced) {
            synced.syncTo(recipient);
        } else if (entity != null) {
            var initial = entity.getUpdatePacket();
            if (initial != null) recipient.connection.send(initial);
        }
        PacketWire.sendTo(
                new PlacementEscapePayload(level.dimension().identifier(), packed), recipient);
    }
}
