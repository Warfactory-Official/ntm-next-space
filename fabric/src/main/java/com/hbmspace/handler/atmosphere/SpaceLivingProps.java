// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbm.packet.PacketWire;
import com.hbm.platform.FabricEntityData;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.lib.ModDamageTypesSpace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
    private static final Map<LivingEntity, Measured> CLIENT = new WeakHashMap<>();

    private static final class Measured {
        @Nullable CBT_Atmosphere atmosphere;
        boolean gravity;
        int tankTicks;
        int sentOxygen = FULL_BREATH;
        boolean sentGravity;
        boolean sentTankInUse;
    }

    private SpaceLivingProps() {}

    static void register() {
        breath =
                FabricEntityData.register(
                        Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "breath"),
                        () -> new Breath(FULL_BREATH),
                        BREATH_CODEC,
                        false);
    }

    private static Map<LivingEntity, Measured> side(LivingEntity entity) {
        return entity.level().isClientSide() ? CLIENT : SERVER;
    }

    private static Measured measured(LivingEntity entity) {
        return side(entity).computeIfAbsent(entity, e -> new Measured());
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
        Measured measured = side(entity).get(entity);
        return measured == null ? null : measured.atmosphere;
    }

    public static void setAtmosphere(LivingEntity entity, @Nullable CBT_Atmosphere atmosphere) {
        Measured measured = measured(entity);
        measured.atmosphere = atmosphere;
        measured.gravity = atmosphere != null;
    }

    public static boolean hasGravity(LivingEntity entity) {
        Measured measured = side(entity).get(entity);
        return measured != null && measured.gravity;
    }

    public static void setTankInUse(LivingEntity entity, boolean inUse) {
        Measured measured = measured(entity);
        measured.tankTicks = inUse ? Math.min(measured.tankTicks + 1, 21) : 0;
    }

    public static boolean isTankInUse(LivingEntity entity) {
        Measured measured = side(entity).get(entity);
        return measured != null && measured.tankTicks > 20;
    }

    public static void sync(ServerPlayer player) {
        Measured measured = measured(player);
        int oxygen = getOxy(player);
        boolean tankInUse = measured.tankTicks > 20;
        boolean moved =
                oxygen != measured.sentOxygen
                        || measured.gravity != measured.sentGravity
                        || tankInUse != measured.sentTankInUse;
        if (!moved && player.tickCount % 20 != 0) return;
        measured.sentOxygen = oxygen;
        measured.sentGravity = measured.gravity;
        measured.sentTankInUse = tankInUse;
        PacketWire.sendTo(new SpaceLivingSyncPayload(oxygen, measured.gravity, tankInUse), player);
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
