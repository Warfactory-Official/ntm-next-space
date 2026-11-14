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

package com.hbmspace.compat.c2me;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.world.gen.density.BedrockFloor;
import com.hbmspace.world.gen.density.CelestialBiomes;
import com.hbmspace.world.gen.density.CelestialRelief;
import com.hbmspace.world.gen.density.LimitNoise;
import com.hbmspace.world.gen.layer.BiomeLayer;
import com.hbmspace.world.gen.layer.BiomeLayers;
import com.ishland.c2me.opts.accel.opencl.common.compiler.GeneratedCLSource;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenData;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public final class OpenCl {

    private static final Map<OpenCLCGenContext, Program> PROGRAMS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final String LIBRARY =
            """
            static uint64_t hbmspace_mix(uint64_t v) {
                v = (v ^ (v >> 30)) * 0xBF58476D1CE4E5B9UL;
                v = (v ^ (v >> 27)) * 0x94D049BB133111EBUL;
                return v ^ (v >> 31);
            }

            static int32_t hbmspace_draw(const uint64_t seed, const int32_t x, const int32_t z, const int32_t index,
                                         const int32_t bound) {
                uint64_t hash = hbmspace_mix(seed ^ (uint64_t) (int64_t) x * 0x9E3779B97F4A7C15UL
                        ^ (uint64_t) (int64_t) z * 0x165667B19E3779F9UL ^ (uint64_t) (int64_t) index * 0xD6E8FEB86659FD93UL);
                const uint64_t threshold = ((uint64_t) -(int64_t) bound) % (uint64_t) bound;
                while (hash < threshold) hash = hbmspace_mix(hash);
                return (int32_t) (hash % (uint64_t) bound);
            }

            static int32_t hbmspace_any(const uint64_t seed, const int32_t x, const int32_t z, const int32_t a,
                                        const int32_t b, const int32_t c, const int32_t d) {
                switch (hbmspace_draw(seed, x, z, 2, 4)) {
                    case 0: return a;
                    case 1: return b;
                    case 2: return c;
                    default: return d;
                }
            }

            static int32_t hbmspace_mode_or_any(const uint64_t seed, const int32_t x, const int32_t z, const int32_t a,
                                                const int32_t b, const int32_t c, const int32_t d) {
                if (b == c && c == d) return b;
                if ((a == b && a == c) || (a == b && a == d) || (a == c && a == d)) return a;
                if ((a == b && c != d) || (a == c && b != d) || (a == d && b != c)) return a;
                if ((b == c && a != d) || (b == d && a != c)) return b;
                if (c == d && a != b) return c;
                return hbmspace_any(seed, x, z, a, b, c, d);
            }

            static float hbmspace_java_minf(const float a, const float b) {
                if (a != a) return a;
                if (a == 0.0f && b == 0.0f && as_uint(b) == 0x80000000u) return b;
                return a <= b ? a : b;
            }

            static double hbmspace_java_min(const double a, const double b) {
                if (a != a) return a;
                if (a == 0.0 && b == 0.0 && as_ulong(b) == 0x8000000000000000UL) return b;
                return a <= b ? a : b;
            }

            static double hbmspace_java_max(const double a, const double b) {
                if (a != a) return a;
                if (a == 0.0 && b == 0.0 && as_ulong(a) == 0x8000000000000000UL) return b;
                return a >= b ? a : b;
            }
            """;

    private static final String NOISE_CAVES =
            """
            static aquifer_result_t hbmspace_noise_caves_sample(const sample_int32_ctx_t ctx, const double density) {
                if (!(density > 0.0) && df_binding_barrier(ctx) < 0.0) {
                    return (aquifer_result_t) {
                        .blockState = BLOCK_AIR,
                        .needsFluidTick = false
                    };
                }
                return aquifer_sample(ctx, density);
            }

            """;
    private static final String KERNEL = "#ifdef DF_COMPILE_NOISE_KERNEL";
    private static final String SUBSTANCE =
            "aquifer_res = aquifer_sample(ctx, df_binding_final_final_density(ctx));";

    private OpenCl() {}

    static void register() {
        OpenCLCGenData.REGISTRY.registerExactMatch(
                Dfc.Node.class, (OpenCLCEmitter<Dfc.Node>) OpenCl::emit);
    }

    public static GeneratedCLSource noiseCaves(GeneratedCLSource source) {
        String text = source.getGeneratedSource();
        if (count(text, KERNEL) != 1 || count(text, SUBSTANCE) != 1) {
            throw new IllegalStateException(
                    "C2ME's OpenCL noise kernel no longer matches hbmspace's noise caves");
        }
        String patched =
                text.replace(KERNEL, NOISE_CAVES + KERNEL)
                        .replace(
                                SUBSTANCE,
                                "aquifer_res = hbmspace_noise_caves_sample(ctx, "
                                        + "df_binding_final_final_density(ctx));");
        return new GeneratedCLSource(
                source.getOrdinal(),
                patched,
                source.getConstData(),
                source.getGlobalDynamicDataOffsets(),
                source.getFlatCachePrefills(),
                source.getCache2dPrefills(),
                source.getInterpolatorPrefills(),
                source.getDefines(),
                source.getBiomeMappings(),
                source.getDumpedPath());
    }

    private static int count(String text, String part) {
        int count = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length()))
            count++;
        return count;
    }

    private static String emit(Dfc.Node node, OpenCLCGenFunctionContext context, String storeTo) {
        Program program = PROGRAMS.computeIfAbsent(context.getGlobalContext(), Program::new);
        return switch (node.getDelegate()) {
            case BedrockFloor floor -> program.bedrockFloor(floor, storeTo);
            case LimitNoise noise -> program.limitNoise(noise, storeTo);
            case CelestialRelief relief -> program.relief(relief, storeTo);
            case CelestialBiomes biomes -> program.biomes(biomes, storeTo);
            default ->
                    throw new IllegalStateException(
                            node.getDelegate() + " is no celestial density function");
        };
    }

    private static String d(double value) {
        if (!Double.isFinite(value))
            throw new IllegalArgumentException("no OpenCL literal for " + value);
        return "(" + Double.toHexString(value) + ")";
    }

    private static String f(float value) {
        if (!Float.isFinite(value))
            throw new IllegalArgumentException("no OpenCL literal for " + value);
        return "(" + Float.toHexString(value) + "f)";
    }

    private static String ul(long value) {
        return "0x" + Long.toHexString(value) + "UL";
    }

    private static String ints(int[] values) {
        if (values.length == 0)
            throw new IllegalArgumentException("an empty id list draws nothing");
        return Arrays.stream(values)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", ", "{ ", " }"));
    }

    private static final class Program {

        private final OpenCLCGenContext context;
        private final Map<PerlinNoise, String> perlins = new IdentityHashMap<>();
        private final Map<Area, String> layers = new HashMap<>();
        private final Map<CelestialRelief, String> reliefs = new HashMap<>();
        private int next;

        private record Area(BiomeLayer layer, int width, int depth) {}

        Program(OpenCLCGenContext context) {
            this.context = context;
            context.appendRaw(LIBRARY);
        }

        private String name(String kind) {
            return "hbmspace_" + kind + "_" + next++;
        }

        String bedrockFloor(BedrockFloor floor, String storeTo) {
            if (!(floor.random()
                    instanceof XoroshiroRandomSource.XoroshiroPositionalRandomFactory random)) {
                throw new UnsupportedOperationException(
                        "hbmspace: no OpenCL bedrock floor over " + floor.random());
            }
            String solid = d(floor.maxValue());
            String open = d(floor.minValue());
            String below = d(floor.trueAtAndBelow());
            String above = d(floor.falseAtAndAbove());
            return "if (ctx.y <= "
                    + floor.trueAtAndBelow()
                    + ") {\n"
                    + "    "
                    + storeTo
                    + " = "
                    + solid
                    + ";\n"
                    + "} else if (ctx.y >= "
                    + floor.falseAtAndAbove()
                    + ") {\n"
                    + "    "
                    + storeTo
                    + " = "
                    + open
                    + ";\n"
                    + "} else {\n"
                    + "    const double probability = "
                    + d(1.0D)
                    + " + (((double) ctx.y - "
                    + below
                    + ") / ("
                    + above
                    + " - "
                    + below
                    + ")) * ("
                    + d(0.0D)
                    + " - "
                    + d(1.0D)
                    + ");\n"
                    + "    random_state_t random = { .type = RANDOM_Xoroshiro128PlusPlus, .seedLo = "
                    + ul(random.seedLo)
                    + ", .seedHi = "
                    + ul(random.seedHi)
                    + " };\n"
                    + "    random_state_split_coords(&random, ctx.x, ctx.y, ctx.z);\n"
                    + "    const float draw = (float) random_state_Xoroshiro128PlusPlus_next(&random, 24) * "
                    + f(5.9604645E-8F)
                    + ";\n"
                    + "    "
                    + storeTo
                    + " = (double) draw < probability ? "
                    + solid
                    + " : "
                    + open
                    + ";\n"
                    + "}\n";
        }

        String limitNoise(LimitNoise noise, String storeTo) {
            if (noise.noise() == null)
                throw new IllegalStateException("limit noise compiled before a level wired it");
            String perlin = perlin(noise.noise());
            String xz = d(LimitNoise.BLENDED_SCALE * noise.xzScale());
            return storeTo
                    + " = "
                    + perlin
                    + "(ctx.const_data, (double) ctx.x * "
                    + xz
                    + ", (double) ctx.y * "
                    + d(LimitNoise.BLENDED_SCALE)
                    + " * "
                    + d(noise.yScale())
                    + ", (double) ctx.z * "
                    + xz
                    + ") * "
                    + d(LimitNoise.OCTAVE_SUM)
                    + " / "
                    + d(512.0D)
                    + " / "
                    + d(128.0D)
                    + ";\n";
        }

        String biomes(CelestialBiomes biomes, String storeTo) {
            if (biomes.regions() == null)
                throw new IllegalStateException(biomes.layers() + " compiled before wiring");
            String layer = layer(biomes.regions().layer(), 1, 1);
            return "int32_t id[1];\n"
                    + layer
                    + "(math_floorDiv(ctx.x, 4), math_floorDiv(ctx.z, 4), id);\n"
                    + storeTo
                    + " = (double) id[0];\n";
        }

        String relief(CelestialRelief relief, String storeTo) {
            String function = reliefs.get(relief);
            if (function == null) {
                function = reliefFunction(relief);
                reliefs.put(relief, function);
            }
            return storeTo
                    + " = "
                    + function
                    + "(ctx.const_data, math_floorDiv(ctx.x, 4), math_floorDiv(ctx.z, 4));\n";
        }

        private String perlin(PerlinNoise noise) {
            String existing = perlins.get(noise);
            if (existing != null) return existing;
            String name = name("perlin");
            StringBuilder body =
                    new StringBuilder("static double ")
                            .append(name)
                            .append(
                                    "(global const void * restrict const const_data, const double x, const double y, ")
                            .append("const double z) {\n    double value = 0.0;\n");
            double factor = noise.lowestFreqInputFactor;
            double valueFactor = noise.lowestFreqValueFactor;
            for (int i = 0; i < noise.noiseLevels.length; i++) {
                ImprovedNoise level = noise.noiseLevels[i];
                if (level != null) {
                    int permutation = context.allocGlobalConstData(level.p.clone(), 16);
                    body.append("    value += ")
                            .append(d(noise.amplitudes.getDouble(i)))
                            .append(
                                    " * math_noise_perlin_sample_global(ptr_shift_global(const_data, ")
                            .append(permutation)
                            .append("), ")
                            .append(d(level.xo))
                            .append(", ")
                            .append(d(level.yo))
                            .append(", ")
                            .append(d(level.zo))
                            .append(", math_octave_maintainPrecision(x * ")
                            .append(d(factor))
                            .append("), math_octave_maintainPrecision(y * ")
                            .append(d(factor))
                            .append("), math_octave_maintainPrecision(z * ")
                            .append(d(factor))
                            .append("), 0.0, 0.0) * ")
                            .append(d(valueFactor))
                            .append(";\n");
                }
                factor *= 2.0;
                valueFactor /= 2.0;
            }
            context.appendRaw(body.append("    return value;\n}\n").toString());
            perlins.put(noise, name);
            return name;
        }

        private String reliefFunction(CelestialRelief relief) {
            if (relief.depthNoise() == null)
                throw new IllegalStateException("relief compiled before a level wired it");
            DensityFunction biome = relief.biome();
            String fill;
            IntSet ids = new IntOpenHashSet();
            if (biome.minValue() == biome.maxValue()) {
                int id = (int) biome.minValue();
                ids.add(id);
                fill = "    for (int32_t i = 0; i < 25; i++) ids[i] = " + id + ";\n";
            } else if (biome instanceof CelestialBiomes layered && layered.regions() != null) {
                emits(layered.regions().layer(), ids);
                fill = "    " + layer(layered.regions().layer(), 5, 5) + "(qx - 2, qz - 2, ids);\n";
            } else {
                throw new UnsupportedOperationException("hbmspace: no OpenCL relief over " + biome);
            }
            float[] roots = new float[256];
            float[] variations = new float[256];
            Arrays.fill(roots, Float.NaN);
            Arrays.fill(variations, Float.NaN);
            for (int id = 0; id < 256; id++) {
                BiomeGenBaseCelestial known = BiomeGenBaseCelestial.getBiomeOrNull(id);
                if (known == null) continue;
                roots[id] = known.rootHeight;
                variations[id] = known.heightVariation;
            }
            for (int id : ids) {
                if (BiomeGenBaseCelestial.getBiomeOrNull(id) == null) {
                    throw new IllegalStateException(
                            "celestial relief reads biome id " + id + ", which no biome holds");
                }
            }
            int rootsAt = context.allocGlobalConstDataObject(roots);
            int variationsAt = context.allocGlobalConstDataObject(variations);
            int fieldAt = context.allocGlobalConstDataObject(CelestialRelief.parabolicField());

            String tail =
                    switch (relief.output()) {
                        case OFFSET -> {
                            String perlin = perlin(relief.depthNoise());
                            yield "    double noise = "
                                    + perlin
                                    + "(const_data, (double) qx * "
                                    + d(200.0D)
                                    + ", "
                                    + d(10.0D)
                                    + ", (double) qz * "
                                    + d(200.0D)
                                    + ") * "
                                    + d(65535.0D)
                                    + " / "
                                    + d(8000.0D)
                                    + ";\n"
                                    + "    if (noise < 0.0) noise = -noise * "
                                    + d(0.3D)
                                    + ";\n"
                                    + "    noise = noise * "
                                    + d(3.0D)
                                    + " - "
                                    + d(2.0D)
                                    + ";\n"
                                    + "    noise = noise < 0.0 ? hbmspace_java_max(noise / "
                                    + d(2.0D)
                                    + ", "
                                    + d(-1.0D)
                                    + ") / "
                                    + d(2.8D)
                                    + " : hbmspace_java_min(noise, "
                                    + d(1.0D)
                                    + ") / "
                                    + d(8.0D)
                                    + ";\n"
                                    + "    const double scaledDepth = ((double) ((depth / weight * "
                                    + f(4.0F)
                                    + " - "
                                    + f(1.0F)
                                    + ") / "
                                    + f(8.0F)
                                    + ") + noise * "
                                    + d(0.2D)
                                    + ") * "
                                    + d(8.5D)
                                    + " / "
                                    + d(8.0D)
                                    + ";\n"
                                    + "    return ("
                                    + d(68.0D)
                                    + " + scaledDepth * "
                                    + d(32.0D)
                                    + ") / "
                                    + d(128.0D)
                                    + ";\n";
                        }
                        case FACTOR ->
                                "    return "
                                        + d(0.75D)
                                        + " / (double) (scale / weight * "
                                        + f(0.9F)
                                        + " + "
                                        + f(0.1F)
                                        + ");\n";
                        case CAVE_CEILING ->
                                "    return (double) (8 * (int32_t) (("
                                        + d(56.0D)
                                        + " + (double) lowest * "
                                        + d(20.0D)
                                        + ") / "
                                        + d(8.0D)
                                        + "));\n";
                        case CAVE_GUARD ->
                                "    return ("
                                        + d(20.0D)
                                        + " - (double) lowest * "
                                        + d(30.0D)
                                        + ") / "
                                        + d(128.0D)
                                        + ";\n";
                    };

            String name = name("relief");
            context.appendRaw(
                    "static double "
                            + name
                            + "(global const void * restrict const const_data, "
                            + "const int32_t qx, const int32_t qz) {\n"
                            + "    int32_t ids[25];\n"
                            + fill
                            + "    global const float * const roots = ptr_shift_global(const_data, "
                            + rootsAt
                            + ");\n"
                            + "    global const float * const variations = ptr_shift_global(const_data, "
                            + variationsAt
                            + ");\n"
                            + "    global const float * const field = ptr_shift_global(const_data, "
                            + fieldAt
                            + ");\n"
                            + "    const float centre = roots[ids[12]];\n"
                            + "    float scale = 0.0f;\n"
                            + "    float depth = 0.0f;\n"
                            + "    float weight = 0.0f;\n"
                            + "    float lowest = 0.0f;\n"
                            + "    for (int32_t j = -2; j <= 2; j++) {\n"
                            + "        for (int32_t k = -2; k <= 2; k++) {\n"
                            + "            const int32_t at = j + 2 + (k + 2) * 5;\n"
                            + "            const float root = roots[ids[at]];\n"
                            + "            float w = field[at] / (root + "
                            + f(2.0F)
                            + ");\n"
                            + "            if (root > centre) w /= "
                            + f(2.0F)
                            + ";\n"
                            + "            scale += variations[ids[at]] * w;\n"
                            + "            depth += root * w;\n"
                            + "            weight += w;\n"
                            + "            lowest = hbmspace_java_minf(lowest, root);\n"
                            + "        }\n"
                            + "    }\n"
                            + tail
                            + "}\n");
            return name;
        }

        private static void emits(BiomeLayer layer, IntSet ids) {
            switch (layer) {
                case BiomeLayers.Pick pick -> Arrays.stream(pick.ids()).forEach(ids::add);
                case BiomeLayers.PickRare pick -> {
                    Arrays.stream(pick.rare()).forEach(ids::add);
                    Arrays.stream(pick.common()).forEach(ids::add);
                }
                case BiomeLayers.Zoom zoom -> emits(zoom.parent(), ids);
                case BiomeLayers.Smooth smooth -> emits(smooth.parent(), ids);
                case BiomeLayers.River _ -> {
                    ids.add(BiomeLayers.NO_RIVER);
                    ids.add(BiomeLayers.RIVER);
                }
                case BiomeLayers.Border border -> {
                    emits(border.parent(), ids);
                    ids.add(border.to());
                }
                case BiomeLayers.Interior interior -> {
                    emits(interior.parent(), ids);
                    ids.add(interior.to());
                }
                case BiomeLayers.Islands islands -> {
                    emits(islands.parent(), ids);
                    ids.add(islands.coast());
                    ids.add(islands.ocean());
                }
                case BiomeLayers.RiverMix mix -> {
                    emits(mix.biomes(), ids);
                    ids.add(mix.river());
                }
                case BiomeLayers.Diversify diversify -> {
                    emits(diversify.parent(), ids);
                    Arrays.stream(diversify.ids()).forEach(ids::add);
                }
                case BiomeLayers.Enclosed enclosed -> {
                    emits(enclosed.parent(), ids);
                    ids.add(enclosed.to());
                }
                default ->
                        throw new UnsupportedOperationException(
                                "hbmspace: no OpenCL area for " + layer);
            }
        }

        private String layer(BiomeLayer layer, int w, int d) {
            Area key = new Area(layer, w, d);
            String existing = layers.get(key);
            if (existing != null) return existing;
            String body =
                    switch (layer) {
                        case BiomeLayers.Pick pick ->
                                "    const int32_t ids[] = "
                                        + ints(pick.ids())
                                        + ";\n"
                                        + loops(
                                                w,
                                                d,
                                                "out[i + k * "
                                                        + w
                                                        + "] = ids[hbmspace_draw("
                                                        + ul(pick.seed())
                                                        + ", x + i, z + k, 0, "
                                                        + pick.ids().length
                                                        + ")];");
                        case BiomeLayers.PickRare pick ->
                                "    const int32_t rare[] = "
                                        + ints(pick.rare())
                                        + ";\n"
                                        + "    const int32_t common[] = "
                                        + ints(pick.common())
                                        + ";\n"
                                        + loops(
                                                w,
                                                d,
                                                "out[i + k * "
                                                        + w
                                                        + "] = hbmspace_draw("
                                                        + ul(pick.seed())
                                                        + ", x + i, z + k, 0, "
                                                        + pick.oneIn()
                                                        + ") == 0 ? rare[hbmspace_draw("
                                                        + ul(pick.seed())
                                                        + ", x + i, z + k, 1, "
                                                        + pick.rare().length
                                                        + ")] : common[hbmspace_draw("
                                                        + ul(pick.seed())
                                                        + ", x + i, z + k, 1, "
                                                        + pick.common().length
                                                        + ")];");
                        case BiomeLayers.Zoom zoom -> zoom(zoom, w, d);
                        case BiomeLayers.Smooth smooth ->
                                neighbours(
                                        smooth.parent(),
                                        w,
                                        d,
                                        "(left == right && up == down) ? (hbmspace_draw("
                                                + ul(smooth.seed())
                                                + ", x + i, z + k, 0, 2) == 0 ? left : up) : (up == down ? up : (left == right ? left : centre))");
                        case BiomeLayers.River river ->
                                neighbours(
                                        river.parent(),
                                        w,
                                        d,
                                        "centre == left && centre == right && centre == up && centre == down ? "
                                                + BiomeLayers.NO_RIVER
                                                + " : "
                                                + BiomeLayers.RIVER);
                        case BiomeLayers.Border border ->
                                neighbours(
                                        border.parent(),
                                        w,
                                        d,
                                        "centre == "
                                                + border.from()
                                                + " && (left == "
                                                + border.beside()
                                                + " || right == "
                                                + border.beside()
                                                + " || up == "
                                                + border.beside()
                                                + " || down == "
                                                + border.beside()
                                                + ") ? "
                                                + border.to()
                                                + " : centre");
                        case BiomeLayers.Interior interior ->
                                neighbours(
                                        interior.parent(),
                                        w,
                                        d,
                                        "centre != "
                                                + interior.from()
                                                + " ? centre : ("
                                                + outside("left", interior)
                                                + " || "
                                                + outside("right", interior)
                                                + " || "
                                                + outside("up", interior)
                                                + " || "
                                                + outside("down", interior)
                                                + ") ? centre : "
                                                + interior.to());
                        case BiomeLayers.Islands islands ->
                                neighbours(
                                        islands.parent(),
                                        w,
                                        d,
                                        "centre == "
                                                + islands.coast()
                                                + " ? ((left == "
                                                + islands.island()
                                                + " || right == "
                                                + islands.island()
                                                + " || up == "
                                                + islands.island()
                                                + " || down == "
                                                + islands.island()
                                                + ") && hbmspace_draw("
                                                + ul(islands.seed())
                                                + ", x + i, z + k, 0, 50) == 0 ? "
                                                + islands.coast()
                                                + " : "
                                                + islands.ocean()
                                                + ") : centre == "
                                                + islands.island()
                                                + " ? (hbmspace_draw("
                                                + ul(islands.seed())
                                                + ", x + i, z + k, 0, 3) == 0 ? "
                                                + islands.coast()
                                                + " : "
                                                + islands.island()
                                                + ") : centre");
                        case BiomeLayers.RiverMix mix -> {
                            String biomes = layer(mix.biomes(), w, d);
                            String rivers = layer(mix.rivers(), w, d);
                            StringBuilder exempt = new StringBuilder();
                            for (int kept : mix.exempt())
                                exempt.append(" || out[i] == ").append(kept);
                            yield "    "
                                    + biomes
                                    + "(x, z, out);\n"
                                    + "    int32_t flow["
                                    + w * d
                                    + "];\n"
                                    + "    "
                                    + rivers
                                    + "(x, z, flow);\n"
                                    + "    for (int32_t i = 0; i < "
                                    + w * d
                                    + "; i++) {\n"
                                    + "        if (flow[i] != "
                                    + BiomeLayers.RIVER
                                    + ") continue;\n"
                                    + (exempt.isEmpty()
                                            ? ""
                                            : "        if ("
                                                    + exempt.substring(4)
                                                    + ") continue;\n")
                                    + "        out[i] = "
                                    + mix.river()
                                    + ";\n"
                                    + "    }\n";
                        }
                        case BiomeLayers.Diversify diversify ->
                                "    const int32_t ids[] = "
                                        + ints(diversify.ids())
                                        + ";\n"
                                        + "    "
                                        + layer(diversify.parent(), w, d)
                                        + "(x, z, out);\n"
                                        + loops(
                                                w,
                                                d,
                                                "if (hbmspace_draw("
                                                        + ul(diversify.seed())
                                                        + ", x + i, z + k, 0, "
                                                        + diversify.oneIn()
                                                        + ") == 0) out[i + k * "
                                                        + w
                                                        + "] = ids[hbmspace_draw("
                                                        + ul(diversify.seed())
                                                        + ", x + i, z + k, 1, "
                                                        + diversify.ids().length
                                                        + ")];");
                        case BiomeLayers.Enclosed enclosed -> {
                            int span = w + 2;
                            String of = Integer.toString(enclosed.of());
                            yield "    int32_t in["
                                    + span * (d + 2)
                                    + "];\n"
                                    + "    "
                                    + layer(enclosed.parent(), span, d + 2)
                                    + "(x - 1, z - 1, in);\n"
                                    + loops(
                                            w,
                                            d,
                                            "const int32_t centre = in[i + 1 + (k + 1) * "
                                                    + span
                                                    + "]; "
                                                    + "out[i + k * "
                                                    + w
                                                    + "] = centre == "
                                                    + of
                                                    + " && in[i + k * "
                                                    + span
                                                    + "] == "
                                                    + of
                                                    + " && in[i + 2 + k * "
                                                    + span
                                                    + "] == "
                                                    + of
                                                    + " && in[i + (k + 2) * "
                                                    + span
                                                    + "] == "
                                                    + of
                                                    + " && in[i + 2 + (k + 2) * "
                                                    + span
                                                    + "] == "
                                                    + of
                                                    + " ? "
                                                    + enclosed.to()
                                                    + " : centre;");
                        }
                        default ->
                                throw new UnsupportedOperationException(
                                        "hbmspace: no OpenCL area for " + layer);
                    };
            String name = name("layer");
            context.appendRaw(
                    "static void "
                            + name
                            + "(const int32_t x, const int32_t z, int32_t * const out) {\n"
                            + body
                            + "}\n");
            layers.put(key, name);
            return name;
        }

        private static String outside(String side, BiomeLayers.Interior interior) {
            return "("
                    + side
                    + " != "
                    + interior.from()
                    + " && "
                    + side
                    + " != "
                    + interior.to()
                    + ")";
        }

        private static String loops(int w, int d, String statement) {
            return "    for (int32_t k = 0; k < "
                    + d
                    + "; k++) {\n"
                    + "        for (int32_t i = 0; i < "
                    + w
                    + "; i++) {\n"
                    + "            "
                    + statement
                    + "\n"
                    + "        }\n"
                    + "    }\n";
        }

        private String neighbours(BiomeLayer parent, int w, int d, String cell) {
            int span = w + 2;
            return "    int32_t in["
                    + span * (d + 2)
                    + "];\n"
                    + "    "
                    + layer(parent, span, d + 2)
                    + "(x - 1, z - 1, in);\n"
                    + loops(
                            w,
                            d,
                            "const int32_t at = i + 1 + (k + 1) * "
                                    + span
                                    + "; "
                                    + "const int32_t centre = in[at]; const int32_t left = in[at - 1]; const int32_t right = in[at + 1]; "
                                    + "const int32_t up = in[at - "
                                    + span
                                    + "]; const int32_t down = in[at + "
                                    + span
                                    + "]; "
                                    + "out[i + k * "
                                    + w
                                    + "] = "
                                    + cell
                                    + ";");
        }

        private String zoom(BiomeLayers.Zoom zoom, int w, int d) {
            int pw = (w >> 1) + 2;
            int pd = (d >> 1) + 2;
            int zw = pw - 1 << 1;
            String seed = ul(zoom.seed());
            String corner = zoom.fuzzy() ? "hbmspace_any" : "hbmspace_mode_or_any";
            return "    const int32_t px = math_floorDiv(x, 2);\n"
                    + "    const int32_t pz = math_floorDiv(z, 2);\n"
                    + "    int32_t in["
                    + pw * pd
                    + "];\n"
                    + "    "
                    + layer(zoom.parent(), pw, pd)
                    + "(px, pz, in);\n"
                    + "    int32_t zoomed["
                    + zw * (pd - 1 << 1)
                    + "];\n"
                    + "    for (int32_t k = 0; k < "
                    + (pd - 1)
                    + "; k++) {\n"
                    + "        int32_t o = k * 2 * "
                    + zw
                    + ";\n"
                    + "        int32_t a = in[k * "
                    + pw
                    + "];\n"
                    + "        int32_t below = in[(k + 1) * "
                    + pw
                    + "];\n"
                    + "        for (int32_t i = 0; i < "
                    + (pw - 1)
                    + "; i++) {\n"
                    + "            const int32_t cx = (i + px) * 2;\n"
                    + "            const int32_t cz = (k + pz) * 2;\n"
                    + "            const int32_t right = in[i + 1 + k * "
                    + pw
                    + "];\n"
                    + "            const int32_t corner = in[i + 1 + (k + 1) * "
                    + pw
                    + "];\n"
                    + "            zoomed[o] = a;\n"
                    + "            zoomed[o + "
                    + zw
                    + "] = hbmspace_draw("
                    + seed
                    + ", cx, cz, 0, 2) == 0 ? a : below;\n"
                    + "            zoomed[o + 1] = hbmspace_draw("
                    + seed
                    + ", cx, cz, 1, 2) == 0 ? a : right;\n"
                    + "            zoomed[o + 1 + "
                    + zw
                    + "] = "
                    + corner
                    + "("
                    + seed
                    + ", cx, cz, a, right, below, corner);\n"
                    + "            o += 2;\n"
                    + "            a = right;\n"
                    + "            below = corner;\n"
                    + "        }\n"
                    + "    }\n"
                    + "    const int32_t ox = x & 1;\n"
                    + "    const int32_t oz = z & 1;\n"
                    + loops(
                            w,
                            d,
                            "out[i + k * " + w + "] = zoomed[(k + oz) * " + zw + " + ox + i];");
        }
    }
}
