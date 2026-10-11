// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-FileCopyrightText: 2024-2025 JamesH_2
// SPDX-FileCopyrightText: 2026 detree05
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.dim.trait.CBT_Temperature;
import com.hbmspace.dim.trait.CBT_Water;
import com.hbmspace.dim.trait.CelestialBodyTrait.CBT_BATTLEFIELD;
import com.hbmspace.util.AstronomyUtil;
import com.mojang.serialization.Codec;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;

public final class SolarSystem {

    public static final CelestialBody kerbol;

    public static final double RENDER_SCALE = 180;
    public static final double SUN_RENDER_SCALE = 4;

    public static final float MAX_APPARENT_SIZE_SURFACE = 24;
    public static final float MAX_APPARENT_SIZE_ORBIT = 160;

    public static final double ORRERY_MAX_RADIUS = 20_000;
    public static final double ORRERY_MIN_RADIUS = 2_000;

    static {
        kerbol = init();
    }

    private SolarSystem() {}

    public static void ensureInit() {}

    private static CelestialBody init() {
        return new CelestialBody("kerbol")
                .withMassRadius(1.757e28F, 261_600)
                .withRotationalPeriod(432_000)
                .withColor(1.0F, 0.8667F, 0.0F)
                .withShader(space("shaders/blackhole.frag"), 3)
                .withSatellites(
                        new CelestialBody("moho", SpaceDimensions.MOHO, Body.MOHO)
                                .withMassRadius(2.526e21F, 250)
                                .withOrbitalParameters(5_263_138, 0.2F, 15.0F, 7.0F, 70.0F)
                                .withRotationalPeriod(210_000)
                                .withColor(0.4863F, 0.4F, 0.3456F)
                                .withBlockTextures(
                                        spaceBlock("moho_stone"), spaceBlock("moho_regolith"))
                                .withAxialTilt(30F)
                                .withTraits(new CBT_Temperature(200)),
                        new CelestialBody("eve", SpaceDimensions.EVE, Body.EVE)
                                .withMassRadius(1.224e23F, 700)
                                .withOrbitalParameters(9_832_684, 0.01F, 0.0F, 2.1F, 15.0F)
                                .withRotationalPeriod(80_500)
                                .withColor(0.408F, 0.298F, 0.553F)
                                .withBlockTextures(
                                        spaceBlock("eve_stone_2"), spaceBlock("eve_silt"))
                                .withMinProcessingLevel(2)
                                .withTraits(
                                        new CBT_Atmosphere(fluid("EVEAIR"), 5D),
                                        new CBT_Temperature(400),
                                        new CBT_Water(fluid("MERCURY")))
                                .withSatellites(
                                        new CelestialBody("gilly")
                                                .withMassRadius(1.242e17F, 13)
                                                .withOrbitalParameters(
                                                        31_500, 0.55F, 10.0F, 12.0F, 80.0F)
                                                .withRotationalPeriod(28_255)
                                                .withColor(0.612F, 0.494F, 0.439F)),
                        new CelestialBody("kerbin", Level.OVERWORLD, Body.KERBIN)
                                .withMassRadius(5.292e22F, 600)
                                .withOrbitalParameters(13_599_840, 0.0F, 0.0F, 0.0F, 0.0F)
                                .withRotationalPeriod(21_549)
                                .withColor(0.608F, 0.914F, 1.0F)
                                .withTraits(
                                        new CBT_Atmosphere(fluid("EARTHAIR"), 1D), new CBT_Water())
                                .withBlockTextures(vanillaBlock("stone"), vanillaBlock("dirt"))
                                .withCityMask(space("textures/misc/space/kerbin_mask.png"))
                                .withBiomeMask(space("textures/misc/space/kerbin_biomes.png"))
                                .withSatellites(
                                        new CelestialBody("mun", SpaceDimensions.MUN, Body.MUN)
                                                .withMassRadius(9.76e20F, 200)
                                                .withOrbitalParameters(
                                                        12_000, 0.054F, 0.0F, 5.15F, 17.0F)
                                                .withRotationalPeriod(138_984)
                                                .withColor(0.51F, 0.51F, 0.51F)
                                                .withTidalLockingTo("kerbin")
                                                .withBlockTextures(
                                                        spaceBlock("moon_rock"),
                                                        ntmBlock("moon_turf"))
                                                .withIce(true),
                                        new CelestialBody(
                                                        "minmus",
                                                        SpaceDimensions.MINMUS,
                                                        Body.MINMUS)
                                                .withMassRadius(2.646e19F, 60)
                                                .withOrbitalParameters(
                                                        47_000, 0, 38.0F, 6.0F, 78.0F)
                                                .withRotationalPeriod(40_400)
                                                .withColor(0.6F, 0.882F, 0.764F)
                                                .withBlockTextures(
                                                        spaceBlock("minmus_stone"),
                                                        spaceBlock("minmus_regolith"))
                                                .withTraits(new CBT_Water(fluid("MILK")))
                                                .withIce(true)),
                        new CelestialBody("duna", SpaceDimensions.DUNA, Body.DUNA)
                                .withMassRadius(4.515e21F, 320)
                                .withOrbitalParameters(20_726_155, 0.05F, 0.0F, 0.06F, 135.5F)
                                .withRotationalPeriod(65_518)
                                .withTidalLockingTo("ike")
                                .withColor(0.6471F, 0.2824F, 0.1608F)
                                .withBlockTextures(
                                        spaceBlock("duna_rock"), spaceBlock("duna_sands"))
                                .withTraits(new CBT_Atmosphere(fluid("DUNAAIR"), 0.1D))
                                .withCityMask(space("textures/misc/space/duna_mask.png"))
                                .withIce(true)
                                .withSatellites(
                                        new CelestialBody("ike", SpaceDimensions.IKE, Body.IKE)
                                                .withMassRadius(2.782e20F, 130)
                                                .withOrbitalParameters(
                                                        3_200, 0.03F, 0.0F, 0.2F, 0.0F)
                                                .withBlockTextures(
                                                        spaceBlock("ike_stone"),
                                                        spaceBlock("ike_regolith"))
                                                .withRotationalPeriod(65_518)
                                                .withTidalLockingTo("duna")
                                                .withColor(0.533F, 0.502F, 0.526F)
                                                .withTraits(new CBT_Water(fluid("BROMINE")))
                                                .withIce(true)),
                        new CelestialBody("dres", SpaceDimensions.DRES, Body.DRES)
                                .withMassRadius(3.219e20F, 138)
                                .withOrbitalParameters(40_839_348, 0.145F, 90.0F, 5.0F, 280.0F)
                                .withRotationalPeriod(34_800)
                                .withColor(0.318F, 0.306F, 0.318F)
                                .withBlockTextures(
                                        spaceBlock("dresbase"), ntmBlock("sellafield_slaked"))
                                .withRings(10.0F, 3, 0.4F, 0.4F, 0.4F)
                                .withMinProcessingLevel(2)
                                .withIce(true),
                        new CelestialBody("jool")
                                .withMassRadius(4.233e24F, 6_000)
                                .withOrbitalParameters(68_773_560, 0.05F, 0.0F, 1.304F, 52.0F)
                                .withRotationalPeriod(36_000)
                                .withColor(0.4588F, 0.6784F, 0.3059F)
                                .withGas(fluid("JOOLGAS"))
                                .withSatellites(
                                        new CelestialBody(
                                                        "laythe",
                                                        SpaceDimensions.LAYTHE,
                                                        Body.LAYTHE)
                                                .withMassRadius(2.94e22F, 500)
                                                .withOrbitalParameters(
                                                        27_184, 0.0288F, 0.0F, 0.348F, 0.0F)
                                                .withRotationalPeriod(52_981)
                                                .withTidalLockingTo("jool")
                                                .withColor(0.388F, 0.384F, 0.851F)
                                                .withMinProcessingLevel(3)
                                                .withTraits(
                                                        new CBT_Atmosphere(fluid("EARTHAIR"), 0.45D)
                                                                .and(fluid("XENON"), 0.15D),
                                                        new CBT_Water())
                                                .withBlockTextures(
                                                        vanillaBlock("stone"),
                                                        spaceBlock("laythe_silt"))
                                                .withCityMask(
                                                        space(
                                                                "textures/misc/space/laythe_mask.png")),
                                        new CelestialBody("vall")
                                                .withMassRadius(3.109e21F, 300)
                                                .withOrbitalParameters(
                                                        43_152, 0.111F, 342.9F, 7.48F, 128.0F)
                                                .withRotationalPeriod(105_962)
                                                .withColor(0.773F, 0.843F, 0.855F),
                                        new CelestialBody("tylo")
                                                .withMassRadius(4.233e22F, 600)
                                                .withOrbitalParameters(
                                                        68_500, 0.002F, 0.0F, 0.3F, 0.0F)
                                                .withRotationalPeriod(211_926)
                                                .withColor(0.875F, 0.863F, 0.851F),
                                        new CelestialBody("bop")
                                                .withMassRadius(3.726e19F, 65)
                                                .withOrbitalParameters(
                                                        128_500, 0.235F, 25.0F, 15F, 10.0F)
                                                .withRotationalPeriod(544_507)
                                                .withColor(0.329F, 0.302F, 0.263F),
                                        new CelestialBody("pol")
                                                .withMassRadius(1.081e19F, 44)
                                                .withOrbitalParameters(
                                                        179_890, 0.171F, 15.0F, 4.25F, 2.0F)
                                                .withRotationalPeriod(901_902)
                                                .withColor(0.541F, 0.478F, 0.373F)),
                        new CelestialBody("sarnus")
                                .withMassRadius(1.223e24F, 5_300)
                                .withOrbitalParameters(125_798_522, 0.0534F, 0.0F, 2.02F, 184.0F)
                                .withRotationalPeriod(28_500)
                                .withColor(1f, 0.6862f, 0.5882f)
                                .withRings(10.0F, 3, 0.6F, 0.4F, 0.3F)
                                .withGas(fluid("SARNUSGAS"))
                                .withSatellites(
                                        new CelestialBody("hale")
                                                .withMassRadius(1.2166e16F, 6)
                                                .withOrbitalParameters(10_488, 0, 0.0F, 1.0F, 55.0F)
                                                .withRotationalPeriod(23_555)
                                                .withColor(0.306F, 0.259F, 0.235F),
                                        new CelestialBody("ovok")
                                                .withMassRadius(4.233e17F, 26)
                                                .withOrbitalParameters(
                                                        12_169, 0.01F, 0.0F, 1.5F, 55.0F)
                                                .withRotationalPeriod(29_440)
                                                .withColor(0.361F, 0.361F, 0.361F),
                                        new CelestialBody("eeloo")
                                                .withMassRadius(1.115e21F, 210)
                                                .withOrbitalParameters(
                                                        19_106, 0.0034F, 0.0F, 2.3F, 55.0F)
                                                .withRotationalPeriod(57_915)
                                                .withColor(0.62F, 0.647F, 0.655F),
                                        new CelestialBody("slate")
                                                .withMassRadius(2.965e22F, 540)
                                                .withOrbitalParameters(
                                                        42_593, 0.04F, 0.0F, 2.3F, 55.0F)
                                                .withRotationalPeriod(192_771)
                                                .withColor(0.81F, 0.62F, 0.483F),
                                        new CelestialBody(
                                                        "tekto", SpaceDimensions.TEKTO, Body.TEKTO)
                                                .withMassRadius(2.883e21F, 480)
                                                .withOrbitalParameters(
                                                        67_355, 0.028F, 0.0F, 9.4F, 55.0F)
                                                .withRotationalPeriod(57_915)
                                                .withColor(0.357F, 0.47F, 0.313F)
                                                .withAxialTilt(25F)
                                                .withMinProcessingLevel(3)
                                                .withTraits(
                                                        new CBT_Atmosphere(fluid("TEKTOAIR"), 1.5F),
                                                        new CBT_Water(fluid("CCL")))
                                                .withBlockTextures(
                                                        ntmBlock("basalt"),
                                                        spaceBlock("rubber_silt"))),
                        new CelestialBody("urlum")
                                .withMassRadius(1.7896e23F, 2_177)
                                .withOrbitalParameters(254_317_012, 0.045F, 0.0F, 0.64F, 61.0F)
                                .withRotationalPeriod(41_000)
                                .withColor(0.67F, 0.86F, 0.922F)
                                .withRings(6.0F, 3, 0.6F, 0.8F, 0.9F)
                                .withGas(fluid("UGAS"))
                                .withSatellites(
                                        new CelestialBody("polta")
                                                .withMassRadius(1.3512e21F, 220)
                                                .withOrbitalParameters(
                                                        11_728, 0.002F, 0.60F, 2.45F, 40.0F)
                                                .withRotationalPeriod(73_017)
                                                .withColor(0.31F, 0.377F, 0.33F),
                                        new CelestialBody("priax")
                                                .withMassRadius(5.0691e19F, 74)
                                                .withOrbitalParameters(
                                                        11_728, 0.002F, 0.0F, 15.5F, 40.0F)
                                                .withRotationalPeriod(73_017)
                                                .withColor(0.251F, 0.24F, 0.212F),
                                        new CelestialBody("wal")
                                                .withMassRadius(7.4427e21F, 370)
                                                .withOrbitalParameters(
                                                        67_553, 0.023F, 0.0F, 1.9F, 40.0F)
                                                .withRotationalPeriod(1_009_410)
                                                .withColor(0.502F, 0.42F, 0.224F)
                                                .withSatellites(
                                                        new CelestialBody("tal")
                                                                .withMassRadius(3.2e18F, 22)
                                                                .withOrbitalParameters(
                                                                        3_109, 0.0F, 0.0F, 1.9F,
                                                                        40.0F)
                                                                .withRotationalPeriod(48_874)
                                                                .withColor(0.596F, 0.51F, 0.416F))),
                        new CelestialBody("neidon")
                                .withMassRadius(2.1228e23F, 2_145)
                                .withOrbitalParameters(409_355_192, 0.0534F, 0.0F, 2.02F, 184.0F)
                                .withRotationalPeriod(40_250)
                                .withColor(0.424F, 0.298F, 0.659F)
                                .withGas(fluid("NGAS"))
                                .withSatellites(
                                        new CelestialBody("thatmo")
                                                .withMassRadius(2.788e21F, 286)
                                                .withOrbitalParameters(
                                                        32_301, 0.0534F, 0.0F, 4.02F, 284.0F)
                                                .withRotationalPeriod(306_443)
                                                .withColor(0.757F, 0.765F, 0.773F)
                                                .withTraits(
                                                        new CBT_Atmosphere(
                                                                fluid("NITROGEN"), 0.005F),
                                                        new CBT_BATTLEFIELD())
                                                .withIce(true),
                                        new CelestialBody("nissee")
                                                .withMassRadius(5.951e18F, 30)
                                                .withOrbitalParameters(
                                                        487_744, 0.0534F, 0.0F, 45.02F, 84.0F)
                                                .withRotationalPeriod(27_924)
                                                .withColor(0.173F, 0.149F, 0.118F)
                                                .withMinProcessingLevel(3)),
                        new CelestialBody("plock")
                                .withMassRadius(7.768e20F, 189)
                                .withOrbitalParameters(535_833_706, 0.26F, 0.50F, 6.15F, 260.0F)
                                .withRotationalPeriod(106_309)
                                .withColor(0.737F, 0.592F, 0.443F)
                                .withSatellites(
                                        new CelestialBody("karen")
                                                .withMassRadius(7.014e19F, 85)
                                                .withOrbitalParameters(
                                                        2_458, 0.0F, 50.0F, 0.0F, 260.0F)
                                                .withRotationalPeriod(106_327)
                                                .withColor(0.483F, 0.392F, 0.341F)));
    }

