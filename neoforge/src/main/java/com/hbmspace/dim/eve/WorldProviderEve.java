// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve;

import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.eve.genlayer.GenLayerEveBiomes;
import com.hbmspace.dim.eve.genlayer.GenLayerEveRiverMix;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerRiver;
import com.hbmspace.dim.legacy.layer.GenLayerSmooth;
import com.hbmspace.dim.legacy.layer.GenLayerVoronoiZoom;
import com.hbmspace.dim.legacy.layer.GenLayerZoom;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.render.util.AtmosphereRenderUtil;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderEve extends WorldProviderCelestial {

    public WorldProviderEve() {
        super(SpaceDimensions.EVE);
    }

    @Override
    public BlockState getStone() {
        return TerrainBlocksSpace.EVE_ROCK.get().defaultBlockState();
    }

    @Override
    public FluidStackNTM getBedrockAcid() {
        return new FluidStackNTM(FluidsSpace.kmno4(), 500);
    }

    @Override
    public void addLayers(EnvironmentAttributeSystem.Builder builder, Level level) {
        if (!level.isClientSide()) return;
        builder.addTimeBasedLayer(
                EnvironmentAttributes.SKY_COLOR,
                (value, tick) -> skyColor(value, flash(body(), level.getGameTime())));
        builder.addTimeBasedLayer(
                EnvironmentAttributes.SKY_LIGHT_FACTOR,
                (value, tick) -> sunBrightness(value, flash(body(), level.getGameTime())));
    }

    public static float flash(CelestialBody body, long gameTime) {
        return AtmosphereRenderUtil.getBodyEveFlashStrength(body, gameTime / 20.0F);
    }

    public static int skyColor(int color, float flash) {
        return ARGB.colorFromFloat(
                ARGB.alphaFloat(color),
                Math.min(ARGB.redFloat(color) + flash, 1.0F),
                Math.min(ARGB.greenFloat(color) + flash, 1.0F),
                Math.min(ARGB.blueFloat(color) + flash, 1.0F));
    }

    public static float sunBrightness(float value, float flash) {
        return Math.min(value + flash * 0.7F, 1.0F);
    }

    public static BiomeGenLayers createBiomeGenerators(long seed) {
        GenLayer biomes = new GenLayerEveBiomes(seed);

        biomes = new GenLayerZoom(1000L, biomes);
        biomes = new GenLayerZoom(1001L, biomes);
        biomes = new GenLayerZoom(1002L, biomes);
        biomes = new GenLayerZoom(1003L, biomes);
        biomes = new GenLayerZoom(1004L, biomes);
        biomes = new GenLayerZoom(1005L, biomes);

        GenLayer riverZoom = new GenLayerZoom(1000L, biomes);
        GenLayer river = new GenLayerRiver(1001L, riverZoom);
        GenLayer riverSmooth = new GenLayerSmooth(1000L, river);

        GenLayer biomeSmooth = new GenLayerSmooth(1000L, biomes);
        GenLayer riverMix = new GenLayerEveRiverMix(100L, biomeSmooth, riverSmooth);
        GenLayer voronoiZoom = new GenLayerVoronoiZoom(10L, riverMix);

        return new BiomeGenLayers(riverMix, voronoiZoom, seed);
    }
}
