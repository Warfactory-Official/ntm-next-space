// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.main;

import com.hbm.registration.FabricRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class ChunkLoaderManager {
    public static final RegistryHandle<TicketType> TICKET =
            FabricRegistrar.registerTicketType(
                    HbmSpace.MOD_ID + ":chunk_loader",
                    () ->
                            new TicketType(
                                    TicketType.NO_TIMEOUT,
                                    TicketType.FLAG_PERSIST
                                            | TicketType.FLAG_LOADING
                                            | TicketType.FLAG_SIMULATION
                                            | TicketType.FLAG_KEEP_DIMENSION_ACTIVE));
    private static final int ENTITY_TICKING_RADIUS = 2;

    private ChunkLoaderManager() {}

    public static void forceChunk(ServerLevel level, BlockPos owner) {
        ChunkPos chunk = ChunkPos.containing(owner);
        Owners owners = level.getDataStorage().computeIfAbsent(Owners.TYPE);
        owners.forcedBy.put(owner.immutable(), chunk.pack());
        owners.setDirty();

        level.getChunkSource().addTicketWithRadius(TICKET.get(), chunk, ENTITY_TICKING_RADIUS);
    }

    public static void unforceChunk(ServerLevel level, BlockPos owner) {
        Owners owners = level.getDataStorage().computeIfAbsent(Owners.TYPE);
        if (!owners.forcedBy.containsKey(owner)) return;
        long chunk = owners.forcedBy.removeLong(owner);
        owners.setDirty();

        if (!owners.forcedBy.containsValue(chunk)) {
            level.getChunkSource()
                    .removeTicketWithRadius(
                            TICKET.get(), ChunkPos.unpack(chunk), ENTITY_TICKING_RADIUS);
        }
    }

    public static boolean isForcedBy(ServerLevel level, BlockPos owner) {
        return level.getDataStorage().computeIfAbsent(Owners.TYPE).forcedBy.containsKey(owner);
    }

    private static final class Owners extends SavedData {

        static final SavedDataType<Owners> TYPE =
                new SavedDataType<>(
                        Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "chunk_loader"),
                        Owners::new,
                        CompoundTag.CODEC.xmap(Owners::load, Owners::save),
                        null);

        final Object2LongLinkedOpenHashMap<BlockPos> forcedBy =
                new Object2LongLinkedOpenHashMap<>();

        private static Owners load(CompoundTag nbt) {
            Owners owners = new Owners();
            ListTag list = nbt.getListOrEmpty("chunks");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompoundOrEmpty(i);
                owners.forcedBy.put(
                        BlockPos.of(tag.getLongOr("pos", 0L)), tag.getLongOr("chunk", 0L));
            }
            return owners;
        }

        private CompoundTag save() {
            ListTag list = new ListTag();
            for (var entry : forcedBy.object2LongEntrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putLong("pos", entry.getKey().asLong());
                tag.putLong("chunk", entry.getLongValue());
                list.add(tag);
            }
            CompoundTag nbt = new CompoundTag();
            nbt.put("chunks", list);
            return nbt;
        }
    }
}
