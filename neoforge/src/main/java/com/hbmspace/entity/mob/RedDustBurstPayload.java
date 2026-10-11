// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.entity.mob;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.client.particle.RedDustBurst;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class RedDustBurstPayload extends ThreadedPayload {

    public static final Type<RedDustBurstPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "red_dust_burst"));
    public static final StreamCodec<ByteBuf, RedDustBurstPayload> STREAM_CODEC =
            streamCodec(RedDustBurstPayload::decode);

    public final double x, y, z;
    public final int count;
    public final double motion;

    public RedDustBurstPayload(double x, double y, double z, int count, double motion) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.count = count;
        this.motion = motion;
    }

    private static RedDustBurstPayload decode(ByteBuf buf) {
        return new RedDustBurstPayload(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readByte(),
                buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeByte(count);
        buf.writeDouble(motion);
    }

    @Override
    protected int bodyCapacity() {
        return 3 * 8 + 1 + 8;
    }

    public static void handle(RedDustBurstPayload payload, IPayloadHandlerContext context) {
        RedDustBurst.spawn(payload);
    }

    @Override
    public Type<RedDustBurstPayload> type() {
        return TYPE;
    }
}
