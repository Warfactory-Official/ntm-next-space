// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.extprop;

import com.hbm.platform.NeoForgeAttachments;
import com.hbmspace.HbmSpace;
import com.hbmspace.potion.HbmPotionSpace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;

public final class SpacePlayerProps {

    private static final class Nitan {
        int count;
        float health;

        Nitan(int count, float health) {
            this.count = count;
            this.health = health;
        }
    }

    private static final MapCodec<Nitan> NITAN_CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            Codec.INT
                                                    .optionalFieldOf("nitan", 0)
                                                    .forGetter((Nitan held) -> held.count),
                                            Codec.floatRange(0F, Float.MAX_VALUE)
                                                    .optionalFieldOf("nitan_health", 0F)
                                                    .forGetter(held -> held.health))
                                    .apply(instance, Nitan::new));
    private static Supplier<AttachmentType<Nitan>> nitan;

    private static final class Warped {
        boolean done;

        Warped(boolean done) {
            this.done = done;
        }
    }

    private static final MapCodec<Warped> WARPED_CODEC =
            Codec.BOOL.optionalFieldOf("hasWarped", false).xmap(Warped::new, held -> held.done);
    private static Supplier<AttachmentType<Warped>> warped;

    private SpacePlayerProps() {}

    public static void register() {
        nitan =
                NeoForgeAttachments.register(
                        Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "nitan"),
                        () -> new Nitan(0, 0F),
                        NITAN_CODEC,
                        true);
        warped =
                NeoForgeAttachments.register(
                        Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "has_warped"),
                        () -> new Warped(false),
                        WARPED_CODEC,
                        true);
    }

    public static int getNitanCount(Player player) {
        Nitan held = ((Entity) player).getExistingDataOrNull(nitan.get());
        return held == null ? 0 : held.count;
    }

    public static void setNitanCount(Player player, int count) {
        Nitan held = ((Entity) player).getData(nitan.get());
        held.count = count;
    }

    public static void addNitanDose(Player player) {
        Nitan held = ((Entity) player).getData(nitan.get());
        held.count++;
        held.health += 10F;
    }

    public static float getNitanHealth(Player player) {
        Nitan held = ((Entity) player).getExistingDataOrNull(nitan.get());
        return held == null ? 0F : held.health;
    }

    public static void clearNitanHealth(Player player) {
        Nitan held = ((Entity) player).getExistingDataOrNull(nitan.get());
        if (held != null) held.health = 0F;
    }

    public static float absorbNitan(Player player, float amount) {
        Nitan held = ((Entity) player).getExistingDataOrNull(nitan.get());
        if (held == null) return amount;
        if (!player.hasEffect(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HbmPotionSpace.NITAN.get()))) {
            held.health = 0F;
            return amount;
        }
        float consumed = Math.min(held.health, Math.max(amount, 0F));
        held.health -= consumed;
        return amount - consumed;
    }

    public static boolean hasWarped(Player player) {
        Warped held = ((Entity) player).getExistingDataOrNull(warped.get());
        return held != null && held.done;
    }

    public static void setWarped(Player player, boolean done) {
        Warped held = ((Entity) player).getData(warped.get());
        held.done = done;
    }
}
