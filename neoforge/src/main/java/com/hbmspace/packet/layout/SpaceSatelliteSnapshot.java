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
import com.hbmspace.saveddata.satellites.ClientSatellites;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import static com.hbm.lib.internal.UnsafeHolder.BA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.U;

public final class SpaceSatelliteSnapshot {
    public static final int VARIANT = 0;
    public static final int VARIANT_BITS = 3;
    public static final int MIN_BODY_BYTES = 2;
    public static final int MAX_BODY_BYTES = 1048576;

    private SpaceSatelliteSnapshot() {}

    public static EncodedPayload encode(
            Map<ResourceKey<Level>, Map<Integer, ClientSatellites.Entry>> satellites) {
        EncodedPayload payload =
                EncodedPayload.allocate(
                        PacketFamilySpaceSync.TYPE, 2, MAX_BODY_BYTES, true, false, false);
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
            ByteBuf codecBuffer = encodedBuffer;
            {
                U.putByte(storageBase, storageAddress, (byte) (0L));
            }
            encodedBuffer.writerIndex(bufferStart + 1);
            ClientSatellites.STREAM_CODEC.encode(codecBuffer, satellites);
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
        ByteBuf codecBuffer = decodedBuffer;
        if (((U.getByte(storageBase, storageAddress) & 255) >>> 3) != 0)
            throw new DecoderException("Nonzero packet padding");
        decodedBuffer.readerIndex(bufferStart + 1);
        Map<ResourceKey<Level>, Map<Integer, ClientSatellites.Entry>> satellites =
                ClientSatellites.STREAM_CODEC.decode(codecBuffer);
        if (decodedBuffer.readerIndex() != bufferStart + bodyBytes)
            throw new DecoderException("Unexpected packet tail");
        ClientSatellites.replace(satellites);
    }
}
