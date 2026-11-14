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

package com.hbmspace.client.particle;

import com.hbm.client.particle.InstanceParticle;
import com.hbm.client.particle.QuadParticleVisual;
import com.hbm.client.particle.VisualParticle;
import com.hbm.client.render.ParticleRenderTypes;
import com.hbm.particle.QuadParticle;
import com.hbm.particle.QuadParticleGroup;
import com.hbmspace.HbmSpace;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public final class DuoDecParticle extends Particle implements QuadParticle, VisualParticle {
    public static final ParticleRenderType TUNE_GROUP =
            new ParticleRenderType("hbmspace:duoewe", "HSE");
    public static final ParticleRenderType JESUS_GROUP =
            new ParticleRenderType("hbmspace:duodec", "HSDC");
    private static final Identifier TUNE =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/particle/particlenote2.png");
    private static final Identifier JESUS =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/particle/particlenote1.png");
    private static final Model TUNE_MODEL = QuadParticleVisual.texturedLitModel(TUNE);
    private static final Model JESUS_MODEL = QuadParticleVisual.texturedLitModel(JESUS);
    private static final InstanceType<InstanceParticle> TYPE =
            SimpleInstanceType.builder(InstanceParticle::new)
                    .layout(InstanceParticle.TYPE.layout())
                    .seed(InstanceParticle.TYPE.seed())
                    .vertexShader(
                            Identifier.fromNamespaceAndPath(
                                    HbmSpace.MOD_ID, "instance/duodec.vert"))
                    .cullShader(InstanceParticle.TYPE.cullShader())
                    .build();
    private final boolean tune;
    private final float shade;
    private @Nullable VisualizationManager visualManager;

    private DuoDecParticle(
            ClientLevel level, double x, double y, double z, boolean tune, RandomSource random) {
        super(level, x, y, z);
        this.tune = tune;
        this.shade = 1F - random.nextFloat() * 0.2F;
        this.lifetime = 50 + random.nextInt(50);
        this.gravity = 0.2F;
    }

    public static ParticleGroup<?> tuneGroup(ParticleEngine engine) {
        return new QuadParticleGroup(engine, ParticleRenderTypes.litTranslucent(TUNE));
    }

    public static ParticleGroup<?> jesusGroup(ParticleEngine engine) {
        return new QuadParticleGroup(engine, ParticleRenderTypes.litTranslucent(JESUS));
    }

    @Override
    public void tick() {
        super.tick();
        if (!onGround) {
            xd += random.nextGaussian() * 0.002D;
            zd += random.nextGaussian() * 0.002D;
            if (yd < -0.025D) yd = -0.025D;
        }
    }

    @Override
    public float size(float partialTick) {
        return 0.3F;
    }

    @Override
    public int argb(float partialTick) {
        int color = (int) (shade * 255F);
        return 0xFF000000 | color << 16 | color << 8 | color;
    }

    @Override
    public int light(float partialTick) {
        return getLightCoords(partialTick);
    }

    @Override
    public double interpX(float partialTick) {
        return xo + (x - xo) * partialTick;
    }

    @Override
    public double interpY(float partialTick) {
        return yo + (y - yo) * partialTick;
    }

    @Override
    public double interpZ(float partialTick) {
        return zo + (z - zo) * partialTick;
    }

    @Override
    public ParticleRenderType getGroup() {
        return tune ? TUNE_GROUP : JESUS_GROUP;
    }

    @Override
    public ClientLevel level() {
        return level;
    }

    @Override
    public @Nullable VisualizationManager visualManager() {
        return visualManager;
    }

    @Override
    public void visualManager(@Nullable VisualizationManager manager) {
        visualManager = manager;
    }

    @Override
    public EffectVisual<?> visualize(VisualizationContext context, float partialTick) {
        return new QuadParticleVisual<>(
                context, this, tune ? TUNE_MODEL : JESUS_MODEL, TYPE, partialTick);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final boolean tune;

        public Provider(boolean tune) {
            this.tune = tune;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                RandomSource random) {
            return new DuoDecParticle(level, x, y, z, tune, random);
        }
    }
}
