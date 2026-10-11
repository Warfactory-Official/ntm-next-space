// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.google.common.base.Suppliers;
import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.ClientRegistry;
import com.hbm.client.model.Meshes;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityDishControl;
import com.hbmspace.blocks.IndustryBlockEntitiesSpace;
import com.hbmspace.client.render.PosedParts.Part;
import com.hbmspace.client.render.flywheel.HydroponicVisual;
import com.hbmspace.client.render.flywheel.MagmaVisual;
import com.hbmspace.client.render.flywheel.PosedPartsVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class IndustryRenders {
    private static final class Parts {

        private static final HFRWavefrontObject DISH_CONTROLLER =
                Meshes.load(
                        Identifier.fromNamespaceAndPath(
                                HbmSpace.MOD_ID, "models/machines/dish_controller.obj"));
        private static final Identifier DISH_CONTROLLER_TEX =
                Identifier.fromNamespaceAndPath(
                        HbmSpace.MOD_ID, "textures/block/models/machines/dish_controller.png");
        private static final HFRWavefrontObject MAGMA_DRILL =
                Meshes.load(
                        Identifier.fromNamespaceAndPath(
                                HbmSpace.MOD_ID, "models/machines/magma_drill.obj"));
        private static final Identifier MAGMA_DRILL_TEX =
                Identifier.fromNamespaceAndPath(
                        HbmSpace.MOD_ID, "textures/block/models/machines/magma_drill.png");
        private static final HFRWavefrontObject HYDROPONIC =
                Meshes.load(
                        Identifier.fromNamespaceAndPath(
                                HbmSpace.MOD_ID, "models/machines/hydroponic.obj"));
        private static final Identifier HYDROPONIC_TEX =
                Identifier.fromNamespaceAndPath(
                        HbmSpace.MOD_ID, "textures/block/models/machines/hydroponic.png");

        private static final PosedParts<BlockEntityDishControl> DISH_CONTROL =
                new PosedParts<>(
                        List.of(
                                new Part<BlockEntityDishControl>(
                                        DISH_CONTROLLER,
                                        "drive",
                                        DISH_CONTROLLER_TEX,
                                        true,
                                        (be, pt, out) -> {
                                            out.translate(0.5F, 0F, 0.5F)
                                                    .rotateY(
                                                            Mth.DEG_TO_RAD
                                                                    * Facing.yaw(
                                                                            be.getBlockState()
                                                                                    .getValue(
                                                                                            BlockMultiblockCore
                                                                                                    .FACING),
                                                                            90));
                                            return be.starDarHasDisk;
                                        })),
                        IndustryRenders::dishBox,
                        IndustryRenders::dishBox);
    }

    private IndustryRenders() {}

    private static AABB dishBox(BlockPos pos) {
        return new AABB(
                pos.getX() - 1,
                pos.getY(),
                pos.getZ() - 1,
                pos.getX() + 2,
                pos.getY() + 2,
                pos.getZ() + 2);
    }

    public static void registerBlockEntityRenderers(
            ClientRegistry.BlockEntityRendererRegistrar reg) {
        reg.accept(
                IndustryBlockEntitiesSpace.DISH_CONTROL.get(),
                context -> new RenderPosedParts<>(Parts.DISH_CONTROL));
        reg.accept(IndustryBlockEntitiesSpace.VACUUM_CIRCUIT.get(), RenderVacuumCircuit::new);
        reg.accept(
                IndustryBlockEntitiesSpace.MAGMA.get(),
                context -> new RenderMagma(Parts.MAGMA_DRILL, Parts.MAGMA_DRILL_TEX));
        reg.accept(
                IndustryBlockEntitiesSpace.HYDROBAY.get(),
                context -> new RenderHydroponic(Parts.HYDROPONIC, Parts.HYDROPONIC_TEX));
    }

    public static void registerVisualizers() {
        Supplier<MeshPart[]> meshes =
                Suppliers.memoize(() -> PosedPartsVisual.meshesOf(Parts.DISH_CONTROL));
        SimpleBlockEntityVisualizer.builder(IndustryBlockEntitiesSpace.DISH_CONTROL.get())
                .factory(
                        (context, be, pt) ->
                                new PosedPartsVisual<>(
                                        context, be, pt, Parts.DISH_CONTROL, meshes.get()))
                .apply();
        Supplier<MeshPart> drillHead =
                Suppliers.memoize(
                        () ->
                                MagmaVisual.meshOf(
                                        Parts.MAGMA_DRILL, "Drillhead", Parts.MAGMA_DRILL_TEX));
        Supplier<MeshPart> blades =
                Suppliers.memoize(
                        () ->
                                MagmaVisual.meshOf(
                                        Parts.MAGMA_DRILL, "Blades", Parts.MAGMA_DRILL_TEX));
        SimpleBlockEntityVisualizer.builder(IndustryBlockEntitiesSpace.MAGMA.get())
                .factory(
                        (context, be, pt) ->
                                new MagmaVisual(context, be, pt, drillHead.get(), blades.get()))
                .apply();
        Supplier<MeshPart> lamps =
                Suppliers.memoize(
                        () -> HydroponicVisual.meshOf(Parts.HYDROPONIC, Parts.HYDROPONIC_TEX));
        SimpleBlockEntityVisualizer.builder(IndustryBlockEntitiesSpace.HYDROBAY.get())
                .factory((context, be, pt) -> new HydroponicVisual(context, be, pt, lamps.get()))
                .apply();
    }
}