    private static Fluid fluid(String legacyName) {
        return NTMFluids.byName(legacyName);
    }

    private static Identifier space(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }

    private static Identifier spaceBlock(String name) {
        return space("textures/block/" + name + ".png");
    }

    private static Identifier ntmBlock(String name) {
        return Identifier.fromNamespaceAndPath("hbm", "textures/block/" + name + ".png");
    }

    private static Identifier vanillaBlock(String name) {
        return Identifier.withDefaultNamespace("textures/block/" + name + ".png");
    }

    public enum Body implements StringRepresentable {
        ORBIT(""),
        KERBIN("kerbin"),
        MUN("mun"),
        MINMUS("minmus"),
        DUNA("duna"),
        MOHO("moho"),
        DRES("dres"),
        EVE("eve"),
        IKE("ike"),
        LAYTHE("laythe"),
        TEKTO("tekto");

        public static final Codec<Body> CODEC = StringRepresentable.fromEnum(Body::values);

        public final String name;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        Body(String name) {
            this.name = name;
        }

        private @Nullable CelestialBody body;

        public @Nullable CelestialBody getBody() {
            if (this == ORBIT) return null;

            if (body == null) body = CelestialBody.getBody(name);

            return body;
        }

        public int getProcessingLevel(CelestialBody from) {
            if (this == ORBIT) return 0;
            return getBody().getProcessingLevel(from);
        }

