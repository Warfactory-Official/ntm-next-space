// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.render.util.BeamPronter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;

public final class BeamAlphaSpace {

    public static final String PRONT =
            "Lcom/hbm/render/util/BeamPronter;prontBeam(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/phys/Vec3;"
                    + "Lcom/hbm/render/util/BeamPronter$EnumWaveType;Lcom/hbm/render/util/BeamPronter$EnumBeamType;IIIIFIF)V";
    public static final String BEAM_UPDATE =
            "Lcom/hbm/client/render/flywheel/BeamVisual;update(Lorg/joml/Matrix4fc;"
                    + "Lnet/minecraft/world/phys/Vec3;Lcom/hbm/render/util/BeamPronter$EnumWaveType;IIFFII)V";

    private static float alpha = 0.5F;

    private BeamAlphaSpace() {}

    public static void pront(
            PoseStack ps,
            SubmitNodeCollector collector,
            Vec3 skeleton,
            EnumWaveType wave,
            EnumBeamType beam,
            int outer,
            int inner,
            int start,
            int segments,
            float size,
            int layers,
            float thickness) {
        BeamPronter.prontBeam(
                ps, collector, skeleton, wave, beam, outer, inner, start, segments, size, layers,
                thickness, alpha);
    }

    public static int premultiply(int rgb) {
        if (alpha >= 1F) return rgb;
        return Math.round((rgb >> 16 & 255) * alpha) << 16
                | Math.round((rgb >> 8 & 255) * alpha) << 8
                | Math.round((rgb & 255) * alpha);
    }
}
