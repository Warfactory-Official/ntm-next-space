/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.client.render;

import com.google.common.base.Suppliers;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.ClientRegistry;
import com.hbm.client.model.Meshes;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.CombatDropPodBlockEntity;
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity;
import com.hbmspace.blockentity.machine.DriveProcessorBlockEntity;
import com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity;
import com.hbmspace.blockentity.machine.StardarBlockEntity;
import com.hbmspace.blocks.RocketryBlocksSpace;
import com.hbmspace.client.render.flywheel.PosedPartsVisual;
import com.hbmspace.client.render.flywheel.RocketHostVisual;
import com.hbmspace.handler.RocketStruct.RocketStage;
import com.hbmspace.handler.RocketStruct;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public final class RocketryRenders {

    private static final Identifier ROCKET_PAD = space("models/machines/rocket_pad.obj");
    private static final Identifier ROCKET_PAD_SUPPORT =
            space("textures/block/models/machines/rocket_pad_support.png");
    private static final Identifier ROCKET_ASSEMBLY = space("models/machines/rocket_assembly.obj");
    private static final Identifier ROCKET_ASSEMBLY_TEX =
            space("textures/block/models/machines/rocket_assembly.png");

    public record PadSnapshot(Direction facing, int tower, @Nullable RocketStruct rocket) {}

    public static final RocketHost<LaunchPadRocketBlockEntity, PadSnapshot> PAD =
            new RocketHost<>() {
                @Override
                public PadSnapshot snapshot(LaunchPadRocketBlockEntity pad) {
                    RocketStruct rocket = pad.rocket;
                    return new PadSnapshot(
                            pad.getBlockState().getValue(MultiblockCoreBlock.FACING),
                            pad.towerHeight(),
                            rocket == null ? null : rocket.copy());
                }

                @Override
                public void draw(
                        PadSnapshot pad, RocketPronter pronter, PoseStack pose, PieceSink sink) {
                    pose.pushPose();
                    pose.translate(0.5F, 0F, 0.5F);
                    pose.rotate(
                            (Quaternionfc) Axis.YP.rotationDegrees(Facing.yaw(pad.facing(), 90)));

                    if (pad.tower() >= 8) {
                        HFRWavefrontObject mesh = pronter.machine(ROCKET_PAD, false);
                        pose.pushPose();
                        sink.accept(support(mesh, "Tower_Base"), false);
                        for (int oy = 8; oy < pad.tower() - 2; oy += 3) {
                            sink.accept(support(mesh, "Tower_Segment"), false);
                            pose.translate(0F, 3F, 0F);
                        }
                        pose.translate(0F, -3F, 0F);
                        for (int oy = 0; oy < (pad.tower() - 2) % 3; oy++) {
                            sink.accept(support(mesh, "Tower_Segment_Small"), false);
                            pose.translate(0F, 1F, 0F);
                        }
                        pose.translate(0F, -1F, 0F);
                        sink.accept(support(mesh, "Tower_Cap"), false);
                        pose.popPose();
                    }

                    if (pad.rocket() != null) {
                        pose.translate(0F, 3F, 0F);
                        pronter.visit(
                                pad.rocket(),
                                null,
                                true,
                                0,
                                0,
                                0F,
                                pose,
                                piece -> sink.accept(piece, true));
                    }

                    pose.popPose();
                }

                @Override
                public AABB bounds(BlockPos pos, PadSnapshot pad) {
                    double top =
                            Math.max(
                                    pad.tower() + 3,
                                    pad.rocket() == null ? 3 : 3 + pad.rocket().getHeight());
                    return new AABB(
                            pos.getX() - 7,
                            pos.getY(),
                            pos.getZ() - 7,
                            pos.getX() + 8,
                            pos.getY() + top + 1,
                            pos.getZ() + 8);
                }
            };

    public record VabSnapshot(RocketStruct rocket) {}

    public static final RocketHost<RocketAssemblyBlockEntity, VabSnapshot> VAB =
            new RocketHost<>() {
                @Override
                public VabSnapshot snapshot(RocketAssemblyBlockEntity vab) {
                    return new VabSnapshot(vab.rocket.copy());
                }

                @Override
                public void draw(
                        VabSnapshot vab, RocketPronter pronter, PoseStack pose, PieceSink sink) {
                    RocketStruct rocket = vab.rocket();
                    if (!rocket.extraIssues.isEmpty()) return;

                    pose.pushPose();
                    pose.translate(0.5F, -2F, 0.5F);

                    pose.pushPose();
                    pose.translate(0F, 2.95F, 0F);
                    if (!rocket.stages.isEmpty() && rocket.stages.getFirst().thruster() != null) {
                        pose.translate(0, -rocket.stages.getFirst().thruster().height, 0);
                    }
                    pronter.visit(
                            rocket, null, false, 0, 0, 0F, pose, piece -> sink.accept(piece, true));
                    pose.popPose();

                    HFRWavefrontObject mesh = pronter.machine(ROCKET_ASSEMBLY, true);
                    RocketPronter.Piece support =
                            new RocketPronter.Piece(
                                    ROCKET_ASSEMBLY,
                                    mesh,
                                    mesh.partId("Support"),
                                    ROCKET_ASSEMBLY_TEX,
                                    Float.NaN);
                    RocketPronter.Piece level =
                            new RocketPronter.Piece(
                                    ROCKET_ASSEMBLY,
                                    mesh,
                                    mesh.partId("Level"),
                                    ROCKET_ASSEMBLY_TEX,
                                    Float.NaN);
                    for (int i = 0; i < rocket.stages.size(); i++) {
                        RocketStage stage = rocket.stages.get(i);
                        RocketStage nextStage =
                                i < rocket.stages.size() - 1 ? rocket.stages.get(i + 1) : null;

                        double targetHeight = 0;
                        if (stage.fuselage() != null)
                            targetHeight += stage.fuselage().height * stage.getStack();
                        if (nextStage != null && nextStage.thruster() != null)
                            targetHeight += nextStage.thruster().height;

                        while (targetHeight > 1) {
                            pose.translate(0, 1, 0);
                            sink.accept(support, true);
                            targetHeight--;
                        }
                        pose.translate(0, targetHeight, 0);

                        sink.accept(level, true);
                    }

                    pose.popPose();
                }

                @Override
                public AABB bounds(BlockPos pos, VabSnapshot vab) {
                    return new AABB(
                            pos.getX() - 4,
                            pos.getY() - 2,
                            pos.getZ() - 4,
                            pos.getX() + 5,
                            pos.getY() + vab.rocket().getHeight() + 2,
                            pos.getZ() + 5);
                }
            };

    private static final class Parts {

        private static final HFRWavefrontObject DRIVE_PROCESSOR_MESH =
                Meshes.load(space("models/machines/drive_processor.obj"));

        private static final PosedParts<DriveProcessorBlockEntity> DRIVE_PROCESSOR =
                new PosedParts<>(
                        List.of(
                                new PosedParts.Part<DriveProcessorBlockEntity>(
                                        DRIVE_PROCESSOR_MESH,
                                        "Drive",
                                        space("textures/block/models/machines/drive_processor.png"),
                                        true,
                                        (be, pt, out) -> {
                                            out.translate(0.5F, 0F, 0.5F)
                                                    .rotateY(
                                                            Facing.yaw(
                                                                            be.getBlockState()
                                                                                    .getValue(
                                                                                            MultiblockCoreBlock
                                                                                                    .FACING),
                                                                            90)
                                                                    * Mth.DEG_TO_RAD);
                                            return be.hasDrive;
                                        })),
                        RocketryRenders::driveProcessorBox,
                        RocketryRenders::driveProcessorBox);

        private static final Identifier STARDAR = space("models/machines/stardar.obj");
        private static final Identifier STARDAR_TEX =
                space("textures/block/models/machines/stardar.png");
        private static final HFRWavefrontObject STARDAR_MESH = Meshes.load(STARDAR);
        private static final float DISH_PIVOT = 10.6F;
        private static final float STARDAR_REACH =
                reach(Objects.requireNonNull(STARDAR_MESH.boundsOfParts("rotation", "pitch")));

        private static final PosedParts<StardarBlockEntity> STARDAR_PARTS =
                new PosedParts<>(
                        List.of(
                                new PosedParts.Part<StardarBlockEntity>(
                                        STARDAR_MESH,
                                        "rotation",
                                        STARDAR_TEX,
                                        true,
                                        (be, pt, out) -> {
                                            dish(be, pt, out);
                                            return true;
                                        }),
                                new PosedParts.Part<StardarBlockEntity>(
                                        Meshes.flatShaded(Meshes.load(STARDAR)),
                                        "pitch",
                                        STARDAR_TEX,
                                        true,
                                        (be, pt, out) -> {
                                            dish(be, pt, out)
                                                    .translate(0F, DISH_PIVOT, 0F)
                                                    .rotateX(
                                                            Mth.lerp(
                                                                            pt,
                                                                            be.prevDishPitch,
                                                                            be.dishPitch)
                                                                    * Mth.DEG_TO_RAD)
                                                    .translate(0F, -DISH_PIVOT, 0F);
                                            return true;
                                        })),
                        Parts::stardarBox,
                        Parts::stardarBox);

        private static final HFRWavefrontObject COMBAT_POD_MESH =
                Meshes.load(CombatDropPodRenderer.MESH);

        private static final PosedParts<CombatDropPodBlockEntity> COMBAT_POD =
                new PosedParts<>(
                        combatPod(), RocketryRenders::combatPodBox, RocketryRenders::combatPodBox);

        private static List<PosedParts.Part<CombatDropPodBlockEntity>> combatPod() {
            List<PosedParts.Part<CombatDropPodBlockEntity>> parts = new ArrayList<>();
            for (int skin = 0; skin < CombatDropPodRenderer.skinCount(); skin++) {
                int shown = skin;
                Identifier texture = CombatDropPodRenderer.skinTexture(skin);
                parts.add(
                        new PosedParts.Part<>(
                                COMBAT_POD_MESH,
                                "hatch1",
                                texture,
                                true,
                                (be, pt, out) -> {
                                    double open = Mth.lerp(pt, be.prevHatchopen, be.hatchopen);
                                    pod(be, out)
                                            .translate(
                                                    0F,
                                                    (float) (open * 0.013),
                                                    (float) (open * 0.01))
                                            .rotateX((float) open * Mth.DEG_TO_RAD);
                                    return CombatDropPodRenderer.skinIndex(be.color) == shown;
                                }));
                parts.add(
                        new PosedParts.Part<>(
                                COMBAT_POD_MESH,
                                "hatch2",
                                texture,
                                true,
                                (be, pt, out) -> {
                                    double open = Mth.lerp(pt, be.prevHatchopen, be.hatchopen);
                                    double open2 = Mth.lerp(pt, be.prevHatchopen2, be.hatchopen2);
                                    pod(be, out)
                                            .translate(
                                                    0F,
                                                    (float) (-open2 * 0.013),
                                                    (float) (-open * 0.01))
                                            .rotateX((float) open2 * Mth.DEG_TO_RAD);
                                    return CombatDropPodRenderer.skinIndex(be.color) == shown;
                                }));
                parts.add(
                        new PosedParts.Part<>(
                                COMBAT_POD_MESH,
                                "bomb",
                                texture,
                                true,
                                (be, pt, out) -> {
                                    pod(be, out);
                                    return CombatDropPodRenderer.skinIndex(be.color) == shown;
                                }));
            }
            return parts;
        }

        private static Matrix4f pod(CombatDropPodBlockEntity be, Matrix4f out) {
            float[] tilt = be.tilt();
            return out.translate(0.5F, 1.25F, 0.5F)
                    .rotateY(tilt[0] * Mth.DEG_TO_RAD)
                    .rotateX(tilt[1] * Mth.DEG_TO_RAD)
                    .rotateZ(tilt[2] * Mth.DEG_TO_RAD);
        }

        private static Matrix4f dish(StardarBlockEntity be, float pt, Matrix4f out) {
            return out.translate(0.5F, -3F, 0.5F)
                    .rotateY(Mth.lerp(pt, be.prevDishYaw, be.dishYaw) * Mth.DEG_TO_RAD);
        }

        private static float reach(float[] box) {
            float dx = Math.max(-box[0], box[3]);
            float dy = Math.max(DISH_PIVOT - box[1], box[4] - DISH_PIVOT);
            float dz = Math.max(-box[2], box[5]);
            return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        private static AABB stardarBox(BlockPos pos) {
            return new AABB(
                    pos.getX() + 0.5 - STARDAR_REACH,
                    pos.getY() - 3 + DISH_PIVOT - STARDAR_REACH,
                    pos.getZ() + 0.5 - STARDAR_REACH,
                    pos.getX() + 0.5 + STARDAR_REACH,
                    pos.getY() - 3 + DISH_PIVOT + STARDAR_REACH,
                    pos.getZ() + 0.5 + STARDAR_REACH);
        }
    }

    private RocketryRenders() {}

    private static AABB combatPodBox(BlockPos pos) {
        return new AABB(
                pos.getX() - 3,
                pos.getY(),
                pos.getZ() - 3,
                pos.getX() + 4,
                pos.getY() + 8,
                pos.getZ() + 4);
    }

    private static AABB driveProcessorBox(BlockPos pos) {
        return new AABB(
                pos.getX() - 1,
                pos.getY(),
                pos.getZ() - 1,
                pos.getX() + 2,
                pos.getY() + 1,
                pos.getZ() + 2);
    }

    private static RocketPronter.Piece support(HFRWavefrontObject mesh, String group) {
        return new RocketPronter.Piece(
                ROCKET_PAD, mesh, mesh.partId(group), ROCKET_PAD_SUPPORT, Float.NaN);
    }

    public static void registerBlockEntityRenderers(
            ClientRegistry.BlockEntityRendererRegistrar reg) {
        reg.accept(
                RocketryBlocksSpace.LAUNCH_PAD_ROCKET_BE.get(),
                context -> new RocketHostRenderer<>(PAD));
        reg.accept(
                RocketryBlocksSpace.ROCKET_ASSEMBLY.get(),
                context -> new RocketHostRenderer<>(VAB));
        reg.accept(
                RocketryBlocksSpace.DRIVE_PROCESSOR_BE.get(),
                context -> new PosedPartsRenderer<>(Parts.DRIVE_PROCESSOR));
        reg.accept(
                RocketryBlocksSpace.STARDAR_BE.get(),
                context -> new PosedPartsRenderer<>(Parts.STARDAR_PARTS));
        reg.accept(
                RocketryBlocksSpace.COMBAT_DROP_BE.get(),
                context -> new PosedPartsRenderer<>(Parts.COMBAT_POD));
    }

    public static void registerVisualizers() {
        visualize(RocketryBlocksSpace.LAUNCH_PAD_ROCKET_BE.get(), PAD);
        visualize(RocketryBlocksSpace.ROCKET_ASSEMBLY.get(), VAB);
        visualizeParts(RocketryBlocksSpace.DRIVE_PROCESSOR_BE.get(), () -> Parts.DRIVE_PROCESSOR);
        visualizeParts(RocketryBlocksSpace.STARDAR_BE.get(), () -> Parts.STARDAR_PARTS);
        visualizeParts(RocketryBlocksSpace.COMBAT_DROP_BE.get(), () -> Parts.COMBAT_POD);
    }

    private static <T extends BlockEntity, S> void visualize(
            BlockEntityType<T> type, RocketHost<T, S> host) {
        RocketPronter pronter = new RocketPronter();
        SimpleBlockEntityVisualizer.builder(type)
                .factory(
                        (context, be, pt) -> new RocketHostVisual<>(context, be, pt, host, pronter))
                .apply();
    }

    private static <T extends BlockEntity> void visualizeParts(
            BlockEntityType<T> type, Supplier<PosedParts<T>> parts) {
        Supplier<MeshPart[]> meshes =
                Suppliers.memoize(() -> PosedPartsVisual.meshesOf(parts.get()));
        SimpleBlockEntityVisualizer.builder(type)
                .factory(
                        (context, be, pt) ->
                                new PosedPartsVisual<>(context, be, pt, parts.get(), meshes.get()))
                .apply();
    }

    private static Identifier space(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }
}
