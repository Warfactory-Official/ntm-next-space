// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.render.FlatCutout;
import com.hbm.client.render.RenderCrucible;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.util.Vertices;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.BlockEntityMachineMagma.PourStream;
import com.hbmspace.blockentity.machine.BlockEntityMachineMagma;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderMagma
        implements BlockEntityRenderer<BlockEntityMachineMagma, RenderMagma.State>,
                ConcurrentRenderStateExtraction {

    private final HFRWavefrontObject mesh;
    private final int drillHead;
    private final int blades;
    private final RenderType partType;
    private final RenderType lavaType = RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS);
    private final RenderType streamType = FlatCutout.of(ResourceManager.foundry_stream_tex);

    public RenderMagma(HFRWavefrontObject mesh, Identifier texture) {
        this.mesh = mesh;
        this.drillHead = mesh.partId("Drillhead");
        this.blades = mesh.partId("Blades");
        this.partType = RenderTypes.entityCutoutCull(texture);
    }

    public static float yaw(Direction facing) {
        return Facing.yaw(facing, 180);
    }

    public static AABB extent(BlockPos pos) {
        return new AABB(
                pos.getX() - 4,
                pos.getY() - 5,
                pos.getZ() - 4,
                pos.getX() + 5,
                pos.getY() + 3,
                pos.getZ() + 5);
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
            BlockEntityMachineMagma be,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTicks, cameraPosition, breakProgress);
        state.facing = BlockMultiblockCore.coreFacing(be.getBlockState());
        state.rotation =
                be.prevDrillRotation + (be.drillRotation - be.prevDrillRotation) * partialTicks;
        state.lavaHeight = be.prevLavaHeight + (be.lavaHeight - be.prevLavaHeight) * partialTicks;
        state.lava =
                Minecraft.getInstance()
                        .getModelManager()
                        .getBlockStateModelSet()
                        .getParticleMaterial(Blocks.LAVA.defaultBlockState())
                        .sprite();
        state.streams.clear();
        long now = be.getLevel().getGameTime();
        for (PourStream stream : be.streams) {
            float age = now - stream.birth() + partialTicks;
            if (age < 20F) state.streams.add(new StreamState(stream.color(), stream.len(), age));
        }
    }

    @Override
    public void submit(State s, PoseStack ps, SubmitNodeCollector col, CameraRenderState camera) {
        int light = s.lightCoords;
        ps.pushPose();
        ps.translate(0.5, 0.0, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(yaw(s.facing)));
        ps.translate(0.0, -3.0, 0.0);

        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(s.rotation));
        col.submitCustomGeometry(
                ps, partType, (pose, buf) -> mesh.renderPart(pose, buf, light, -1, drillHead));
        ps.popPose();
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(-s.rotation));
        col.submitCustomGeometry(
                ps, partType, (pose, buf) -> mesh.renderPart(pose, buf, light, -1, blades));
        ps.popPose();

        if (s.lavaHeight > 0.01F && s.lava != null) {
            TextureAtlasSprite lava = s.lava;
            float y = s.lavaHeight;
            col.submitCustomGeometry(
                    ps,
                    lavaType,
                    (pose, buf) -> {
                        for (int ox = -1; ox <= 1; ox++) {
                            for (int oz = -1; oz <= 1; oz++)
                                lavaFace(pose, buf, light, ox - 0.5F, oz - 0.5F, y, lava);
                        }
                    });
        }
        ps.popPose();

        Direction dir = s.facing;
        for (StreamState stream : s.streams) {
            ps.pushPose();
            ps.translate(
                    0.5 + dir.getStepX() * BlockEntityMachineMagma.SPOUT,
                    1.0,
                    0.5 + dir.getStepZ() * BlockEntityMachineMagma.SPOUT);
            col.submitCustomGeometry(
                    ps,
                    streamType,
                    (pose, buf) ->
                            RenderCrucible.emitStream(
                                    pose,
                                    buf,
                                    stream.color(),
                                    dir,
                                    stream.len(),
                                    stream.age(),
                                    0.625F,
                                    0.625F));
            ps.popPose();
        }
    }

    private static void lavaFace(
            PoseStack.Pose pose,
            VertexConsumer buf,
            int light,
            float x,
            float z,
            float y,
            TextureAtlasSprite sprite) {
        vertex(pose, buf, light, x + 1, y, z + 1, sprite.getU1(), sprite.getV1());
        vertex(pose, buf, light, x + 1, y, z, sprite.getU1(), sprite.getV0());
        vertex(pose, buf, light, x, y, z, sprite.getU0(), sprite.getV0());
        vertex(pose, buf, light, x, y, z + 1, sprite.getU0(), sprite.getV1());
    }

    private static void vertex(
            PoseStack.Pose pose,
            VertexConsumer buf,
            int light,
            float x,
            float y,
            float z,
            float u,
            float v) {
        Vertices.emit(buf, pose, x, y, z, -1, u, v, light, 0F, 1F, 0F);
    }

    private record StreamState(int color, float len, float age) {}

    public static final class State extends BlockEntityRenderState {
        final List<StreamState> streams = new ArrayList<>();
        Direction facing = Direction.NORTH;
        float rotation;
        float lavaHeight;
        @Nullable TextureAtlasSprite lava;
    }
}
