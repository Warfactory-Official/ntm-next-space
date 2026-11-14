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
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import static com.hbm.lib.internal.UnsafeHolder.BA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.U;

public final class SpaceLivingSync {
    public static final int VARIANT = 5;
    public static final int VARIANT_BITS = 3;
    public static final int MIN_BODY_BYTES = 5;
    public static final int MAX_BODY_BYTES = 5;
    public static final int BODY_BYTES = MIN_BODY_BYTES;

    private SpaceLivingSync() {}

    public static EncodedPayload encode(int oxygen, boolean gravity, boolean tankInUse) {
        EncodedPayload payload =
                EncodedPayload.allocate(
                        PacketFamilySpaceSync.TYPE, BODY_BYTES, MAX_BODY_BYTES, true, false, false);
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
            long oxygenBits = oxygen;
            long gravityBits = gravity ? 1L : 0L;
            long tankInUseBits = tankInUse ? 1L : 0L;
            U.putInt(storageBase, storageAddress, (int) (5L | (oxygenBits << 3)));
            U.putByte(
                    storageBase,
                    storageAddress + 4L,
                    (byte)
                            (((oxygenBits >>> 29) & 7L)
                                    | (gravityBits << 3)
                                    | (tankInUseBits << 4)));
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
        long oxygenWord = (U.getInt(storageBase, storageAddress) & 0xFFFFFFFFL);
        long oxygenToTankInUseWord = (U.getByte(storageBase, storageAddress + 4L) & 255L);
        int oxygen = (int) ((oxygenWord >>> 3) | (oxygenToTankInUseWord << 29));
        boolean gravity = ((oxygenToTankInUseWord >>> 3) & 1L) != 0;
        boolean tankInUse = ((oxygenToTankInUseWord >>> 4) & 1L) != 0;
        if ((oxygenToTankInUseWord >>> 5) != 0)
            throw new DecoderException("Nonzero packet padding");
        var player = context.playerOrNull();
        if (player == null) return;
        SpaceLivingProps.acceptSync(player, oxygen, gravity, tankInUse);
    }
}
