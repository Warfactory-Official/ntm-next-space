// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class NukeShockSyncPayload extends ThreadedPayload {

    public static final Type<NukeShockSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "nuke_shocks"));
    public static final StreamCodec<ByteBuf, NukeShockSyncPayload> STREAM_CODEC =
            streamCodec(NukeShockSyncPayload::decode);

    private final byte[] data;

    private NukeShockSyncPayload(byte[] data) {
        this.data = data;
    }

    static NukeShockSyncPayload of(long currentTime) {
        ByteBuf buf = Unpooled.buffer();
        CelestialNukeShockHandler.writeSync(buf, currentTime);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new NukeShockSyncPayload(data);
    }

    private static NukeShockSyncPayload decode(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new NukeShockSyncPayload(data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(data);
    }

    @Override
    protected int bodyCapacity() {
        return data.length;
    }

    public static void handle(NukeShockSyncPayload payload, IPayloadHandlerContext context) {
        CelestialNukeShockHandler.readSync(Unpooled.wrappedBuffer(payload.data));
    }

    @Override
    public Type<NukeShockSyncPayload> type() {
        return TYPE;
    }
}
