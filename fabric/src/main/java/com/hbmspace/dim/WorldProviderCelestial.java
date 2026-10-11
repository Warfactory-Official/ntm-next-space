// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-FileCopyrightText: 2025 JamesH_2
// SPDX-FileCopyrightText: 2026 detree05
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.config.SpaceConfig;
import com.hbmspace.dim.SolarSystem.AstroMetric;
import com.hbmspace.dim.orbit.WorldProviderOrbit;
import com.hbmspace.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.dim.trait.CBT_Destroyed;
import com.hbmspace.dim.trait.CBT_War;
import com.hbmspace.dim.trait.CBT_Water;
import com.hbmspace.dim.trait.CBT_Weather;
import com.hbmspace.handler.atmosphere.ChunkAtmosphereManager;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WorldProviderCelestial {

    private static final Map<ResourceKey<Level>, WorldProviderCelestial> PROVIDERS =
            new HashMap<>();

    public final ResourceKey<Level> dimension;

    private WeakReference<Level> eclipseLevel = new WeakReference<>(null);
    private long eclipseTime;
    private double eclipseAmount;

    public WorldProviderCelestial(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public static void register(WorldProviderCelestial provider) {
        if (PROVIDERS.putIfAbsent(provider.dimension, provider) != null) {
            throw new IllegalStateException(
                    provider.dimension + " already has a celestial provider");
        }
    }

    public static @Nullable WorldProviderCelestial of(Level level) {
        return PROVIDERS.get(level.dimension());
    }

    public CelestialBody body() {
        return CelestialBody.getBody(dimension);
    }

    public static @Nullable TeleportTransition respawn(
            ServerPlayer player, TeleportTransition.PostTeleportTransition then) {
        ServerPlayer.RespawnConfig config = player.getRespawnConfig();
        if (WorldTypeTeleport.isMartian() && player.level().dimension() == SpaceDimensions.DUNA) {
            if (config != null && config.respawnData().dimension() == SpaceDimensions.DUNA)
                return null;
            ServerLevel duna = player.level();
            LevelData.RespawnData spawn = duna.getRespawnData();
            return new TeleportTransition(
                    duna,
                    Vec3.atBottomCenterOf(player.adjustSpawnLocation(duna, spawn.pos())),
                    Vec3.ZERO,
                    spawn.yaw(),
                    spawn.pitch(),
                    then);
        }
        if (config == null) return null;
        ServerLevel level = player.level().getServer().getLevel(config.respawnData().dimension());
        if (level == null
                || !CelestialBody.inOrbit(level)
                        && (level.dimension() == Level.OVERWORLD || of(level) == null)) {
            return null;
        }
        if (player.level() == level
                && ChunkAtmosphereManager.proxy.canBreathe(
                        ChunkAtmosphereManager.proxy.getAtmosphere(
                                level, config.respawnData().pos()))) {
            return null;
        }
        return TeleportTransition.createDefault(player, then);
    }

    public BlockState getStone() {
        return Blocks.STONE.defaultBlockState();
    }

    public @Nullable FluidStackNTM getBedrockAcid() {
        return null;
    }

    public boolean hasIce() {
        return body().hasIce;
    }

    public boolean hasLife() {
        return false;
    }

    public int getWaterOpacity() {
        return 3;
    }

    public boolean hasWeatherCycle() {
        return CBT_Weather.supportsWeather(body());
    }

    public static int getCloudLayerCount(@Nullable CBT_Atmosphere atmosphere) {
        if (atmosphere == null || atmosphere.getPressure() < 0.5F) {
            return 0;
        }

        if (atmosphere.getPressure() >= 5.0F) {
            return 3;
        }

        if (atmosphere.getPressure() >= 2.5F) {
            return 2;
        }

        return 1;
    }

    public double getDayLength() {
        CelestialBody body = body();
        return body.getRotationalPeriod() / (1 - (1 / body.getPlanet().getOrbitalPeriod()));
    }

    public static float calculateCelestialAngle(
            long worldTime, float partialTicks, double dayLength) {
        double j = worldTime % dayLength;
        double f1 = (j + partialTicks) / dayLength - 0.25F;

        if (f1 < 0.0F) {
            ++f1;
        }

        if (f1 > 1.0F) {
            --f1;
        }

        double f2 = f1;
        f1 = 0.5F - Math.cos(f1 * Math.PI) / 2.0F;
        return (float) (f2 + (f1 - f2) / 3.0D);
    }

    public float getCelestialAngle(Level level, float partialTicks) {
        return calculateCelestialAngle(level.getDefaultClockTime(), partialTicks, getDayLength());
    }

    public static double getEclipseFactor(
            List<AstroMetric> metrics, double sunSize, double maxSize) {
        double factor = 0;
        double sunArc = getArc(sunSize);

        for (AstroMetric metric : metrics) {
            if (metric.apparentSize < 1) continue;

            double planetArc = getArc(Mth.clamp(metric.apparentSize, 0, maxSize));
            double minPhase = 1 - (planetArc + sunArc);
            double maxPhase = 1 - (planetArc - sunArc);
            if (metric.phaseObscure < minPhase) continue;

            double thisFactor = 1 - (metric.phaseObscure - maxPhase) / (minPhase - maxPhase);

            factor = Math.min(Math.max(factor, thisFactor), 1.0);
        }

        return factor;
    }

    private static double getArc(double apparentSize) {
        return apparentSize * 0.0017D + Math.sqrt(apparentSize * 0.00003D);
    }

    public double getEclipseAmount(Level level) {
        boolean cached = !level.isClientSide();
        long time = level.getGameTime();
        if (cached && level == eclipseLevel.get() && time == eclipseTime) return eclipseAmount;

        CelestialBody body = body();
        double sunSize = SolarSystem.calculateSunSize(body);
        float solarAngle = getCelestialAngle(level, 0);

        List<AstroMetric> metrics =
                SolarSystem.calculateMetricsFromBody(level, 0, body, solarAngle);
        double amount = getEclipseFactor(metrics, sunSize, SolarSystem.MAX_APPARENT_SIZE_SURFACE);

        if (cached) {
            eclipseAmount = amount;
            eclipseLevel = new WeakReference<>(level);
            eclipseTime = time;
        }
        return amount;
    }

    public boolean isEclipse(Level level) {
        return getEclipseAmount(level) > 0.0;
    }

    public float getSunPower(Level level) {
        return body().getSunPower() * (1 - (float) getEclipseAmount(level));
    }

    public static float getSunPower(Level level, int x, int z) {
        if (CelestialBody.inOrbit(level)) return WorldProviderOrbit.getSunPower(level, x, z);

        WorldProviderCelestial provider = of(level);
        if (provider != null) return provider.getSunPower(level);

        return CelestialBody.getBody(level).getSunPower();
    }

    public int getHorizon(Level level) {
        return 63;
    }

    public void updateWeather(ServerLevel level) {}

    public void resetRainAndThunder(ServerLevel level) {}

    public void addLayers(EnvironmentAttributeSystem.Builder builder, Level level) {}

    public record Sky(float solarAngle, double eclipse, float dust, float fire) {}

    private static final int BLACK = 0xFF000000;

    public int getSkyColor(Level level, int vanilla, Sky sky) {
        CelestialBody body = body();
        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
        if (atmosphere == null) return BLACK;

        float sun = getSunBrightnessFactor(level, sky.solarAngle());
        float totalPressure = (float) atmosphere.getPressure();
        double r = 0, g = 0, b = 0;

        for (FluidEntry entry : atmosphere.fluids) {
            double fr, fg, fb;
            if (entry.fluid == gas("EVEAIR")) {
                fr = 53F / 255F * sun;
                fg = 32F / 255F * sun;
                fb = 74F / 255F * sun;
            } else if (entry.fluid == gas("DUNAAIR") || entry.fluid == NTMFluids.CARBONDIOXIDE) {
                fr = 212F / 255F * sun;
                fg = 112F / 255F * sun;
                fb = 78F / 255F * sun;
            } else if (isEarthLike(entry.fluid)) {
                fr = ARGB.redFloat(vanilla);
                fg = ARGB.greenFloat(vanilla);
                fb = ARGB.blueFloat(vanilla);
            } else {
                int hex = hex(entry.fluid);
                fr = ARGB.redFloat(hex) * sun;
                fg = ARGB.greenFloat(hex) * sun;
                fb = ARGB.blueFloat(hex) * sun;
            }

            float percentage = (float) entry.pressure / totalPressure;
            r += fr * percentage;
            g += fg * percentage;
            b += fb * percentage;
        }

        float flash = warFlash(body) * 0.5F;
        r += flash;
        g += flash;
        b += flash;

        float pressureFactor = Mth.clamp(totalPressure, 0.0F, 1.0F);
        r *= pressureFactor;
        g *= pressureFactor;
        b *= pressureFactor;

        if (sky.eclipse() > 0) {
            r *= 1 - sky.eclipse() * 0.6;
            g *= 1 - sky.eclipse() * 0.6;
            b *= 1 - sky.eclipse() * 0.5;
        }

        float dust = sky.dust();
        float fire = sky.fire();
        if (dust > 0) {
            if (fire > 0) {
                r *= 1.3;
                g *= Math.max(1 - dust * 1.4F, 0);
                b *= Math.max(1 - dust * 4, 0);
            } else {
                g *= 1 - dust * 0.5F;
                b *= Math.max(1 - dust * 4, 0);
            }

            r *= fire + (1 - dust);
            g *= fire + (1 - dust);
            b *= fire + (1 - dust);
        }

        return rgb(r, g, b);
    }

    public int getFogColor(Level level, Sky sky, double cameraY) {
        CBT_Atmosphere atmosphere = body().getTrait(CBT_Atmosphere.class);
        if (atmosphere == null) return BLACK;

        float sun =
                Mth.clamp(
                        Mth.cos(sky.solarAngle() * (float) Math.PI * 2.0F) * 2.0F + 0.5F,
                        0.0F,
                        1.0F);
        float sunR = sun;
        float sunG = sun;
        float sunB = sun;
        if (!SpaceConfig.enableHardcoreDarkness) {
            sunR *= 0.94F;
            sunG *= 0.94F;
            sunB *= 0.91F;
        }

        float totalPressure = (float) atmosphere.getPressure();
        double r = 0, g = 0, b = 0;

        for (FluidEntry entry : atmosphere.fluids) {
            double fr, fg, fb;
            if (entry.fluid == gas("EVEAIR")) {
                fr = 53F / 255F * sunR;
                fg = 32F / 255F * sunG;
                fb = 74F / 255F * sunB;
            } else if (entry.fluid == gas("DUNAAIR") || entry.fluid == NTMFluids.CARBONDIOXIDE) {
                fr = 212F / 255F * sunR;
                fg = 112F / 255F * sunG;
                fb = 78F / 255F * sunB;
            } else if (isEarthLike(entry.fluid)) {
                fr = 0.7529412F * sunR;
                fg = 0.84705883F * sunG;
                fb = 1.0F * sunB;
            } else {
                int hex = hex(entry.fluid);
                fr = ARGB.redFloat(hex) * (sunR * 1.4F);
                fg = ARGB.greenFloat(hex) * (sunG * 1.4F);
                fb = ARGB.blueFloat(hex) * (sunB * 1.4F);
            }

            float percentage = (float) entry.pressure / totalPressure;
            r += fr * percentage;
            g += fg * percentage;
            b += fb * percentage;
        }

        if (!SpaceConfig.enableHardcoreDarkness) {
            float nightDensity = Mth.clamp(totalPressure, 0.0F, 1.0F);
            r += 0.06F * nightDensity;
            g += 0.06F * nightDensity;
            b += 0.09F * nightDensity;
        }

        float pressureFactor = Mth.clamp(totalPressure * 10.0F, 0.0F, 1.0F);
        r *= pressureFactor;
        g *= pressureFactor;
        b *= pressureFactor;

        if (cameraY > 600) {
            double curvature = Mth.clamp((1000.0F - (float) cameraY) / 400.0F, 0.0F, 1.0F);
            r *= curvature;
            g *= curvature;
            b *= curvature;
        }

        if (sky.eclipse() > 0) {
            r *= 1 - sky.eclipse() * 0.3;
            g *= 1 - sky.eclipse() * 0.3;
            b *= 1 - sky.eclipse() * 0.3;

            float[] sunsetFog = sunrise(atmosphere, legacySunrise(0.25F), 0.25F, sky.dust());
            if (sunsetFog != null) {
                double sunsetAmount = Mth.clamp(sky.eclipse() * 0.5 - (1 - sun), 0.0, 1.0);
                r = r * (1.0F - sunsetAmount) + sunsetFog[0] * sunsetAmount;
                g = g * (1.0F - sunsetAmount) + sunsetFog[1] * sunsetAmount;
                b = b * (1.0F - sunsetAmount) + sunsetFog[2] * sunsetAmount;
            }
        }

        float dust = sky.dust();
        g *= 1 - (dust * 0.5F);
        b *= 1 - dust;

        if (sky.fire() > 0) {
            float thinned = Math.max(1 - (dust * 2), 0);
            r *= thinned;
            g *= thinned;
            b *= thinned;
        } else {
            r *= 1 - dust;
            g *= 1 - dust;
            b *= 1 - dust;
        }

        return rgb(r, g, b);
    }

    public int getSunriseColor(Level level, int vanilla, Sky sky) {
        CBT_Atmosphere atmosphere = body().getTrait(CBT_Atmosphere.class);
        if (atmosphere == null || atmosphere.getPressure() < 0.05F || ARGB.alpha(vanilla) == 0)
            return 0;

        float[] colors = {
            ARGB.redFloat(vanilla),
            ARGB.greenFloat(vanilla),
            ARGB.blueFloat(vanilla),
            ARGB.alphaFloat(vanilla)
        };
        colors = sunrise(atmosphere, colors, sky.solarAngle(), sky.dust());
        return colors == null
                ? 0
                : ARGB.colorFromFloat(
                        Mth.clamp(colors[3], 0.0F, 1.0F),
                        Mth.clamp(colors[0], 0.0F, 1.0F),
                        Mth.clamp(colors[1], 0.0F, 1.0F),
                        Mth.clamp(colors[2], 0.0F, 1.0F));
    }

    private static float @Nullable [] sunrise(
            CBT_Atmosphere atmosphere, float[] colors, float solarAngle, float dust) {
        if (atmosphere.hasFluid(gas("DUNAAIR")) || atmosphere.hasFluid(NTMFluids.CARBONDIOXIDE)) {
            float tmp = colors[0];
            colors[0] = colors[2];
            colors[2] = tmp;
        } else if (atmosphere.hasFluid(gas("EVEAIR"))) {
            float f3 = Mth.cos(solarAngle * (float) Math.PI * 2.0F);
            if (f3 < -0.4F || f3 > 0.4F) return null;
            float f5 = f3 / 0.4F * 0.5F + 0.5F;
            float f6 = 1.0F - (1.0F - Mth.sin(f5 * (float) Math.PI)) * 0.99F;
            f6 *= f6;
            colors[0] = f5 * 0.01F;
            colors[1] = f5 * f5 * 0.9F + 0.3F;
            colors[2] = f5 * f5;
            colors[3] = f6;
        } else if (atmosphere.hasFluid(gas("TEKTOAIR"))
                || atmosphere.hasFluid(gas("JOOLGAS"))
                || atmosphere.hasFluid(NTMFluids.CHLORINE)) {
            float tmp = colors[1];
            colors[1] = colors[2];
            colors[2] = tmp;
        }

        float dustFactor = 1 - dust;
        colors[0] *= dustFactor;
        colors[1] *= dustFactor;
        colors[2] *= dustFactor;
        colors[3] *= dustFactor;
        return colors;
    }

    private static float[] legacySunrise(float solarAngle) {
        float f3 = Mth.cos(solarAngle * (float) Math.PI * 2.0F);
        float f5 = f3 / 0.4F * 0.5F + 0.5F;
        float f6 = 1.0F - (1.0F - Mth.sin(f5 * (float) Math.PI)) * 0.99F;
        f6 *= f6;
        return new float[] {f5 * 0.3F + 0.7F, f5 * f5 * 0.7F + 0.2F, f5 * f5 * 0.0F + 0.2F, f6};
    }

    public float getStarBrightness(float vanilla, float dust) {
        float distanceStart = 20_000_000;
        float distanceEnd = 80_000_000;

        float semiMajorAxisKm = body().getPlanet().semiMajorAxisKm;
        float distanceFactor =
                Mth.clamp(
                        (semiMajorAxisKm - distanceStart) / (distanceEnd - distanceStart), 0F, 1F);

        return Mth.clamp(vanilla, distanceFactor, 1F) * (1 - dust);
    }

    public float getSunBrightness(float vanilla, Sky sky) {
        CelestialBody body = body();
        if (body.getStar().hasTrait(CBT_Destroyed.class)) return 0;

        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);

        float sunBrightness = vanilla;
        sunBrightness *= 1 - sky.eclipse() * 0.6;
        sunBrightness *= (1 - sky.dust());

        float insideBrightness = warFlash(body);

        if (atmosphere == null) return sunBrightness + insideBrightness;

        return sunBrightness
                        * Mth.clamp(
                                1.0F - ((float) atmosphere.getPressure() - 1.5F) * 0.2F,
                                0.25F,
                                1.0F)
                + insideBrightness;
    }

    public int getCloudColor(int vanilla) {
        CBT_Atmosphere atmosphere = body().getTrait(CBT_Atmosphere.class);
        if (getCloudLayerCount(atmosphere) <= 0) return ARGB.color(0, vanilla);

        Vec3 tinted =
                CloudProviderCelestial.getTintedCloudColor(
                        atmosphere,
                        new Vec3(
                                ARGB.redFloat(vanilla),
                                ARGB.greenFloat(vanilla),
                                ARGB.blueFloat(vanilla)));
        return ARGB.color(ARGB.alpha(vanilla), rgb(tinted.x, tinted.y, tinted.z));
    }

    public MoonPhase getMoonPhase(Level level) {
        CelestialBody body = body();
        if (body.satellites.isEmpty()) return MoonPhase.THIRD_QUARTER;

        int phase =
                Math.round(
                        8
                                - ((float)
                                                        SolarSystem.calculateSingleAngle(
                                                                level,
                                                                body,
                                                                body.satellites.getFirst())
                                                / 45
                                        + 4));
        return phase >= 8 ? MoonPhase.FULL_MOON : MoonPhase.values()[phase];
    }

    public static Vec3 getAtmosphereFluidColor(@Nullable Fluid fluid) {
        if (fluid == null) return new Vec3(1.0D, 1.0D, 1.0D);
        if (fluid == gas("EVEAIR")) return new Vec3(53F / 255F, 32F / 255F, 74F / 255F);
        if (fluid == gas("DUNAAIR")) return new Vec3(198F / 255F, 96F / 255F, 64F / 255F);
        if (fluid == NTMFluids.CARBONDIOXIDE)
            return new Vec3(188F / 255F, 192F / 255F, 198F / 255F);
        if (isEarthLike(fluid)) return new Vec3(0.7529412F, 0.84705883F, 1.0F);

        int hex = hex(fluid);
        return new Vec3(ARGB.redFloat(hex), ARGB.greenFloat(hex), ARGB.blueFloat(hex));
    }

    public int getWeatherColor() {
        CBT_Water water = body().getTrait(CBT_Water.class);
        if (water == null) return 0xFFFFFFFF;

        int sea = hex(water.fluid);
        double r = ARGB.redFloat(sea);
        double g = ARGB.greenFloat(sea);
        double b = ARGB.blueFloat(sea);
        double luminance = r * 0.299D + g * 0.587D + b * 0.114D;
        double saturation = 0.35D;
        return rgb(
                luminance + (r - luminance) * saturation,
                luminance + (g - luminance) * saturation,
                luminance + (b - luminance) * saturation);
    }

    public int getSnowColor() {
        CBT_Water water = body().getTrait(CBT_Water.class);
        if (water == null || water.fluid == NTMFluids.WATER) return 0xFFFFFFFF;
        return getWeatherColor();
    }

    public float[] getSunColor() {
        CBT_Atmosphere atmosphere = body().getTrait(CBT_Atmosphere.class);
        float[] sunColor = {1.0F, 1.0F, 1.0F};
        if (atmosphere == null) return sunColor;

        for (FluidEntry entry : atmosphere.fluids) {
            if (entry.fluid == gas("TEKTOAIR")
                    || entry.fluid == NTMFluids.CHLORINE
                    || entry.fluid == gas("CHLOROMETHANE")
                    || entry.fluid == NTMFluids.RADIOSOLVENT
                    || entry.fluid == gas("CCL")) {
                float absorption = Mth.clamp(1.0F - (float) entry.pressure * 0.5F, 0.0F, 1.0F);
                sunColor[1] *= absorption;
                sunColor[2] *= absorption;
            }
        }

        return sunColor;
    }

    public static float getSunBrightnessFactor(Level level, float solarAngle) {
        float f2 = 1.0F - (Mth.cos(solarAngle * (float) Math.PI * 2.0F) * 2.0F + 0.5F);
        f2 = 1.0F - Mth.clamp(f2, 0.0F, 1.0F);
        f2 = (float) ((double) f2 * (1.0D - (double) (level.getRainLevel(1.0F) * 5.0F) / 16.0D));
        f2 = (float) ((double) f2 * (1.0D - (double) (level.getThunderLevel(1.0F) * 5.0F) / 16.0D));
        return f2;
    }

    private static float warFlash(CelestialBody body) {
        CBT_War war = body.getTrait(CBT_War.class);
        if (war == null) return 0;

        float flash = 0;
        for (CBT_War.Projectile projectile : war.getProjectiles()) {
            if (projectile.getAnimtime() > 0) flash += 100 - projectile.getFlashtime();
        }
        return flash;
    }

    private static boolean isEarthLike(Fluid fluid) {
        return fluid == gas("EARTHAIR") || fluid == NTMFluids.OXYGEN || fluid == gas("NITROGEN");
    }

    private static Fluid gas(String legacyName) {
        return NTMFluids.byName(legacyName);
    }

    private static int hex(Fluid fluid) {
        return NTMFluidProperties.get(fluid).color();
    }

    private static int rgb(double r, double g, double b) {
        return ARGB.colorFromFloat(
                1.0F,
                (float) Mth.clamp(r, 0.0D, 1.0D),
                (float) Mth.clamp(g, 0.0D, 1.0D),
                (float) Mth.clamp(b, 0.0D, 1.0D));
    }
}
