// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbmspace.HbmSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public final class SpaceLivingSyncPayload extends ThreadedPayload {

    public static final Type<SpaceLivingSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "living_sync"));
    public static final StreamCodec<ByteBuf, SpaceLivingSyncPayload> STREAM_CODEC =
            streamCodec(
                    buf ->
                            new SpaceLivingSyncPayload(
                                    buf.readInt(), buf.readBoolean(), buf.readBoolean()));

    private final int oxygen;
    private final boolean gravity;
    private final boolean tankInUse;

    public SpaceLivingSyncPayload(int oxygen, boolean gravity, boolean tankInUse) {
        this.oxygen = oxygen;
        this.gravity = gravity;
        this.tankInUse = tankInUse;
    }

    public static void handle(SpaceLivingSyncPayload payload, IPayloadHandlerContext ctx) {
        Player player = ctx.playerOrNull();
        if (player == null) return;
        SpaceLivingProps.receive(player, payload.oxygen, payload.gravity, payload.tankInUse);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(oxygen);
        buf.writeBoolean(gravity);
        buf.writeBoolean(tankInUse);
    }

    @Override
    public @NotNull Type<SpaceLivingSyncPayload> type() {
        return TYPE;
    }
}
