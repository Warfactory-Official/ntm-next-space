// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.duna;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class DunaStormSyncPayload extends ThreadedPayload {

    public static final Type<DunaStormSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "duna_dust_storm"));
    public static final StreamCodec<ByteBuf, DunaStormSyncPayload> STREAM_CODEC =
            streamCodec(buf -> new DunaStormSyncPayload(buf.readFloat()));

    private final float intensity;

    DunaStormSyncPayload(float intensity) {
        this.intensity = intensity;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(intensity);
    }

    @Override
    protected int bodyCapacity() {
        return Float.BYTES;
    }

    public static void handle(DunaStormSyncPayload payload, IPayloadHandlerContext context) {
        DunaDustStorm.clientIntensity = payload.intensity;
    }

    @Override
    public Type<DunaStormSyncPayload> type() {
        return TYPE;
    }
}
