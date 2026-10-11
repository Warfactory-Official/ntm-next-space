// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import com.hbm.client.render.flywheel.HbmDynamicBlockEntityVisual;
import com.hbm.client.render.flywheel.LightBounds;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbmspace.client.render.PosedParts;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class PosedPartsVisual<T extends BlockEntity> extends HbmDynamicBlockEntityVisual<T>
        implements ShaderLightVisual {

    private final PosedParts<T> parts;
    private final MeshPart[] meshes;
    private final TransformedInstance[] instances;
    private final Matrix4f[] poses;
    private final Matrix4f[] written;
    private final boolean[] drawn;
    private final Matrix4f instancePose = new Matrix4f();
    private final AABB lit;
    private final AABB visible;
    private final double[] lightBoundsAccumulator = new double[6];
    private @Nullable AABB lastLightBounds;
    private boolean initialized;
    private List<Extra> extras = List.of();

    protected record Extra(TransformedInstance instance, MeshPart part, Matrix4f local) {}

    public PosedPartsVisual(
            VisualizationContext context,
            T blockEntity,
            float partialTick,
            PosedParts<T> parts,
            MeshPart[] meshes) {
        super(context, blockEntity, partialTick);
        this.parts = parts;
        this.meshes = meshes;
        this.lit = parts.lit().at(pos);
        this.visible = parts.drawn().at(pos);
        int count = meshes.length;
        instances = new TransformedInstance[count];
        poses = new Matrix4f[count];
        written = new Matrix4f[count];
        drawn = new boolean[count];
        for (int i = 0; i < count; i++) {
            instances[i] =
                    instancerProvider()
                            .instancer(InstanceTypes.TRANSFORMED, meshes[i].model())
                            .createInstance();
            poses[i] = new Matrix4f();
            written[i] = new Matrix4f();
        }
        writeFrame(partialTick);
    }

    public static <T extends BlockEntity> MeshPart[] meshesOf(PosedParts<T> parts) {
        List<PosedParts.Part<T>> list = parts.parts();
        MeshPart[] meshes = new MeshPart[list.size()];
        for (int i = 0; i < meshes.length; i++) {
            PosedParts.Part<T> part = list.get(i);
            Material material =
                    SimpleMaterial.builderOf(
                                    part.cull() ? Materials.CUTOUT : Materials.CUTOUT_NO_CULL)
                            .texture(part.texture())
                            .mipmap(false)
                            .cutout(CutoutShaders.ONE_TENTH)
                            .light(LightShaders.SMOOTH)
                            .ambientOcclusion(false)
                            .cardinalLightingMode(CardinalLightingMode.CHUNK)
                            .build();
            meshes[i] =
                    MeshPart.obj(
                            part.mesh().groups[part.groupId()], part.mesh().smoothing(), material);
        }
        return meshes;
    }

    @Override
    protected void frame(Context context) {
        writeFrame(context.partialTick());
    }

    private void writeFrame(float partialTick) {
        List<PosedParts.Part<T>> list = parts.parts();
        boolean moved = false;
        for (int i = 0; i < instances.length; i++) {
            boolean draws = list.get(i).pose().at(blockEntity, partialTick, poses[i].identity());
            boolean revealed = draws && (!initialized || !drawn[i]);
            if (draws != drawn[i] || !initialized) instances[i].setVisible(draws);
            drawn[i] = draws;
            if (!draws) continue;
            if (!revealed && poses[i].equals(written[i])) continue;
            written[i].set(poses[i]);
            instancePose
                    .translation(visualPos.getX(), visualPos.getY(), visualPos.getZ())
                    .mul(poses[i]);
            instances[i].setTransform(instancePose).light(0).setChanged();
            moved = true;
        }
        initialized = true;
        if (moved) relight();
    }

    protected final void replaceExtras(List<Extra> next) {
        for (Extra extra : extras) extra.instance().delete();
        extras = next;
        relight();
    }

    private void relight() {
        LightBounds.resetBounds(lightBoundsAccumulator, lit);
        for (int i = 0; i < instances.length; i++) {
            if (drawn[i])
                LightBounds.includeLightBounds(
                        lightBoundsAccumulator, meshes[i].model(), poses[i], pos);
        }
        for (Extra extra : extras) {
            LightBounds.includeLightBounds(
                    lightBoundsAccumulator, extra.part().model(), extra.local(), pos);
        }
        lastLightBounds =
                LightBounds.sections(lightSections, lightBoundsAccumulator, lastLightBounds);
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return lit;
    }

    @Override
    protected AABB visibleBounds() {
        return visible;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        for (int i = 0; i < instances.length; i++) {
            if (drawn[i]) consumer.accept(instances[i]);
        }
        for (Extra extra : extras) consumer.accept(extra.instance());
    }

    @Override
    protected void _delete() {
        for (TransformedInstance instance : instances) instance.delete();
        for (Extra extra : extras) extra.instance().delete();
    }
}
