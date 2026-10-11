// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.google.common.base.Suppliers;
import com.hbm.client.model.Meshes;
import com.hbm.client.render.BobbleFigures;
import com.hbm.client.render.PlushieFigures;
import com.hbm.client.render.RenderBobble;
import com.hbm.client.render.RenderPlushie;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.generic.BobbleTypeSpace;
import com.hbmspace.blocks.generic.PlushieTypeSpace;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;

public final class TrinketFiguresSpace {

    private TrinketFiguresSpace() {}

    public static void register() {
        Supplier<Vocaloid.Model> teto = Vocaloid.model("teto");
        PlushieFigures.register(
                PlushieTypeSpace.TETO, new Vocaloid(teto, 0.5F, 0.5F, 0.5F, 1.5F, 1.5F, 1.5F));
        PlushieFigures.register(
                PlushieTypeSpace.MIKU,
                new Vocaloid(Vocaloid.model("miku"), 0.5F, 0.5F, 0.5F, 1.5F, 1.5F, 1.5F));
        PlushieFigures.register(
                PlushieTypeSpace.NERU,
                new Vocaloid(Vocaloid.model("neru"), 0.5F, 0.5F, 0.5F, 1.5F, 1.5F, 1.5F));
        PlushieFigures.register(
                PlushieTypeSpace.FATO, new Vocaloid(teto, 1.0F, 0.5F, 1.0F, 3.0F, 1.5F, 3.0F));

        BobbleFigures.register(
                BobbleTypeSpace.GWEN,
                new RenderBobble.Figure(skin("gwen"), BobbleFigures.NO_POSE, null));
        BobbleFigures.register(
                BobbleTypeSpace.JUICE,
                new RenderBobble.Figure(skin("juicy_lad"), BobbleFigures.NO_POSE, null));
        BobbleFigures.register(
                BobbleTypeSpace.JAMESH_2,
                new RenderBobble.Figure(skin("jamesh_2"), BobbleFigures.NO_POSE, null));
        BobbleFigures.register(
                BobbleTypeSpace.MRKIMKIMORA,
                new RenderBobble.Figure(
                        skin("mrkimkimora"),
                        BobbleFigures.NO_POSE,
                        Suppliers.memoize(
                                () ->
                                        new RenderBobble.Body(
                                                ResourceManager.bobble,
                                                ResourceManager.bobble.partId("MrKimkimora")))));
    }

    private static Identifier skin(String name) {
        return id("textures/models/trinkets/" + name + ".png");
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }

    private record Vocaloid(
            Supplier<Model> model, float x, float y, float z, float itemX, float itemY, float itemZ)
            implements RenderPlushie.Figure {

        private static final RenderPlushie.Face CALM = new RenderPlushie.Face(Long.MIN_VALUE, 6);
        private static final RenderPlushie.Face BLUSHING =
                new RenderPlushie.Face(7, Long.MAX_VALUE);

        record Model(HFRWavefrontObject mesh, RenderPlushie.Part skin, RenderPlushie.Part blush) {}

        static Supplier<Model> model(String name) {
            return Suppliers.memoize(
                    () ->
                            new Model(
                                    Meshes.load(id("models/trinkets/" + name + ".obj")),
                                    new RenderPlushie.Part(skin(name), true),
                                    new RenderPlushie.Part(skin(name + "_squished"), true)));
        }

        @Override
        public void scale(PoseStack poses) {
            poses.scale(x, y, z);
        }

        @Override
        public void itemPose(PoseStack poses) {
            poses.translate(0, 0.25, 0);
            poses.scale(itemX, itemY, itemZ);
        }

        @Override
        public void visitParts(PoseStack poses, RenderPlushie.PartVisitor visitor) {
            Model built = model.get();
            for (int group = 0; group < built.mesh().groups.length; group++) {
                visitor.accept(built.skin(), built.mesh(), group, CALM, poses);
                visitor.accept(built.blush(), built.mesh(), group, BLUSHING, poses);
            }
        }

        @Override
        public List<HFRWavefrontObject> meshes() {
            return List.of(model.get().mesh());
        }
    }
}
