// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler.atmosphere;

import com.hbm.api.environment.EnvironmentProvider;
import com.hbm.inventory.fluid.NTMFluidProperties;
import com.hbm.inventory.fluid.NTMFluids;
import com.hbm.saveddata.TomSaveData;
import com.hbmspace.data.SpaceData;
import com.hbmspace.dim.BodyReach;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.Target;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.trait.CBT_Atmosphere;
import com.hbmspace.dim.trait.CBT_Water;
import com.hbmspace.entity.effect.TorexVacuum;
import com.hbmspace.handler.EntityEffectHandlerSpace;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.inventory.fluid.trait.FT_Terraformer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

final class SpaceEnvironment implements EnvironmentProvider {
    @Override
    public boolean breatheAir(ServerLevel level, BlockPos pos, int amount) {
        Fluid earthAir = NTMFluids.byName("EARTHAIR");
        CBT_Atmosphere atmosphere =
                CelestialBody.inOrbit(level)
                        ? null
                        : CelestialBody.getTrait(level, CBT_Atmosphere.class);
        if (atmosphere != null) {
            if (atmosphere.hasFluid(earthAir, 0.19)
                    || atmosphere.hasFluid(NTMFluids.OXYGEN, 0.09)) {
                return true;
            }
        }

        for (AtmosphereBlob blob : ChunkAtmosphereManager.proxy.getBlobs(level, pos)) {
            if (blob.hasFluid(earthAir, 0.19) || blob.hasFluid(NTMFluids.OXYGEN, 0.09)) {
                blob.consume(amount);
                return true;
            }
        }

        return false;
    }

    @Override
    public TriState canBreathe(LivingEntity entity) {
        return TriState.from(ChunkAtmosphereManager.proxy.canBreathe(entity));
    }

    @Override
    public void tickLiving(LivingEntity entity) {
        EntityEffectHandlerSpace.onUpdate(entity);
    }

    @Override
    public void blockChanged(
            ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        ChunkAtmosphereManager.proxy.blockChanged(level, pos, oldState, newState);
    }

    @Override
    public boolean vacuum(Level level) {
        return TorexVacuum.airless(level.dimension());
    }

    @Override
    public boolean evaporates(ServerLevel level, BlockPos pos, boolean vacuumProof) {
        TomSaveData impact = TomSaveData.published(level);
        if (impact != null && impact.fire > 1e-5 && level.getBrightness(LightLayer.SKY, pos) > 7)
            return true;
        return !vacuumProof && vacuum(level);
    }

    @Override
    public boolean hasAir(ServerLevel level, BlockPos pos) {
        CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(level, pos);
        return atmosphere != null && atmosphere.getPressure() > 0.01D;
    }

    @Override
    public boolean compressAir(ServerLevel level, BlockPos pos, int deficit) {
        CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(level, pos);
        if (atmosphere == null || atmosphere.getPressure() <= 0.01D) return false;

        boolean isInPressurizedRoom = ChunkAtmosphereManager.proxy.hasAtmosphere(level, pos);

        int consumption = deficit;
        if (consumption <= 0) return true;
        consumption = Math.max(1, consumption / 100);

        if (!isInPressurizedRoom) {
            GasExchange.capture(level, atmosphere.getMainFluid(), consumption);
            return true;
        }

        for (AtmosphereBlob blob : ChunkAtmosphereManager.proxy.getBlobs(level, pos)) {
            if (!blob.hasFluid(NTMFluids.AIR) && blob.hasPressure(0.1)) {
                blob.consume(consumption);
                return true;
            }
        }

        return false;
    }

    @Override
    public void releaseGas(ServerLevel level, Fluid fluid, double mB) {
        GasExchange.release(level, fluid, mB);
    }

    @Override
    public @Nullable Fluid groundwater(ServerLevel level) {
        if (CelestialBody.inOrbit(level)) return null;
        CBT_Water table = CelestialBody.getTrait(level, CBT_Water.class);
        return table == null ? null : table.fluid;
    }

