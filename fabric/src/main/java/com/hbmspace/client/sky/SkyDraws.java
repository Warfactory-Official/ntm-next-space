/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.client.sky;

import com.hbm.api.client.ShaderPacks;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicGpuDataStorage.DynamicGpuData;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public final class SkyDraws implements AutoCloseable {

    public static final Matrix4fc IDENTITY = new Matrix4f();
    private static final Vector3f NO_OFFSET = new Vector3f();
    private static final int SHADER_TIME_SIZE =
            new Std140SizeCalculator().putFloat().putInt().get();

    private final StagedVertexBuffer vertices = new StagedVertexBuffer(() -> "Space sky", 1 << 16);
    private final Map<SkyMesh, Uploaded> meshes = new IdentityHashMap<>();
    private final DynamicGpuDataStorageMapped<PlanetInfo> planetInfo =
            new DynamicGpuDataStorageMapped<>(
                    "Space sky planets", PlanetInfo.SIZE, GpuBuffer.USAGE_UNIFORM, 32);
    private final DynamicGpuDataStorageMapped<ShaderTime> shaderTime =
            new DynamicGpuDataStorageMapped<>(
                    "Space sky shader time", SHADER_TIME_SIZE, GpuBuffer.USAGE_UNIFORM, 4);
    private final List<Step> steps = new ArrayList<>();
    private final List<Prepared> run = new ArrayList<>();

    private sealed interface Step {}

    private record Draw(
            StagedVertexBuffer.@Nullable Draw draw,
            @Nullable SkyMesh mesh,
            RenderPipeline pipeline,
            Matrix4f modelView,
            Vector4f color,
            Matrix4f uvTransform,
            @Nullable Identifier texture0,
            @Nullable Identifier texture1,
            @Nullable Identifier texture2,
            @Nullable PlanetInfo planet,
            float time)
            implements Step {}

    private record ClearDepth() implements Step {}

    private record Prepared(
            RenderPipeline pipeline,
            StagedVertexBuffer.ExecuteInfo info,
            GpuBufferSlice transforms,
            @Nullable GpuBufferSlice planet,
            @Nullable GpuBufferSlice time,
            @Nullable AbstractTexture texture0,
            @Nullable AbstractTexture texture1,
            @Nullable AbstractTexture texture2) {}

    private record Uploaded(GpuBuffer vertices, int indexCount) {}

    private record ShaderTime(float time, int baseVertex) implements DynamicGpuData {

        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer).putFloat(time).putInt(baseVertex);
        }
    }

    public static Matrix4f lifted(float y, float size) {
        return new Matrix4f().translation(0.0F, y, 0.0F).scale(size, 1.0F, size);
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
        steps.add(
                new Draw(
                        draw,
                        null,
                        pipeline,
                        modelView(pose, IDENTITY),
                        color(r, g, b, a),
                        new Matrix4f(),
                        texture0,
                        texture1,
                        texture2,
                        planet,
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

    public void mesh(
            SkyMesh mesh,
            RenderPipeline pipeline,
            PoseStack pose,
            Matrix4fc local,
            float r,
            float g,
            float b,
            float a,
            @Nullable Identifier texture0,
            @Nullable Identifier texture1,
            @Nullable PlanetInfo planet,
            float time,
            float uOffset) {
        assert mesh.format() == pipeline.getVertexFormatBinding(0)
                && mesh.topology() == pipeline.getPrimitiveTopology();
        steps.add(
                new Draw(
                        null,
                        mesh,
                        pipeline,
                        modelView(pose, local),
                        color(r, g, b, a),
                        new Matrix4f().translation(uOffset, 0.0F, 0.0F),
                        texture0,
                        texture1,
                        null,
                        planet,
                        time));
    }

    public void mesh(
            SkyMesh mesh,
            RenderPipeline pipeline,
            PoseStack pose,
            Matrix4fc local,
            float r,
            float g,
            float b,
            float a,
            @Nullable Identifier texture) {
        mesh(mesh, pipeline, pose, local, r, g, b, a, texture, null, null, 0, 0);
    }

    private static Matrix4f modelView(PoseStack pose, Matrix4fc local) {
        return new Matrix4f(RenderSystem.getModelViewStack()).mul(pose.last().pose()).mul(local);
    }

    private static Vector4f color(float r, float g, float b, float a) {
        return new Vector4f(
                Mth.clamp(r, 0, 1), Mth.clamp(g, 0, 1), Mth.clamp(b, 0, 1), Mth.clamp(a, 0, 1));
    }

    public void clearDepth() {
        steps.add(new ClearDepth());
    }

    public RenderPass beginPass() {
        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        RenderPass pass =
                RenderSystem.getDevice()
                        .createCommandEncoder()
                        .createRenderPass(
                                () -> "Space sky",
                                target.getColorTextureView(),
                                Optional.empty(),
                                target.getDepthTextureView(),
                                OptionalDouble.empty());
        RenderSystem.bindDefaultUniforms(pass);
        return pass;
    }

    public void flush() {
        if (steps.isEmpty()) return;
        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        flush(target.getColorTextureView(), target.getDepthTextureView());
    }

    public void flush(GpuTextureView color, GpuTextureView depth) {
        if (steps.isEmpty()) return;

        vertices.upload();
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        var textures = Minecraft.getInstance().getTextureManager();
        for (Step step : steps) {
            if (step instanceof ClearDepth) {
                drawRun(color, depth);
                encoder.clearDepthTexture(depth.texture(), 0.0D);
                continue;
            }

            Draw draw = (Draw) step;
            StagedVertexBuffer.ExecuteInfo info =
                    draw.mesh != null
                            ? execute(draw.mesh)
                            : vertices.getExecuteInfo(Objects.requireNonNull(draw.draw));
            if (info == null) continue;

            GpuBufferSlice transforms =
                    RenderSystem.getDynamicUniforms()
                            .writeTransform(
                                    draw.modelView, draw.color, NO_OFFSET, draw.uvTransform);
            GpuBufferSlice planet = null;
            if (draw.planet != null) {
                planet = planetInfo.writeData(draw.planet);
            }
            GpuBufferSlice time = null;
            if (draw.pipeline == SkyPipelines.SWARM
                    || draw.pipeline == SkyPipelines.BLACK_HOLE
                    || draw.pipeline == SkyPipelines.FLESH) {
                time = shaderTime.writeData(new ShaderTime(draw.time, info.baseVertex()));
            }
            run.add(
                    new Prepared(
                            draw.pipeline,
                            info,
                            transforms,
                            planet,
                            time,
                            draw.texture0 == null ? null : textures.getTexture(draw.texture0),
                            draw.texture1 == null ? null : textures.getTexture(draw.texture1),
                            draw.texture2 == null ? null : textures.getTexture(draw.texture2)));
        }
        drawRun(color, depth);

        vertices.endDraw();
        steps.clear();
    }

    private void drawRun(GpuTextureView color, GpuTextureView depth) {
        if (run.isEmpty()) return;

        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        boolean privateTarget =
                color != target.getColorTextureView() || depth != target.getDepthTextureView();
        boolean shared = !ShaderPacks.inUse();
        RenderPass pass = null;
        try {
            for (Prepared draw : run) {
                if (pass == null) {
                    pass =
                            RenderSystem.getDevice()
                                    .createCommandEncoder()
                                    .createRenderPass(
                                            () -> "Space sky",
                                            color,
                                            Optional.empty(),
                                            depth,
                                            OptionalDouble.empty());
                    RenderSystem.bindDefaultUniforms(pass);
                }
                if (privateTarget) pass.setPipeline(ShaderPacks.nativePipeline(draw.pipeline));
                else pass.setPipeline(RenderSystem.getCompiledPipeline(draw.pipeline));
                pass.setUniform("DynamicTransforms", draw.transforms);
                if (draw.planet != null) pass.setUniform("PlanetInfo", draw.planet);
                if (draw.time != null) pass.setUniform("ShaderTime", draw.time);
                if (draw.texture0 != null) bind(pass, "Sampler0", draw.texture0);
                if (draw.texture1 != null) bind(pass, "Sampler1", draw.texture1);
                if (draw.texture2 != null) bind(pass, "Sampler2", draw.texture2);
                pass.setVertexBuffer(0, draw.info.vertexBuffer().slice());
                pass.setIndexBuffer(draw.info.indexBuffer(), draw.info.indexType());
                pass.drawIndexed(
                        draw.info.indexCount(),
                        1,
                        draw.info.firstIndex(),
                        draw.info.baseVertex(),
                        0);
                if (!shared) {
                    pass.close();
                    pass = null;
                }
            }
        } finally {
            if (pass != null) pass.close();
        }
        run.clear();
    }

    private StagedVertexBuffer.ExecuteInfo execute(SkyMesh mesh) {
        Uploaded uploaded = meshes.computeIfAbsent(mesh, SkyDraws::upload);
        RenderSystem.AutoStorageIndexBuffer indices =
                RenderSystem.getSequentialBuffer(mesh.topology());
        return new StagedVertexBuffer.ExecuteInfo(
                uploaded.vertices,
                indices.getBuffer(uploaded.indexCount),
                indices.type(),
                0,
                0,
                uploaded.indexCount,
                mesh.topology());
    }

    private static Uploaded upload(SkyMesh mesh) {
        try (ByteBufferBuilder bytes = new ByteBufferBuilder(mesh.format().getVertexSize() * 4)) {
            BufferBuilder builder = new BufferBuilder(bytes, mesh.topology(), mesh.format());
            mesh.vertices().accept(builder);
            try (MeshData data = builder.buildOrThrow()) {
                return new Uploaded(
                        RenderSystem.getDevice()
                                .createBuffer(
                                        mesh::name, GpuBuffer.USAGE_VERTEX, data.vertexBuffer()),
                        data.drawState().indexCount());
            }
        }
    }

    public void endFrame() {
        vertices.endFrame();
        planetInfo.endFrame();
        shaderTime.endFrame();
    }

    private static void bind(RenderPass pass, String sampler, AbstractTexture texture) {
        pass.setUniform(
                sampler,
                texture.getTextureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
    }

    @Override
    public void close() {
        vertices.close();
        meshes.values().forEach(mesh -> mesh.vertices.close());
        meshes.clear();
        planetInfo.close();
        shaderTime.close();
    }
}
