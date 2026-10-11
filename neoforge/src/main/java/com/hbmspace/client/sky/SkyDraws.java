// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbm.render.loader.GroupObject;
import com.hbm.render.loader.HFRWavefrontObject;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public final class SkyDraws implements AutoCloseable {

    private static final int SHADER_TIME_SIZE =
            new Std140SizeCalculator().putFloat().putInt().get();

    private final StagedVertexBuffer vertices = new StagedVertexBuffer(() -> "Space sky", 1 << 16);
    private final DynamicUniformStorage<PlanetInfo> planetInfo =
            new DynamicUniformStorage<>("Space sky planets", PlanetInfo.SIZE, 32);
    private final DynamicUniformStorage<ShaderTime> shaderTime =
            new DynamicUniformStorage<>("Space sky shader time", SHADER_TIME_SIZE, 4);
    private final List<Step> steps = new ArrayList<>();

    private sealed interface Step {}

    private record Draw(
            StagedVertexBuffer.Draw draw,
            RenderPipeline pipeline,
            Matrix4f modelView,
            Vector4f color,
            @Nullable Identifier texture0,
            @Nullable Identifier texture1,
            @Nullable Identifier texture2,
            @Nullable PlanetInfo planet,
            float time)
            implements Step {}

    private record ClearDepth() implements Step {}

    private record ShaderTime(float time, int baseVertex)
            implements DynamicUniformStorage.DynamicUniform {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putFloat(time).putInt(baseVertex);
        }
    }

    public VertexConsumer draw(
            RenderPipeline pipeline,
            PoseStack pose,
            float r,
            float g,
            float b,
            float a,
            @Nullable Identifier texture0,
            @Nullable Identifier texture1,
            @Nullable Identifier texture2,
            @Nullable PlanetInfo planet,
            float time) {
        StagedVertexBuffer.Draw draw =
                vertices.appendDraw(
                        Objects.requireNonNull(pipeline.getVertexFormatBinding(0)),
                        pipeline.getPrimitiveTopology());
        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewStack()).mul(pose.last().pose());
        Vector4f color =
                new Vector4f(
                        Mth.clamp(r, 0, 1),
                        Mth.clamp(g, 0, 1),
                        Mth.clamp(b, 0, 1),
                        Mth.clamp(a, 0, 1));
        steps.add(
                new Draw(
                        draw, pipeline, modelView, color, texture0, texture1, texture2, planet,
                        time));
        return vertices.getVertexBuilder(draw);
    }

    public VertexConsumer draw(
            RenderPipeline pipeline,
            PoseStack pose,
            float r,
            float g,
            float b,
            float a,
            @Nullable Identifier texture0,
            @Nullable Identifier texture1,
            @Nullable PlanetInfo planet,
            float time) {
        return draw(pipeline, pose, r, g, b, a, texture0, texture1, null, planet, time);
    }

    public VertexConsumer draw(
            RenderPipeline pipeline,
            PoseStack pose,
            float r,
            float g,
            float b,
            float a,
            @Nullable Identifier texture) {
        return draw(pipeline, pose, r, g, b, a, texture, null, null, 0);
    }

    public void obj(
            RenderPipeline pipeline,
            PoseStack pose,
            float r,
            float g,
            float b,
            float a,
            Identifier texture,
            HFRWavefrontObject mesh,
            float time) {
        VertexConsumer buffer = draw(pipeline, pose, r, g, b, a, texture, null, null, time);
        for (GroupObject group : mesh.groups) {
            float[] corners = group.quads(true);
            for (int i = 0; i < corners.length; i += GroupObject.STRIDE) {
                buffer.addVertex(corners[i], corners[i + 1], corners[i + 2])
                        .setUv(corners[i + 3], corners[i + 4]);
            }
        }
    }

    public void clearDepth() {
        steps.add(new ClearDepth());
    }

    public void flush() {
        if (steps.isEmpty()) return;
        vertices.upload();

        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        GpuTextureView color =
                RenderSystem.outputColorTextureOverride != null
                        ? RenderSystem.outputColorTextureOverride
                        : target.getColorTextureView();
        GpuTextureView depth =
                RenderSystem.outputDepthTextureOverride != null
                        ? RenderSystem.outputDepthTextureOverride
                        : target.getDepthTextureView();
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        for (Step step : steps) {
            if (step instanceof ClearDepth) {
                encoder.clearDepthTexture(depth.texture(), 0.0D);
                continue;
            }

            Draw draw = (Draw) step;
            StagedVertexBuffer.ExecuteInfo info = vertices.getExecuteInfo(draw.draw);
            if (info == null) continue;

            GpuBufferSlice transforms =
                    RenderSystem.getDynamicUniforms().writeTransform(draw.modelView, draw.color);
            try (RenderPass pass =
                    encoder.createRenderPass(
                            () -> "Space sky",
                            color,
                            Optional.empty(),
                            depth,
                            OptionalDouble.empty())) {
                pass.setPipeline(draw.pipeline);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", transforms);
                if (draw.planet != null)
                    pass.setUniform("PlanetInfo", planetInfo.writeUniform(draw.planet));
                if (draw.pipeline == SkyPipelines.SWARM
                        || draw.pipeline == SkyPipelines.BLACK_HOLE
                        || draw.pipeline == SkyPipelines.FLESH) {
                    pass.setUniform(
                            "ShaderTime",
                            shaderTime.writeUniform(new ShaderTime(draw.time, info.baseVertex())));
                }
                if (draw.texture0 != null) bind(pass, "Sampler0", draw.texture0);
                if (draw.texture1 != null) bind(pass, "Sampler1", draw.texture1);
                if (draw.texture2 != null) bind(pass, "Sampler2", draw.texture2);
                pass.setVertexBuffer(0, info.vertexBuffer().slice());
                pass.setIndexBuffer(info.indexBuffer(), info.indexType());
                pass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
            }
        }

        vertices.endDraw();
        steps.clear();
    }

    public void endFrame() {
        vertices.endFrame();
        planetInfo.endFrame();
        shaderTime.endFrame();
    }

    private static void bind(RenderPass pass, String sampler, Identifier location) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        pass.bindTexture(
                sampler,
                texture.getTextureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
    }

    @Override
    public void close() {
        vertices.close();
        planetInfo.close();
        shaderTime.close();
    }
}
