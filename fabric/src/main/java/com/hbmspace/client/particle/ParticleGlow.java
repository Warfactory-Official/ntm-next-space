// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.particle;

import com.hbm.client.render.ParticleRenderTypes;
import com.hbm.particle.IQuadParticle;
import com.hbm.particle.QuadParticleGroup;
import com.hbmspace.HbmSpace;
import com.hbmspace.particle.FlareParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class ParticleGlow extends Particle implements IQuadParticle {

    public static final ParticleRenderType GROUP = new ParticleRenderType("hbmspace:glow", "HSG");
    private static final Identifier FLARE =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/particle/yelflare.png");
    private static final int COLOR = ARGB.colorFromFloat(0.8F, 1.0F, 1.0F, 1.0F);

    private final float peakScale;
    private float scale;

    private ParticleGlow(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            float scale,
            RandomSource random) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.scale = peakScale = scale;
        this.age = 1;
        this.lifetime = 50 + random.nextInt(50);
        this.hasPhysics = false;
    }

    public static ParticleGroup<?> group(ParticleEngine engine) {
        return new QuadParticleGroup(engine, ParticleRenderTypes.litTranslucent(FLARE));
    }

    @Override
    public void tick() {
        super.tick();
        float t = (float) age / lifetime;
        t = Math.abs(0.5F - (t - 0.5F)) * 2;
        scale = peakScale * Mth.sin(t * Mth.PI / 2);
    }

    @Override
    public int argb(float pt) {
        return COLOR;
    }

    @Override
    public float size(float pt) {
        return 0.1F * scale;
    }

    @Override
    public int light(float pt) {
        return LightCoordsUtil.FULL_BRIGHT;
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
    public ParticleRenderType getGroup() {
        return GROUP;
    }

    public static final class Provider implements ParticleProvider<FlareParticleOptions> {
        @Override
        public Particle createParticle(
                FlareParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                RandomSource random) {
            return new ParticleGlow(level, x, y, z, xd, yd, zd, options.scale(), random);
        }
    }
}
