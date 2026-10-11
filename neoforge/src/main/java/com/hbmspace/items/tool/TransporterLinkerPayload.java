// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.tool;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class TransporterLinkerPayload extends ThreadedPayload {

    public static final Type<TransporterLinkerPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "transporter_linker"));
    public static final StreamCodec<ByteBuf, TransporterLinkerPayload> STREAM_CODEC =
            streamCodec(TransporterLinkerPayload::decode);

    private final TransporterInfo from;
    private final List<TransporterInfo> transporters;

    public TransporterLinkerPayload(TransporterInfo from, List<TransporterInfo> transporters) {
        this.from = from;
        this.transporters = transporters;
    }

    private static TransporterLinkerPayload decode(ByteBuf buf) {
        TransporterInfo from = TransporterInfo.read(buf);
        int count = buf.readInt();
        List<TransporterInfo> transporters = new ArrayList<>(count);
        for (int i = 0; i < count; i++) transporters.add(TransporterInfo.read(buf));
        return new TransporterLinkerPayload(from, transporters);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        from.write(buf);
        buf.writeInt(transporters.size());
        for (TransporterInfo info : transporters) info.write(buf);
    }

    public static void handle(TransporterLinkerPayload payload, IPayloadHandlerContext context) {
        ItemTransporterLinker.OPEN_SCREEN.accept(payload.from, payload.transporters);
    }

    @Override
    public Type<TransporterLinkerPayload> type() {
        return TYPE;
    }
}
