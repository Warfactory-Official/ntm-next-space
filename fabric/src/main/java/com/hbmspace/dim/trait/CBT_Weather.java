// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 detree05
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

public class CBT_Weather extends CelestialBodyTrait {

    private static final RandomSource WEATHER_RANDOM = RandomSource.create();
    private static final int SAVE_INTERVAL = 200;
    private static final float LIGHTNING_CLOUD_PRESSURE = 0.5F;
    private static final float LIGHTNING_HAZE_PRESSURE = 3.0F;
    private static final float LIGHTNING_OPAQUE_PRESSURE = 5.0F;
    private static final float CLOUD_LIGHTNING_ACTIVITY = 0.55F;
    private static final float HAZE_LIGHTNING_ACTIVITY = 0.82F;
    private static final int[][] LIGHTNING_BIOME_SAMPLES =
            new int[][] {
                {0, 0},
                {512, 0},
                {-512, 0},
                {0, 512},
                {0, -512}
            };

    public boolean raining;
    public boolean thundering;
    public boolean canSpawnLightning = true;
    public int rainTime;
    public int thunderTime;
    public float prevRainStrength;
    public float rainStrength;
    public float prevThunderStrength;
    public float thunderStrength;

    private long lastUpdateTick = Long.MIN_VALUE;

    public static boolean supportsWeather(@Nullable CelestialBody body) {
        if (body == null || body.gas != null) {
            return false;
        }

        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
        CBT_Water water = body.getTrait(CBT_Water.class);
        return atmosphere != null
                && atmosphere.getPressure() > 0.5D
                && water != null
                && water.fluid != null;
    }

    public static @Nullable CBT_Weather ensureTrait(@Nullable CelestialBody body) {
        if (body == null) {
            return null;
        }

        CBT_Weather weather = body.getTrait(CBT_Weather.class);
        if (weather == null && supportsWeather(body)) {
            body.modifyTraits(new CBT_Weather());
            weather = body.getTrait(CBT_Weather.class);
        }

        return weather;
    }

    public static void updateGlobalWeather(MinecraftServer server) {
        long tick = server.overworld().getGameTime();
        boolean dirty = false;

        for (CelestialBody body : CelestialBody.getAllBodies()) {
            CBT_Weather weather = ensureTrait(body);
            if (weather == null) {
                continue;
            }

            ServerLevel level = body.getLevel(server);
            RandomSource random = level != null ? level.getRandom() : WEATHER_RANDOM;
            if (weather.updateForTick(tick, random, body, level)) {
                dirty = true;
            }
        }

        if (dirty) {
            SolarSystemWorldSavedData.get(server).setDirty();
        }
    }

