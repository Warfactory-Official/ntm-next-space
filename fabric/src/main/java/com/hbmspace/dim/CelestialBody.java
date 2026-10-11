// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-FileCopyrightText: 2024-2026 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.dim.trait.CBT_Dyson;
import com.hbmspace.dim.trait.CBT_War;
import com.hbmspace.dim.trait.CBT_Water;
import com.hbmspace.dim.trait.CelestialBodyTrait;
import com.hbmspace.handler.atmosphere.GasExchange;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import com.hbmspace.inventory.recipes.AtmosphereRecipe;
import com.hbmspace.inventory.recipes.AtmosphereRecipes;
import com.hbmspace.util.AstronomyUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class CelestialBody {

    public String name;

    public @Nullable ResourceKey<Level> dimension;

    public boolean canLand = false;

    public float massKg = 0;
    public float radiusKm = 0;
    public float semiMajorAxisKm = 0;
    public float semiMinorAxisFactor = 0;
    public float eccentricity = 0;
    public float inclination = 0;
    public float ascendingNode = 0;
    public float argumentPeriapsis = 0;

    private int rotationalPeriod = 6 * 60 * 60;

    public float axialTilt = 0;

    private int minProcessingLevel = 0;

    public Identifier texture;
    public @Nullable Identifier biomeMask = null;
    public @Nullable Identifier cityMask = null;
    public float[] color = new float[] {0.4F, 0.4F, 0.4F};

    public @Nullable String tidallyLockedTo = null;

    public boolean hasRings = false;
    public float ringTilt = 0;
    public float[] ringColor = new float[] {0.5F, 0.5F, 0.5F};
    public float ringSize = 2;

    public boolean hasIce = false;

    public @Nullable Fluid gas;

    public List<CelestialBody> satellites = new ArrayList<>();
    public @Nullable CelestialBody parent = null;

    private final Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits =
            new LinkedHashMap<>();

    public @Nullable Identifier stoneTexture;
    public @Nullable Identifier surfaceTexture;
    public SolarSystem.@Nullable Body type;

    public @Nullable Identifier shader;

    public float shaderScale = 1;

    private static final Map<ResourceKey<Level>, CelestialBody> dimToBodyMap = new HashMap<>();
    private static final Map<String, CelestialBody> nameToBodyMap = new LinkedHashMap<>();

    public CelestialBody(String name) {
        this.name = name;
        this.texture =
                Identifier.fromNamespaceAndPath(
                        HbmSpace.MOD_ID, "textures/misc/space/" + name + ".png");

        nameToBodyMap.put(name, this);
    }

    public CelestialBody(String name, ResourceKey<Level> dimension, SolarSystem.Body type) {
        this(name);
        this.dimension = dimension;
        this.canLand = true;
        this.type = type;

        dimToBodyMap.put(dimension, this);
    }

    public CelestialBody withMassRadius(float kg, float km) {
        this.massKg = kg;
        this.radiusKm = km;
        return this;
    }

    public CelestialBody withOrbitalParameters(
            float semiMajorAxisKm,
            float eccentricity,
            float argumentPeriapsisDegrees,
            float inclinationDegrees,
            float ascendingNodeDegrees) {
        this.semiMajorAxisKm = semiMajorAxisKm;
        this.semiMinorAxisFactor = (float) Math.sqrt(1 - eccentricity * eccentricity);
        this.eccentricity = eccentricity;
        this.argumentPeriapsis = (float) Math.toRadians(argumentPeriapsisDegrees);
        this.inclination = (float) Math.toRadians(inclinationDegrees);
        this.ascendingNode = (float) Math.toRadians(ascendingNodeDegrees);
        return this;
    }

    public CelestialBody withRotationalPeriod(int seconds) {
        this.rotationalPeriod = seconds;
        return this;
    }

    public CelestialBody withAxialTilt(float degrees) {
        this.axialTilt = degrees;
        return this;
    }

    public CelestialBody withMinProcessingLevel(int level) {
        this.minProcessingLevel = level;
        return this;
    }

    public CelestialBody withTexture(Identifier location) {
        this.texture = location;
        return this;
    }

    public CelestialBody withCityMask(Identifier location) {
        this.cityMask = location;
        return this;
    }

    public CelestialBody withBiomeMask(Identifier location) {
        this.biomeMask = location;
        return this;
    }

    public CelestialBody withBlockTextures(Identifier stone, Identifier surface) {
        this.stoneTexture = stone;
        this.surfaceTexture = surface;
        return this;
    }

    public CelestialBody withColor(float... color) {
        this.color = color;
        return this;
    }

    public CelestialBody withTidalLockingTo(String name) {
        tidallyLockedTo = name;
        return this;
    }

    public CelestialBody withRings(float tilt, float size, float... color) {
        this.hasRings = true;
        this.ringTilt = tilt;
        this.ringSize = size;
        this.ringColor = color;
        return this;
    }

    public CelestialBody withGas(Fluid gas) {
        this.gas = gas;
        return this;
    }

    public CelestialBody withSatellites(CelestialBody... bodies) {
        Collections.addAll(satellites, bodies);
        for (CelestialBody body : bodies) {
            body.parent = this;
        }
        return this;
    }

    public CelestialBody withTraits(CelestialBodyTrait... traits) {
        for (CelestialBodyTrait trait : traits) this.traits.put(trait.getClass(), trait);
        return this;
    }

    public CelestialBody withShader(Identifier fragmentShader) {
        return withShader(fragmentShader, 1);
    }

    public CelestialBody withShader(Identifier fragmentShader, float scale) {
        shader = fragmentShader;
        shaderScale = scale;
        return this;
    }

    public CelestialBody withIce(boolean hasIce) {
        this.hasIce = hasIce;
        return this;
    }

    public static void setTraits(Level level, CelestialBodyTrait... traits) {
        getBody(level).setTraits(traits);
    }

    public static void setTraits(
            Level level, Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits) {
        getBody(level).setTraits(traits);
    }

    public void setTraits(CelestialBodyTrait... traits) {
        SolarSystemWorldSavedData.get().setTraits(name, traits);
    }

    public void setTraits(Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> traits) {
        setTraits(traits.values().toArray(new CelestialBodyTrait[0]));
    }

    public static HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> getTraits(
            Level level) {
        return getBody(level).getTraits();
    }

    public HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> getTraits() {
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                SolarSystemWorldSavedData.get().getTraits(name);

        if (currentTraits == null) {
            currentTraits = new HashMap<>();
            for (CelestialBodyTrait trait : traits.values()) {
                currentTraits.put(trait.getClass(), copy(trait));
            }
        } else {
            currentTraits = new HashMap<>(currentTraits);
        }

        return currentTraits;
    }

    private static CelestialBodyTrait copy(CelestialBodyTrait trait) {
        CompoundTag nbt = new CompoundTag();
        trait.writeToNBT(nbt);
        CelestialBodyTrait copy = CelestialBodyTrait.create(trait.getClass());
        copy.readFromNBT(nbt);
        return copy;
    }

    public static void modifyTraits(Level level, CelestialBodyTrait... traits) {
        getBody(level).modifyTraits(traits);
    }

    public void modifyTraits(CelestialBodyTrait... traits) {
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                getTraits();

        for (CelestialBodyTrait trait : traits) {
            currentTraits.put(trait.getClass(), trait);
        }

        setTraits(currentTraits);
    }

    public static void clearTraits(Level level) {
        getBody(level).clearTraits();
    }

    public void clearTraits() {
        SolarSystemWorldSavedData.get().clearTraits(name);
    }

    public static void degas(Level level) {
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                getTraits(level);

        currentTraits.remove(CBT_Atmosphere.class);

        setTraits(level, currentTraits);
    }

    public static boolean consumeGas(Level level, Fluid fluid, double amount) {
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                getTraits(level);
        if (!currentTraits.containsKey(CBT_Atmosphere.class)) return false;

        boolean didConsume = consumeGas(currentTraits, fluid, amount);

        setTraits(level, currentTraits);

        return didConsume;
    }

    public static boolean consumeGas(
            Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits,
            Fluid fluid,
            double amount) {
        CBT_Atmosphere atmosphere = (CBT_Atmosphere) currentTraits.get(CBT_Atmosphere.class);
        if (atmosphere == null) return false;

        boolean didConsume = false;
        int emptyIndex = -1;
        for (int i = 0; i < atmosphere.fluids.size(); i++) {
            FluidEntry entry = atmosphere.fluids.get(i);
            if (entry.fluid == fluid) {
                entry.pressure -= amount / AstronomyUtil.MB_PER_ATM;
                didConsume = true;
                emptyIndex = entry.pressure <= 0 ? i : -1;
                break;
            }
        }

        if (emptyIndex >= 0) {
            atmosphere.fluids.remove(emptyIndex);

            if (atmosphere.fluids.isEmpty()) {
                currentTraits.remove(CBT_Atmosphere.class);
            }
        }

        return didConsume;
    }

    public static void emitGas(Level level, Fluid fluid, double amount) {
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                getTraits(level);

        emitGas(currentTraits, fluid, amount);

        setTraits(level, currentTraits);
    }

    public static void emitGas(
            Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits,
            Fluid fluid,
            double amount) {
        CBT_Atmosphere atmosphere = (CBT_Atmosphere) currentTraits.get(CBT_Atmosphere.class);
        if (atmosphere == null) {
            atmosphere = new CBT_Atmosphere();
            currentTraits.put(CBT_Atmosphere.class, atmosphere);
        }

        boolean hasFluid = false;
        for (FluidEntry entry : atmosphere.fluids) {
            if (entry.fluid == fluid) {
                entry.pressure += amount / AstronomyUtil.MB_PER_ATM;
                hasFluid = true;
                break;
            }
        }

        if (!hasFluid) {
            if (atmosphere.fluids.size() >= 8) {
                atmosphere.sortDescending();
                atmosphere.fluids.removeLast();
            }

            atmosphere.fluids.add(new FluidEntry(fluid, amount / AstronomyUtil.MB_PER_ATM));
        }
    }

    private static boolean reactAtmosphere(
            Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits,
            AtmosphereRecipe recipe) {
        CBT_Atmosphere atmosphere = (CBT_Atmosphere) currentTraits.get(CBT_Atmosphere.class);
        if (atmosphere == null) return false;

        for (FluidStackNTM recipeFluid : recipe.inputFluid) {
            boolean hasInput = false;

            for (FluidEntry entry : atmosphere.fluids) {
                if (entry.fluid == recipeFluid.type()
                        && entry.pressure * AstronomyUtil.MB_PER_ATM
                                >= recipeFluid.amount() * AtmosphereRecipes.SCALE) {
                    hasInput = true;
                }
            }

            if (!hasInput) return false;
        }

        for (FluidStackNTM recipeFluid : recipe.inputFluid) {
            GasExchange.capture(
                    currentTraits,
                    recipeFluid.type(),
                    recipeFluid.amount() * AtmosphereRecipes.SCALE);
        }

        FluidStackNTM output = recipe.outputFluid[0];
        GasExchange.release(
                currentTraits, output.type(), output.amount() * AtmosphereRecipes.SCALE);

        return true;
    }

    public static void updateChemistry(Level level) {
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                getTraits(level);

        if (updateChemistry(currentTraits)) setTraits(level, currentTraits);
    }

    public static boolean updateChemistry(
            Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits) {
        boolean hasUpdated = false;
        CBT_Atmosphere atmosphere = (CBT_Atmosphere) currentTraits.get(CBT_Atmosphere.class);

        CBT_Water water = (CBT_Water) currentTraits.get(CBT_Water.class);
        if (water == null) {

            if (atmosphere != null) {
                double pressure = 0;
                for (FluidEntry entry : atmosphere.fluids) {
                    if (entry.fluid == NTMFluids.STEAM
                            || entry.fluid == NTMFluids.HOTSTEAM
                            || entry.fluid == NTMFluids.SUPERHOTSTEAM
                            || entry.fluid == NTMFluids.ULTRAHOTSTEAM
                            || entry.fluid == NTMFluids.SPENTSTEAM) {
                        pressure += entry.pressure;
                    }
                }

                if (pressure > 0.2D) {
                    currentTraits.put(CBT_Water.class, new CBT_Water());
                    hasUpdated = true;
                }
            }
        }

        if (atmosphere != null) {
            for (AtmosphereRecipe recipe : AtmosphereRecipes.INSTANCE.recipes()) {
                if (reactAtmosphere(currentTraits, recipe)) {
                    hasUpdated = true;
                }
            }
        }

        return hasUpdated;
    }

    public static void updateSwarms() {
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                SolarSystem.kerbol.getTraits();

        CBT_Dyson dyson = (CBT_Dyson) currentTraits.get(CBT_Dyson.class);
        if (dyson == null) return;

        dyson.attenuate();
    }

    public static void damage(int dmg, Level level) {
        damage(dmg, getBody(level));
    }

    public static void damage(int dmg, CelestialBody body) {
        HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                body.getTraits();

        CBT_War war = (CBT_War) currentTraits.get(CBT_War.class);
        if (war == null) {
            war = new CBT_War();
            currentTraits.put(CBT_War.class, war);
        }

        if (war.shield > 0) {
            war.shield -= dmg;
        } else {
            war.health -= dmg;
        }

        body.setTraits(currentTraits);
    }

    public static Collection<CelestialBody> getAllBodies() {
        SolarSystem.ensureInit();
        return nameToBodyMap.values();
    }

    public static Collection<CelestialBody> getLandableBodies() {
        SolarSystem.ensureInit();
        return dimToBodyMap.values();
    }

    public static CelestialBody getBody(String name) {
        SolarSystem.ensureInit();
        CelestialBody body = nameToBodyMap.get(name);
        return body != null ? body : dimToBodyMap.get(Level.OVERWORLD);
    }

    public static CelestialBody getBody(ResourceKey<Level> dimension) {
        SolarSystem.ensureInit();
        CelestialBody body = dimToBodyMap.get(dimension);
        return body != null ? body : dimToBodyMap.get(Level.OVERWORLD);
    }

    public static @Nullable CelestialBody getBodyOrNull(String name) {
        SolarSystem.ensureInit();
        return nameToBodyMap.get(name);
    }

    public static @Nullable CelestialBody getBodyOrNull(ResourceKey<Level> dimension) {
        SolarSystem.ensureInit();
        return dimToBodyMap.get(dimension);
    }

    public static CelestialBody getBody(Level level) {
        return getBody(level.dimension());
    }

    public static Target getTarget(Level level, int x, int z) {
        if (inOrbit(level)) {
            OrbitalStation station =
                    !level.isClientSide()
                            ? OrbitalStation.getStationFromPosition(x, z)
                            : OrbitalStation.clientStation;
            return new Target(station.orbiting, true, station.hasStation);
        }

        return new Target(getBody(level), false, true);
    }

    public static CelestialBody getStar(Level level) {
        return getBody(level).getStar();
    }

    public static CelestialBody getPlanet(Level level) {
        return getBody(level).getPlanet();
    }

    public static float getGravity(LivingEntity entity) {
        if (entity instanceof WaterAnimal || entity instanceof AgeableWaterCreature) {
            return AstronomyUtil.STANDARD_GRAVITY;
        }

        if (inOrbit(entity.level())) {
            if (SpaceLivingProps.hasGravity(entity)) {
                OrbitalStation station =
                        entity.level().isClientSide()
                                ? OrbitalStation.clientStation
                                : OrbitalStation.getStationFromPosition(
                                        entity.getBlockX(), entity.getBlockZ());

                float gravity = AstronomyUtil.STANDARD_GRAVITY * station.gravityMultiplier;
                if (gravity < 0.2) return 0;
                return gravity;
            }

            return 0;
        }

        CelestialBody body = CelestialBody.getBody(entity.level());
        return body.getSurfaceGravity() * AstronomyUtil.PLAYER_GRAVITY_MODIFIER;
    }

    public static boolean inOrbit(Level level) {
        return level.dimension() == SpaceDimensions.ORBIT;
    }

    public static SolarSystem.@Nullable Body getEnum(Level level) {
        return getBody(level).getEnum();
    }

    public static int getMeta(Level level) {
        return getBody(level).getEnum().ordinal();
    }

    public static double getRotationalPeriod(Level level) {
        return getBody(level).getRotationalPeriod();
    }

    public static float getSemiMajorAxis(Level level) {
        return getBody(level).semiMajorAxisKm;
    }

    public static boolean hasTrait(Level level, Class<? extends CelestialBodyTrait> trait) {
        return getBody(level).hasTrait(trait);
    }

    public static <T extends CelestialBodyTrait> @Nullable T getTrait(
            Level level, Class<? extends T> trait) {
        return getBody(level).getTrait(trait);
    }

    public static boolean hasDefaultTrait(Level level, Class<? extends CelestialBodyTrait> trait) {
        return getBody(level).hasDefaultTrait(trait);
    }

    public static <T extends CelestialBodyTrait> @Nullable T getDefaultTrait(
            Level level, Class<? extends T> trait) {
        return getBody(level).getDefaultTrait(trait);
    }

    public String getUnlocalizedName() {
        return name;
    }

    public SolarSystem.@Nullable Body getEnum() {
        return type;
    }

    public @Nullable ServerLevel getLevel(MinecraftServer server) {
        return dimension == null ? null : server.getLevel(dimension);
    }

    public CelestialBody getStar() {
        CelestialBody body = this;
        while (body.parent != null) body = body.parent;

        return body;
    }

    public CelestialBody getPlanet() {
        if (this.parent == null) return this;
        CelestialBody body = this;
        while (body.parent.parent != null) body = body.parent;

        return body;
    }

    public double getRotationalPeriod() {
        return (double) rotationalPeriod
                * (AstronomyUtil.DAY_FACTOR / (double) AstronomyUtil.TIME_MULTIPLIER)
                * 20;
    }

    public double getOrbitalPeriod() {
        double semiMajorAxis = semiMajorAxisKm * 1_000;
        double orbitalPeriod =
                2
                        * Math.PI
                        * Math.sqrt(
                                (semiMajorAxis * semiMajorAxis * semiMajorAxis)
                                        / (AstronomyUtil.GRAVITATIONAL_CONSTANT * parent.massKg));
        return orbitalPeriod / (double) AstronomyUtil.SECONDS_IN_KSP_DAY;
    }

    public float getSurfaceGravity() {
        float radius = radiusKm * 1000;
        return AstronomyUtil.GRAVITATIONAL_CONSTANT * massKg / (radius * radius);
    }

    public float getSunPower() {
        float distanceAU = getPlanet().semiMajorAxisKm / AstronomyUtil.KM_IN_AU;
        return 1 / (distanceAU * distanceAU);
    }

    public int getProcessingLevel(CelestialBody from) {
        int level;

        if (this == from) {
            level = 0;
        } else if (this == from.parent || this.parent == from) {
            level = 0;
        } else {
            level = 1;
        }

        return Math.max(level, minProcessingLevel);
    }

    public boolean hasTrait(Class<? extends CelestialBodyTrait> trait) {
        return currentTraits().containsKey(trait);
    }

    @SuppressWarnings("unchecked")
    public <T extends CelestialBodyTrait> @Nullable T getTrait(Class<? extends T> trait) {
        return (T) currentTraits().get(trait);
    }

    Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits() {
        Map<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> current =
                SolarSystemWorldSavedData.readTraits(name);
        return current != null ? current : this.traits;
    }

    public boolean hasDefaultTrait(Class<? extends CelestialBodyTrait> trait) {
        return traits.containsKey(trait);
    }

    @SuppressWarnings("unchecked")
    public <T extends CelestialBodyTrait> @Nullable T getDefaultTrait(Class<? extends T> trait) {
        return (T) traits.get(trait);
    }
}
