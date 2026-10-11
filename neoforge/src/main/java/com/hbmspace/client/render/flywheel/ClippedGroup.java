// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import com.hbm.render.loader.GroupObject;
import java.util.Arrays;
import org.jspecify.annotations.Nullable;

final class ClippedGroup {

    private static final int STRIDE = GroupObject.STRIDE;

    private ClippedGroup() {}

    static @Nullable GroupObject below(GroupObject group, boolean smoothing, float top) {
        float[] src = group.quads(smoothing);
        float[] quads = new float[src.length * 2];
        int written = 0;
        float[] in = new float[6 * STRIDE];
        float[] out = new float[6 * STRIDE];

        for (int q = 0; q < src.length; q += GroupObject.QUAD) {
            System.arraycopy(src, q, in, 0, GroupObject.QUAD);
            int n = 0;
            for (int i = 0; i < 4; i++) {
                int a = i * STRIDE, b = ((i + 1) % 4) * STRIDE;
                float da = top - in[a + 1], db = top - in[b + 1];
                if (da >= 0) System.arraycopy(in, a, out, n++ * STRIDE, STRIDE);
                if ((da >= 0) != (db >= 0)) {
                    float t = da / (da - db);
                    for (int k = 0; k < STRIDE; k++)
                        out[n * STRIDE + k] = in[a + k] + (in[b + k] - in[a + k]) * t;
                    n++;
                }
            }
            for (int i = 1; i + 1 < n; i += 2) {
                if (written + GroupObject.QUAD > quads.length)
                    quads = Arrays.copyOf(quads, quads.length * 2);
                int[] corners = {0, i, i + 1, i + 2 < n ? i + 2 : i + 1};
                for (int corner : corners) {
                    System.arraycopy(out, corner * STRIDE, quads, written, STRIDE);
                    written += STRIDE;
                }
            }
        }

        if (written == 0) return null;
        quads = Arrays.copyOf(quads, written);
        int faces = written / GroupObject.QUAD;
        float[] normals = new float[faces * 3];
        for (int f = 0; f < faces; f++) faceNormal(quads, f * GroupObject.QUAD, normals, f * 3);
        return new GroupObject(group.name, quads, normals);
    }

    private static void faceNormal(float[] quads, int base, float[] out, int at) {
        float ax = quads[base + STRIDE] - quads[base],
                ay = quads[base + STRIDE + 1] - quads[base + 1],
                az = quads[base + STRIDE + 2] - quads[base + 2];
        float bx = quads[base + 2 * STRIDE] - quads[base],
                by = quads[base + 2 * STRIDE + 1] - quads[base + 1],
                bz = quads[base + 2 * STRIDE + 2] - quads[base + 2];
        float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0e-6F) len = 1F;
        out[at] = nx / len;
        out[at + 1] = ny / len;
        out[at + 2] = nz / len;
    }
}
