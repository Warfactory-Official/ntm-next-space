// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.google.common.base.Suppliers;
import com.hbm.client.VanishedEntities;
import com.hbm.client.render.flywheel.EffectVisuals;
import com.hbm.client.render.flywheel.HbmDynamicEntityVisual;
import com.hbm.client.render.flywheel.PackedQuadMesh;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.entity.mob.EntityBFAngel;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import java.util.function.Supplier;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class BFAngelVisual extends HbmDynamicEntityVisual<EntityBFAngel> {

    private static final Supplier<Models> MODELS = Suppliers.memoize(Models::create);
    private static final float REACH = 16F;

    private final Models models = MODELS.get();
    private final TransformedInstance[] core;
    private final TransformedInstance[] wings;
    private final TransformedInstance[] rays = new TransformedInstance[RenderBFAngel.RAYS];
    private final Vector3f visualPosition = new Vector3f();
    private final Matrix4f root = new Matrix4f();
    private final Matrix4f body = new Matrix4f();
    private final Matrix4f turn = new Matrix4f();
    private final Matrix4f scratch = new Matrix4f();
    private boolean shown;

    public BFAngelVisual(VisualizationContext context, EntityBFAngel entity, float partialTick) {
        super(context, entity, partialTick);
        core = instances(models.core);
        wings = instances(models.wings);
        for (int i = 0; i < rays.length; i++) {
            rays[i] =
                    instancerProvider()
                            .instancer(InstanceTypes.TRANSFORMED, models.ray)
                            .createInstance();
            rays[i].setVisible(false);
        }
        writeFrame(partialTick);
    }

    private TransformedInstance[] instances(Model[] source) {
        TransformedInstance[] out = new TransformedInstance[source.length];
        for (int i = 0; i < out.length; i++) {
            out[i] =
                    instancerProvider()
                            .instancer(InstanceTypes.TRANSFORMED, source[i])
                            .createInstance();
            out[i].setVisible(false);
        }
        return out;
    }

    @Override
    public boolean isVisible(FrustumIntersection frustum) {
        return sphereVisible(frustum, 1F, REACH);
    }

    @Override
    protected void frame(Context context) {
        writeFrame(context.partialTick());
    }

    private void writeFrame(float partialTick) {
        boolean drawn = !VanishedEntities.isVanished(entity);
        if (drawn != shown) {
            for (var instance : core) instance.setVisible(drawn);
            for (var instance : wings) instance.setVisible(drawn);
            for (var instance : rays) instance.setVisible(drawn);
            shown = drawn;
        }
        if (!drawn) return;

        int light = computePackedLight(partialTick);
        getVisualPosition(partialTick, visualPosition);
        root.translation(visualPosition.x, visualPosition.y, visualPosition.z);
        RenderBFAngel.rootPose(root, entity, partialTick);

        float seed = RenderBFAngel.raySeed(entity, partialTick);
        turn.identity();
        for (int i = 0; i < rays.length; i++) {
            RenderBFAngel.turnRay(turn, i, seed);
            scratch.set(root)
                    .translate(1F, 2.2F, 1F)
                    .mul(turn)
                    .scale(
                            RenderBFAngel.RAY_WIDTH[i],
                            RenderBFAngel.RAY_LENGTH[i],
                            RenderBFAngel.RAY_WIDTH[i]);
            rays[i].setTransform(scratch).setChanged();
        }

        body.set(root);
        RenderBFAngel.bodyPose(body);
        for (var instance : core) instance.setTransform(body).light(light).setChanged();
        for (int i = 0; i < wings.length; i++) {
            scratch.set(body);
            RenderBFAngel.wingPose(scratch, i, entity, partialTick);
            wings[i].setTransform(scratch).light(light).setChanged();
        }
    }

    @Override
    protected void _delete() {
        for (var instance : core) instance.delete();
        for (var instance : wings) instance.delete();
        for (var instance : rays) instance.delete();
    }

    private record Models(Model[] core, Model[] wings, Model ray) {

        private static Models create() {
            HFRWavefrontObject mesh = SiegeModels.ANGEL.get();
            Material lit =
                    SimpleMaterial.builderOf(Materials.CUTOUT)
                            .texture(SiegeModels.ANGEL_TEX)
                            .mipmap(false)
                            .cutout(CutoutShaders.ONE_TENTH)
                            .cardinalLightingMode(CardinalLightingMode.ENTITY)
                            .backfaceCulling(false)
                            .useOverlay(false)
                            .build();
            Material unlit =
                    SimpleMaterial.builderOf(Materials.CUTOUT)
                            .texture(SiegeModels.ANGEL_TEX)
                            .mipmap(false)
                            .cutout(CutoutShaders.ONE_TENTH)
                            .cardinalLightingMode(CardinalLightingMode.OFF)
                            .backfaceCulling(false)
                            .useOverlay(false)
                            .build();
            Material flash =
                    SimpleMaterial.builder()
                            .texture(ResourceManager.white_tex)
                            .mipmap(false)
                            .cutout(CutoutShaders.OFF)
                            .transparency(Transparency.ORDER_INDEPENDENT_ADDITIVE)
                            .writeMask(WriteMask.COLOR)
                            .fog(EffectVisuals.FADE)
                            .useLight(false)
                            .cardinalLightingMode(CardinalLightingMode.OFF)
                            .ambientOcclusion(false)
                            .backfaceCulling(true)
                            .useOverlay(false)
                            .build();
            Model[] core = new Model[RenderBFAngel.CORE.length];
            for (int i = 0; i < core.length; i++) {
                core[i] =
                        new SingleMeshModel(
                                PackedQuadMesh.of(mesh, mesh.partId(RenderBFAngel.CORE[i])), lit);
            }
            Model[] wings = new Model[RenderBFAngel.WINGS.length];
            for (int i = 0; i < wings.length; i++) {
                wings[i] =
                        new SingleMeshModel(
                                PackedQuadMesh.of(mesh, mesh.partId(RenderBFAngel.WINGS[i])),
                                unlit);
            }
            return new Models(core, wings, new SingleMeshModel(spike(), flash));
        }

        private static PackedQuadMesh spike() {
            PackedQuadMesh.Builder mesh = PackedQuadMesh.builder(3);
            float[][] rim = {{-.866F, 1F, -.5F}, {.866F, 1F, -.5F}, {0F, 1F, 1F}};
            for (int i = 0; i < 3; i++) {
                float[] a = rim[i], b = rim[(i + 1) % 3];
                mesh.vertex(0, 0, 0, RenderBFAngel.RAY_TIP);
                mesh.vertex(a[0], a[1], a[2], RenderBFAngel.RAY_EDGE);
                mesh.vertex(b[0], b[1], b[2], RenderBFAngel.RAY_EDGE);
                mesh.vertex(b[0], b[1], b[2], RenderBFAngel.RAY_EDGE);
            }
            return mesh.build();
        }
    }
}
