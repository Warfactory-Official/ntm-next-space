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

package com.hbmspace.items.tool;

import com.hbmspace.blockentity.machine.TransporterBaseBlockEntity;
import com.hbmspace.dim.CelestialBody;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public record TransporterInfo(
        String name,
        ResourceKey<Level> dimension,
        BlockPos pos,
        @Nullable TransporterInfo linkedTo) {

    public static final StreamCodec<ByteBuf, TransporterInfo> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public TransporterInfo decode(ByteBuf input) {
                    return read(input, true);
                }

                @Override
                public void encode(ByteBuf output, TransporterInfo value) {
                    value.write(output);
                }
            };

    public static TransporterInfo from(
            ResourceKey<Level> dimension, TransporterBaseBlockEntity transporter) {
        return new TransporterInfo(
                transporter.getTransporterName(),
                dimension,
                transporter.getBlockPos(),
                transporter.getLinkedTransporter());
    }

    public Identifier planet() {
        return CelestialBody.getBody(dimension).texture;
    }

    public void write(ByteBuf buf) {
        write(buf, true);
    }

    private void write(ByteBuf buf, boolean recurse) {
        Utf8String.write(buf, name, Short.MAX_VALUE);
        Utf8String.write(buf, dimension.identifier().toString(), Short.MAX_VALUE);
        buf.writeLong(pos.asLong());
        boolean linked = recurse && linkedTo != null;
        buf.writeBoolean(linked);
        if (linked) linkedTo.write(buf, false);
    }

    public static TransporterInfo read(ByteBuf buf) {
        return read(buf, true);
    }

    private static TransporterInfo read(ByteBuf buf, boolean allowLinked) {
        String name = Utf8String.read(buf, Short.MAX_VALUE);
        ResourceKey<Level> dimension =
                ResourceKey.create(
                        Registries.DIMENSION,
                        Identifier.parse(Utf8String.read(buf, Short.MAX_VALUE)));
        BlockPos pos = BlockPos.of(buf.readLong());
        boolean hasLinked = buf.readBoolean();
        if (hasLinked && !allowLinked)
            throw new DecoderException("Transporter link nesting exceeds one entry");
        TransporterInfo linked = hasLinked ? read(buf, false) : null;
        return new TransporterInfo(name, dimension, pos, linked);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TransporterInfo info
                && dimension.equals(info.dimension)
                && pos.equals(info.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, pos);
    }
}
