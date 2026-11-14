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

package com.hbmspace.client.fog;

import com.hbm.NuclearTech;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.Nullable;

public final class FogShaderPatch {
    private static final Identifier VANILLA_FOG =
            Identifier.withDefaultNamespace("shaders/include/fog.glsl");
    private static final Identifier SODIUM_FOG =
            Identifier.fromNamespaceAndPath("sodium", "shaders/include/fog.glsl");
    private static final Identifier SODIUM_GLOBALS =
            Identifier.fromNamespaceAndPath("sodium", "shaders/include/globals.glsl");
    private static final Pattern TOTAL = Pattern.compile("\\bfloat\\s+total_fog_value\\s*\\(");
    private static final Pattern PARAMETER = Pattern.compile("\\s*float\\s+(\\w+)\\s*");

    private FogShaderPatch() {}

    public static Map<Identifier, Resource> patch(Map<Identifier, Resource> files) {
        Map<Identifier, Resource> patched = new LinkedHashMap<>(files);
        Resource vanilla = files.get(VANILLA_FOG);
        String fog = vanillaFog(read(vanilla));
        if (fog != null) patched.put(VANILLA_FOG, replaced(vanilla, fog));
        Resource sodiumFog = files.get(SODIUM_FOG);
        Resource sodiumGlobals = files.get(SODIUM_GLOBALS);
        if (sodiumFog != null && sodiumGlobals != null) {
            String terrainFog = sodiumFog(read(sodiumFog));
            String globals =
                    appendMembers(
                            read(sodiumGlobals),
                            "u_Globals",
                            "    float SpaceTerrainFogDensity;\n    int SpaceTerrainFogFlags;\n");
            if (terrainFog == null || globals == null) {
                NuclearTech.LOGGER.warn(
                        "Sodium's terrain fog includes ({}, {}) lack the anchors Space's EXP fog patches; "
                                + "terrain keeps their fog",
                        sodiumFog.sourcePackId(),
                        sodiumGlobals.sourcePackId());
            } else {
                patched.put(SODIUM_FOG, replaced(sodiumFog, terrainFog));
                patched.put(SODIUM_GLOBALS, replaced(sodiumGlobals, globals));
            }
        }
        return patched;
    }

    public static @Nullable String vanillaFog(String source) {
        String block =
                appendMembers(
                        source, "Fog", "    float SpaceFogDensity;\n    int SpaceFogFlags;\n");
        String fog = block == null ? null : wrapTotal(block, "SpaceFogDensity", "SpaceFogFlags");
        if (fog == null) {
            NuclearTech.LOGGER.warn(
                    "{} lacks the vanilla Fog block or total_fog_value; Space's EXP fog stays off",
                    VANILLA_FOG);
        }
        return fog;
    }

    private static @Nullable String sodiumFog(String source) {
        String fog = wrapTotal(source, "SpaceTerrainFogDensity", "SpaceTerrainFogFlags");
        return fog;
    }

    private static @Nullable String appendMembers(String source, String block, String members) {
        Matcher open = Pattern.compile("\\buniform\\s+" + block + "\\s*\\{").matcher(source);
        if (!open.find()) return null;
        int close = source.indexOf('}', open.end());
        return close < 0 ? null : source.substring(0, close) + members + source.substring(close);
    }

    private static @Nullable String wrapTotal(String source, String density, String flags) {
        Matcher total = TOTAL.matcher(source);
        if (!total.find() || source.lastIndexOf("linear_fog_value(", total.start()) < 0)
            return null;
        int list = total.end(), close = source.indexOf(')', list), open = source.indexOf('{', list);
        if (close < 0
                || open < close
                || !source.substring(close + 1, open).isBlank()
                || total.find()) return null;
        String[] parameters = source.substring(list, close).split(",", -1);
        if (parameters.length != 6) return null;
        String[] names = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Matcher name = PARAMETER.matcher(parameters[i]);
            if (!name.matches()) return null;
            names[i] = name.group(1);
        }
        return source.substring(0, open + 1)
                + String.format(
                        Locale.ROOT,
                        """

                    if ((%2$s & %3$d) != 0) {
                        float value = 1.0 - exp(-%1$s * %6$s);
                        if ((%2$s & %4$d) != 0) {
                            value = max(value, linear_fog_value(%7$s, %10$s, %11$s));
                        }
                        if ((%2$s & %5$d) != 0) {
                            value = max(value, linear_fog_value(%6$s, %8$s, %9$s));
                        }
                        return value;
                    }
                """,
                        density,
                        flags,
                        TerrainExpFog.EXP,
                        TerrainExpFog.EDGE,
                        TerrainExpFog.DARKNESS,
                        names[0],
                        names[1],
                        names[2],
                        names[3],
                        names[4],
                        names[5])
                + source.substring(open + 1);
    }

    private static String read(Resource resource) {
        try (InputStream input = resource.open()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Reading " + resource.sourcePackId() + " fog include", e);
        }
    }

    private static Resource replaced(Resource original, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new Resource(
                original.source(), () -> new ByteArrayInputStream(bytes), original::metadata);
    }
}
