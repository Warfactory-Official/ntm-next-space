// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class PlacementEscapePayload extends ThreadedPayload {

    public static final Type<PlacementEscapePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "placement_escape"));
    public static final StreamCodec<ByteBuf, PlacementEscapePayload> STREAM_CODEC =
            streamCodec(
                    buf ->
                            new PlacementEscapePayload(
                                    Identifier.parse(ByteBufCodecs.STRING_UTF8.decode(buf)),
                                    buf.readLong()));

    private final Identifier dimension;
    private final long core;

    public PlacementEscapePayload(Identifier dimension, long core) {
        this.dimension = dimension;
        this.core = core;
    }

    public static void handle(PlacementEscapePayload payload, IPayloadHandlerContext context) {
        Player player = context.playerOrNull();
        if (player != null && player.level().dimension().identifier().equals(payload.dimension)) {
            ((SpacePlacementEscape) player)
                    .hbmspace$grantPlacementEscape(player.level().dimension(), payload.core);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufCodecs.STRING_UTF8.encode(buf, dimension.toString());
        buf.writeLong(core);
    }

    @Override
    public Type<PlacementEscapePayload> type() {
        return TYPE;
    }
}
