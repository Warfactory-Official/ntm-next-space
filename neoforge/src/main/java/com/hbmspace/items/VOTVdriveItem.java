/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.items;

import com.hbm.client.ClientPlayerAccess;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import com.hbm.util.TooltipStyle;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.Target;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.entity.missile.RideableRocketEntity.RocketState;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class VOTVdriveItem extends Item {

    public final SolarSystem.Body body;

    public VOTVdriveItem(Properties properties, SolarSystem.Body body) {
        super(properties);
        this.body = body;
    }

    public static SolarSystem.Body getBody(ItemStack stack) {
        return ((VOTVdriveItem) stack.getItem()).body;
    }

    public static Data data(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentsSpace.DRIVE.get(), Data.EMPTY);
    }

    public static Destination getDestination(ItemStack stack) {
        Data data = data(stack);
        return new Destination(getBody(stack), data.x, data.z);
    }

    public static Target getTarget(ItemStack stack, Level level) {
        if (!(stack.getItem() instanceof VOTVdriveItem)) return new Target(null, false, false);

        Destination destination = getDestination(stack);

        if (destination.body == SolarSystem.Body.ORBIT) {
            if (level.isClientSide()) {
                Data data = data(stack);
                return new Target(
                        CelestialBody.getBody(data.stationBody), true, data.stationLaunched);
            }
            OrbitalStation station = station(destination, level);
            return new Target(station.orbiting, true, station.hasStation);
        }

        return new Target(destination.body.getBody(), false, true);
    }

    public static void publishStation(ItemStack stack, Level level) {
        if (!(stack.getItem() instanceof VOTVdriveItem) || getBody(stack) != SolarSystem.Body.ORBIT)
            return;
        Data data = data(stack);
        OrbitalStation station = station(getDestination(stack), level);
        Data next =
                new Data(
                        data.x,
                        data.z,
                        data.approx,
                        data.processed,
                        data.copied,
                        station.name,
                        station.orbiting.name,
                        station.hasStation);
        if (!next.equals(data)) stack.set(ModDataComponentsSpace.DRIVE.get(), next);
    }

    private static OrbitalStation station(Destination destination, Level level) {
        OrbitalStation station = OrbitalStation.getStation(destination.x, destination.z);
        if (!station.hasStation) station.orbiting = CelestialBody.getBody(level);
        return station;
    }

    public static void setCoordinates(ItemStack stack, int x, int z) {
        Data data = data(stack);
        stack.set(
                ModDataComponentsSpace.DRIVE.get(),
                new Data(
                        x,
                        z,
                        data.approx,
                        data.processed,
                        data.copied,
                        data.stationName,
                        data.stationBody,
                        data.stationLaunched));
    }

    public static int getProcessingTier(ItemStack stack, CelestialBody from) {
        return getBody(stack).getProcessingLevel(from);
    }

    public static boolean getProcessed(ItemStack stack) {
        return data(stack).processed;
    }

    public static void setProcessed(ItemStack stack, boolean processed) {
        Data data = data(stack);
        stack.set(
                ModDataComponentsSpace.DRIVE.get(),
                new Data(
                        data.x,
                        data.z,
                        data.approx,
                        processed,
                        data.copied,
                        data.stationName,
                        data.stationBody,
                        data.stationLaunched));
    }

    public static Destination getApproximateDestination(ItemStack stack, RandomSource random) {
        Data data = data(stack);
        ChunkPos approx = data.approx.orElse(null);
        if (approx == null) {
            int range = SpaceData.MAX_PROBE_DISTANCE.get();
            approx =
                    new ChunkPos(
                            random.nextInt(range * 2) - range, random.nextInt(range * 2) - range);
            stack.set(
                    ModDataComponentsSpace.DRIVE.get(),
                    new Data(
                            data.x,
                            data.z,
                            Optional.of(approx),
                            data.processed,
                            data.copied,
                            data.stationName,
                            data.stationBody,
                            data.stationLaunched));
        }
        return new Destination(getBody(stack), approx.x(), approx.z());
    }

    public static void setApproximateDestination(ItemStack stack, int x, int z) {
        Data data = data(stack);
        stack.set(
                ModDataComponentsSpace.DRIVE.get(),
                new Data(
                        data.x,
                        data.z,
                        Optional.of(new ChunkPos(x, z)),
                        data.processed,
                        data.copied,
                        data.stationName,
                        data.stationBody,
                        data.stationLaunched));
    }

    public static void markCopied(ItemStack stack) {
        Data data = data(stack);
        stack.set(
                ModDataComponentsSpace.DRIVE.get(),
                new Data(
                        data.x,
                        data.z,
                        data.approx,
                        data.processed,
                        true,
                        data.stationName,
                        data.stationBody,
                        data.stationLaunched));
    }

    public static boolean wasCopied(ItemStack stack) {
        return data(stack).copied;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean onDestination = level.dimension() == getBody(stack).getDimension();

        if (!getProcessed(stack) && (player.isCreative() || onDestination)) {
            setProcessed(stack, true);
        }

        if (getProcessed(stack)
                && player.getVehicle() instanceof RideableRocketEntity rocket
                && (!rocket.getRocket().stages.isEmpty()
                        || CelestialBody.inOrbit(level)
                        || rocket.isReusable())) {
            RocketState state = rocket.getState();
            if (state == RocketState.LANDED || state == RocketState.AWAITING) {
                if (level.isClientSide()) return InteractionResult.SUCCESS;

                ItemStack returned = rocket.navDrive;
                rocket.navDrive = stack.copyWithCount(1);
                rocket.setState(RocketState.AWAITING);
                EntitySounds.play(
                        player,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModSounds.UPGRADE_PLUG.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
                return InteractionResult.SUCCESS.heldItemTransformedTo(returned);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Destination destination = getDestination(stack);

        if (destination.body == SolarSystem.Body.ORBIT) {
            if (CelestialBody.inOrbit(level)) return InteractionResult.PASS;

            Player player = context.getPlayer();
            if (!level.isClientSide() && player != null) {
                OrbitalStation station = OrbitalStation.getStation(destination.x, destination.z);
                SolarSystem.Body here = CelestialBody.getEnum(level);
                int x = context.getClickedPos().getX();
                int z = context.getClickedPos().getZ();

                boolean recalled = here != null && station.recallPod(new Destination(here, x, z));
                player.sendSystemMessage(
                        (recalled
                                        ? Component.translatable(
                                                "desc.item.votvDrive.recalling", x, z)
                                        : Component.translatable(
                                                "desc.item.votvDrive.recallFailed"))
                                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            }
            return InteractionResult.SUCCESS;
        }

        if (level.dimension() != destination.body.getDimension()) return InteractionResult.PASS;

        int x = context.getClickedPos().getX();
        int z = context.getClickedPos().getZ();
        setCoordinates(stack, x, z);
        setProcessed(stack, true);

        Player player = context.getPlayer();
        if (!level.isClientSide() && player != null) {
            player.sendSystemMessage(
                    Component.translatable("desc.item.votvDrive.landingSet", x, z)
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> adder,
            TooltipFlag flag) {
        Player player = ClientPlayerAccess.player();
        Level level = player != null ? player.level() : null;
        Destination destination = getDestination(stack);

        if (destination.body == SolarSystem.Body.ORBIT) {
            Data data = data(stack);
            String identifier =
                    data.stationName.isEmpty()
                            ? "0x"
                                    + Integer.toHexString(
                                                    new ChunkPos(destination.x, destination.z)
                                                            .hashCode())
                                            .toUpperCase(Locale.ROOT)
                            : data.stationName;

            adder.accept(Component.translatable("desc.item.votvDrive.destinationStation"));
            adder.accept(Component.translatable("desc.item.votvDrive.station", identifier));

            if (level != null && level.dimension() != destination.body.getDimension()) {
                gold(adder, "item.hbmspace.hard_drive_full.orbit.desc");
            }
            return;
        }

        adder.accept(
                Component.translatable(
                        "desc.item.votvDrive.destination",
                        Component.translatable("body." + destination.body.name)
                                .withStyle(ChatFormatting.AQUA)));

        if (destination.x == 0 && destination.z == 0) {
            adder.accept(
                    Component.translatable("desc.item.votvDrive.needsCoordinates")
                            .withStyle(ChatFormatting.GOLD));
        } else if (!getProcessed(stack)) {
            if (level != null) {
                adder.accept(
                        Component.translatable(
                                "desc.item.votvDrive.requirement",
                                destination.body.getProcessingLevel(CelestialBody.getBody(level))));
            }
            adder.accept(
                    Component.translatable("desc.item.votvDrive.needsProcessing")
                            .withStyle(ChatFormatting.GOLD));
            adder.accept(
                    Component.translatable(
                            "desc.item.votvDrive.coordinates", destination.x, destination.z));
        } else {
            adder.accept(
                    Component.translatable("desc.item.votvDrive.processed")
                            .withStyle(ChatFormatting.GREEN));
            adder.accept(
                    Component.translatable(
                            "desc.item.votvDrive.coordinates", destination.x, destination.z));
        }

        if (level != null && level.dimension() == destination.body.getDimension()) {
            gold(adder, "item.hbmspace.hard_drive_full.surface.desc");
        }
    }

    private static void gold(Consumer<Component> adder, String key) {
        for (Component line : TooltipStyle.lines(Component.translatable(key))) {
            adder.accept(line.copy().withStyle(ChatFormatting.GOLD));
        }
    }

    public record Destination(SolarSystem.Body body, int x, int z) {

        public static final Codec<Destination> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                SolarSystem.Body.CODEC
                                                        .fieldOf("body")
                                                        .forGetter(Destination::body),
                                                Codec.INT.fieldOf("x").forGetter(Destination::x),
                                                Codec.INT.fieldOf("z").forGetter(Destination::z))
                                        .apply(i, Destination::new));

        public ChunkPos getChunk() {
            return new ChunkPos(x >> 4, z >> 4);
        }
    }

    public record Data(
            int x,
            int z,
            Optional<ChunkPos> approx,
            boolean processed,
            boolean copied,
            String stationName,
            String stationBody,
            boolean stationLaunched) {

        public static final Data EMPTY =
                new Data(0, 0, Optional.empty(), false, false, "", "", false);

        public static final Codec<Data> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                Codec.INT
                                                        .optionalFieldOf("x", 0)
                                                        .forGetter(Data::x),
                                                Codec.INT
                                                        .optionalFieldOf("z", 0)
                                                        .forGetter(Data::z),
                                                ChunkPos.CODEC
                                                        .optionalFieldOf("approx")
                                                        .forGetter(Data::approx),
                                                Codec.BOOL
                                                        .optionalFieldOf("processed", false)
                                                        .forGetter(Data::processed),
                                                Codec.BOOL
                                                        .optionalFieldOf("copied", false)
                                                        .forGetter(Data::copied),
                                                Codec.STRING
                                                        .optionalFieldOf("station_name", "")
                                                        .forGetter(Data::stationName),
                                                Codec.STRING
                                                        .optionalFieldOf("station_body", "")
                                                        .forGetter(Data::stationBody),
                                                Codec.BOOL
                                                        .optionalFieldOf("station_launched", false)
                                                        .forGetter(Data::stationLaunched))
                                        .apply(i, Data::new));

        public static final StreamCodec<ByteBuf, Data> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public Data decode(ByteBuf buf) {
                        return new Data(
                                ByteBufCodecs.VAR_INT.decode(buf),
                                ByteBufCodecs.VAR_INT.decode(buf),
                                ByteBufCodecs.optional(ChunkPos.STREAM_CODEC).decode(buf),
                                buf.readBoolean(),
                                buf.readBoolean(),
                                ByteBufCodecs.STRING_UTF8.decode(buf),
                                ByteBufCodecs.STRING_UTF8.decode(buf),
                                buf.readBoolean());
                    }

                    @Override
                    public void encode(ByteBuf buf, Data data) {
                        ByteBufCodecs.VAR_INT.encode(buf, data.x);
                        ByteBufCodecs.VAR_INT.encode(buf, data.z);
                        ByteBufCodecs.optional(ChunkPos.STREAM_CODEC).encode(buf, data.approx);
                        buf.writeBoolean(data.processed);
                        buf.writeBoolean(data.copied);
                        ByteBufCodecs.STRING_UTF8.encode(buf, data.stationName);
                        ByteBufCodecs.STRING_UTF8.encode(buf, data.stationBody);
                        buf.writeBoolean(data.stationLaunched);
                    }
                };
    }
}
