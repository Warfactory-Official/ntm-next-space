/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2023 JamesH_2. All rights reserved.
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

package com.hbmspace.handler.atmosphere;

import com.hbm.packet.PacketWire;
import com.hbm.platform.EntityData;
import com.hbm.platform.PlayerLevelChange;
import com.hbm.platform.ServerEvents;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SpaceGravity;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.lib.ModDamageTypesSpace;
import com.hbmspace.packet.layout.SpaceLivingSync;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class SpaceLivingProps {

    public static final int FULL_BREATH = 100;

    private static final class Breath {
        int oxygen;

        Breath(int oxygen) {
            this.oxygen = oxygen;
        }
    }

    private static final MapCodec<Breath> BREATH_CODEC =
            Codec.INT.optionalFieldOf("oxygen", FULL_BREATH).xmap(Breath::new, held -> held.oxygen);
    private static AttachmentType<Breath> breath;

    private static final Map<LivingEntity, Measured> SERVER = new WeakHashMap<>();
    private static WeakReference<LivingEntity> clientOwner = new WeakReference<>(null);
    private static Measured clientSelf = new Measured();

    private static final class Measured {
        @Nullable CBT_Atmosphere atmosphere;
        boolean gravity;
        int tankTicks;
        boolean resend = true;
        int sentOxygen;
        boolean sentGravity;
        boolean sentTankInUse;
    }

    private SpaceLivingProps() {}

    static void register() {
        breath =
                EntityData.register(
                        Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "breath"),
                        () -> new Breath(FULL_BREATH),
                        BREATH_CODEC,
                        false);
        final Consumer<ServerPlayer> callback = SERVER::remove;
        ServerEvents.RESPAWN.add(callback);
        final PlayerLevelChange callback2 =
                (player, origin, destination) -> measured(player).resend = true;
        ServerEvents.LEVEL_CHANGE.add(callback2);
    }

    private static @Nullable Measured find(LivingEntity entity) {
        if (!entity.level().isClientSide()) return SERVER.get(entity);
        return clientOwner.get() == entity ? clientSelf : null;
    }

    private static Measured measured(LivingEntity entity) {
        if (!entity.level().isClientSide())
            return SERVER.computeIfAbsent(entity, e -> new Measured());
        if (clientOwner.get() != entity) {
            clientOwner = new WeakReference<>(entity);
            clientSelf = new Measured();
        }
        return clientSelf;
    }

    public static int getOxy(LivingEntity entity) {
        Breath held = ((Entity) entity).getAttached(breath);
        return held == null ? FULL_BREATH : held.oxygen;
    }

    public static void setOxy(LivingEntity entity, int oxygen) {
        if (oxygen <= 0) {
            if (entity.tickCount < 20) oxygen = 0;

            int damageInterval = Mth.clamp(oxygen / 60 + 8, 1, 8);
            int damageAmount = Mth.clamp(-oxygen / 40 - 10, 1, Integer.MAX_VALUE);

            if (entity.tickCount % damageInterval == 0
                    && entity.level() instanceof ServerLevel level) {
                entity.hurtServer(
                        level,
                        level.damageSources().source(ModDamageTypesSpace.OXYPRIME),
                        damageAmount);
            }
        }

        setOxyRecord(entity, oxygen);
    }

    public static @Nullable CBT_Atmosphere getAtmosphere(LivingEntity entity) {
        Measured measured = find(entity);
        return measured == null ? null : measured.atmosphere;
    }

    public static void setAtmosphere(LivingEntity entity, @Nullable CBT_Atmosphere atmosphere) {
        Measured measured = measured(entity);
        measured.atmosphere = atmosphere;
        measured.gravity = atmosphere != null;
        if (CelestialBody.inOrbit(entity.level())) SpaceGravity.measure(entity);
    }

    public static boolean hasGravity(LivingEntity entity) {
        Measured measured = find(entity);
        return measured != null && measured.gravity;
    }

    public static void setTankInUse(LivingEntity entity, boolean inUse) {
        Measured measured = measured(entity);
        measured.tankTicks = inUse ? Math.min(measured.tankTicks + 1, 21) : 0;
    }

    public static boolean isTankInUse(LivingEntity entity) {
        Measured measured = find(entity);
        return measured != null && measured.tankTicks > 20;
    }

    public static void sync(ServerPlayer player) {
        Measured measured = measured(player);
        int oxygen = getOxy(player);
        boolean tankInUse = measured.tankTicks > 20;
        if (!measured.resend
                && oxygen == measured.sentOxygen
                && measured.gravity == measured.sentGravity
                && tankInUse == measured.sentTankInUse) return;
        measured.resend = false;
        measured.sentOxygen = oxygen;
        measured.sentGravity = measured.gravity;
        measured.sentTankInUse = tankInUse;
        PacketWire.sendTo(SpaceLivingSync.encode(oxygen, measured.gravity, tankInUse), player);
    }

    public static void acceptSync(Player player, int oxygen, boolean gravity, boolean tankInUse) {
        receive(player, oxygen, gravity, tankInUse);
    }

    static void receive(LivingEntity player, int oxygen, boolean gravity, boolean tankInUse) {
        setOxyRecord(player, oxygen);
        Measured measured = measured(player);
        measured.gravity = gravity;
        measured.tankTicks = tankInUse ? 21 : 0;
    }

    private static void setOxyRecord(LivingEntity entity, int oxygen) {
        Breath held = ((Entity) entity).getAttached(breath);
        if (held == null) {
            if (oxygen == FULL_BREATH) return;
            held = ((Entity) entity).getAttachedOrCreate(breath);
        }
        held.oxygen = oxygen;
    }
}
