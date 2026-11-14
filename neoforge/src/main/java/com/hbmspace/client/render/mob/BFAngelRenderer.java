/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 JamesH_2. All rights reserved.
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

package com.hbmspace.client.render.mob;

import com.hbm.client.render.FlatCutout;
import com.hbm.client.render.WorldRenderPipeline;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.util.Vertices;
import com.hbmspace.entity.mob.BFAngelEntity;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class BFAngelRenderer extends EntityRenderer<BFAngelEntity, BFAngelRenderer.State>
        implements ConcurrentRenderStateExtraction {

    static final String[] CORE = {"eye", "body"};
    static final String[] WINGS = {"wingL1", "wingR1", "wingL2", "wingR2", "wingL3", "wingR3"};
    static final int RAYS = 64;
    static final float RAY_SCALE = 0.4F * 0.4F * 2F;
    static final int RAY_TIP = ARGB.colorFromFloat(0.4F * 0.4F * 2F, 1F, 0.6F, 0.6F);
    static final int RAY_EDGE = ARGB.colorFromFloat(0F, 0.1F, 0.6F, 0.1F);
    static final float[][] RAY_TURNS = new float[RAYS][5];
    static final float[] RAY_LENGTH = new float[RAYS];
    static final float[] RAY_WIDTH = new float[RAYS];

    public static final RenderPipeline FLASH_PIPELINE =
            WorldRenderPipeline.of(
                    RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                            .withLocation(SiegeModels.id("pipeline/bfa_flash"))
                            .withVertexShader("core/rendertype_lightning")
                            .withFragmentShader("core/rendertype_lightning")
                            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                            .withPrimitiveTopology(PrimitiveTopology.QUADS)
                            .withDepthStencilState(
                                    new DepthStencilState(
                                            DepthStencilState.DEFAULT.depthTest(), false)));
    private static final RenderType FLASH =
            RenderType.create(
                    "hbmspace_bfa_flash", RenderSetup.builder(FLASH_PIPELINE).createRenderSetup());

    static {
        Random random = new Random(425L);
        for (int i = 0; i < RAYS; i++) {
            for (int k = 0; k < 5; k++) RAY_TURNS[i][k] = random.nextFloat() * 360F;
            RAY_LENGTH[i] = (random.nextFloat() * 20F + 5F + 10F) * RAY_SCALE;
            RAY_WIDTH[i] = (random.nextFloat() * 2F + 1F + 2F) * RAY_SCALE;
        }
    }

    private final HFRWavefrontObject mesh = SiegeModels.ANGEL.get();
    private final int[] core = mesh.partIds(CORE);
    private final int[] wings = mesh.partIds(WINGS);
    private final RenderType coreType = RenderTypes.entityCutout(SiegeModels.ANGEL_TEX);
    private final RenderType wingType = FlatCutout.of(SiegeModels.ANGEL_TEX);

    public BFAngelRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static void forEachPipeline(Consumer<RenderPipeline> registrar) {
        registrar.accept(FLASH_PIPELINE);
    }

    static void rootPose(Matrix4f pose, BFAngelEntity entity, float partialTick) {
        float yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTick;
        float pitch = entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTick;
        pose.translate(0F, 1F, 0F).rotateY(-yaw * Mth.DEG_TO_RAD).rotateX(pitch * Mth.DEG_TO_RAD);
    }

    static float raySeed(BFAngelEntity entity, float partialTick) {
        return (entity.tickCount + partialTick + partialTick) * 0.06F;
    }

    static void turnRay(Matrix4f pose, int i, float seed) {
        float[] turns = RAY_TURNS[i];
        pose.rotateX((turns[0] + seed) * Mth.DEG_TO_RAD);
        pose.rotateY((turns[1] + seed) * Mth.DEG_TO_RAD);
        pose.rotateZ((turns[2] + seed) * Mth.DEG_TO_RAD);
        pose.rotateX((turns[3] + seed) * Mth.DEG_TO_RAD);
        pose.rotateY((turns[4] + seed) * Mth.DEG_TO_RAD);
    }

    static void bodyPose(Matrix4f pose) {
        pose.scale(2F).translate(0.5F, 0.2F, 0.5F).translate(0F, 0.5F, 0F);
    }

    static void wingPose(Matrix4f pose, int i, BFAngelEntity entity, float partialTick) {
        double time = entity.tickCount + partialTick;
        double cy0 = Math.sin(time * 0.3 % (Math.PI * 2));
        double cy1 = Math.sin(time * 0.25 % (Math.PI * 2) - Math.PI * 0.2);
        double cy2 = Math.sin(time * 0.3 % (Math.PI * 2) - Math.PI * 0.4);
        double sign = i % 2 == 0 ? 1D : -1D;
        switch (i / 2) {
            case 0 -> pose.rotateZ(rad(cy0 * 2 * sign)).rotateY(rad(cy0 * 20 * sign));
            case 1 -> pose.rotateZ(rad(cy1 * 5 * sign)).rotateY(rad(cy1 * 20 * sign));
            default -> pose.rotateX(rad(cy1 * 5 * sign)).rotateY(rad(cy2 * 20 * sign));
        }
    }

    private static float rad(double degrees) {
        return (float) Math.toRadians(degrees);
    }

    @Override
    protected boolean affectedByCulling(BFAngelEntity entity) {
        return false;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BFAngelEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        rootPose(state.root.identity(), entity, partialTicks);
        state.seed = raySeed(entity, partialTicks);
        for (int i = 0; i < WINGS.length; i++)
            wingPose(state.wings[i].identity(), i, entity, partialTicks);
    }

    @Override
    public void submit(
            State state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        int light = state.lightCoords;
        poseStack.pushPose();
        poseStack.mulPose(state.root);

        poseStack.pushPose();
        poseStack.translate(1F, 2.2F, 1F);
        float seed = state.seed;
        collector.submitCustomGeometry(
                poseStack,
                FLASH,
                (pose, buffer) -> {
                    Matrix4f ray = new Matrix4f();
                    PoseStack rays = new PoseStack();
                    for (int i = 0; i < RAYS; i++) {
                        turnRay(ray, i, seed);
                        rays.last().set(pose);
                        rays.mulPose(ray);
                        spike(rays.last(), buffer, RAY_LENGTH[i], RAY_WIDTH[i]);
                    }
                });
        poseStack.popPose();

        poseStack.pushPose();
        Matrix4f body = new Matrix4f();
        bodyPose(body);
        poseStack.mulPose(body);
        for (int part : core) {
            collector.submitCustomGeometry(
                    poseStack,
                    coreType,
                    (pose, buffer) -> mesh.renderPart(pose, buffer, light, -1, part));
        }
        for (int i = 0; i < wings.length; i++) {
            int part = wings[i];
            poseStack.pushPose();
            poseStack.mulPose(state.wings[i]);
            collector.submitCustomGeometry(
                    poseStack,
                    wingType,
                    (pose, buffer) -> mesh.renderPart(pose, buffer, light, -1, part));
            poseStack.popPose();
        }
        poseStack.popPose();

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void spike(
            PoseStack.Pose pose, VertexConsumer buffer, float length, float width) {
        float ax = -0.866F * width, az = -0.5F * width;
        float bx = 0.866F * width, bz = -0.5F * width;
        float cx = 0F, cz = width;
        triangle(pose, buffer, ax, length, az, bx, length, bz);
        triangle(pose, buffer, bx, length, bz, cx, length, cz);
        triangle(pose, buffer, cx, length, cz, ax, length, az);
    }

    private static void triangle(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2) {
        Vertices.emit(buffer, pose, 0F, 0F, 0F, RAY_TIP);
        Vertices.emit(buffer, pose, x1, y1, z1, RAY_EDGE);
        Vertices.emit(buffer, pose, x2, y2, z2, RAY_EDGE);
        Vertices.emit(buffer, pose, x2, y2, z2, RAY_EDGE);
    }

    public static final class State extends EntityRenderState {
        final Matrix4f root = new Matrix4f();
        final Matrix4f[] wings = new Matrix4f[WINGS.length];
        float seed;

        public State() {
            for (int i = 0; i < wings.length; i++) wings[i] = new Matrix4f();
        }
    }
}
