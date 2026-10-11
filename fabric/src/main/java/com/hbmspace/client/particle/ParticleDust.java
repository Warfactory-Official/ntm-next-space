// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.particle;

import com.hbm.client.particle.QuadParticleVisual;
import com.hbm.client.particle.VisualParticle;
import com.hbm.client.render.ParticleRenderTypes;
import com.hbm.main.ResourceManager;
import com.hbm.particle.IQuadParticle;
import com.hbm.particle.QuadParticleGroup;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public final class ParticleDust extends Particle implements IQuadParticle, VisualParticle {

    public static final ParticleRenderType DUST_GROUP =
            new ParticleRenderType("hbmspace:dust", "HSD");
    private static final float SCALE = 4F;
    private static final int COLOR = ARGB.colorFromFloat(0.5F, 0.4F, 0.2F, 0.1F);

    private @Nullable VisualizationManager visualManager;

    private ParticleDust(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            RandomSource random) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.age = 1;
        this.lifetime = 50 + random.nextInt(50);
        this.hasPhysics = false;
    }

    public static ParticleGroup<?> group(ParticleEngine engine) {
        return new QuadParticleGroup(
                engine, ParticleRenderTypes.litTranslucent(ResourceManager.particle_base_tex));
    }

    @Override
    public int argb(float pt) {
        return COLOR;
    }

    @Override
    public float size(float pt) {
        return SCALE;
    }

    @Override
    public int light(float pt) {
        return getLightCoords(pt);
    }

    @Override
    public double interpX(float pt) {
        return this.xo + (this.x - this.xo) * pt;
    }

    @Override
    public double interpY(float pt) {
        return this.yo + (this.y - this.yo) * pt;
    }

    @Override
    public double interpZ(float pt) {
        return this.zo + (this.z - this.zo) * pt;
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
        return QuadParticleVisual.particleBase(context, this, partialTick);
    }

    @Override
    public ParticleRenderType getGroup() {
        return DUST_GROUP;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
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
            return new ParticleDust(level, x, y, z, xd, yd, zd, random);
        }
    }
}
