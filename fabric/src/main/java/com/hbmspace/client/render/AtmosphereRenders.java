// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.google.common.base.Suppliers;
import com.hbm.client.ClientRegistry;
import com.hbm.client.model.Meshes;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.util.Facing;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityAirScrubber;
import com.hbmspace.blockentity.machine.BlockEntityAtmosphericCompressor;
import com.hbmspace.blockentity.machine.BlockEntityAtmosphericEmitter;
import com.hbmspace.blocks.AtmosphereBlockEntitiesSpace;
import com.hbmspace.client.render.flywheel.SpinningPartVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;

public final class AtmosphereRenders {
    private static final class Parts {
        private static final SpinningPart<BlockEntityAirScrubber> SCRUBBER =
                new SpinningPart<>(
                        Meshes.load(id("models/machines/air_scrubber.obj")),
                        id("textures/block/models/machines/air_scrubber.png"),
                        "Fan",
                        true,
                        (facing, pose) ->
                                pose.translate(0.5F, 0F, 0.5F)
                                        .rotateY(Facing.yaw(facing, 180) * Mth.DEG_TO_RAD),
                        (pose, degrees) -> pose.rotateY(degrees * Mth.DEG_TO_RAD),
                        (be, pt) -> Mth.lerp(pt, be.prevRot, be.rot),
                        pos ->
                                new AABB(
                                        pos.getX(),
                                        pos.getY(),
                                        pos.getZ(),
                                        pos.getX() + 1,
                                        pos.getY() + 2,
                                        pos.getZ() + 1));

        private static final SpinningPart<BlockEntityAtmosphericCompressor> COMPRESSOR =
                new SpinningPart<>(
                        Meshes.load(id("models/machines/atmo_vent.obj")),
                        id("textures/block/models/machines/atmo_vent.png"),
                        "Fan_Cylinder.001",
                        false,
                        (facing, pose) ->
                                pose.translate(0.5F, 0F, 0.5F)
                                        .rotateY(Facing.yaw(facing, 180) * Mth.DEG_TO_RAD)
                                        .translate(0.5F, 0F, -0.5F),
                        (pose, degrees) ->
                                pose.translate(-0.19F, 0F, 0.19F)
                                        .rotateY(-degrees * Mth.DEG_TO_RAD)
                                        .translate(0.19F, 0F, -0.19F),
                        (be, pt) -> Mth.lerp(pt, be.prevRot, be.rot),
                        pos ->
                                new AABB(
                                        pos.getX() - 1,
                                        pos.getY(),
                                        pos.getZ() - 1,
                                        pos.getX() + 2,
                                        pos.getY() + 10,
                                        pos.getZ() + 2));

        private static final SpinningPart<BlockEntityAtmosphericEmitter> EMITTER =
                new SpinningPart<>(
                        Meshes.load(id("models/machines/outtake.obj")),
                        id("textures/block/models/machines/outtake.png"),
                        "Fan",
                        false,
                        (facing, pose) ->
                                pose.translate(0.5F, 0F, 0.5F)
                                        .rotateY(Facing.yaw(facing, 90) * Mth.DEG_TO_RAD)
                                        .translate(-0.5F, 0F, 0.5F),
                        (pose, degrees) -> pose.rotateY(-degrees * Mth.DEG_TO_RAD),
                        (be, pt) -> Mth.lerp(pt, be.prevFan, be.fan),
                        pos ->
                                new AABB(
                                        pos.getX() - 1,
                                        pos.getY(),
                                        pos.getZ() - 1,
                                        pos.getX() + 2,
                                        pos.getY() + 3,
                                        pos.getZ() + 2));
    }

    private AtmosphereRenders() {}

    public static void registerBlockEntityRenderers(
            ClientRegistry.BlockEntityRendererRegistrar reg) {
        reg.accept(
                AtmosphereBlockEntitiesSpace.AIR_SCRUBBER.get(),
                context -> new RenderSpinningPart<>(Parts.SCRUBBER));
        reg.accept(
                AtmosphereBlockEntitiesSpace.ATMOSPHERIC_COMPRESSOR.get(),
                context -> new RenderSpinningPart<>(Parts.COMPRESSOR));
        reg.accept(
                AtmosphereBlockEntitiesSpace.ATMOSPHERIC_EMITTER.get(),
                context -> new RenderSpinningPart<>(Parts.EMITTER));
    }

    public static void registerVisualizers() {
        visualize(AtmosphereBlockEntitiesSpace.AIR_SCRUBBER.get(), () -> Parts.SCRUBBER);
        visualize(
                AtmosphereBlockEntitiesSpace.ATMOSPHERIC_COMPRESSOR.get(), () -> Parts.COMPRESSOR);
        visualize(AtmosphereBlockEntitiesSpace.ATMOSPHERIC_EMITTER.get(), () -> Parts.EMITTER);
    }

    private static <T extends BlockEntity> void visualize(
            BlockEntityType<T> type, Supplier<SpinningPart<T>> part) {
        Supplier<MeshPart> mesh = Suppliers.memoize(() -> SpinningPartVisual.meshOf(part.get()));
        SimpleBlockEntityVisualizer.builder(type)
                .factory(
                        (context, be, pt) ->
                                new SpinningPartVisual<>(context, be, pt, part.get(), mesh.get()))
                .apply();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }
}
