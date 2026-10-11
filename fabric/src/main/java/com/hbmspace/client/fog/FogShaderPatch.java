// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.fog;

import com.hbm.NuclearTech;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
    private static final String RANGE = "hbmspace_range_fog_value";

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
        return fog == null ? null : "#moj_import <sodium:globals.glsl>\n" + fog;
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
        int start = total.start(), open = source.indexOf('{', total.end());
        if (open < 0 || total.find()) return null;
        int end = open, depth = 0;
        do {
            char c = source.charAt(end++);
            if (c == '{') depth++;
            else if (c == '}') depth--;
        } while (depth > 0 && end < source.length());
        if (depth != 0) return null;
        return source.substring(0, start)
                + "float "
                + RANGE
                + "("
                + source.substring(source.indexOf('(', start) + 1, end)
                + """


                float total_fog_value(float sphericalDistance, float cylindricalDistance, float environmentalStart, float environmentalEnd, float renderStart, float renderEnd) {
                    if ((%2$s & %3$d) == 0) {
                        return %6$s(sphericalDistance, cylindricalDistance, environmentalStart, environmentalEnd, renderStart, renderEnd);
                    }
                    float value = 1.0 - exp(-%1$s * sphericalDistance);
                    if ((%2$s & %4$d) != 0) {
                        value = max(value, linear_fog_value(cylindricalDistance, renderStart, renderEnd));
                    }
                    if ((%2$s & %5$d) != 0) {
                        value = max(value, linear_fog_value(sphericalDistance, environmentalStart, environmentalEnd));
                    }
                    return value;
                }"""
                        .formatted(
                                density,
                                flags,
                                TerrainExpFog.EXP,
                                TerrainExpFog.EDGE,
                                TerrainExpFog.DARKNESS,
                                RANGE)
                + source.substring(end);
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