        public @Nullable Identifier getStoneTexture() {
            if (this == ORBIT) return null;
            return getBody().stoneTexture;
        }

        public @Nullable Identifier getSurfaceTexture() {
            if (this == ORBIT) return null;
            return getBody().surfaceTexture;
        }

        public ResourceKey<Level> getDimension() {
            if (this == ORBIT) return SpaceDimensions.ORBIT;
            return getBody().dimension;
        }
    }

    public static class AstroMetric {

        public double distance;
        public double angle;
        public double inclination;
        public double apparentSize;
        public double phase;
        public double phaseObscure;

        protected Vec3 position;

        public CelestialBody body;

        public AstroMetric(CelestialBody body, Vec3 position) {
            this.body = body;
            this.position = position;
        }
    }

    public static class OrreryMetric {

        public Vec3 position;
        public Vec3[] orbitalPath;

        public CelestialBody body;

        private static final int PATH_RESOLUTION = 32;

        public OrreryMetric(CelestialBody body, Vec3 position) {
            this.body = body;
            this.position = position;
            this.orbitalPath = new Vec3[PATH_RESOLUTION];
        }
    }

    private static double ticks(Level level, float partialTicks) {
        return ((double) level.getGameTime() + partialTicks)
                * (double) AstronomyUtil.TIME_MULTIPLIER;
    }

