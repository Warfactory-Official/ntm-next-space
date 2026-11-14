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
import com.hbm.packet.threading.EncodedPayload;
import com.hbmspace.client.particle.SpinlaunchBurst;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import static com.hbm.lib.internal.UnsafeHolder.BA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.U;
import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

public final class SpaceSpinlaunch {
    public static final int VARIANT = 1;
    public static final int VARIANT_BITS = 1;
    public static final int MIN_BODY_BYTES = 60;
    public static final int MAX_BODY_BYTES = 60;
    public static final int BODY_BYTES = MIN_BODY_BYTES;

    private SpaceSpinlaunch() {}

    public static EncodedPayload encode(
            double x,
            double y,
            double z,
            int count,
            float scale,
            double motionX,
            double motionY,
            double motionZ,
            int maxAge) {
        if (count < 0) throw new IllegalArgumentException("Invalid count");
        EncodedPayload payload =
                EncodedPayload.allocate(
                        PacketFamilySpaceEffects.TYPE,
                        BODY_BYTES,
                        MAX_BODY_BYTES,
                        true,
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
            long xBits = doubleToRawLongBits(x);
            long yBits = doubleToRawLongBits(y);
            long zBits = doubleToRawLongBits(z);
            long countBits = (long) count;
            long scaleBits = floatToRawIntBits(scale);
            long motionXBits = doubleToRawLongBits(motionX);
            long motionYBits = doubleToRawLongBits(motionY);
            long motionZBits = doubleToRawLongBits(motionZ);
            long maxAgeBits = maxAge;
            U.putLong(storageBase, storageAddress, 1L | (xBits << 1));
            U.putLong(storageBase, storageAddress + 8L, (xBits >>> 63) | (yBits << 1));
            U.putLong(storageBase, storageAddress + 16L, (yBits >>> 63) | (zBits << 1));
            U.putLong(
                    storageBase,
                    storageAddress + 24L,
                    (zBits >>> 63) | (countBits << 1) | (scaleBits << 32));
            U.putLong(storageBase, storageAddress + 32L, motionXBits);
            U.putLong(storageBase, storageAddress + 40L, motionYBits);
            U.putLong(storageBase, storageAddress + 48L, motionZBits);
            U.putInt(storageBase, storageAddress + 56L, (int) (maxAgeBits));
            encodedBuffer.writerIndex(bufferStart + BODY_BYTES);
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
        if (bodyBytes != BODY_BYTES) throw new DecoderException("Invalid packet length");
        long xWord = U.getLong(storageBase, storageAddress);
        long xToYWord = U.getLong(storageBase, storageAddress + 8L);
        long yToZWord = U.getLong(storageBase, storageAddress + 16L);
        long zToScaleWord = U.getLong(storageBase, storageAddress + 24L);
        long motionXWord = U.getLong(storageBase, storageAddress + 32L);
        long motionYWord = U.getLong(storageBase, storageAddress + 40L);
        long motionZWord = U.getLong(storageBase, storageAddress + 48L);
        long maxAgeWord = (U.getInt(storageBase, storageAddress + 56L) & 0xFFFFFFFFL);
        double x = longBitsToDouble(((xWord >>> 1) | (xToYWord << 63)));
        double y = longBitsToDouble(((xToYWord >>> 1) | (yToZWord << 63)));
        double z = longBitsToDouble(((yToZWord >>> 1) | (zToScaleWord << 63)));
        int count = (int) ((zToScaleWord >>> 1) & 0x7FFFFFFFL);
        float scale = intBitsToFloat((int) (zToScaleWord >>> 32));
        double motionX = longBitsToDouble(motionXWord);
        double motionY = longBitsToDouble(motionYWord);
        double motionZ = longBitsToDouble(motionZWord);
        int maxAge = (int) maxAgeWord;
        SpinlaunchBurst.spawn(x, y, z, count, scale, motionX, motionY, motionZ, maxAge);
    }
}
