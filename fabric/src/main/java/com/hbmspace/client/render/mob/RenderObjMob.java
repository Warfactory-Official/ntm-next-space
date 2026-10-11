// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.hbm.client.render.WorldRenderPipeline;
import com.hbm.client.render.flywheel.MobObjVisual;
import com.hbm.client.render.flywheel.ObjEntityVisual;
import com.hbm.render.loader.HFRWavefrontObject;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

public class RenderObjMob<T extends LivingEntity> extends EntityRenderer<T, RenderObjMob.State>
        implements ConcurrentRenderStateExtraction {

    private final ObjMobRig<T> rig;
    private final HFRWavefrontObject mesh;
    private final int[] groups;
    private final RenderType[] types;
    private final RenderType[] groupTypes;

    public RenderObjMob(EntityRendererProvider.Context context, ObjMobRig<T> rig) {
        super(context);
        this.rig = rig;
        this.mesh = rig.mesh().get();
        this.groups = rig.parts().stream().mapToInt(part -> mesh.partId(part.name())).toArray();
        this.types =
                rig.skins().stream()
                        .map(WorldRenderPipeline::oneSidedCutout)
                        .toArray(RenderType[]::new);
        this.groupTypes =
                rig.parts().stream()
                        .map(part -> rig.groupSkins().get(part.name()))
                        .map(own -> own == null ? null : WorldRenderPipeline.oneSidedCutout(own))
                        .toArray(RenderType[]::new);
        this.shadowRadius = rig.shadowRadius();
    }

    @Override
    public State createRenderState() {
        return new State(groups.length);
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.skin = rig.skin().applyAsInt(entity);
        state.overlay = ObjEntityVisual.damageOverlay(entity);
        Matrix4f base = state.base.identity();
        rig.pose().apply(base, entity, partialTicks);
        for (int i = 0; i < groups.length; i++) {
            MobObjVisual.Part<T> part = rig.parts().get(i);
            state.visible[i] = part.visible().test(entity);
            part.pose().apply(state.parts[i].set(base), entity, partialTicks);
        }
    }

    @Override
    public void submit(
            State state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        int light = state.lightCoords;
        int overlay = state.overlay;
        RenderType skin = types[state.skin];
        for (int i = 0; i < groups.length; i++) {
            if (!state.visible[i]) continue;
            int group = groups[i];
            RenderType type = groupTypes[i] != null ? groupTypes[i] : skin;
            poseStack.pushPose();
            poseStack.mulPose(state.parts[i]);
            collector.submitCustomGeometry(
                    poseStack,
                    type,
                    (pose, buffer) -> mesh.renderPart(pose, buffer, light, -1, overlay, group));
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    public static final class State extends EntityRenderState {
        final Matrix4f base = new Matrix4f();
        final Matrix4f[] parts;
        final boolean[] visible;
        int skin;
        int overlay;

        State(int groups) {
            parts = new Matrix4f[groups];
            for (int i = 0; i < groups; i++) parts[i] = new Matrix4f();
            visible = new boolean[groups];
        }
    }
}
