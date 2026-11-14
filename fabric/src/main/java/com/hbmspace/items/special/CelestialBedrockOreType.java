/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-FileCopyrightText: 2024 HbmMods (The Bobcat)
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * Copyright (C) 2024 HbmMods (The Bobcat). All rights reserved.
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

package com.hbmspace.items.special;

import com.hbm.inventory.material.Material;
import com.hbm.items.special.BedrockOreKind;
import com.hbm.items.special.BedrockOreNewItem.BedrockOreGrade;
import com.hbm.items.special.BedrockOreNewItem.BedrockOreOutput;
import com.hbm.items.special.BedrockOreNewItem.BedrockOreType;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.SolarSystem.Body;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class CelestialBedrockOreType implements BedrockOreKind {

    public final Body body;
    public final String suffix;
    public final BedrockOreOutput primary;
    public final BedrockOreOutput byproductAcid;
    public final BedrockOreOutput byproductSolvent;
    public final BedrockOreOutput byproductRad;
    public final int light;
    public final int dark;
    private final boolean ntmName;

    CelestialBedrockOreType(
            Body body,
            String suffix,
            BedrockOreOutput primary,
            BedrockOreOutput byproductAcid,
            BedrockOreOutput byproductSolvent,
            BedrockOreOutput byproductRad) {
        this.body = body;
        this.suffix = suffix;
        this.primary = primary;
        this.byproductAcid = byproductAcid;
        this.byproductSolvent = byproductSolvent;
        this.byproductRad = byproductRad;
        this.light = averageColor(false, primary, byproductAcid, byproductSolvent, byproductRad);
        this.dark = averageColor(true, primary, byproductAcid, byproductSolvent, byproductRad);
        boolean ntm = false;
        for (BedrockOreType type : BedrockOreType.VALUES) ntm |= type.suffix.equals(suffix);
        this.ntmName = ntm;
    }

    private static int averageColor(boolean dark, BedrockOreOutput... outputs) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (BedrockOreOutput output : outputs) {
            int color = dark ? output.mat.darkColor() : output.mat.lightColor();
            r += color >> 16 & 255;
            g += color >> 8 & 255;
            b += color & 255;
        }
        r /= outputs.length;
        g /= outputs.length;
        b /= outputs.length;
        return r << 16 | g << 8 | b;
    }

    @Override
    public String id() {
        return body.name + "_" + suffix;
    }

    @Override
    public String suffix() {
        return suffix;
    }

    @Override
    public Component typeName() {
        return ntmName
                ? BedrockOreKind.super.typeName()
                : Component.translatable("item." + HbmSpace.MOD_ID + ".bedrock_ore.type." + suffix);
    }

    @Override
    public int light() {
        return light;
    }

    @Override
    public int dark() {
        return dark;
    }

    @Override
    public Identifier channel() {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "bedrock_ore/" + id());
    }

    @Override
    public Component nameSuffix() {
        return Component.literal(" (")
                .append(Component.translatable("body." + body.name))
                .append(")");
    }

    @Override
    public void appendHoverText(BedrockOreGrade grade, Consumer<Component> adder) {
        if (grade != BedrockOreGrade.BASE) return;
        adder.accept(
                Component.translatable("item.hbmspace.bedrock_ore.outputs")
                        .withStyle(ChatFormatting.DARK_GRAY));
        adder.accept(output("main", primary.mat));
        adder.accept(output("sulfuric", byproductAcid.mat));
        adder.accept(output("solvent", byproductSolvent.mat));
        adder.accept(output("hps", byproductRad.mat));
    }

    @Override
    public String toString() {
        return id();
    }

    private static Component output(String step, Material mat) {
        return Component.translatable(
                        "item.hbmspace.bedrock_ore.output." + step,
                        Component.translatable(mat.translationKey()).withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.DARK_GRAY);
    }
}
