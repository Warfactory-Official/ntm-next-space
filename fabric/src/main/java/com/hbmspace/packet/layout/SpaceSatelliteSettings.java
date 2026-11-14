/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.packet.layout;

import com.hbm.packet.PayloadHandlerContext;
import com.hbm.packet.codec.PacketValues;
import com.hbm.packet.threading.EncodedPayload;
import com.hbmspace.saveddata.satellites.SatelliteSettingsControl;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.server.level.ServerPlayer;

import static com.hbm.lib.internal.UnsafeHolder.BA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.U;
import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

public final class SpaceSatelliteSettings {
    public static final int VARIANT = 0;
    public static final int VARIANT_BITS = 5;
    public static final int MIN_BODY_BYTES = 32;
    public static final int MAX_BODY_BYTES = 32767;

    private SpaceSatelliteSettings() {}

    public static EncodedPayload encode(
            int containerId,
            String owner,
            float altitude,
            float inclination,
            float phaseOffset,
            boolean blinking,
            float blinkPeriod,
            int red,
            int green,
            int blue) {
        int ownerBytes = PacketValues.utf8Length(owner, 32767, 32767);
        if (((long) (containerId - (1)) & 0xFFFFFFFFL) > 99L)
            throw new IllegalArgumentException("Invalid containerId");
        long bodyBytesWide = 31L + (PacketValues.varIntSize(ownerBytes) + ownerBytes);
        if (bodyBytesWide > 32767) throw new EncoderException("Packet exceeds frame limit");
        int bodyBytes = (int) bodyBytesWide;
        EncodedPayload payload =
                EncodedPayload.allocate(
                        PacketFamilySpaceControl.TYPE,
                        bodyBytes,
                        MAX_BODY_BYTES,
                        false,
                        false,
                        false);
        try {
            ByteBuf encodedBuffer = payload.buffer();
            int bufferStart = encodedBuffer.writerIndex();
            Object storageBase;
            long storageAddress;
            if (encodedBuffer.hasArray()) {
                storageBase = encodedBuffer.array();
                storageAddress = BA_BASE + encodedBuffer.arrayOffset() + bufferStart;
            } else {
                storageBase = null;
                storageAddress = encodedBuffer.memoryAddress() + bufferStart;
            }
            {
                long containerIdBits = ((long) containerId - 1L);
                U.putShort(storageBase, storageAddress, (short) ((containerIdBits << 5)));
            }
            long ownerEnd =
                    PacketValues.putUtf8(storageBase, storageAddress + 2L, owner, ownerBytes);
            {
                long altitudeBits = floatToRawIntBits(altitude);
                long inclinationBits = floatToRawIntBits(inclination);
                long phaseOffsetBits = floatToRawIntBits(phaseOffset);
                long blinkingBits = blinking ? 1L : 0L;
                long blinkPeriodBits = floatToRawIntBits(blinkPeriod);
                long redBits = red;
                long greenBits = green;
                long blueBits = blue;
                U.putLong(
                        storageBase,
                        ownerEnd,
                        (altitudeBits & 0xFFFFFFFFL) | (inclinationBits << 32));
                U.putLong(
                        storageBase,
                        ownerEnd + 8L,
                        (phaseOffsetBits & 0xFFFFFFFFL)
                                | (blinkingBits << 32)
                                | (blinkPeriodBits << 33));
                U.putLong(
                        storageBase,
                        ownerEnd + 16L,
                        ((blinkPeriodBits >>> 31) & 1L)
                                | ((redBits & 0xFFFFFFFFL) << 1)
                                | (greenBits << 33));
                U.putLong(
                        storageBase,
                        ownerEnd + 21L,
                        ((greenBits >>> 7) & 0x1FFFFFFL) | ((blueBits & 0xFFFFFFFFL) << 25));
            }
            encodedBuffer.writerIndex(bufferStart + bodyBytes);
            return payload;
        } catch (Throwable encodingFailure) {
            payload.release();
            throw encodingFailure;
        }
    }

    static void receiveFields(
            ByteBuf decodedBuffer,
            PayloadHandlerContext context,
            Object storageBase,
            long storageAddress,
            int bufferStart,
            int bodyBytes) {
        if (bodyBytes < MIN_BODY_BYTES) throw new DecoderException("Invalid packet length");
        long bodyLimit = storageAddress + bodyBytes;
        long containerIdWord = (U.getShort(storageBase, storageAddress) & 65535L);
        long containerIdBits = ((containerIdWord >>> 5) & 127L);
        if (containerIdBits > 99L) throw new DecoderException("Invalid containerId");
        int containerId = (int) (containerIdBits + 1L);
        if ((containerIdWord >>> 12) != 0) throw new DecoderException("Nonzero packet padding");
        int ownerBytes = PacketValues.getVarInt(storageBase, storageAddress + 2L, bodyLimit);
        long ownerAddress = storageAddress + 2L + PacketValues.varIntSize(ownerBytes);
        if ((ownerBytes & 0xFFFFFFFFL) > 32767 || ownerBytes > bodyLimit - ownerAddress)
            throw new DecoderException("Invalid text length");
        String owner = PacketValues.getUtf8(storageBase, ownerAddress, ownerBytes, 32767);
        long ownerEnd = ownerAddress + ownerBytes;
        if (bodyLimit - ownerEnd != 29) throw new DecoderException("Invalid packet tail length");
        long altitudeToInclinationWord = U.getLong(storageBase, ownerEnd);
        long phaseOffsetToBlinkPeriodWord = U.getLong(storageBase, ownerEnd + 8L);
        long blinkPeriodToGreenWord = U.getLong(storageBase, ownerEnd + 16L);
        long greenToBlueWord = U.getLong(storageBase, ownerEnd + 21L);
        float altitude = intBitsToFloat((int) altitudeToInclinationWord);
        float inclination = intBitsToFloat((int) (altitudeToInclinationWord >>> 32));
        float phaseOffset = intBitsToFloat((int) phaseOffsetToBlinkPeriodWord);
        boolean blinking = ((phaseOffsetToBlinkPeriodWord >>> 32) & 1L) != 0;
        float blinkPeriod =
                intBitsToFloat(
                        (int)
                                ((phaseOffsetToBlinkPeriodWord >>> 33)
                                        | (blinkPeriodToGreenWord << 31)));
        int red = (int) (blinkPeriodToGreenWord >>> 1);
        int green = (int) ((blinkPeriodToGreenWord >>> 33) | ((greenToBlueWord >>> 24) << 31));
        int blue = (int) (greenToBlueWord >>> 25);
        if ((greenToBlueWord >>> 57) != 0) throw new DecoderException("Nonzero packet padding");
        if (!(context.player() instanceof ServerPlayer player)) return;
        SatelliteSettingsControl.apply(
                player,
                containerId,
                owner,
                altitude,
                inclination,
                phaseOffset,
                blinking,
                blinkPeriod,
                red,
                green,
                blue);
    }
}
