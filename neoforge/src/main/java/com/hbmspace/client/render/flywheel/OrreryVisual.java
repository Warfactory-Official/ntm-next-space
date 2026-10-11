// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import com.hbm.client.render.flywheel.EffectVisuals;
import com.hbm.client.render.flywheel.HbmDynamicBlockEntityVisual;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.client.render.flywheel.PackedQuadMesh;
import com.hbmspace.blockentity.deco.BlockEntityOrrery;
import com.hbmspace.client.render.OrreryPronter;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem.OrreryMetric;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.material.StandardMaterialShaders;
import dev.engine_room.flywheel.lib.model.LineModelBuilder;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class OrreryVisual extends HbmDynamicBlockEntityVisual<BlockEntityOrrery> {

    private static final Identifier WHITE =
            Identifier.fromNamespaceAndPath("flywheel", "textures/flywheel/white.png");
    private static final int LIT =
            ARGB.colorFromFloat(1.0F, OrreryPronter.LIT, OrreryPronter.LIT, OrreryPronter.LIT);
    private static final Material ORBIT_MATERIAL =
            SimpleMaterial.builder()
                    .texture(WHITE)
                    .mipmap(false)
                    .shaders(StandardMaterialShaders.LINE)
                    .useLight(false)
                    .useOverlay(false)
                    .ambientOcclusion(false)
                    .cardinalLightingMode(CardinalLightingMode.OFF)
                    .backfaceCulling(false)
                    .transparency(Transparency.ORDER_INDEPENDENT_ADDITIVE)
                    .writeMask(WriteMask.COLOR)
                    .fog(EffectVisuals.FADE)
                    .build();
    private static final Map<String, Model> CUBES = new ConcurrentHashMap<>();
    private static final Map<String, Model> ORBITS = new ConcurrentHashMap<>();

    private final TransformedInstance sun;
    private final List<TransformedInstance> bodies = new ArrayList<>();
    private final List<TransformedInstance> orbits = new ArrayList<>();
    private final Matrix4f center = new Matrix4f();
    private final Matrix4f pose = new Matrix4f();
    private final AABB extent;

    public OrreryVisual(
            VisualizationContext context, BlockEntityOrrery blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        extent =
                new AABB(
                        pos.getX() - 49,
                        pos.getY() - 19,
                        pos.getZ() - 49,
                        pos.getX() + 50,
                        pos.getY() + 20,
                        pos.getZ() + 50);
        OrreryPronter.Scene scene = OrreryPronter.scene(level, partialTick);
        sun =
                instancerProvider()
                        .instancer(
                                InstanceTypes.TRANSFORMED,
                                cube(
                                        scene.sun().texture,
                                        OrreryPronter.SUN_UV_MIN,
                                        OrreryPronter.SUN_UV_MAX))
                        .createInstance();
        for (OrreryMetric metric : scene.metrics()) {
            bodies.add(
                    instancerProvider()
                            .instancer(
                                    InstanceTypes.TRANSFORMED,
                                    cube(metric.body.texture, 0.0F, 1.0F))
                            .createInstance());
            orbits.add(
                    instancerProvider()
                            .instancer(InstanceTypes.TRANSFORMED, orbit(metric))
                            .createInstance());
        }
        writeFrame(scene);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {}

    private static Model cube(Identifier texture, float uvMin, float uvMax) {
        return CUBES.computeIfAbsent(
                texture + "@" + uvMin,
                key -> {
                    float[] vertex = new float[5];
                    float[] data = new float[OrreryPronter.CUBE_VERTICES * 8];
                    for (int i = 0; i < OrreryPronter.CUBE_VERTICES; i++) {
                        OrreryPronter.cubeVertex(i, uvMin, uvMax, vertex);
                        System.arraycopy(vertex, 0, data, i * 8, 5);
                        data[i * 8 + 7] = 1.0F;
                    }
                    Material material =
                            SimpleMaterial.builder()
                                    .texture(texture)
                                    .mipmap(false)
                                    .cutout(CutoutShaders.ONE_TENTH)
                                    .useLight(false)
                                    .useOverlay(false)
                                    .ambientOcclusion(false)
                                    .cardinalLightingMode(CardinalLightingMode.OFF)
                                    .transparency(Transparency.ORDER_INDEPENDENT_ADDITIVE)
                                    .writeMask(WriteMask.COLOR)
                                    .fog(EffectVisuals.FADE)
                                    .build();
                    return new SingleMeshModel(PackedQuadMesh.of(data, null, null), material);
                });
    }

    private static Model orbit(OrreryMetric metric) {
        return ORBITS.computeIfAbsent(
                metric.body.name,
                key -> {
                    Vec3[] path = metric.orbitalPath;
                    Vec3 origin = path[0];
                    LineModelBuilder lines = new LineModelBuilder(path.length);
                    for (int i = 0; i < path.length; i++) {
                        Vec3 from = path[i].subtract(origin);
                        Vec3 to = path[(i + 1) % path.length].subtract(origin);
                        lines.line(
                                (float) from.x,
                                (float) from.z,
                                (float) from.y,
                                (float) to.x,
                                (float) to.z,
                                (float) to.y);
                    }
                    return new SingleMeshModel(MeshPart.lines(lines, 1.0F), ORBIT_MATERIAL);
                });
    }

    @Override
    protected void frame(Context context) {
        writeFrame(OrreryPronter.scene(level, context.partialTick()));
    }

    private void writeFrame(OrreryPronter.Scene scene) {
        center.translation(
                visualPos.getX() + 0.5F, visualPos.getY() + 0.5F, visualPos.getZ() + 0.5F);
        sun.setTransform(center).colorArgb(LIT).setChanged();

        float scale = (float) OrreryPronter.scale(scene.sun());
        List<OrreryMetric> metrics = scene.metrics();
        for (int i = 0; i < metrics.size(); i++) {
            OrreryMetric metric = metrics.get(i);
            CelestialBody body = metric.body;
            float size = (float) OrreryPronter.bodyScale(body);
            pose.set(center)
                    .scale(scale)
                    .translate(
                            (float) metric.position.x,
                            (float) metric.position.z,
                            (float) metric.position.y)
                    .scale(size);
            bodies.get(i).setTransform(pose).colorArgb(LIT).setChanged();

            Vec3 origin = metric.orbitalPath[0];
            pose.set(center)
                    .scale(scale)
                    .translate((float) origin.x, (float) origin.z, (float) origin.y);
            orbits.get(i).setTransform(pose).colorArgb(OrreryPronter.orbitColor(body)).setChanged();
        }
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return extent;
    }

    @Override
    protected void _delete() {
        sun.delete();
        bodies.forEach(TransformedInstance::delete);
        orbits.forEach(TransformedInstance::delete);
    }
}
