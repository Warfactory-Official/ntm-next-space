/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.dim.eve;

import com.hbm.inventory.fluid.FluidStackNTM;
import com.hbmspace.blocks.TerrainBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.eve.biome.BiomeGenBaseEve;
import com.hbmspace.inventory.fluid.FluidsSpace;
import com.hbmspace.render.util.AtmosphereRenderUtil;
import com.hbmspace.world.gen.layer.BiomeLayer;
import com.hbmspace.world.gen.layer.BiomeLayers;
import com.hbmspace.world.gen.layer.CelestialLayers;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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

    public static Vector3fc skyColor(Vector3fc color, float flash) {
        return new Vector3f(
                Math.min(color.x() + flash, 1.0F),
                Math.min(color.y() + flash, 1.0F),
                Math.min(color.z() + flash, 1.0F));
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

    public static CelestialLayers.Stack layers(long seed) {
        int ocean = BiomeGenBaseEve.eveOcean.biomeID;
        BiomeLayer biomes =
                BiomeLayers.pick(
                        seed,
                        0L,
                        20,
                        new int[] {BiomeGenBaseEve.eveSeismicPlains.biomeID},
                        new int[] {
                            BiomeGenBaseEve.evePlains.biomeID,
                            ocean,
                            BiomeGenBaseEve.eveMountains.biomeID
                        });

        for (long salt = 1000L; salt <= 1005L; salt++)
            biomes = BiomeLayers.zoom(seed, salt, biomes);

        BiomeLayer river = BiomeLayers.river(BiomeLayers.zoom(seed, 1000L, biomes));
        river = BiomeLayers.smooth(seed, 1000L, river);
        BiomeLayer smoothed = BiomeLayers.smooth(seed, 1000L, biomes);

        return new CelestialLayers.Stack(
                BiomeLayers.riverMix(smoothed, river, BiomeGenBaseEve.eveRiver.biomeID, ocean));
    }
}
