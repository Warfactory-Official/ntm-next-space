// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.flywheel;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.render.flywheel.EffectVisuals;
import com.hbm.client.render.flywheel.HbmDynamicBlockEntityVisual;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.blockentity.machine.BlockEntityHydroponic;
import com.hbmspace.client.render.RenderHydroponic;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class HydroponicVisual extends HbmDynamicBlockEntityVisual<BlockEntityHydroponic> {

    private final TransformedInstance lights;
    private final AABB extent;
    private boolean lit;

    public HydroponicVisual(
            VisualizationContext context,
            BlockEntityHydroponic blockEntity,
            float partialTick,
            MeshPart lamps) {
        super(context, blockEntity, partialTick);
        extent = RenderHydroponic.extent(pos);
        Matrix4f pose =
                new Matrix4f()
                        .translation(
                                visualPos.getX() + 0.5F, visualPos.getY(), visualPos.getZ() + 0.5F)
                        .rotateY(
                                RenderHydroponic.yaw(BlockMultiblockCore.coreFacing(blockState))
                                        * Mth.DEG_TO_RAD);
        lights =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, lamps.model())
                        .createInstance();
        lights.setTransform(pose).light(LightCoordsUtil.FULL_BRIGHT).setChanged();
        lit = RenderHydroponic.lit(blockEntity);
        lights.setVisible(lit);
    }

    public static MeshPart meshOf(HFRWavefrontObject mesh, Identifier texture) {
        Material material =
                SimpleMaterial.builder()
                        .texture(texture)
                        .mipmap(false)
                        .cutout(CutoutShaders.ONE_TENTH)
                        .useLight(false)
                        .useOverlay(false)
                        .ambientOcclusion(false)
                        .cardinalLightingMode(CardinalLightingMode.CHUNK)
                        .transparency(Transparency.ORDER_INDEPENDENT_ADDITIVE)
                        .writeMask(WriteMask.COLOR)
                        .backfaceCulling(false)
                        .fog(EffectVisuals.FADE)
                        .build();
        return MeshPart.obj(mesh.groups[mesh.partId("Lights")], mesh.smoothing(), material);
    }

    @Override
    protected void frame(Context context) {
        boolean now = RenderHydroponic.lit(blockEntity);
        if (now == lit) return;
        lit = now;
        lights.setVisible(now);
    }

    @Override
    protected AABB getRenderBoundingBox() {
        return extent;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {}

    @Override
    protected void _delete() {
        lights.delete();
    }
}
