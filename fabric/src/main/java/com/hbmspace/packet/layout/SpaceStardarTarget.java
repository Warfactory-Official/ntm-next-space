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

import com.hbm.inventory.container.BlockEntityMenu;
import com.hbm.packet.ControlAuthority;
import com.hbm.packet.PayloadHandlerContext;
import com.hbm.packet.threading.EncodedPayload;
import com.hbmspace.blockentity.machine.StardarBlockEntity;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import static com.hbm.lib.internal.UnsafeHolder.BA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.U;

public final class SpaceStardarTarget {
    public static final int VARIANT = 11;
    public static final int VARIANT_BITS = 5;
    public static final int MIN_BODY_BYTES = 3;
    public static final int MAX_BODY_BYTES = 32767;

    private SpaceStardarTarget() {}

    public static EncodedPayload encode(int containerId, Identifier dimension, boolean here) {
        if (((long) (containerId - (1)) & 0xFFFFFFFFL) > 99L)
            throw new IllegalArgumentException("Invalid containerId");
        EncodedPayload payload =
                EncodedPayload.allocate(
                        PacketFamilySpaceControl.TYPE, 3, MAX_BODY_BYTES, false, false, false);
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
                long containerIdBits = ((long) containerId - 1L);
                long hereBits = here ? 1L : 0L;
                U.putShort(
                        storageBase,
                        storageAddress,
                        (short) (11L | (containerIdBits << 5) | (hereBits << 12)));
            }
            encodedBuffer.writerIndex(bufferStart + 2);
            Identifier.STREAM_CODEC.encode(codecBuffer, dimension);
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
        long containerIdToHereWord = (U.getShort(storageBase, storageAddress) & 65535L);
        long containerIdBits = ((containerIdToHereWord >>> 5) & 127L);
        if (containerIdBits > 99L) throw new DecoderException("Invalid containerId");
        int containerId = (int) (containerIdBits + 1L);
        boolean here = ((containerIdToHereWord >>> 12) & 1L) != 0;
        if ((containerIdToHereWord >>> 13) != 0)
            throw new DecoderException("Nonzero packet padding");
        decodedBuffer.readerIndex(bufferStart + 2);
        Identifier dimension = Identifier.STREAM_CODEC.decode(codecBuffer);
        if (decodedBuffer.readerIndex() != bufferStart + bodyBytes)
            throw new DecoderException("Unexpected packet tail");
        if (!(context.player() instanceof ServerPlayer player)
                || player.isSpectator()
                || !(ControlAuthority.openMenu(player, containerId)
                        instanceof BlockEntityMenu<?> menu)
                || !(menu.blockEntity() instanceof StardarBlockEntity receiver)) return;
        receiver.setTarget(dimension, here);
    }
}
