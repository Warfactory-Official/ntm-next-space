// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.tool;

import com.hbmspace.blockentity.machine.BlockEntityTransporterBase;
import com.hbmspace.dim.CelestialBody;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Utf8String;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public record TransporterInfo(
        String name,
        ResourceKey<Level> dimension,
        BlockPos pos,
        @Nullable TransporterInfo linkedTo) {

    public static TransporterInfo from(
            ResourceKey<Level> dimension, BlockEntityTransporterBase transporter) {
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
        String name = Utf8String.read(buf, Short.MAX_VALUE);
        ResourceKey<Level> dimension =
                ResourceKey.create(
                        Registries.DIMENSION,
                        Identifier.parse(Utf8String.read(buf, Short.MAX_VALUE)));
        BlockPos pos = BlockPos.of(buf.readLong());
        TransporterInfo linked = buf.readBoolean() ? read(buf) : null;
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
