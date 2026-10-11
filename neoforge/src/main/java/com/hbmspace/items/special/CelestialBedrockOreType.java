// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2024 HbmMods (The Bobcat)
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.special;

import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.special.BedrockOreKind;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreGrade;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreOutput;
import com.hbm.items.special.ItemBedrockOreNew.BedrockOreType;
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
            int color = dark ? output.mat.solidColorDark : output.mat.solidColorLight;
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

    private static Component output(String step, NTMMaterial mat) {
        return Component.translatable(
                        "item.hbmspace.bedrock_ore.output." + step,
                        Component.translatable(mat.getUnlocalizedName())
                                .withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.DARK_GRAY);
    }
}
