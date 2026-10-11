// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.saveddata.satellites;

import com.hbmspace.util.AstronomyUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SatelliteOrbit(
        float inclination,
        float altitude,
        float phaseOffset,
        boolean blinking,
        float blinkPeriod,
        String owner,
        float red,
        float green,
        float blue) {

    public static final float MIN_INCLINATION = -180F;
    public static final float MAX_INCLINATION = 180F;
    public static final float MIN_ALTITUDE_KM = 80F;
    public static final float MAX_ALTITUDE_KM = 125F;
    public static final float MIN_BLINK_PERIOD = 0.3F;
    public static final float MAX_BLINK_PERIOD = 1F;
    public static final String DEFAULT_OWNER = "None";
    public static final double REVOLUTION_TICKS = 600;

    public static final Codec<SatelliteOrbit> CODEC =
            RecordCodecBuilder.create(
                    i ->
                            i.group(
                                            Codec.FLOAT
                                                    .fieldOf("inclination")
                                                    .forGetter(SatelliteOrbit::inclination),
                                            Codec.FLOAT
                                                    .fieldOf("altitude")
                                                    .forGetter(SatelliteOrbit::altitude),
                                            Codec.FLOAT
                                                    .fieldOf("phase")
                                                    .forGetter(SatelliteOrbit::phaseOffset),
                                            Codec.BOOL
                                                    .fieldOf("blinking")
                                                    .forGetter(SatelliteOrbit::blinking),
                                            Codec.FLOAT
                                                    .fieldOf("blink_period")
                                                    .forGetter(SatelliteOrbit::blinkPeriod),
                                            Codec.STRING
                                                    .fieldOf("owner")
                                                    .forGetter(SatelliteOrbit::owner),
                                            Codec.FLOAT
                                                    .fieldOf("red")
                                                    .forGetter(SatelliteOrbit::red),
                                            Codec.FLOAT
                                                    .fieldOf("green")
                                                    .forGetter(SatelliteOrbit::green),
                                            Codec.FLOAT
                                                    .fieldOf("blue")
                                                    .forGetter(SatelliteOrbit::blue))
                                    .apply(i, SatelliteOrbit::new));
    public static final StreamCodec<ByteBuf, SatelliteOrbit> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    SatelliteOrbit::inclination,
                    ByteBufCodecs.FLOAT,
                    SatelliteOrbit::altitude,
                    ByteBufCodecs.FLOAT,
                    SatelliteOrbit::phaseOffset,
                    ByteBufCodecs.BOOL,
                    SatelliteOrbit::blinking,
                    ByteBufCodecs.FLOAT,
                    SatelliteOrbit::blinkPeriod,
                    ByteBufCodecs.STRING_UTF8,
                    SatelliteOrbit::owner,
                    ByteBufCodecs.FLOAT,
                    SatelliteOrbit::red,
                    ByteBufCodecs.FLOAT,
                    SatelliteOrbit::green,
                    ByteBufCodecs.FLOAT,
                    SatelliteOrbit::blue,
                    SatelliteOrbit::new);

    public SatelliteOrbit {
        phaseOffset = normalizePhaseOffset(phaseOffset);
        blinkPeriod = clampBlinkPeriod(blinkPeriod);
    }

    public static SatelliteOrbit defaults(float red, float green, float blue) {
        return new SatelliteOrbit(
                0F,
                AstronomyUtil.DEFAULT_ALTITUDE_KM,
                0F,
                false,
                MIN_BLINK_PERIOD,
                DEFAULT_OWNER,
                red,
                green,
                blue);
    }

    public float orbitAngle(double baseAngle, float fullRotation) {
        return applyPhaseOffsetToOrbitAngle(phaseOffset, altitude, baseAngle, fullRotation);
    }

    public float blinkAlpha(double ticks) {
        return blinkAlpha(blinking, blinkPeriod, ticks);
    }

    public static float applyPhaseOffsetToOrbitAngle(
            float phaseOffset, float altitude, double baseAngle, float fullRotation) {
        double phase = normalizePhaseOffset(phaseOffset) / 360.0D * fullRotation;
        double angle = baseAngle * altitudeOrbitSpeed(altitude) + phase;
        double wrapped = angle % fullRotation;
        if (wrapped < 0.0D) wrapped += fullRotation;
        return (float) wrapped;
    }

    public static float orbitSpeedKmPerSecond(float altitude) {
        double radiusKm = Math.max(1.0D, altitude);
        double turnsPerSecond = altitudeOrbitSpeed(altitude) / 30.0D;
        return (float) (2.0D * Math.PI * radiusKm * turnsPerSecond);
    }

    private static double altitudeOrbitSpeed(float altitude) {
        return Math.pow(
                (double) AstronomyUtil.DEFAULT_ALTITUDE_KM / Math.max(1.0D, altitude), 1.5D);
    }

    public static float blinkAlpha(boolean blinking, float blinkPeriod, double ticks) {
        if (!blinking) return 1.0F;
        double cycle = (long) (clampBlinkPeriod(blinkPeriod) * 1000.0F) / 50.0D;
        return 1.0F - (float) ((ticks % cycle) / cycle);
    }

    public static float normalizePhaseOffset(float phaseOffset) {
        float wrapped = phaseOffset % 360.0F;
        if (wrapped < 0.0F) wrapped += 360.0F;
        return wrapped;
    }

    public static float clampBlinkPeriod(float blinkPeriod) {
        return Math.max(MIN_BLINK_PERIOD, Math.min(MAX_BLINK_PERIOD, blinkPeriod));
    }
}
