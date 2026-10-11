// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.hbm.client.render.FlatCutout;
import com.hbm.client.render.WeaponRenderTypes;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.render.util.BeamPronter;
import com.hbm.util.GameTime;
import com.hbmspace.entity.mob.siege.EntitySiegeCraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import java.util.Random;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class RenderSiegeCraft extends EntityRenderer<EntitySiegeCraft, RenderSiegeCraft.State>
        implements ConcurrentRenderStateExtraction {

    static final int SPOKES = 8;
    static final Vec3 SPOKE = new Vec3(-1.125D, 0D, 2.875D);
    static final int SPOKE_OUTER = 0x80D0FF;
    static final int SPOKE_INNER = 0xFFFFFF;
    static final int GLINT_TINT = ARGB.colorFromFloat(1F, 0.5F * 0.76F, 0.76F, 0.76F);
    static final float GLINT_SPEED = 5F;
    static final int GLINT_LAYERS = 2;
    static final double LOCKON_SCALE = 0.1D;

    private static final RenderType[] GLINTS = new RenderType[GLINT_LAYERS];

    private final HFRWavefrontObject mesh = SiegeModels.SIEGE_UFO.get();
    private final int hull = mesh.partId("UFO");
    private final int coils = mesh.partId("Coils");
    private final RenderType[] hullTypes =
            SiegeModels.CRAFT_SKINS.stream()
                    .map(RenderTypes::entityCutout)
                    .toArray(RenderType[]::new);
    private final RenderType coilType = FlatCutout.culled(ResourceManager.white_tex);

    static {
        for (int k = 0; k < GLINT_LAYERS; k++) {
            int layer = k;
            String name = "hbmspace_siege_glint_" + k;
            GLINTS[k] =
                    RenderType.create(
                            name,
                            RenderSetup.builder(WeaponRenderTypes.TINTED_GLINT_PIPELINE)
                                    .withTexture("Sampler0", ResourceManager.glint_bf_tex)
                                    .useLightmap()
                                    .setTextureTransform(
                                            new TextureTransform(name, () -> glintMatrix(layer)))
                                    .createRenderSetup());
        }
    }

    public RenderSiegeCraft(EntityRendererProvider.Context context) {
        super(context);
    }

    static Matrix4f glintMatrix(int k) {
        float movement = (float) (GameTime.now() / 50D) * (0.001F + k * 0.003F) * GLINT_SPEED;
        return new Matrix4f()
                .rotateZ((float) Math.toRadians(30F - k * 60F))
                .translate(0F, movement, 0F);
    }

    static void hullPose(Matrix4f pose, EntitySiegeCraft entity, float partialTick) {
        pose.rotateY((float) ((entity.tickCount + partialTick) * 5D % 360D) * Mth.DEG_TO_RAD);
        if (!entity.isAlive()) {
            pose.rotate(
                    new Quaternionf()
                            .rotateAxis(
                                    (entity.deathTime + partialTick) * 5F * Mth.DEG_TO_RAD,
                                    0.70710677F,
                                    0F,
                                    0.70710677F));
        }
    }

    static boolean[] spokes(EntitySiegeCraft entity, boolean[] out) {
        Random rand = new Random(entity.tickCount / 4);
        boolean beam = entity.getBeam();
        for (int i = 0; i < SPOKES; i++) out[i] = rand.nextInt(5) == 0 || beam;
        return out;
    }

    public static int coilColor(EntitySiegeCraft entity) {
        float health = entity.getHealth() / entity.getMaxHealth();
        return ARGB.colorFromFloat(1F, 1F - health, health, 0F);
    }

    @Override
    protected boolean affectedByCulling(EntitySiegeCraft entity) {
        return false;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntitySiegeCraft entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tier = entity.getTier().id;
        hullPose(state.hull.identity(), entity, partialTicks);
        state.coils = coilColor(entity);
        state.beam = entity.getBeam();
        spokes(entity, state.spokes);
        state.arcStart = (int) (GameTime.now() % 1000L) / 50;
        state.lockon = entity.getLockon().subtract(entity.getX(), entity.getY(), entity.getZ());
        state.age = entity.tickCount;
    }

    @Override
    public void submit(
            State state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        int light = state.lightCoords;
        poseStack.pushPose();
        poseStack.translate(0F, 0.5F, 0F);

        poseStack.pushPose();
        poseStack.mulPose(state.hull);
        collector.submitCustomGeometry(
                poseStack,
                hullTypes[state.tier],
                (pose, buffer) -> mesh.renderPart(pose, buffer, light, -1, hull));
        int coilColor = state.coils;
        collector.submitCustomGeometry(
                poseStack,
                coilType,
                (pose, buffer) ->
                        mesh.renderPart(
                                pose, buffer, LightCoordsUtil.FULL_BRIGHT, coilColor, coils));
        if (state.beam) {
            for (RenderType glint : GLINTS) {
                collector.submitCustomGeometry(
                        poseStack,
                        glint,
                        (pose, buffer) -> mesh.renderPart(pose, buffer, light, GLINT_TINT, hull));
            }
        }
        poseStack.pushPose();
        for (int i = 0; i < SPOKES; i++) {
            poseStack.mulPose(Axis.YP.rotationDegrees(45F));
            if (!state.spokes[i]) continue;
            poseStack.pushPose();
            poseStack.translate(4F, 0F, 0F);
            BeamPronter.prontBeam(
                    poseStack,
                    collector,
                    SPOKE,
                    EnumWaveType.RANDOM,
                    EnumBeamType.LINE,
                    SPOKE_OUTER,
                    SPOKE_INNER,
                    state.arcStart,
                    15,
                    0.125F,
                    1,
                    0F);
            poseStack.popPose();
        }
        poseStack.popPose();
        poseStack.popPose();

        if (state.beam) {
            int segments = (int) (state.lockon.length() / 2 + 1);
            BeamPronter.prontBeam(
                    poseStack,
                    collector,
                    state.lockon,
                    EnumWaveType.RANDOM,
                    EnumBeamType.SOLID,
                    0x101020,
                    0x101020,
                    state.age / 6,
                    segments,
                    (float) LOCKON_SCALE,
                    4,
                    0.25F);
            BeamPronter.prontBeam(
                    poseStack,
                    collector,
                    state.lockon,
                    EnumWaveType.RANDOM,
                    EnumBeamType.SOLID,
                    0x202060,
                    0x202060,
                    state.age / 2,
                    segments,
                    (float) LOCKON_SCALE * 7F,
                    2,
                    0.0625F);
            BeamPronter.prontBeam(
                    poseStack,
                    collector,
                    state.lockon,
                    EnumWaveType.RANDOM,
                    EnumBeamType.SOLID,
                    0x202060,
                    0x202060,
                    state.age / 4,
                    segments,
                    (float) LOCKON_SCALE * 7F,
                    2,
                    0.0625F);
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static final class State extends EntityRenderState {
        final Matrix4f hull = new Matrix4f();
        final boolean[] spokes = new boolean[SPOKES];
        int tier;
        int coils;
        boolean beam;
        int arcStart;
        Vec3 lockon = Vec3.ZERO;
        int age;
    }
}