    public static List<OrreryMetric> calculatePositionsOrrery(Level level, float partialTicks) {
        List<OrreryMetric> metrics = new ArrayList<>();

        calculatePositionsRecursiveOrrery(
                metrics,
                null,
                CelestialBody.getBody(level).getStar(),
                ticks(level, partialTicks),
                0);

        return metrics;
    }

    public static List<AstroMetric> calculateMetricsFromBody(
            Level level, float partialTicks, CelestialBody body, float solarAngle) {
        List<AstroMetric> metrics = new ArrayList<>();

        double rotOffset = -120.0F;
        CelestialBody tidalLockedBody =
                body.tidallyLockedTo != null ? CelestialBody.getBody(body.tidallyLockedTo) : null;
        if (tidalLockedBody != null) {
            if (tidalLockedBody.getPlanet() != body) {
                tidalLockedBody = body;
                rotOffset += 180.0F;
            }
        }

        calculatePositionsRecursive(
                metrics,
                null,
                body.getStar(),
                ticks(level, partialTicks),
                tidalLockedBody,
                solarAngle * 360.0,
                rotOffset);

        calculateMetricsFromBody(metrics, body);

        metrics.sort((a, b) -> (int) (b.distance - a.distance));

        return metrics;
    }

    public static List<AstroMetric> calculateMetricsFromSatellite(
            Level level, float partialTicks, CelestialBody orbiting, double altitude) {
        List<AstroMetric> metrics = new ArrayList<>();

        double ticks = ticks(level, partialTicks);

        calculatePositionsRecursive(metrics, null, orbiting.getStar(), ticks);

        Vec3 position = calculatePositionSatellite(orbiting, altitude, ticks);
        for (AstroMetric metric : metrics) {
            if (metric.body == orbiting) {
                position = position.add(metric.position.x, metric.position.y, metric.position.z);
                break;
            }
        }

        calculateMetricsFromPosition(metrics, position);

        metrics.sort((a, b) -> (int) (b.distance - a.distance));

        return metrics;
    }

    public static List<AstroMetric> calculateMetricsBetweenSatelliteOrbits(
            Level level,
            float partialTicks,
            CelestialBody from,
            CelestialBody to,
            double fromAltitude,
            double toAltitude,
            double t) {
        List<AstroMetric> metrics = new ArrayList<>();

        double ticks = ticks(level, partialTicks);

        calculatePositionsRecursive(metrics, null, from.getStar(), ticks);

        Vec3 fromPos = calculatePositionSatellite(from, fromAltitude, ticks);
        Vec3 toPos = calculatePositionSatellite(to, toAltitude, ticks);
        for (AstroMetric metric : metrics) {
            if (metric.body == from) {
                fromPos = fromPos.add(metric.position.x, metric.position.y, metric.position.z);
            }
            if (metric.body == to) {
                toPos = toPos.add(metric.position.x, metric.position.y, metric.position.z);
            }
        }

        Vec3 position = lerp(fromPos, toPos, t);

        calculateMetricsFromPosition(metrics, position);

        metrics.sort((a, b) -> (int) (b.distance - a.distance));

        return metrics;
    }

