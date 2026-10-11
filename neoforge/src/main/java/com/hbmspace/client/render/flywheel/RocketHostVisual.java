// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import com.hbm.client.render.flywheel.HbmDynamicBlockEntityVisual;
import com.hbm.client.render.flywheel.LightBounds;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.render.loader.GroupObject;
import com.hbmspace.client.render.RocketHost;
import com.hbmspace.client.render.RocketPronter;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visual.SectionTrackedVisual;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class RocketHostVisual<T extends BlockEntity, S> extends HbmDynamicBlockEntityVisual<T>
        implements ShaderLightVisual {
    private static final Map<PieceKey, MeshPart[]> PARTS = new ConcurrentHashMap<>();

    private final RocketHost<T, S> host;
    private final RocketPronter pronter;
    private final List<TransformedInstance> instances = new ArrayList<>();
    private final Matrix4f instancePose = new Matrix4f();
    private final double[] lightBoundsAccumulator = new double[6];
    private final AABB anchor;
    private S drawn;
    private AABB bounds;
    private boolean stale = true;
    private @Nullable AABB lastLightBounds;

    public RocketHostVisual(
            VisualizationContext context,
            T blockEntity,
            float partialTick,
            RocketHost<T, S> host,
            RocketPronter pronter) {
        super(context, blockEntity, partialTick);
        this.host = host;
        this.pronter = pronter;
        this.anchor = new AABB(pos);
        this.drawn = host.snapshot(blockEntity);
        this.bounds = host.bounds(pos, drawn);
        rebuild();
    }

    @Override
    protected void trackExtent() {
        S snapshot = host.snapshot(blockEntity);
        if (snapshot.equals(drawn)) return;
        drawn = snapshot;
        bounds = host.bounds(pos, snapshot);
        stale = true;
        refreshVisibleBounds();
    }

    @Override
    protected void frame(Context context) {
        if (stale) rebuild();
    }

    private void rebuild() {
        stale = false;
        for (TransformedInstance instance : instances) instance.delete();
        instances.clear();

        LightBounds.resetBounds(lightBoundsAccumulator, anchor);
        PoseStack pose = new PoseStack();
        host.draw(
                drawn,
                pronter,
                pose,
                (piece, cull) -> {
                    Matrix4f local = pose.last().pose();
                    for (MeshPart part : parts(piece, cull)) {
                        TransformedInstance instance =
                                instancerProvider()
                                        .instancer(InstanceTypes.TRANSFORMED, part.model())
                                        .createInstance();
                        instancePose
                                .translation(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                                .mul(local);
                        instance.setTransform(instancePose).light(0).setChanged();
                        instances.add(instance);
                        LightBounds.includeLightBounds(
                                lightBoundsAccumulator, part.model(), local, pos);
                    }
                });
        lastLightBounds =
                LightBounds.sections(lightSections, lightBoundsAccumulator, lastLightBounds);
    }

    public static MeshPart[] parts(RocketPronter.Piece piece, boolean cull) {
        return PARTS.computeIfAbsent(
                new PieceKey(piece.id(), piece.group(), piece.texture(), piece.clipTop(), cull),
                key -> {
                    Material material =
                            SimpleMaterial.builderOf(
                                            cull ? Materials.CUTOUT : Materials.CUTOUT_NO_CULL)
                                    .texture(piece.texture())
                                    .mipmap(false)
                                    .cutout(CutoutShaders.ONE_TENTH)
                                    .light(LightShaders.SMOOTH)
                                    .ambientOcclusion(false)
                                    .cardinalLightingMode(CardinalLightingMode.CHUNK)
                                    .build();
                    GroupObject[] groups = piece.mesh().groups;
                    int first = piece.group() < 0 ? 0 : piece.group();
                    int last = piece.group() < 0 ? groups.length - 1 : piece.group();
                    List<MeshPart> parts = new ArrayList<>();
                    for (int g = first; g <= last; g++) {
                        GroupObject group =
                                Float.isNaN(piece.clipTop())
                                        ? groups[g]
                                        : ClippedGroup.below(
                                                groups[g],
                                                piece.mesh().smoothing(),
                                                piece.clipTop());
                        if (group == null) continue;
                        parts.add(
                                MeshPart.obj(
                                        group,
                                        Float.isNaN(piece.clipTop())
                                                ? piece.mesh().smoothing()
                                                : true,
                                        material));
                    }
                    return parts.toArray(MeshPart[]::new);
                });
    }

    @Override
    public void setSectionCollector(SectionTrackedVisual.SectionCollector collector) {
        super.setSectionCollector(collector);
        lastLightBounds =
                LightBounds.sections(lightSections, lightBoundsAccumulator, lastLightBounds);
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return bounds;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        instances.forEach(consumer);
    }

    @Override
    protected void _delete() {
        for (TransformedInstance instance : instances) instance.delete();
    }

    private record PieceKey(
            Identifier mesh, int group, Identifier texture, float clipTop, boolean cull) {}
}
