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
import com.hbm.packet.codec.PacketValues;
import com.hbm.packet.threading.EncodedPayload;
import com.hbmspace.blockentity.machine.TransporterBaseBlockEntity;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.server.level.ServerPlayer;

import static com.hbm.lib.internal.UnsafeHolder.BA_BASE;
import static com.hbm.lib.internal.UnsafeHolder.U;

public final class SpaceTransporterName {
    public static final int VARIANT = 2;
    public static final int VARIANT_BITS = 5;
    public static final int MIN_BODY_BYTES = 3;
    public static final int MAX_BODY_BYTES = 32767;

    private SpaceTransporterName() {}

    public static EncodedPayload encode(int containerId, String name) {
        int nameBytes = PacketValues.utf8Length(name, 2097151, 2097151);
        if (((long) (containerId - (1)) & 0xFFFFFFFFL) > 99L)
            throw new IllegalArgumentException("Invalid containerId");
        long bodyBytesWide = 2L + (PacketValues.varIntSize(nameBytes) + nameBytes);
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
                U.putShort(storageBase, storageAddress, (short) (2L | (containerIdBits << 5)));
            }
            PacketValues.putUtf8(storageBase, storageAddress + 2L, name, nameBytes);
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
        int nameBytes = PacketValues.getVarInt(storageBase, storageAddress + 2L, bodyLimit);
        long nameAddress = storageAddress + 2L + PacketValues.varIntSize(nameBytes);
        if ((nameBytes & 0xFFFFFFFFL) > 2097151 || nameBytes > bodyLimit - nameAddress)
            throw new DecoderException("Invalid text length");
        String name = PacketValues.getUtf8(storageBase, nameAddress, nameBytes, 2097151);
        long nameEnd = nameAddress + nameBytes;
        if (bodyLimit != nameEnd) throw new DecoderException("Unexpected packet tail");
        if (!(context.player() instanceof ServerPlayer player)
                || player.isSpectator()
                || !(ControlAuthority.openMenu(player, containerId)
                        instanceof BlockEntityMenu<?> menu)
                || !(menu.blockEntity() instanceof TransporterBaseBlockEntity receiver)) return;
        receiver.setName(name);
    }
}