    public static double calculateDistanceBetweenTwoBodies(
            Level level, CelestialBody from, CelestialBody to) {
        List<AstroMetric> metrics = new ArrayList<>();

        calculatePositionsRecursive(metrics, null, from.getStar(), ticks(level, 0));

        Vec3 fromPos = Vec3.ZERO;
        Vec3 toPos = Vec3.ZERO;
        for (AstroMetric metric : metrics) {
            if (metric.body == from) fromPos = metric.position;
            if (metric.body == to) toPos = metric.position;
        }

        return fromPos.distanceTo(toPos);
    }

    private static double clampedLerp(double start, double end, double delta) {
        if (delta < 0.0D) return start;
        return delta > 1.0D ? end : start + delta * (end - start);
    }

    private static Vec3 lerp(Vec3 from, Vec3 to, double t) {
        return new Vec3(
                clampedLerp(from.x, to.x, t),
                clampedLerp(from.y, to.y, t),
                clampedLerp(from.z, to.z, t));
    }

    private static void calculatePositionsRecursive(
            List<AstroMetric> metrics,
            @Nullable AstroMetric parentMetric,
            CelestialBody body,
            double ticks) {
        calculatePositionsRecursive(metrics, parentMetric, body, ticks, null, 0, 0);
    }

    private static void calculatePositionsRecursive(
            List<AstroMetric> metrics,
            @Nullable AstroMetric parentMetric,
            CelestialBody body,
            double ticks,
            @Nullable CelestialBody lockBody,
            double solarAngle,
            double rotOffset) {
        Vec3 parentPosition = parentMetric != null ? parentMetric.position : Vec3.ZERO;

        for (CelestialBody satellite : body.satellites) {
            Vec3 position =
                    satellite == lockBody
                            ? calculatePositionFromAngle(
                                    satellite,
                                    solarAngle
                                            + calculateSiderealAngle(satellite, ticks)
                                            - Math.toDegrees(satellite.argumentPeriapsis)
                                            - Math.toDegrees(satellite.ascendingNode)
                                            + rotOffset)
                            : calculatePositionFromTime(satellite, ticks);
            position = position.add(parentPosition.x, parentPosition.y, parentPosition.z);
            AstroMetric metric = new AstroMetric(satellite, position);

            metrics.add(metric);

            calculatePositionsRecursive(
                    metrics, metric, satellite, ticks, lockBody, solarAngle, rotOffset);
        }
    }

    private static void calculatePositionsRecursiveOrrery(
            List<OrreryMetric> metrics,
            @Nullable OrreryMetric parentMetric,
            CelestialBody body,
            double ticks,
            int depth) {
        Vec3 parentPosition = parentMetric != null ? parentMetric.position : Vec3.ZERO;

        double distance = Math.min(body.radiusKm, ORRERY_MAX_RADIUS) * 1.42;
        for (CelestialBody satellite : body.satellites) {
            double extraDistance =
                    Mth.clamp(satellite.radiusKm, ORRERY_MIN_RADIUS, ORRERY_MAX_RADIUS)
                            * ((1 + satellite.eccentricity) * 2)
                            * 1.42;
            for (CelestialBody inner : satellite.satellites) {
                extraDistance +=
                        Mth.clamp(inner.radiusKm, ORRERY_MIN_RADIUS, ORRERY_MAX_RADIUS)
                                * ((1 + inner.eccentricity) * 2)
                                * 2;
            }

            distance += extraDistance;

            Vec3 position = calculatePositionFromTime(satellite, ticks, distance);
            position = position.add(parentPosition.x, parentPosition.y, parentPosition.z);

            OrreryMetric metric = new OrreryMetric(satellite, position);
            for (int i = 0; i < metric.orbitalPath.length; i++) {
                metric.orbitalPath[i] =
                        calculatePositionFromAngle(
                                        satellite,
                                        (float) i / metric.orbitalPath.length * 360,
                                        distance)
                                .add(parentPosition.x, parentPosition.y, parentPosition.z);
            }

            metrics.add(metric);

            distance += extraDistance;

            calculatePositionsRecursiveOrrery(metrics, metric, satellite, ticks, depth + 1);
        }
    }

    private static Vec3 calculatePositionFromTime(CelestialBody body, double ticks) {
        return calculatePositionFromTime(body, ticks, body.semiMajorAxisKm);
    }

    private static Vec3 calculatePositionFromTime(
            CelestialBody body, double ticks, double semiMajorAxis) {
        double yearTicks = body.getOrbitalPeriod() * (double) AstronomyUtil.TICKS_IN_DAY;
        double meanAnomaly = 2 * Math.PI * (ticks / yearTicks);

        return calculatePosition(body, meanAnomaly, semiMajorAxis);
    }

    private static Vec3 calculatePositionFromAngle(CelestialBody body, double angle) {
        return calculatePosition(body, Math.toRadians(angle), body.semiMajorAxisKm);
    }

