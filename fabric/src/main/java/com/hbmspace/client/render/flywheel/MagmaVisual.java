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

package com.hbmspace.client.render.flywheel;

import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.client.render.flywheel.PourVisual;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.blockentity.machine.MagmaBlockEntity.PourStream;
import com.hbmspace.blockentity.machine.MagmaBlockEntity;
import com.hbmspace.client.render.MagmaRenderer;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AffineUvTransformedInstance;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.visual.AbstractBoundedBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.CulledDynamicVisual;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class MagmaVisual extends AbstractBoundedBlockEntityVisual<MagmaBlockEntity>
        implements ShaderLightVisual, CulledDynamicVisual {

    private static final MeshPart LAVA =
            MeshPart.face(Direction.UP, MeshPart.litCutout(TextureAtlas.LOCATION_BLOCKS));

    private final TransformedInstance drillHead;
    private final TransformedInstance blades;
    private final AffineUvTransformedInstance[] lava = new AffineUvTransformedInstance[9];
    private final ArrayList<PourVisual> pours = new ArrayList<>();
    private final Direction facing;
    private final Matrix4f base = new Matrix4f();
    private final Matrix4f local = new Matrix4f();
    private final Matrix4f pose = new Matrix4f();
    private final AABB extent;
    private float lastRotation = Float.NaN;
    private float lastLavaHeight = Float.NaN;
    private boolean initialized;

    public MagmaVisual(
            VisualizationContext context,
            MagmaBlockEntity blockEntity,
            float partialTick,
            MeshPart drillHeadMesh,
            MeshPart bladesMesh) {
        super(context, blockEntity, partialTick);
        facing = MultiblockCoreBlock.coreFacing(blockState);
        base.translation(0.5F, 0F, 0.5F)
                .rotateY(MagmaRenderer.yaw(facing) * Mth.DEG_TO_RAD)
                .translate(0F, -3F, 0F);
        extent = MagmaRenderer.extent(pos);
        drillHead =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, drillHeadMesh.model())
                        .createInstance();
        blades =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, bladesMesh.model())
                        .createInstance();
        drillHead.setVisible(false);
        blades.setVisible(false);
        for (int i = 0; i < lava.length; i++) {
            lava[i] =
                    instancerProvider()
                            .instancer(AffineUvTransformedInstance.TYPE, LAVA.model())
                            .createInstance();
            lava[i].setVisible(false);
        }
    }

    public static MeshPart meshOf(HFRWavefrontObject mesh, String group, Identifier texture) {
        Material material =
                SimpleMaterial.builderOf(Materials.CUTOUT)
                        .texture(texture)
                        .mipmap(false)
                        .cutout(CutoutShaders.ONE_TENTH)
                        .light(LightShaders.SMOOTH)
                        .ambientOcclusion(false)
                        .cardinalLightingMode(CardinalLightingMode.CHUNK)
                        .build();
        return MeshPart.obj(mesh.groups[mesh.partId(group)], mesh.smoothing(), material);
    }

    @Override
    public boolean doDistanceLimitThisFrame(Context context) {
        return initialized && super.doDistanceLimitThisFrame(context);
    }

    @Override
    public void frame(Context context) {
        animate(context.partialTick());
        initialized = true;
    }

    private void animate(float partialTick) {
        float rotation =
                blockEntity.prevDrillRotation
                        + (blockEntity.drillRotation - blockEntity.prevDrillRotation) * partialTick;
        if (rotation != lastRotation) {
            lastRotation = rotation;
            place(drillHead, local.set(base).rotateY(rotation * Mth.DEG_TO_RAD));
            place(blades, local.set(base).rotateY(-rotation * Mth.DEG_TO_RAD));
        }

        float height =
                blockEntity.prevLavaHeight
                        + (blockEntity.lavaHeight - blockEntity.prevLavaHeight) * partialTick;
        boolean shown = height > 0.01F;
        if (height != lastLavaHeight) {
            lastLavaHeight = height;
            TextureAtlasSprite sprite =
                    Minecraft.getInstance()
                            .getModelManager()
                            .getBlockStateModelSet()
                            .getParticleMaterial(Blocks.LAVA.defaultBlockState())
                            .sprite();
            int i = 0;
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    AffineUvTransformedInstance face = lava[i++];
                    face.setVisible(shown);
                    if (!shown) continue;
                    local.set(base).translate(ox - 0.5F, height - 1F, oz - 0.5F);
                    pose.translation(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                            .mul(local);
                    face.setTransform(pose).light(0);
                    face.uvRegion(
                                    sprite.getU0(),
                                    sprite.getV0(),
                                    sprite.getU1() - sprite.getU0(),
                                    sprite.getV1() - sprite.getV0())
                            .setChanged();
                }
            }
        }

        long now = level.getGameTime();
        int used = 0;
        for (PourStream stream : blockEntity.streams) {
            float age = now - stream.birth() + partialTick;
            if (age >= 20F) continue;
            if (used == pours.size()) pours.add(new PourVisual(visualizationContext, level, pos));
            pours.get(used++)
                    .update(
                            stream.color(),
                            facing,
                            stream.len(),
                            age,
                            0.625F,
                            0.625F,
                            (float) (0.5D + facing.getStepX() * MagmaBlockEntity.SPOUT),
                            1F,
                            (float) (0.5D + facing.getStepZ() * MagmaBlockEntity.SPOUT));
        }
        while (pours.size() > used) pours.removeLast().delete();
    }

    private void place(TransformedInstance instance, Matrix4f partPose) {
        pose.translation(visualPos.getX(), visualPos.getY(), visualPos.getZ()).mul(partPose);
        instance.setTransform(pose).light(0).setChanged();
        instance.setVisible(true);
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return extent;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(drillHead);
        consumer.accept(blades);
    }

    @Override
    protected void _delete() {
        drillHead.delete();
        blades.delete();
        for (AffineUvTransformedInstance face : lava) face.delete();
        for (PourVisual pour : pours) pour.delete();
        pours.clear();
    }
}
