// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbm.packet.IPayloadHandlerContext;
import com.hbm.packet.threading.ThreadedPayload;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbmspace.HbmSpace;
import com.hbmspace.items.ModDataComponentsSpace;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class SatelliteSettingsPayload extends ThreadedPayload {

    public static final Type<SatelliteSettingsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "satellite_settings"));
    public static final StreamCodec<ByteBuf, SatelliteSettingsPayload> STREAM_CODEC =
            streamCodec(
                    buf ->
                            new SatelliteSettingsPayload(
                                    Utf8String.read(buf, Short.MAX_VALUE),
                                    buf.readFloat(),
                                    buf.readFloat(),
                                    buf.readFloat(),
                                    buf.readBoolean(),
                                    buf.readFloat(),
                                    buf.readInt(),
                                    buf.readInt(),
                                    buf.readInt()));

    private final String owner;
    private final float altitude;
    private final float inclination;
    private final float phaseOffset;
    private final boolean blinking;
    private final float blinkPeriod;
    private final int red;
    private final int green;
    private final int blue;

    public SatelliteSettingsPayload(
            String owner,
            float altitude,
            float inclination,
            float phaseOffset,
            boolean blinking,
            float blinkPeriod,
            int red,
            int green,
            int blue) {
        this.owner = owner;
        this.altitude = altitude;
        this.inclination = inclination;
        this.phaseOffset = phaseOffset;
        this.blinking = blinking;
        this.blinkPeriod = blinkPeriod;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        Utf8String.write(buf, owner, Short.MAX_VALUE);
        buf.writeFloat(altitude);
        buf.writeFloat(inclination);
        buf.writeFloat(phaseOffset);
        buf.writeBoolean(blinking);
        buf.writeFloat(blinkPeriod);
        buf.writeInt(red);
        buf.writeInt(green);
        buf.writeInt(blue);
    }

    public static void handle(SatelliteSettingsPayload payload, IPayloadHandlerContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        ItemStack stack = player.getMainHandItem();
        SatelliteType type = SatelliteType.fromStack(stack);
        if (type == null || !SatellitesSpace.isSpace(type)) return;

        SatelliteOrbit old =
                stack.getOrDefault(
                        ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                        SatellitesSpace.defaults(type));
        String owner =
                payload.owner.equals(old.owner()) ? old.owner() : player.getName().getString();
        stack.set(
                ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                new SatelliteOrbit(
                        Mth.clamp(
                                payload.inclination,
                                SatelliteOrbit.MIN_INCLINATION,
                                SatelliteOrbit.MAX_INCLINATION),
                        Mth.clamp(
                                payload.altitude,
                                SatelliteOrbit.MIN_ALTITUDE_KM,
                                SatelliteOrbit.MAX_ALTITUDE_KM),
                        payload.phaseOffset,
                        payload.blinking,
                        payload.blinkPeriod,
                        owner,
                        Mth.clamp(payload.red, 0, 255) / 255F,
                        Mth.clamp(payload.green, 0, 255) / 255F,
                        Mth.clamp(payload.blue, 0, 255) / 255F));
    }

    @Override
    public Type<SatelliteSettingsPayload> type() {
        return TYPE;
    }
}