    private static Vec3 calculatePositionFromAngle(
            CelestialBody body, double angle, double semiMajorAxis) {
        return calculatePosition(body, Math.toRadians(angle), semiMajorAxis);
    }

    private static Vec3 calculatePosition(
            CelestialBody body, double meanAnomaly, double semiMajorAxis) {
        double eccentricAnomaly = calculateEccentricAnomaly(meanAnomaly, body.eccentricity);

        double x = semiMajorAxis * (Math.cos(eccentricAnomaly) - body.eccentricity);
        double y = semiMajorAxis * body.semiMinorAxisFactor * Math.sin(eccentricAnomaly);
        double z;

        double px = x;
        x = Math.cos(body.argumentPeriapsis) * px - Math.sin(body.argumentPeriapsis) * y;
        y = Math.sin(body.argumentPeriapsis) * px + Math.cos(body.argumentPeriapsis) * y;

        z = Math.sin(body.inclination) * y;
        y = Math.cos(body.inclination) * y;

        px = x;
        x = Math.cos(body.ascendingNode) * px - Math.sin(body.ascendingNode) * y;
        y = Math.sin(body.ascendingNode) * px + Math.cos(body.ascendingNode) * y;

        return new Vec3(x, y, z);
    }

    private static Vec3 calculatePositionSatellite(
            CelestialBody body, double altitude, double ticks) {
        double orbitalPeriod =
                2
                        * Math.PI
                        * Math.sqrt(
                                (altitude * altitude * altitude)
                                        / (AstronomyUtil.GRAVITATIONAL_CONSTANT * body.massKg));
        orbitalPeriod /= (double) AstronomyUtil.SECONDS_IN_KSP_DAY;
        double orbitTicks = orbitalPeriod * (double) AstronomyUtil.TICKS_IN_DAY;
        double meanAnomaly = 2 * Math.PI * (ticks / orbitTicks);

        double x = altitude / 1000 * Math.cos(meanAnomaly);
        double y = altitude / 1000 * Math.sin(meanAnomaly);

        return new Vec3(x, y, 0);
    }

    private static final int ECCENTRICITY_ITERATION_COUNT = 4;

    private static double calculateEccentricAnomaly(double meanAnomaly, float eccentricity) {
        double eccentricAnomaly = meanAnomaly;
        for (int i = 0; i < ECCENTRICITY_ITERATION_COUNT; i++) {
            eccentricAnomaly = meanAnomaly + eccentricity * Math.sin(eccentricAnomaly);
        }

        return eccentricAnomaly;
    }

    private static void calculateMetricsFromBody(List<AstroMetric> metrics, CelestialBody body) {
        AstroMetric from = null;
        for (AstroMetric metric : metrics) {
            if (metric.body == body) {
                from = metric;
                break;
            }
        }

        for (AstroMetric to : metrics) {
            if (from == to) continue;

            calculateMetric(to, from.position);
        }
    }

    private static void calculateMetricsFromPosition(List<AstroMetric> metrics, Vec3 position) {
        for (AstroMetric to : metrics) {
            calculateMetric(to, position);
        }
    }

    private static void calculateMetric(AstroMetric metric, Vec3 position) {
        metric.distance = position.distanceTo(metric.position);
        metric.apparentSize =
                getApparentSize(Math.min(metric.body.radiusKm, 3_000), metric.distance);
        metric.angle = getApparentAngleDegrees(position, metric.position);
        metric.inclination = getApparentInclinationDegrees(position, metric.position);
        metric.phase = getPhase(position, metric.position);
        metric.phaseObscure = getPhaseObscure(position, metric.position);
    }

    private static double getApparentSize(double radius, double distance) {
        return 2D * (float) Math.atan((2D * radius) / (2D * distance)) * RENDER_SCALE;
    }

    private static double getApparentAngleDegrees(Vec3 from, Vec3 to) {
        double angleToOrigin = Math.atan2(-from.y, -from.x);
        double angleToTarget = Math.atan2(to.y - from.y, to.x - from.x);

        return Mth.wrapDegrees(Math.toDegrees(angleToOrigin - angleToTarget));
    }

    private static double getApparentInclinationDegrees(Vec3 from, Vec3 to) {
        double x = from.x - to.x;
        double y = from.y - to.y;
        double planeDistance = Math.sqrt(x * x + y * y);
        double offsetDistance = from.z - to.z;

        return Mth.wrapDegrees(Math.toDegrees(Math.atan2(offsetDistance, planeDistance)));
    }

    private static double getPhase(Vec3 from, Vec3 to) {
        return getApparentAngleDegrees(to, from) / 180.0;
    }

    private static double getPhaseObscure(Vec3 from, Vec3 to) {
        return Math.min(
                Math.abs(getPhase(from, to)),
                1.0F - Math.abs(getApparentInclinationDegrees(from, to) / 180.0F));
    }

    public static double calculateSunSize(CelestialBody from) {
        if (from.parent == null) return 0;
        if (from.parent.parent != null) return calculateSunSize(from.parent);
        return getApparentSize(from.parent.radiusKm, from.semiMajorAxisKm);
    }

    public static double calculateSingleAngle(
            List<AstroMetric> metrics, CelestialBody from, CelestialBody to) {
        AstroMetric metricFrom = null;
        AstroMetric metricTo = null;

        for (AstroMetric metric : metrics) {
            if (metric.body == from) {
                metricFrom = metric;
            } else if (metric.body == to) {
                metricTo = metric;
            }
        }

        if (metricFrom == null || metricTo == null) return 0;

        return getApparentAngleDegrees(metricFrom.position, metricTo.position);
    }

