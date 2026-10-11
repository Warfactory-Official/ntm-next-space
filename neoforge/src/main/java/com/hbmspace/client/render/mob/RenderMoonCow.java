// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.hbm.client.render.WorldRenderPipeline;
import com.hbm.lib.Library;
import com.hbm.render.util.Vertices;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.mob.EntityMoonCow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class RenderMoonCow extends MobRenderer<EntityMoonCow, LivingEntityRenderState, ModelMoonCow>
        implements ConcurrentRenderStateExtraction {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/entity/moon_cow.png");

    private final ModelMoonCow adult;
    private final ModelMoonCow baby;

    public RenderMoonCow(EntityRendererProvider.Context context) {
        super(context, new ModelMoonCow(ModelMoonCow.adult().bakeRoot()), 0.7F);
        adult = model;
        baby = new ModelMoonCow(ModelMoonCow.baby().bakeRoot());
        addLayer(new Helmet(this));
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public void submit(
            LivingEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        model = state.isBaby ? baby : adult;
        super.submit(state, poseStack, collector, camera);
    }

    private static final class Helmet extends RenderLayer<LivingEntityRenderState, ModelMoonCow> {

        private static final RenderType GLASS =
                WorldRenderPipeline.oneSidedCutout(Library.id("textures/block/glass_boron.png"));
        private static final float[][] FACES = {
            {
                -0.5F, 0.5F, -0.5F, 1, 1, 0.5F, 0.5F, -0.5F, 0, 1, 0.5F, -0.5F, -0.5F, 0, 0, -0.5F,
                -0.5F, -0.5F, 1, 0
            },
            {
                -0.5F, 0.5F, 0.5F, 1, 1, -0.5F, 0.5F, -0.5F, 0, 1, -0.5F, -0.5F, -0.5F, 0, 0, -0.5F,
                -0.5F, 0.5F, 1, 0
            },
            {
                0.5F, 0.5F, 0.5F, 1, 1, -0.5F, 0.5F, 0.5F, 0, 1, -0.5F, -0.5F, 0.5F, 0, 0, 0.5F,
                -0.5F, 0.5F, 1, 0
            },
            {
                0.5F, 0.5F, -0.5F, 1, 1, 0.5F, 0.5F, 0.5F, 0, 1, 0.5F, -0.5F, 0.5F, 0, 0, 0.5F,
                -0.5F, -0.5F, 1, 0
            },
            {
                -0.5F, -0.5F, -0.5F, 1, 1, 0.5F, -0.5F, -0.5F, 0, 1, 0.5F, -0.5F, 0.5F, 0, 0, -0.5F,
                -0.5F, 0.5F, 1, 0
            },
            {
                0.5F, 0.5F, -0.5F, 1, 1, -0.5F, 0.5F, -0.5F, 0, 1, -0.5F, 0.5F, 0.5F, 0, 0, 0.5F,
                0.5F, 0.5F, 1, 0
            }
        };
        private static final float[][] NORMALS = {
            {0, 0, -1}, {-1, 0, 0}, {0, 0, 1}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}
        };

        Helmet(RenderLayerParent<LivingEntityRenderState, ModelMoonCow> parent) {
            super(parent);
        }

        @Override
        public void submit(
                PoseStack poseStack,
                SubmitNodeCollector collector,
                int lightCoords,
                LivingEntityRenderState state,
                float yRot,
                float xRot) {
            int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
            poseStack.pushPose();
            getParentModel().head().translateAndRotate(poseStack);
            poseStack.translate(0.0F, 0.0F, -0.25F);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            collector.submitCustomGeometry(
                    poseStack, GLASS, (pose, buffer) -> cube(pose, buffer, lightCoords, overlay));
            poseStack.popPose();
        }

        private static void cube(
                PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay) {
            for (int face = 0; face < FACES.length; face++) {
                float[] f = FACES[face];
                float[] n = NORMALS[face];
                for (int c = 0; c < 20; c += 5) {
                    Vertices.emit(
                            buffer, pose, f[c], f[c + 1], f[c + 2], -1, f[c + 3], f[c + 4], overlay,
                            light, n[0], n[1], n[2]);
                }
            }
        }
    }
}
