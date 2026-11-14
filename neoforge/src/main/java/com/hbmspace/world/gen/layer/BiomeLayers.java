/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

package com.hbmspace.world.gen.layer;

import com.hbm.world.WorldgenHash;

import static com.hbmspace.world.gen.layer.BiomeLayer.draw;

public final class BiomeLayers {
    public static final int RIVER = 7;

    public static final int NO_RIVER = -1;

    private BiomeLayers() {}

    private static long seed(long world, long salt) {
        return WorldgenHash.domainSeed(world, WorldgenHash.mix(salt));
    }

    public static BiomeLayer pick(long world, long salt, int... ids) {
        return new Pick(seed(world, salt), ids);
    }

    public static BiomeLayer pick(long world, long salt, int oneIn, int[] rare, int[] common) {
        return new PickRare(seed(world, salt), oneIn, rare, common);
    }

    public static BiomeLayer zoom(long world, long salt, BiomeLayer parent) {
        return new Zoom(seed(world, salt), parent, false);
    }

    public static BiomeLayer fuzzyZoom(long world, long salt, BiomeLayer parent) {
        return new Zoom(seed(world, salt), parent, true);
    }

    public static BiomeLayer smooth(long world, long salt, BiomeLayer parent) {
        return new Smooth(seed(world, salt), parent);
    }

    public static BiomeLayer river(BiomeLayer parent) {
        return new River(parent);
    }

    public static BiomeLayer riverMix(
            BiomeLayer biomes, BiomeLayer rivers, int river, int... exempt) {
        return new RiverMix(biomes, rivers, river, exempt);
    }

    public static BiomeLayer border(BiomeLayer parent, int from, int beside, int to) {
        return new Border(parent, from, beside, to);
    }

    public static BiomeLayer diversify(
            long world, long salt, BiomeLayer parent, int oneIn, int... ids) {
        return new Diversify(seed(world, salt), parent, oneIn, ids);
    }

    public static BiomeLayer interior(BiomeLayer parent, int from, int to) {
        return new Interior(parent, from, to);
    }

    public static BiomeLayer enclosed(BiomeLayer parent, int of, int to) {
        return new Enclosed(parent, of, to);
    }

    public static BiomeLayer islands(
            long world, long salt, BiomeLayer parent, int island, int coast, int ocean) {
        return new Islands(seed(world, salt), parent, island, coast, ocean);
    }

    public record Pick(long seed, int[] ids) implements BiomeLayer {

        @Override
        public int[] area(int x, int z, int width, int depth) {
            int[] out = new int[width * depth];
            for (int k = 0; k < depth; k++) {
                for (int i = 0; i < width; i++)
                    out[i + k * width] = ids[draw(seed, x + i, z + k, 0, ids.length)];
            }
            return out;
        }
    }

    public record PickRare(long seed, int oneIn, int[] rare, int[] common) implements BiomeLayer {

        @Override
        public int[] area(int x, int z, int width, int depth) {
            int[] out = new int[width * depth];
            for (int k = 0; k < depth; k++) {
                for (int i = 0; i < width; i++) {
                    int[] from = draw(seed, x + i, z + k, 0, oneIn) == 0 ? rare : common;
                    out[i + k * width] = from[draw(seed, x + i, z + k, 1, from.length)];
                }
            }
            return out;
        }
    }

    public record Zoom(long seed, BiomeLayer parent, boolean fuzzy) implements BiomeLayer {

        @Override
        public int[] area(int x, int z, int width, int depth) {
            int px = x >> 1;
            int pz = z >> 1;
            int pw = (width >> 1) + 2;
            int pd = (depth >> 1) + 2;
            int[] in = parent.area(px, pz, pw, pd);
            int zw = pw - 1 << 1;
            int[] zoomed = new int[zw * (pd - 1 << 1)];

            for (int k = 0; k < pd - 1; k++) {
                int out = (k << 1) * zw;
                int a = in[k * pw];
                int below = in[(k + 1) * pw];
                for (int i = 0; i < pw - 1; i++) {
                    int cx = i + px << 1;
                    int cz = k + pz << 1;
                    int right = in[i + 1 + k * pw];
                    int corner = in[i + 1 + (k + 1) * pw];
                    zoomed[out] = a;
                    zoomed[out + zw] = draw(seed, cx, cz, 0, 2) == 0 ? a : below;
                    zoomed[out + 1] = draw(seed, cx, cz, 1, 2) == 0 ? a : right;
                    zoomed[out + 1 + zw] =
                            fuzzy
                                    ? any(cx, cz, a, right, below, corner)
                                    : modeOrAny(cx, cz, a, right, below, corner);
                    out += 2;
                    a = right;
                    below = corner;
                }
            }

            int[] result = new int[width * depth];
            for (int row = 0; row < depth; row++) {
                System.arraycopy(
                        zoomed, (row + (z & 1)) * zw + (x & 1), result, row * width, width);
            }
            return result;
        }

