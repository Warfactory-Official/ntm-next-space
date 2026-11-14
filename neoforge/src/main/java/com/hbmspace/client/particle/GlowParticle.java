/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.client.particle;

import com.hbm.client.render.ParticleRenderTypes;
import com.hbm.particle.QuadParticle;
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

public final class GlowParticle extends Particle implements QuadParticle {

    public static final ParticleRenderType GROUP = new ParticleRenderType("hbmspace:glow", "HSG");
    private static final Identifier FLARE =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/particle/yelflare.png");
    private static final int COLOR = ARGB.colorFromFloat(0.8F, 1.0F, 1.0F, 1.0F);

    private final float peakScale;
    private float scale;

    private GlowParticle(
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
            return new GlowParticle(level, x, y, z, xd, yd, zd, options.scale(), random);
        }
    }
}