    @Override
    public int sunHeat(ServerLevel level, BlockPos pos, int heat) {
        return (int) (heat * WorldProviderCelestial.getSunPower(level, pos.getX(), pos.getZ()));
    }

    @Override
    public double skyRadiation(ServerLevel level, BlockPos pos, double rad) {
        if (WorldProviderCelestial.of(level) == null && !CelestialBody.inOrbit(level)) return rad;
        if (level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken() < 14) return rad;

        Target target = CelestialBody.getTarget(level, pos.getX(), pos.getZ());
        CBT_Atmosphere atmosphere =
                !target.inOrbit() ? CelestialBody.getTrait(level, CBT_Atmosphere.class) : null;

        float targetRad = target.body().getSunPower();

        if (atmosphere != null) {
            targetRad -= (float) atmosphere.getPressure() * 4;
        }

        targetRad *= SpaceData.CELESTIAL_RAD_MULTIPLIER.get().floatValue();

        return targetRad > rad ? targetRad : rad;
    }

    @Override
    public float gravity(LivingEntity entity) {
        return CelestialBody.getGravity(entity);
    }

    @Override
    public boolean orbital(Level level) {
        return CelestialBody.inOrbit(level);
    }

    @Override
    public boolean fertilize(ServerLevel level, BlockPos pos) {
        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        Fluid earthAir = FluidsSpace.earthair();
        Fluid dunaAir = FluidsSpace.dunaair();
        if (atmosphere == null
                || !(atmosphere.hasFluid(earthAir)
                        || atmosphere.hasFluid(dunaAir)
                        || atmosphere.hasFluid(NTMFluids.CARBONDIOXIDE)
                        || atmosphere.hasFluid(NTMFluids.OXYGEN))) {
            return false;
        }

        ChunkAtmosphereManager.proxy.addGrowth(
                level, Blocks.GRASS_BLOCK, pos.getX(), pos.getY(), pos.getZ(), 4, 6);
        int totalConversion = ChunkAtmosphereHandler.GRASS_GROWTH_CONVERSION * 4 * 6;

        if (CelestialBody.consumeGas(level, NTMFluids.CARBONDIOXIDE, totalConversion)
                || CelestialBody.consumeGas(level, dunaAir, totalConversion)) {
            CelestialBody.emitGas(level, earthAir, totalConversion);
        }

        level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos.above(), 0);
        return true;
    }

    @Override
    public void chemicalImpact(ServerLevel level, BlockPos pos, Fluid fluid) {
        FT_Terraformer terra = NTMFluidProperties.getTrait(fluid, FT_Terraformer.class);
        if (terra == null) return;

        Block block = level.getBlockState(pos).getBlock();
        if (block != terra.getTargetBlock()) return;

        level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        ChunkAtmosphereManager.proxy.addGrowth(
                level, block, Blocks.DIRT, pos.getX(), pos.getY(), pos.getZ(), 4, 6);
    }

    @Override
    public TriState eclipse(Level level) {
        WorldProviderCelestial provider = WorldProviderCelestial.of(level);
        if (provider == null) return TriState.DEFAULT;
        float angle = provider.getCelestialAngle(level, 0);
        if (angle > 0.15F && angle < 0.85F) return TriState.FALSE;
        return TriState.from(provider.isEclipse(level));
    }

    @Override
    public double particlePressure(Level level) {
        if (CelestialBody.inOrbit(level)) return 0;
        CBT_Atmosphere atmosphere = CelestialBody.getTrait(level, CBT_Atmosphere.class);
        return atmosphere != null ? atmosphere.getPressure() : 0;
    }

    @Override
    public boolean meteorStrikes(ServerLevel level) {
        return BodyReach.meteorsStrike(level.dimension());
    }

    @Override
    public @Nullable Component deliveryRefusal(ServerLevel level) {
        return BodyReach.bobmazonRefusal(level.dimension());
    }
}