        private int any(int x, int z, int a, int b, int c, int d) {
            return switch (draw(seed, x, z, 2, 4)) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                default -> d;
            };
        }

        private int modeOrAny(int x, int z, int a, int b, int c, int d) {
            if (b == c && c == d) return b;
            if (a == b && a == c || a == b && a == d || a == c && a == d) return a;
            if (a == b && c != d || a == c && b != d || a == d && b != c) return a;
            if (b == c && a != d || b == d && a != c) return b;
            if (c == d && a != b) return c;
            return any(x, z, a, b, c, d);
        }
    }

    public sealed interface Neighbours extends BiomeLayer
            permits Smooth, River, Border, Interior, Islands {

        BiomeLayer parent();

        int cell(int x, int z, int centre, int left, int right, int up, int down);

        @Override
        default int[] area(int x, int z, int width, int depth) {
            int span = width + 2;
            int[] in = parent().area(x - 1, z - 1, span, depth + 2);
            int[] out = new int[width * depth];
            for (int k = 0; k < depth; k++) {
                for (int i = 0; i < width; i++) {
                    int at = i + 1 + (k + 1) * span;
                    out[i + k * width] =
                            cell(
                                    x + i,
                                    z + k,
                                    in[at],
                                    in[at - 1],
                                    in[at + 1],
                                    in[at - span],
                                    in[at + span]);
                }
            }
            return out;
        }
    }

    public record Smooth(long seed, BiomeLayer parent) implements Neighbours {

        @Override
        public int cell(int x, int z, int centre, int left, int right, int up, int down) {
            if (left == right && up == down) return draw(seed, x, z, 0, 2) == 0 ? left : up;
            if (up == down) return up;
            return left == right ? left : centre;
        }
    }

    public record River(BiomeLayer parent) implements Neighbours {

        @Override
        public int cell(int x, int z, int centre, int left, int right, int up, int down) {
            return centre == left && centre == right && centre == up && centre == down
                    ? NO_RIVER
                    : RIVER;
        }
    }

    public record Border(BiomeLayer parent, int from, int beside, int to) implements Neighbours {

        @Override
        public int cell(int x, int z, int centre, int left, int right, int up, int down) {
            return centre == from
                            && (left == beside || right == beside || up == beside || down == beside)
                    ? to
                    : centre;
        }
    }

    public record Interior(BiomeLayer parent, int from, int to) implements Neighbours {

        @Override
        public int cell(int x, int z, int centre, int left, int right, int up, int down) {
            if (centre != from) return centre;
            for (int side : new int[] {left, right, up, down})
                if (side != from && side != to) return centre;
            return to;
        }
    }

    public record Islands(long seed, BiomeLayer parent, int island, int coast, int ocean)
            implements Neighbours {

        @Override
        public int cell(int x, int z, int centre, int left, int right, int up, int down) {
            if (centre == coast) {
                boolean beside =
                        left == island || right == island || up == island || down == island;
                return beside && draw(seed, x, z, 0, 50) == 0 ? coast : ocean;
            }
            if (centre == island) return draw(seed, x, z, 0, 3) == 0 ? coast : island;
            return centre;
        }
    }

    public record RiverMix(BiomeLayer biomes, BiomeLayer rivers, int river, int[] exempt)
            implements BiomeLayer {

        @Override
        public int[] area(int x, int z, int width, int depth) {
            int[] out = biomes.area(x, z, width, depth);
            int[] flow = rivers.area(x, z, width, depth);
            cells:
            for (int i = 0; i < out.length; i++) {
                if (flow[i] != RIVER) continue;
                for (int kept : exempt) if (out[i] == kept) continue cells;
                out[i] = river;
            }
            return out;
        }
    }

    public record Diversify(long seed, BiomeLayer parent, int oneIn, int[] ids)
            implements BiomeLayer {

        @Override
        public int[] area(int x, int z, int width, int depth) {
            int[] out = parent.area(x, z, width, depth);
            for (int k = 0; k < depth; k++) {
                for (int i = 0; i < width; i++) {
                    if (draw(seed, x + i, z + k, 0, oneIn) == 0) {
                        out[i + k * width] = ids[draw(seed, x + i, z + k, 1, ids.length)];
                    }
                }
            }
            return out;
        }
    }

    public record Enclosed(BiomeLayer parent, int of, int to) implements BiomeLayer {

        @Override
        public int[] area(int x, int z, int width, int depth) {
            int span = width + 2;
            int[] in = parent.area(x - 1, z - 1, span, depth + 2);
            int[] out = new int[width * depth];
            for (int k = 0; k < depth; k++) {
                for (int i = 0; i < width; i++) {
                    int centre = in[i + 1 + (k + 1) * span];
                    boolean enclosed =
                            centre == of
                                    && in[i + k * span] == of
                                    && in[i + 2 + k * span] == of
                                    && in[i + (k + 2) * span] == of
                                    && in[i + 2 + (k + 2) * span] == of;
                    out[i + k * width] = enclosed ? to : centre;
                }
            }
            return out;
        }
    }
}