    private static float getAtmospherePressure(@Nullable CelestialBody body) {
        if (body == null || body.gas != null) {
            return 0.0F;
        }

        CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);
        return atmosphere != null ? Math.max(0.0F, (float) atmosphere.getPressure()) : 0.0F;
    }

    public static float getLightningSeverity(CelestialBody body) {
        if (!supportsWeather(body)) {
            return 0.0F;
        }

        float pressure = getAtmospherePressure(body);
        if (pressure > LIGHTNING_HAZE_PRESSURE) {
            float hazeMix =
                    Mth.clamp(
                            (pressure - LIGHTNING_HAZE_PRESSURE)
                                    / (LIGHTNING_OPAQUE_PRESSURE - LIGHTNING_HAZE_PRESSURE),
                            0.0F,
                            1.0F);
            return Mth.clamp(0.65F + hazeMix * 0.35F, 0.65F, 1.0F);
        }

        return Mth.clamp(
                (pressure - LIGHTNING_CLOUD_PRESSURE)
                        / (LIGHTNING_HAZE_PRESSURE - LIGHTNING_CLOUD_PRESSURE)
                        * 0.65F,
                0.0F,
                0.65F);
    }

    public static float getLightningActivityFactor(CelestialBody body) {
        if (!supportsWeather(body)) {
            return 0.0F;
        }

        float pressure = getAtmospherePressure(body);
        if (pressure > LIGHTNING_HAZE_PRESSURE) {
            float hazeMix =
                    Mth.clamp(
                            (pressure - LIGHTNING_HAZE_PRESSURE)
                                    / (LIGHTNING_OPAQUE_PRESSURE - LIGHTNING_HAZE_PRESSURE),
                            0.0F,
                            1.0F);
            return Mth.clamp(
                    HAZE_LIGHTNING_ACTIVITY + hazeMix * (1.0F - HAZE_LIGHTNING_ACTIVITY),
                    HAZE_LIGHTNING_ACTIVITY,
                    1.0F);
        }

        float cloudMix =
                Mth.clamp(
                        (pressure - LIGHTNING_CLOUD_PRESSURE)
                                / (LIGHTNING_HAZE_PRESSURE - LIGHTNING_CLOUD_PRESSURE),
                        0.0F,
                        1.0F);
        return Mth.clamp(
                CLOUD_LIGHTNING_ACTIVITY
                        + cloudMix * (HAZE_LIGHTNING_ACTIVITY - CLOUD_LIGHTNING_ACTIVITY),
                CLOUD_LIGHTNING_ACTIVITY,
                HAZE_LIGHTNING_ACTIVITY);
    }

    private static int getStormDuration(RandomSource rand, float lightningSeverity) {
        int baseDuration = rand.nextInt(12000) + 3600;
        return Math.max(1200, Mth.floor(baseDuration * (0.95F + lightningSeverity * 0.45F)));
    }

    private static int getRainDuration(RandomSource rand) {
        return rand.nextInt(12000) + 12000;
    }

    private static int getClearDuration(RandomSource rand) {
        return rand.nextInt(168000) + 12000;
    }

    private static int getThunderClearDuration(RandomSource rand, float lightningSeverity) {
        int baseDuration = getClearDuration(rand);
        return Math.max(2400, Mth.floor(baseDuration * (1.05F - lightningSeverity * 0.45F)));
    }

    public void forceClear(RandomSource rand, int duration) {
        raining = false;
        thundering = false;
        rainTime = Math.max(1, duration);
        thunderTime = getClearDuration(rand);
        prevRainStrength = 0.0F;
        rainStrength = 0.0F;
        prevThunderStrength = 0.0F;
        thunderStrength = 0.0F;
    }

    public void forceRain(RandomSource rand, int duration) {
        raining = true;
        thundering = false;
        rainTime = Math.max(1, duration);
        thunderTime = getClearDuration(rand);
    }

    public void forceThunder(int duration) {
        raining = true;
        thundering = true;
        rainTime = Math.max(1, duration);
        thunderTime = Math.max(1, duration);
    }

    private static boolean sampleCanSpawnLightning(@Nullable ServerLevel level) {
        if (level == null) {
            return true;
        }

        for (int[] sample : LIGHTNING_BIOME_SAMPLES) {
            BlockPos pos = new BlockPos(sample[0], level.getSeaLevel(), sample[1]);
            if (level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel())
                    == Biome.Precipitation.RAIN) {
                return true;
            }
        }

        return false;
    }

    public boolean updateForTick(
            long tick, RandomSource rand, CelestialBody body, @Nullable ServerLevel level) {
        if (lastUpdateTick == tick) {
            return false;
        }

        lastUpdateTick = tick;

        if (!supportsWeather(body)) {
            boolean hadWeather =
                    rainTime != 0
                            || thunderTime != 0
                            || canSpawnLightning
                            || raining
                            || thundering
                            || prevRainStrength > 0.0F
                            || rainStrength > 0.0F
                            || prevThunderStrength > 0.0F
                            || thunderStrength > 0.0F;

            rainTime = 0;
            thunderTime = 0;
            canSpawnLightning = false;
            raining = false;
            thundering = false;
            prevRainStrength = 0.0F;
            rainStrength = 0.0F;
            prevThunderStrength = 0.0F;
            thunderStrength = 0.0F;
            return hadWeather;
        }

        boolean stateChanged = false;
        float lightningSeverity = getLightningSeverity(body);
        boolean lightningAllowed = sampleCanSpawnLightning(level);
        if (canSpawnLightning != lightningAllowed) {
            canSpawnLightning = lightningAllowed;
            stateChanged = true;
        }

        if (!canSpawnLightning && thundering) {
            thundering = false;
            thunderTime = getThunderClearDuration(rand, lightningSeverity);
            stateChanged = true;
        }

        if (thunderTime <= 0) {
            thunderTime =
                    thundering
                            ? getStormDuration(rand, lightningSeverity)
                            : getThunderClearDuration(rand, lightningSeverity);
            stateChanged = true;
        } else {
            thunderTime--;
            if (thunderTime <= 0) {
                thundering = canSpawnLightning && !thundering;
                thunderTime =
                        thundering
                                ? getStormDuration(rand, lightningSeverity)
                                : getThunderClearDuration(rand, lightningSeverity);
                stateChanged = true;
            }
        }

        prevThunderStrength = thunderStrength;
        if (thundering) {
            thunderStrength = Mth.clamp(thunderStrength + 0.01F, 0.0F, 1.0F);
        } else {
            thunderStrength = Mth.clamp(thunderStrength - 0.01F, 0.0F, 1.0F);
        }

        if (rainTime <= 0) {
            rainTime = raining ? getRainDuration(rand) : getClearDuration(rand);
            stateChanged = true;
        } else {
            rainTime--;
            if (rainTime <= 0) {
                raining = !raining;
                if (!raining) {
                    thundering = false;
                    thunderTime = getThunderClearDuration(rand, lightningSeverity);
                }
                rainTime = raining ? getRainDuration(rand) : getClearDuration(rand);
                stateChanged = true;
            }
        }

        prevRainStrength = rainStrength;
        if (raining) {
            rainStrength = Mth.clamp(rainStrength + 0.01F, 0.0F, 1.0F);
        } else {
            rainStrength = Mth.clamp(rainStrength - 0.01F, 0.0F, 1.0F);
        }

        return stateChanged || Math.floorMod(body.name.hashCode() + (int) tick, SAVE_INTERVAL) == 0;
    }

    public float getRainStrength(float partialTicks) {
        return Mth.clamp(
                prevRainStrength + (rainStrength - prevRainStrength) * partialTicks, 0.0F, 1.0F);
    }

    public float getThunderStrength(float partialTicks) {
        return Mth.clamp(
                prevThunderStrength + (thunderStrength - prevThunderStrength) * partialTicks,
                0.0F,
                1.0F);
    }

    public float getWeightedThunderStrength(float partialTicks) {
        return getRainStrength(partialTicks) * getThunderStrength(partialTicks);
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putBoolean("raining", raining);
        nbt.putBoolean("thundering", thundering);
        nbt.putBoolean("canSpawnLightning", canSpawnLightning);
        nbt.putInt("rainTime", rainTime);
        nbt.putInt("thunderTime", thunderTime);
        nbt.putFloat("prevRainStrength", prevRainStrength);
        nbt.putFloat("rainStrength", rainStrength);
        nbt.putFloat("prevThunderStrength", prevThunderStrength);
        nbt.putFloat("thunderStrength", thunderStrength);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        raining = nbt.getBooleanOr("raining", false);
        thundering = nbt.getBooleanOr("thundering", false);
        canSpawnLightning = nbt.getBooleanOr("canSpawnLightning", true);
        rainTime = nbt.getIntOr("rainTime", 0);
        thunderTime = nbt.getIntOr("thunderTime", 0);
        prevRainStrength = nbt.getFloatOr("prevRainStrength", 0);
        rainStrength = nbt.getFloatOr("rainStrength", 0);
        prevThunderStrength = nbt.getFloatOr("prevThunderStrength", 0);
        thunderStrength = nbt.getFloatOr("thunderStrength", 0);
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeBoolean(raining);
        buf.writeBoolean(thundering);
        buf.writeBoolean(canSpawnLightning);
        buf.writeInt(rainTime);
        buf.writeInt(thunderTime);
        buf.writeFloat(prevRainStrength);
        buf.writeFloat(rainStrength);
        buf.writeFloat(prevThunderStrength);
        buf.writeFloat(thunderStrength);
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        raining = buf.readBoolean();
        thundering = buf.readBoolean();
        canSpawnLightning = buf.readBoolean();
        rainTime = buf.readInt();
        thunderTime = buf.readInt();
        prevRainStrength = buf.readFloat();
        rainStrength = buf.readFloat();
        prevThunderStrength = buf.readFloat();
        thunderStrength = buf.readFloat();
    }
}