    public static double calculateSingleAngle(
            Level level,
            double partialTicks,
            List<AstroMetric> metrics,
            CelestialBody orbiting,
            double altitude) {
        double ticks =
                ((double) level.getGameTime() + partialTicks)
                        * (double) AstronomyUtil.TIME_MULTIPLIER;

        Vec3 from = calculatePositionSatellite(orbiting, altitude, ticks);
        Vec3 to = Vec3.ZERO;
        for (AstroMetric metric : metrics) {
            if (metric.body == orbiting) {
                to = metric.position;
                from = from.add(to.x, to.y, to.z);
                break;
            }
        }

        return getApparentAngleDegrees(from, to);
    }

    public static double calculateSingleAngle(Level level, CelestialBody from, CelestialBody to) {
        List<AstroMetric> metrics = new ArrayList<>();

        calculatePositionsRecursive(metrics, null, from.getStar(), ticks(level, 0));

        AstroMetric metricFrom = null;
        AstroMetric metricTo = null;

        for (AstroMetric metric : metrics) {
            if (metric.body == from) {
                metricFrom = metric;
            } else if (metric.body == to) {
                metricTo = metric;
            }
        }

        return getApparentAngleDegrees(metricFrom.position, metricTo.position);
    }

    public static double calculateSiderealAngle(
            Level level, float partialTicks, CelestialBody body) {
        return calculateSiderealAngle(body, ticks(level, partialTicks));
    }

    public static double calculateSiderealAngle(CelestialBody body, double ticks) {
        body = body.getPlanet();

        Vec3 position = calculatePositionFromTime(body, ticks);

        return Math.toDegrees(Math.atan2(position.y, position.x));
    }

    public static int getCostBetween(
            CelestialBody from,
            CelestialBody to,
            int mass,
            int thrust,
            int isp,
            boolean fromOrbit,
            boolean toOrbit) {
        double fromDrag = getAtmosphericDrag(from.getTrait(CBT_Atmosphere.class));
        double toDrag = getAtmosphericDrag(to.getTrait(CBT_Atmosphere.class));

        double launchDV =
                fromOrbit ? 0 : SolarSystem.getLiftoffDeltaV(from, mass, thrust, fromDrag);
        double travelDV = SolarSystem.getDeltaVBetween(from, to);
        double landerDV = toOrbit ? 0 : SolarSystem.getLandingDeltaV(to, mass, thrust, toDrag);

        double totalDV = launchDV + travelDV + landerDV;

        return getFuelCost(totalDV, mass, isp);
    }

    public static int getFuelCost(double deltaV, int mass, int isp) {
        double g0 = 9.81;
        double exhaustVelocity = isp * g0;
        double massFraction = 1 - Math.exp(-(deltaV / exhaustVelocity));

        double totalMass = mass / (1 - massFraction);
        double propellantMass = totalMass - mass;
        double propellantVolume = propellantMass / 2;

        return propellantVolume + 100 > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : Mth.ceil(propellantVolume * 0.01D) * 100;
    }

    private static double getAtmosphericDrag(@Nullable CBT_Atmosphere atmosphere) {
        if (atmosphere == null) return 0;
        double pressure = atmosphere.getPressure();
        return Math.log(pressure + 1.0D) / 10.0D;
    }

    public static double getLiftoffDeltaV(
            CelestialBody body, float craftMassKg, float craftThrustN, double atmosphericDrag) {
        return calculateSurfaceToOrbitDeltaV(
                body, craftMassKg, craftThrustN, atmosphericDrag, false);
    }

    public static double getLandingDeltaV(
            CelestialBody body, float craftMassKg, float craftThrustN, double atmosphericDrag) {
        return calculateSurfaceToOrbitDeltaV(
                body, craftMassKg, craftThrustN, atmosphericDrag, atmosphericDrag > 0.006);
    }

    private static double calculateSurfaceToOrbitDeltaV(
            CelestialBody body,
            float craftMassKg,
            float craftThrustN,
            double atmosphericDrag,
            boolean lossesOnly) {
        float gravity = body.getSurfaceGravity();
        double orbitalDeltaV =
                Math.sqrt(
                        (AstronomyUtil.GRAVITATIONAL_CONSTANT * body.massKg)
                                / (body.radiusKm * 1_000));
        double thrustToWeightRatio = craftThrustN / (craftMassKg * gravity);

        if (thrustToWeightRatio < 1) return Double.MAX_VALUE;

        double acceleration = (thrustToWeightRatio - 1) * gravity;
        double timeToOrbit = orbitalDeltaV / acceleration;
        double gravityLosses = gravity * timeToOrbit * 2;

        if (lossesOnly) return gravityLosses * (1 - atmosphericDrag);

        return orbitalDeltaV + gravityLosses * (1 + atmosphericDrag);
    }

    public static double getDeltaVBetween(CelestialBody start, CelestialBody end) {
        return calculateHohmannTransfer(start, end);
    }

