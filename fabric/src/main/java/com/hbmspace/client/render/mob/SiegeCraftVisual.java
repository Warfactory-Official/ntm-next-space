// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.google.common.base.Suppliers;
import com.hbm.client.VanishedEntities;
import com.hbm.client.render.flywheel.BeamVisual;
import com.hbm.client.render.flywheel.HbmDynamicEntityVisual;
import com.hbm.client.render.flywheel.PackedQuadMesh;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.util.GameTime;
import com.hbmspace.entity.mob.siege.EntitySiegeCraft;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AffineUvTransformedInstance;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.material.StandardMaterialShaders;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class SiegeCraftVisual extends HbmDynamicEntityVisual<EntitySiegeCraft> {

    private static final Supplier<Models> MODELS = Suppliers.memoize(Models::create);
    private static final float REACH = 8F;

    private final Models models = MODELS.get();
    private final TransformedInstance[] hulls;
    private final TransformedInstance coils;
    private final AffineUvTransformedInstance[] glints =
            new AffineUvTransformedInstance[RenderSiegeCraft.GLINT_LAYERS];
    private final BeamVisual[] spokes = new BeamVisual[RenderSiegeCraft.SPOKES];
    private final BeamVisual[] lockon = new BeamVisual[3];
    private final BlockPos anchor;
    private final boolean[] spokeFlags = new boolean[RenderSiegeCraft.SPOKES];
    private final Vector3f visualPosition = new Vector3f();
    private final Matrix4f hull = new Matrix4f();
    private final Matrix4f beamBase = new Matrix4f();
    private final Matrix4f spoke = new Matrix4f();
    private final Matrix4f arm = new Matrix4f();
    private int shownTier = -1;
    private boolean glinting;

    public SiegeCraftVisual(
            VisualizationContext context, EntitySiegeCraft entity, float partialTick) {
        super(context, entity, partialTick);
        hulls = new TransformedInstance[models.hulls.size()];
        for (int i = 0; i < hulls.length; i++) {
            hulls[i] =
                    instancerProvider()
                            .instancer(InstanceTypes.TRANSFORMED, models.hulls.get(i))
                            .createInstance();
            hulls[i].setVisible(false);
        }
        coils =
                instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, models.coils)
                        .createInstance();
        for (int i = 0; i < glints.length; i++) {
            glints[i] =
                    instancerProvider()
                            .instancer(AffineUvTransformedInstance.TYPE, models.glint)
                            .createInstance();
            glints[i].setVisible(false);
        }
        anchor = entity.blockPosition();
        for (int i = 0; i < spokes.length; i++)
            spokes[i] = new BeamVisual(context, level, anchor, true, 0, 1F);
        lockon[0] = new BeamVisual(context, level, anchor, false, 4, 1F);
        lockon[1] = new BeamVisual(context, level, anchor, false, 2, 1F);
        lockon[2] = new BeamVisual(context, level, anchor, false, 2, 1F);
        writeFrame(partialTick);
    }

    @Override
    public boolean isVisible(FrustumIntersection frustum) {
        float reach = REACH;
        if (entity.getBeam())
            reach = Math.max(reach, (float) entity.getLockon().distanceTo(entity.position()) + 1F);
        return sphereVisible(frustum, 0.5F, reach);
    }

    @Override
    protected void frame(Context context) {
        writeFrame(context.partialTick());
    }

    private void writeFrame(float partialTick) {
        if (VanishedEntities.isVanished(entity)) {
            hide();
            return;
        }
        int light = computePackedLight(partialTick);
        getVisualPosition(partialTick, visualPosition);
        hull.translation(visualPosition.x, visualPosition.y + 0.5F, visualPosition.z);
        RenderSiegeCraft.hullPose(hull, entity, partialTick);

        int tier = entity.getTier().id;
        if (tier != shownTier) {
            if (shownTier >= 0) hulls[shownTier].setVisible(false);
            hulls[tier].setVisible(true);
            shownTier = tier;
        }
        hulls[tier].setTransform(hull).light(light).setChanged();
        coils.setVisible(true);
        coils.setTransform(hull)
                .colorArgb(RenderSiegeCraft.coilColor(entity))
                .light(LightCoordsUtil.FULL_BRIGHT)
                .setChanged();

        boolean beam = entity.getBeam();
        if (beam) {
            float offset = (float) (GameTime.now() / 50D);
            for (int k = 0; k < glints.length; k++) {
                float movement = offset * (.001F + k * .003F) * RenderSiegeCraft.GLINT_SPEED;
                float angle = (float) Math.toRadians(30F - k * 60F);
                float cos = Mth.cos(angle), sin = Mth.sin(angle);
                if (!glinting) glints[k].setVisible(true);
                glints[k].setTransform(hull).colorArgb(RenderSiegeCraft.GLINT_TINT).light(light);
                glints[k].uv(cos, -sin, sin, cos, -sin * movement, cos * movement);
                glints[k].setChanged();
            }
        } else if (glinting) {
            for (var glint : glints) glint.setVisible(false);
        }
        glinting = beam;

        double x = Mth.lerp(partialTick, entity.xo, entity.getX()) - anchor.getX();
        double y = Mth.lerp(partialTick, entity.yo, entity.getY()) - anchor.getY() + 0.5D;
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ()) - anchor.getZ();
        beamBase.translation((float) x, (float) y, (float) z);
        RenderSiegeCraft.spokes(entity, spokeFlags);
        int arcStart = (int) (GameTime.now() % 1000L) / 50;
        spoke.set(beamBase);
        RenderSiegeCraft.hullPose(spoke, entity, partialTick);
        for (int i = 0; i < spokes.length; i++) {
            spoke.rotateY(45F * Mth.DEG_TO_RAD);
            if (!spokeFlags[i]) {
                spokes[i].hide();
                continue;
            }
            arm.set(spoke).translate(4F, 0F, 0F);
            spokes[i].update(
                    arm,
                    RenderSiegeCraft.SPOKE,
                    EnumWaveType.RANDOM,
                    arcStart,
                    15,
                    .125F,
                    0F,
                    RenderSiegeCraft.SPOKE_OUTER,
                    RenderSiegeCraft.SPOKE_INNER);
        }

        if (beam) {
            Vec3 delta = entity.getLockon().subtract(entity.getX(), entity.getY(), entity.getZ());
            int segments = (int) (delta.length() / 2 + 1);
            float scale = (float) RenderSiegeCraft.LOCKON_SCALE;
            int age = entity.tickCount;
            lockon[0].update(
                    beamBase,
                    delta,
                    EnumWaveType.RANDOM,
                    age / 6,
                    segments,
                    scale,
                    .25F,
                    0x101020,
                    0x101020);
            lockon[1].update(
                    beamBase,
                    delta,
                    EnumWaveType.RANDOM,
                    age / 2,
                    segments,
                    scale * 7F,
                    .0625F,
                    0x202060,
                    0x202060);
            lockon[2].update(
                    beamBase,
                    delta,
                    EnumWaveType.RANDOM,
                    age / 4,
                    segments,
                    scale * 7F,
                    .0625F,
                    0x202060,
                    0x202060);
        } else {
            for (var beamVisual : lockon) beamVisual.hide();
        }
    }

    private void hide() {
        if (shownTier >= 0) hulls[shownTier].setVisible(false);
        shownTier = -1;
        coils.setVisible(false);
        for (var glint : glints) glint.setVisible(false);
        glinting = false;
        for (var beamVisual : spokes) beamVisual.hide();
        for (var beamVisual : lockon) beamVisual.hide();
    }

    @Override
    protected void _delete() {
        for (var instance : hulls) instance.delete();
        coils.delete();
        for (var glint : glints) glint.delete();
        for (var beamVisual : spokes) beamVisual.delete();
        for (var beamVisual : lockon) beamVisual.delete();
    }

    private record Models(List<Model> hulls, Model coils, Model glint) {

        private static Models create() {
            HFRWavefrontObject mesh = SiegeModels.SIEGE_UFO.get();
            PackedQuadMesh hullMesh = PackedQuadMesh.of(mesh, mesh.partId("UFO"));
            List<Model> hulls =
                    SiegeModels.CRAFT_SKINS.stream()
                            .<Model>map(skin -> new SingleMeshModel(hullMesh, hull(skin)))
                            .toList();
            Material coil =
                    SimpleMaterial.builder()
                            .texture(ResourceManager.white_tex)
                            .mipmap(false)
                            .cutout(CutoutShaders.OFF)
                            .useLight(false)
                            .cardinalLightingMode(CardinalLightingMode.OFF)
                            .ambientOcclusion(false)
                            .backfaceCulling(true)
                            .useOverlay(false)
                            .build();
            Material glint =
                    SimpleMaterial.builderOf(Materials.GLINT)
                            .shaders(StandardMaterialShaders.DEFAULT)
                            .texture(ResourceManager.glint_bf_tex)
                            .blur(false)
                            .mipmap(false)
                            .cutout(CutoutShaders.ONE_TENTH)
                            .cardinalLightingMode(CardinalLightingMode.OFF)
                            .depthTest(DepthTest.EQUAL)
                            .writeMask(WriteMask.COLOR)
                            .build();
            return new Models(
                    hulls,
                    new SingleMeshModel(PackedQuadMesh.of(mesh, mesh.partId("Coils")), coil),
                    new SingleMeshModel(hullMesh, glint));
        }

        private static Material hull(Identifier skin) {
            return SimpleMaterial.builderOf(Materials.CUTOUT)
                    .texture(skin)
                    .mipmap(false)
                    .cutout(CutoutShaders.ONE_TENTH)
                    .cardinalLightingMode(CardinalLightingMode.ENTITY)
                    .backfaceCulling(false)
                    .useOverlay(false)
                    .build();
        }
    }
}
