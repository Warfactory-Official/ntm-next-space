// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.BlockEntityHydroponic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderHydroponic
        implements BlockEntityRenderer<BlockEntityHydroponic, RenderHydroponic.State>,
                ConcurrentRenderStateExtraction {

    private final HFRWavefrontObject mesh;
    private final int lights;
    private final RenderType type;

    public RenderHydroponic(HFRWavefrontObject mesh, Identifier texture) {
        this.mesh = mesh;
        this.lights = mesh.partId("Lights");
        this.type = IndustryRenderTypes.lamp(texture);
    }

    public static float yaw(Direction facing) {
        return Facing.yaw(facing, 90);
    }

    public static boolean lit(BlockEntityHydroponic be) {
        return be.power >= BlockEntityHydroponic.LIGHT_POWER;
    }

    public static AABB extent(BlockPos pos) {
        return new AABB(
                pos.getX() - 2,
                pos.getY(),
                pos.getZ() - 2,
                pos.getX() + 3,
                pos.getY() + 3,
                pos.getZ() + 3);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void extractRenderState(
            BlockEntityHydroponic be,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTicks, cameraPosition, breakProgress);
        state.facing = BlockMultiblockCore.coreFacing(be.getBlockState());
        state.lit = lit(be);
    }

    @Override
    public void submit(
            State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.lit) return;
        ps.pushPose();
        ps.translate(0.5, 0.0, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(yaw(state.facing)));
        collector.submitCustomGeometry(
                ps,
                type,
                (pose, buffer) ->
                        mesh.renderPart(pose, buffer, LightCoordsUtil.FULL_BRIGHT, -1, lights));
        ps.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean lit;
    }
}