    private static double calculateHohmannTransfer(CelestialBody start, CelestialBody end) {
        if (start == end) {
            return 0;
        } else if (start.parent == null || end.parent == null) {
            throw new NotImplementedException("Transfers to and from solar bodies not supported");
        } else if (start.parent == end.parent) {
            double firstBurnCost =
                    calculateSingleHohmannTransfer(
                            start.parent.massKg,
                            start.semiMajorAxisKm,
                            end.semiMajorAxisKm,
                            start.massKg,
                            start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);
            double secondBurnCost =
                    calculateSingleHohmannTransfer(
                            start.parent.massKg,
                            end.semiMajorAxisKm,
                            start.semiMajorAxisKm,
                            end.massKg,
                            end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);

            return firstBurnCost + secondBurnCost;
        } else if (start == end.parent) {
            double firstBurnCost =
                    calculateSingleHohmannTransfer(
                            start.massKg,
                            start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM,
                            end.semiMajorAxisKm);
            double secondBurnCost =
                    calculateSingleHohmannTransfer(
                            start.massKg,
                            end.semiMajorAxisKm,
                            start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM,
                            end.massKg,
                            end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);

            return firstBurnCost + secondBurnCost;
        } else if (start.parent == end) {
            double firstBurnCost =
                    calculateSingleHohmannTransfer(
                            end.massKg,
                            start.semiMajorAxisKm,
                            end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM,
                            start.massKg,
                            start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);
            double secondBurnCost =
                    calculateSingleHohmannTransfer(
                            end.massKg,
                            end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM,
                            start.semiMajorAxisKm);

            return firstBurnCost + secondBurnCost;
        } else {
            CelestialBody commonParent = getCommonParent(start, end);
            CelestialBody fromBody = start;
            CelestialBody toBody = end;
            float currentFromOrbitRadius = fromBody.semiMajorAxisKm;
            float currentToOrbitRadius = toBody.semiMajorAxisKm;

            double burnCost = 0;

            while (fromBody.parent != commonParent) {
                burnCost +=
                        calculateSingleHohmannTransfer(
                                fromBody.parent.massKg,
                                fromBody.semiMajorAxisKm,
                                fromBody.semiMajorAxisKm,
                                fromBody.massKg,
                                fromBody.semiMajorAxisKm);
                currentFromOrbitRadius = fromBody.semiMajorAxisKm;
                fromBody = fromBody.parent;
            }

            while (toBody.parent != commonParent) {
                burnCost +=
                        calculateSingleHohmannTransfer(
                                toBody.parent.massKg,
                                toBody.semiMajorAxisKm,
                                toBody.semiMajorAxisKm,
                                toBody.massKg,
                                toBody.semiMajorAxisKm);
                currentToOrbitRadius = toBody.semiMajorAxisKm;
                toBody = toBody.parent;
            }

            burnCost +=
                    calculateSingleHohmannTransfer(
                            commonParent.massKg,
                            fromBody.semiMajorAxisKm,
                            toBody.semiMajorAxisKm,
                            fromBody.massKg,
                            currentFromOrbitRadius);
            burnCost +=
                    calculateSingleHohmannTransfer(
                            commonParent.massKg,
                            toBody.semiMajorAxisKm,
                            fromBody.semiMajorAxisKm,
                            toBody.massKg,
                            currentToOrbitRadius);

            return burnCost;
        }
    }

    private static CelestialBody getCommonParent(CelestialBody start, CelestialBody end) {
        CelestialBody startParent = start.parent;

        while (startParent != null) {
            CelestialBody endParent = end.parent;
            while (endParent != null) {
                if (startParent == endParent) return startParent;

                endParent = endParent.parent;
            }
            startParent = startParent.parent;
        }

        throw new InvalidParameterException("Bodies aren't in the same solar system");
    }

    private static double calculateSingleHohmannTransfer(
            float parentMassKg, float fromRadiusKm, float toRadiusKm) {
        double parentGravitationalParameter = parentMassKg * AstronomyUtil.GRAVITATIONAL_CONSTANT;

        double startOrbitalRadius = fromRadiusKm * 1_000;
        double endOrbitalRadius = toRadiusKm * 1_000;

        double transferSemiMajorAxis = (startOrbitalRadius + endOrbitalRadius) / 2;

        double currentOrbitalVelocity =
                Math.sqrt(parentGravitationalParameter / startOrbitalRadius);

        double requiredVelocity =
                Math.sqrt(
                        parentGravitationalParameter
                                * ((2 / startOrbitalRadius) - (1 / transferSemiMajorAxis)));

        return Math.abs(requiredVelocity - currentOrbitalVelocity);
    }

    private static double calculateSingleHohmannTransfer(
            float parentMassKg,
            float fromRadiusKm,
            float toRadiusKm,
            float fromMassKg,
            float parkingOrbitRadiusKm) {
        double hyperbolicVelocity =
                calculateSingleHohmannTransfer(parentMassKg, fromRadiusKm, toRadiusKm);

        double fromGravitationalParameter = fromMassKg * AstronomyUtil.GRAVITATIONAL_CONSTANT;

        double parkingOrbitRadius = parkingOrbitRadiusKm * 1_000;
        double parkingOrbitVelocity = Math.sqrt(fromGravitationalParameter / parkingOrbitRadius);

        double escapeVelocity = Math.sqrt((2 * fromGravitationalParameter) / parkingOrbitRadius);
        double escapeHyperVelocity =
                Math.sqrt(
                        hyperbolicVelocity * hyperbolicVelocity + escapeVelocity * escapeVelocity);

        return escapeHyperVelocity - parkingOrbitVelocity;
    }
}
