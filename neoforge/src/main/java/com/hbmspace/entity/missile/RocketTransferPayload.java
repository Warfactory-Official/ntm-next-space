// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.missile;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.orbit.OrbitalStation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class RocketTransferPayload extends ThreadedPayload {

    public static final Type<RocketTransferPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "rocket_transfer"));
    public static final StreamCodec<ByteBuf, RocketTransferPayload> STREAM_CODEC =
            streamCodec(RocketTransferPayload::decode);

    private final byte[] data;

    private RocketTransferPayload(byte[] data) {
        this.data = data;
    }

    public static RocketTransferPayload of(OrbitalStation station) {
        ByteBuf buf = Unpooled.buffer();
        try {
            station.serialize(buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return new RocketTransferPayload(data);
        } finally {
            buf.release();
        }
    }

    private static RocketTransferPayload decode(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new RocketTransferPayload(data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(data);
    }

    @Override
    protected int bodyCapacity() {
        return data.length;
    }

    public static void handle(RocketTransferPayload payload, IPayloadHandlerContext context) {
        OrbitalStation.clientStation =
                OrbitalStation.deserialize(Unpooled.wrappedBuffer(payload.data));
    }

    @Override
    public Type<RocketTransferPayload> type() {
        return TYPE;
    }
}
