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
import com.hbmspace.blockentity.machine.BlockEntityDysonConverterAnatmogenesis;
import com.hbmspace.blockentity.machine.BlockEntityDysonConverterHE;
import com.hbmspace.blockentity.machine.BlockEntityDysonLauncher;
import com.hbmspace.blockentity.machine.BlockEntityDysonReceiver;
import com.hbmspace.blocks.SkyBlockEntitiesSpace;
import com.hbmspace.client.render.PosedParts.Part;
import com.hbmspace.client.render.flywheel.DysonLauncherVisual;
import com.hbmspace.client.render.flywheel.DysonReceiverVisual;
import com.hbmspace.client.render.flywheel.OrreryVisual;
import com.hbmspace.client.render.flywheel.PosedPartsVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class SkyRenders {

    private static final float DEG = Mth.DEG_TO_RAD;

    public static final class Parts {

        private static final HFRWavefrontObject LAUNCHER =
                Meshes.load(id("models/machines/dyson_spinlaunch.obj"));
        private static final Identifier LAUNCHER_TEX =
                id("textures/block/models/machines/dyson_spinlaunch.png");
        private static final HFRWavefrontObject RECEIVER =
                Meshes.load(id("models/machines/dyson_receiver.obj"));
        private static final Identifier RECEIVER_TEX =
                id("textures/block/models/machines/dyson_receiver.png");
        private static final HFRWavefrontObject HE =
                Meshes.load(id("models/machines/dyson_he_converter.obj"));
        private static final Identifier HE_TEX =
                id("textures/block/models/machines/dyson_he_converter.png");
        private static final HFRWavefrontObject ANATMOGENESIS =
                Meshes.load(id("models/machines/dyson_anatmogenesis.obj"));
        private static final Identifier ANATMOGENESIS_TEX =
                id("textures/block/models/machines/dyson_anatmogenesis.png");

        public static final PosedParts<BlockEntityDysonLauncher> LAUNCHER_PARTS =
                new PosedParts<>(
                        List.of(
                                new Part<BlockEntityDysonLauncher>(
                                        LAUNCHER,
                                        "The_Thing_That_Rotates",
                                        LAUNCHER_TEX,
                                        true,
                                        (be, pt, out) -> {
                                            rotor(be, pt, out);
                                            return true;
                                        }),
                                new Part<BlockEntityDysonLauncher>(
                                        LAUNCHER,
                                        "Payload",
                                        LAUNCHER_TEX,
                                        true,
                                        (be, pt, out) -> {
                                            rotor(be, pt, out);
                                            return be.satCount > 0 && !be.isSpinningDown;
                                        }),
                                new Part<BlockEntityDysonLauncher>(
                                        LAUNCHER,
                                        "Payload",
                                        LAUNCHER_TEX,
                                        true,
                                        (be, pt, out) -> {
                                            base(be, out)
                                                    .translate(1F, 8.5F, 0F)
                                                    .rotateX(45F * DEG)
                                                    .rotateY(90F * DEG)
                                                    .translate((be.payloadTicks + pt) * 10F, 0F, 0F)
                                                    .rotateX(-45F * DEG)
                                                    .translate(0F, -8.5F, 0F);
                                            return be.isSpinningDown;
                                        })),
                        pos -> box(pos, 11, 20),
                        pos ->
                                new AABB(
                                        pos.getX() - 100,
                                        pos.getY(),
                                        pos.getZ() - 100,
                                        pos.getX() + 100,
                                        pos.getY() + 100,
                                        pos.getZ() + 100));

        public static final PosedParts<BlockEntityDysonReceiver> RECEIVER_PARTS =
                new PosedParts<>(
                        SkyRenders.<BlockEntityDysonReceiver>coils(
                                RECEIVER,
                                RECEIVER_TEX,
                                List.of("Coil1", "Coil3"),
                                List.of("Coil2"),
                                (be, pt) -> be.isReceiving ? turn(be.getLevel(), pt) : 0F),
                        pos -> box(pos, 7, 3),
                        pos ->
                                new AABB(
                                        pos.getX() - 25,
                                        pos.getY(),
                                        pos.getZ() - 25,
                                        pos.getX() + 25,
                                        pos.getY() + 19,
                                        pos.getZ() + 25));

        private static final PosedParts<BlockEntityDysonConverterHE> HE_PARTS =
                new PosedParts<>(
                        SkyRenders.<BlockEntityDysonConverterHE>coils(
                                HE,
                                HE_TEX,
                                List.of("Coil1", "Coil3"),
                                List.of("Coil2"),
                                (be, pt) -> be.isConverting ? turn(be.getLevel(), pt) : 0F),
                        pos -> box(pos, 4, 3),
                        SkyRenders::converterBox);

        private static final PosedParts<BlockEntityDysonConverterAnatmogenesis>
                ANATMOGENESIS_PARTS =
                        new PosedParts<>(
                                List.of(
                                        new Part<BlockEntityDysonConverterAnatmogenesis>(
                                                ANATMOGENESIS,
                                                "Coils",
                                                ANATMOGENESIS_TEX,
                                                true,
                                                (be, pt, out) -> {
                                                    base(be, out);
                                                    if (be.isConverting) {
                                                        double t = be.getLevel().getGameTime() + pt;
                                                        out.translate(
                                                                0F,
                                                                (float)
                                                                        (Math.sin(t * 0.1) * 0.025
                                                                                + ThreadLocalRandom
                                                                                                .current()
                                                                                                .nextFloat()
                                                                                        * 0.02),
                                                                0F);
                                                    }
                                                    return true;
                                                })),
                                pos -> box(pos, 4, 6),
                                SkyRenders::converterBox);
    }

    private interface Turn<T> {
        float at(T blockEntity, float partialTick);
    }

    private SkyRenders() {}

    public static Matrix4f base(BlockEntity be, Matrix4f out) {
        return out.translate(0.5F, 0F, 0.5F)
                .rotateY(
                        Facing.yaw(be.getBlockState().getValue(BlockMultiblockCore.FACING), 0)
                                * DEG);
    }

    private static void rotor(BlockEntityDysonLauncher be, float pt, Matrix4f out) {
        float t = be.lastRotation + (be.rotation - be.lastRotation) * pt;
        base(be, out)
                .translate(0F, 8.5F, 0F)
                .rotateX(45F * DEG)
                .rotateY(t * DEG)
                .rotateX(-45F * DEG)
                .translate(0F, -8.5F, 0F);
    }

    private static float turn(Level level, float pt) {
        return level.getGameTime() % 360L + pt;
    }

    private static <T extends BlockEntity> List<Part<T>> coils(
            HFRWavefrontObject mesh,
            Identifier texture,
            List<String> forward,
            List<String> backward,
            Turn<T> turn) {
        List<Part<T>> parts = new ArrayList<>();
        for (String group : forward) {
            parts.add(
                    new Part<T>(
                            mesh,
                            group,
                            texture,
                            true,
                            (be, pt, out) -> {
                                base(be, out)
                                        .translate(0F, 1.5F, 0F)
                                        .rotateZ(turn.at(be, pt) * DEG)
                                        .translate(0F, -1.5F, 0F);
                                return true;
                            }));
        }
        for (String group : backward) {
            parts.add(
                    new Part<T>(
                            mesh,
                            group,
                            texture,
                            true,
                            (be, pt, out) -> {
                                base(be, out)
                                        .translate(0F, 1.5F, 0F)
                                        .rotateZ(-turn.at(be, pt) * DEG)
                                        .translate(0F, -1.5F, 0F);
                                return true;
                            }));
        }
        return parts;
    }

    private static AABB box(BlockPos pos, int reach, int height) {
        return new AABB(
                pos.getX() - reach,
                pos.getY(),
                pos.getZ() - reach,
                pos.getX() + reach + 1,
                pos.getY() + height,
                pos.getZ() + reach + 1);
    }

    private static AABB converterBox(BlockPos pos) {
        return new AABB(
                pos.getX() - 6,
                pos.getY(),
                pos.getZ() - 6,
                pos.getX() + 7,
                pos.getY() + 6,
                pos.getZ() + 7);
    }

    public static void registerBlockEntityRenderers(
            ClientRegistry.BlockEntityRendererRegistrar reg) {
        reg.accept(SkyBlockEntitiesSpace.ORRERY.get(), context -> new RenderOrrery());
        reg.accept(
                SkyBlockEntitiesSpace.DYSON_LAUNCHER.get(),
                context -> new RenderPosedParts<>(Parts.LAUNCHER_PARTS));
        reg.accept(
                SkyBlockEntitiesSpace.DYSON_RECEIVER.get(), context -> new RenderDysonReceiver());
        reg.accept(
                SkyBlockEntitiesSpace.DYSON_CONVERTER_HE.get(),
                context -> new RenderPosedParts<>(Parts.HE_PARTS));
        reg.accept(
                SkyBlockEntitiesSpace.DYSON_CONVERTER_ANATMOGENESIS.get(),
                context -> new RenderPosedParts<>(Parts.ANATMOGENESIS_PARTS));
    }

    public static void registerVisualizers() {
        SimpleBlockEntityVisualizer.builder(SkyBlockEntitiesSpace.ORRERY.get())
                .factory(OrreryVisual::new)
                .apply();
        Supplier<MeshPart[]> launcher =
                Suppliers.memoize(() -> PosedPartsVisual.meshesOf(Parts.LAUNCHER_PARTS));
        SimpleBlockEntityVisualizer.builder(SkyBlockEntitiesSpace.DYSON_LAUNCHER.get())
                .factory(
                        (context, be, pt) ->
                                new DysonLauncherVisual(
                                        context, be, pt, Parts.LAUNCHER_PARTS, launcher.get()))
                .apply();
        Supplier<MeshPart[]> receiver =
                Suppliers.memoize(() -> PosedPartsVisual.meshesOf(Parts.RECEIVER_PARTS));
        SimpleBlockEntityVisualizer.builder(SkyBlockEntitiesSpace.DYSON_RECEIVER.get())
                .factory(
                        (context, be, pt) ->
                                new DysonReceiverVisual(
                                        context, be, pt, Parts.RECEIVER_PARTS, receiver.get()))
                .apply();
        posed(SkyBlockEntitiesSpace.DYSON_CONVERTER_HE.get(), () -> Parts.HE_PARTS);
        posed(
                SkyBlockEntitiesSpace.DYSON_CONVERTER_ANATMOGENESIS.get(),
                () -> Parts.ANATMOGENESIS_PARTS);
    }

    private static <T extends BlockEntity> void posed(
            BlockEntityType<T> type, Supplier<PosedParts<T>> parts) {
        Supplier<MeshPart[]> meshes =
                Suppliers.memoize(() -> PosedPartsVisual.meshesOf(parts.get()));
        SimpleBlockEntityVisualizer.builder(type)
                .factory(
                        (context, be, pt) ->
                                new PosedPartsVisual<>(context, be, pt, parts.get(), meshes.get()))
                .apply();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }
}
