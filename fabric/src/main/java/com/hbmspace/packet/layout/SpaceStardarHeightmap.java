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
import com.hbmspace.blockentity.machine.StardarHeightmapActions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;

import static com.hbm.lib.internal.UnsafeHolder.BA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.IA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.U;
import static net.minecraft.core.BlockPos.of;

public final class SpaceStardarHeightmap {
    public static final int VARIANT = 7;
    public static final int VARIANT_BITS = 3;
    public static final int MIN_BODY_BYTES = 9;
    public static final int MAX_BODY_BYTES = 262157;

    private SpaceStardarHeightmap() {}

    public static EncodedPayload encode(BlockPos pos, boolean heightsPresent, int[] heights) {
        if (heightsPresent) {
            if (heights.length != 65536)
                throw new IllegalArgumentException("Invalid heights count");
        }
        int bodyBytes = 9 + (heightsPresent ? 262144 : 0);
        EncodedPayload payload =
                EncodedPayload.allocate(
                        PacketFamilySpaceSync.TYPE, bodyBytes, MAX_BODY_BYTES, true, false, false);
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
            long partialByte;
            {
                long posBits = pos.asLong();
                long heightsPresentBits = heightsPresent ? 1L : 0L;
                U.putLong(storageBase, storageAddress, 7L | (posBits << 3));
                partialByte = (posBits >>> 61) | (heightsPresentBits << 3);
                U.putByte(storageBase, storageAddress + 8L, (byte) (partialByte));
            }
            if (heightsPresent) {
                long heightsAddress = storageAddress + 8L;
                long heightsOffset = IA_BASE;
                for (long heightsWordEnd = IA_BASE + 262144L;
                        heightsOffset < heightsWordEnd;
                        heightsOffset += 8L, heightsAddress += 8L) {
                    long sourceWord = U.getLong(heights, heightsOffset);
                    U.putLong(storageBase, heightsAddress, partialByte | sourceWord << 4);
                    partialByte = sourceWord >>> 60;
                }
                U.putByte(storageBase, heightsAddress, (byte) partialByte);
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
        long posWord = U.getLong(storageBase, storageAddress);
        long posToHeightsPresentWord = (U.getByte(storageBase, storageAddress + 8L) & 255L);
        BlockPos pos = of(((posWord >>> 3) | (posToHeightsPresentWord << 61)));
        boolean heightsPresent = ((posToHeightsPresentWord >>> 3) & 1L) != 0;
        int[] heights = null;
        if (heightsPresent) {
            if (bodyBytes - 8 < 262145) throw new DecoderException("Truncated packet collection");
            heights = StardarHeightmapActions.heights(context, pos);
            long heightsAddress = storageAddress + 8L;
            long heightsOffset = IA_BASE;
            for (long heightsWordEnd = IA_BASE + 262144L;
                    heightsOffset < heightsWordEnd;
                    heightsOffset += 8L, heightsAddress += 8L) {
                U.putLong(
                        heights,
                        heightsOffset,
                        U.getLong(storageBase, heightsAddress) >>> 4
                                | (long) U.getByte(storageBase, heightsAddress + 8L) << 60);
            }
        }
        long heightsEnd = storageAddress + 8L + (heightsPresent ? 262144L : 0L);
        if (bodyLimit != heightsEnd + 1L) throw new DecoderException("Unexpected packet tail");
        if (((U.getByte(storageBase, heightsEnd) & 255) >>> 4) != 0)
            throw new DecoderException("Nonzero packet padding");
        StardarHeightmapActions.receive(context, pos, heightsPresent, heights);
    }
}
