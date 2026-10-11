// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blockentity.machine;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class StardarHeightmapPayload extends ThreadedPayload {

    public static final Type<StardarHeightmapPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "stardar_heightmap"));
    public static final StreamCodec<ByteBuf, StardarHeightmapPayload> STREAM_CODEC =
            streamCodec(StardarHeightmapPayload::decode);

    private final BlockPos pos;
    private final int @Nullable [] heights;

    public StardarHeightmapPayload(BlockPos pos, int @Nullable [] heights) {
        this.pos = pos;
        this.heights = heights;
    }

    private static StardarHeightmapPayload decode(ByteBuf buf) {
        BlockPos pos = BlockPos.of(buf.readLong());
        int count = buf.readInt();
        if (count == 0) return new StardarHeightmapPayload(pos, null);
        int[] heights = new int[count];
        for (int i = 0; i < count; i++) heights[i] = buf.readShort();
        return new StardarHeightmapPayload(pos, heights);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.asLong());
        buf.writeInt(heights == null ? 0 : heights.length);
        if (heights != null) for (int h : heights) buf.writeShort(h);
    }

    @Override
    protected int bodyCapacity() {
        return 12 + (heights == null ? 0 : heights.length * 2);
    }

    public static void handle(StardarHeightmapPayload payload, IPayloadHandlerContext context) {
        if (context.player().level().getBlockEntity(payload.pos)
                instanceof BlockEntityMachineStardar stardar) {
            stardar.receiveHeightmap(payload.heights);
        }
    }

    @Override
    public Type<StardarHeightmapPayload> type() {
        return TYPE;
    }
}
