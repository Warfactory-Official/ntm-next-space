// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.client.particle.SpinlaunchBurst;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class SpinlaunchPayload extends ThreadedPayload {

    public static final Type<SpinlaunchPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "spinlaunch"));
    public static final StreamCodec<ByteBuf, SpinlaunchPayload> STREAM_CODEC =
            streamCodec(SpinlaunchPayload::decode);

    public final double x, y, z;
    public final int count;
    public final float scale;
    public final double motionX, motionY, motionZ;
    public final int maxAge;

    public SpinlaunchPayload(
            double x,
            double y,
            double z,
            int count,
            float scale,
            double motionX,
            double motionY,
            double motionZ,
            int maxAge) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.count = count;
        this.scale = scale;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.maxAge = maxAge;
    }

    private static SpinlaunchPayload decode(ByteBuf buf) {
        return new SpinlaunchPayload(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readByte(),
                buf.readFloat(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readShort());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeByte(count);
        buf.writeFloat(scale);
        buf.writeDouble(motionX);
        buf.writeDouble(motionY);
        buf.writeDouble(motionZ);
        buf.writeShort(maxAge);
    }

    @Override
    protected int bodyCapacity() {
        return 3 * 8 + 1 + 4 + 3 * 8 + 2;
    }

    public static void handle(SpinlaunchPayload payload, IPayloadHandlerContext context) {
        SpinlaunchBurst.spawn(payload);
    }

    @Override
    public Type<SpinlaunchPayload> type() {
        return TYPE;
    }
}
