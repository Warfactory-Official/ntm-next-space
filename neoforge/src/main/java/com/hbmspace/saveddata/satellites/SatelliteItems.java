// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbm.client.ClientPlayerAccess;
import com.hbm.client.ModifierKeys;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteScience;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbm.util.TooltipStyle;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.handler.RocketPart;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.items.weapon.MissileEditsSpace;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class SatelliteItems {
    public static Runnable openSettings = () -> {};

    private static final int HAND_LAUNCH_MASS = 16_000;
    private static final Map<SatelliteType, List<String>> DESCRIPTIONS = new IdentityHashMap<>();

    static {
        description(SatelliteType.RELAY, "foeq");
        description(
                SatelliteType.HORIZONS,
                "gerald.single_use",
                "gerald.orbital_module",
                "gerald.melter");
        description(SatelliteType.DEATH_RAY, "laser");
        description(SatelliteType.MAPPER, "mapper");
        description(SatelliteType.MINER, "miner");
        description(SatelliteType.LUNAR_MINER, "lunar_miner");
        description(SatelliteType.RADAR, "radar");
        description(SatelliteType.RESONATOR, "resonator");
        description(SatelliteType.SCANNER, "scanner");
    }

    private SatelliteItems() {}

    public static void description(SatelliteType type, String... keys) {
        DESCRIPTIONS.put(type, List.of(keys));
    }

    static void describe(
            ItemStack stack,
            SatelliteType type,
            Item.TooltipContext context,
            TooltipFlag flag,
            Consumer<Component> adder,
            Runnable ntm) {
        if (!SatellitesSpace.isSpace(type)) {
            ntm.run();
            return;
        }

        MissileEditsSpace.describeSatellite(mass(type), adder);

        SatelliteOrbit orbit =
                stack.getOrDefault(
                        ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                        SatellitesSpace.defaults(type));
        if (ModifierKeys.detailsHeld()) {
            entry(adder, "frequency", String.valueOf(ISatChip.getFreqS(stack)));
            entry(adder, "owner", orbit.owner());
            entry(adder, "speed", speed(orbit.altitude()) + "km/s");
            entry(adder, "altitude", format(orbit.altitude()) + "km");
            entry(adder, "inclination", format(orbit.inclination()) + "\u00B0");
            entry(adder, "phase", phase(orbit.phaseOffset()) + "\u00B0");
            entry(
                    adder,
                    "color",
                    String.format(
                            Locale.ROOT,
                            "#%02X%02X%02X",
                            channel(orbit.red()),
                            channel(orbit.green()),
                            channel(orbit.blue())));
        } else {
            adder.accept(TooltipStyle.holdDetails());
        }

        for (String key : DESCRIPTIONS.getOrDefault(type, List.of())) {
            adder.accept(Component.translatable("item.hbmspace.sat.desc." + key));
        }

        if (byHand(type)) {
            adder.accept(
                    Component.translatable("item.hbmspace.sat.desc.launch_by_hand")
                            .withStyle(ChatFormatting.GOLD));
            Player player = ClientPlayerAccess.player();
            if (player != null && CelestialBody.inOrbit(player.level())) {
                adder.accept(
                        Component.translatable("item.hbmspace.sat.desc.deploy_orbit")
                                .withStyle(ChatFormatting.BOLD));
            }
        }
    }

    static InteractionResult use(
            ItemStack stack,
            @Nullable SatelliteType type,
            Level level,
            Player player,
            InteractionHand hand) {
        boolean module =
                stack.is(ModItems.SATELLITE_SCIENCE_SENSOR.get())
                        || stack.is(ModItems.SATELLITE_SCIENCE_ASSEMBLER.get());
        if (!SatellitesSpace.isSpace(type) && type != SatelliteType.SCIENCE && !module)
            return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (type == SatelliteType.SCIENCE || module) return InteractionResult.PASS;
            if (level.isClientSide()) openSettings.run();
            return InteractionResult.SUCCESS;
        }

        if (!(module ? RocketPart.of(stack.getItem()).mass <= HAND_LAUNCH_MASS : byHand(type))
                || !CelestialBody.inOrbit(level)) return InteractionResult.PASS;
        if (module && level.isClientSide()) return InteractionResult.PASS;

        if (level instanceof ServerLevel server) {
            ServerLevel target =
                    CelestialBody.getTarget(level, player.getBlockX(), player.getBlockZ())
                            .body()
                            .getLevel(server.getServer());
            if (target == null) return InteractionResult.PASS;
            int frequency = ISatChip.getFreqS(stack);
            if (module
                    && !(SatelliteSavedData.get(target).getSatFromFreq(frequency)
                            instanceof SatelliteScience)) {
                return InteractionResult.PASS;
            }
            Satellite.orbit(target, stack, frequency, player.getX(), player.getY(), player.getZ());
            player.sendSystemMessage(Component.translatable("chat.hbmspace.satellite.launched"));
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    public static boolean byHand(SatelliteType type) {
        RocketPart part = RocketPart.of(type.item());
        return part != null && part.mass <= HAND_LAUNCH_MASS;
    }

    private static int mass(SatelliteType type) {
        RocketPart part = RocketPart.of(type.item());
        return part == null ? 0 : part.mass;
    }

    private static void entry(Consumer<Component> adder, String key, String value) {
        adder.accept(
                Component.translatable("item.hbmspace.sat.desc." + key)
                        .append(": ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(value).withStyle(ChatFormatting.GOLD)));
    }

    public static String format(float value) {
        return value == (int) value ? Integer.toString((int) value) : Float.toString(value);
    }

    public static String speed(float altitude) {
        return format(Math.round(SatelliteOrbit.orbitSpeedKmPerSecond(altitude) * 10.0F) / 10.0F);
    }

    public static String phase(float phaseOffset) {
        return format(Math.round(SatelliteOrbit.normalizePhaseOffset(phaseOffset) * 10.0F) / 10.0F);
    }

    public static int channel(float value) {
        return Mth.clamp(Math.round(value * 255F), 0, 255);
    }
}
