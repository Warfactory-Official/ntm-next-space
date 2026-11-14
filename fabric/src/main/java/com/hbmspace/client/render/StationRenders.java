/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
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
import com.hbm.blocks.machine.HorizontalMachineBlock;
import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.ClientRegistry;
import com.hbm.client.render.flywheel.MeshPart;
import com.hbm.main.ResourceManager;
import com.hbm.util.Facing;
import com.hbm.util.NtmMath;
import com.hbmspace.blockentity.machine.GasDockBlockEntity;
import com.hbmspace.blockentity.machine.HTR3BlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationComputerBlockEntity;
import com.hbmspace.blockentity.machine.OrbitalStationLauncherBlockEntity;
import com.hbmspace.blockentity.machine.TransporterRocketBlockEntity;
import com.hbmspace.blocks.StationBlockEntitiesSpace;
import com.hbmspace.client.render.PosedParts.Part;
import com.hbmspace.client.render.flywheel.HTRNeoVisual;
import com.hbmspace.client.render.flywheel.PosedPartsVisual;
import com.hbmspace.client.render.flywheel.StationLauncherVisual;
import com.hbmspace.client.render.flywheel.XenonThrusterVisual;
import com.hbmspace.handler.RocketStruct;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

public final class StationRenders {

    private static final float DEG = Mth.DEG_TO_RAD;

    private static final class Parts {

        private static final PosedParts<OrbitalStationBlockEntity> STATION =
                new PosedParts<>(
                        stationArms(
                                StationModels.docking_port_tex,
                                (OrbitalStationBlockEntity be, float pt) ->
                                        be.prevRot + (be.rot - be.prevRot) * pt),
                        StationRenders::stationBox,
                        StationRenders::stationBox);

        private static final PosedParts<OrbitalStationLauncherBlockEntity> LAUNCHER =
                new PosedParts<>(
                        stationArms(
                                StationModels.docking_port_launcher_tex,
                                (OrbitalStationLauncherBlockEntity be, float pt) ->
                                        be.prevRot + (be.rot - be.prevRot) * pt),
                        StationRenders::stationBox,
                        StationRenders::stationBox);

        private static final PosedParts<OrbitalStationComputerBlockEntity> COMPUTER =
                new PosedParts<>(
                        List.of(
                                new Part<OrbitalStationComputerBlockEntity>(
                                        StationModels.orbital_computer,
                                        "drive",
                                        StationModels.orbital_computer_tex,
                                        true,
                                        (be, pt, out) -> {
                                            out.translate(0.5F, 0F, 0.5F)
                                                    .rotateY(
                                                            Facing.yaw(
                                                                            be.getBlockState()
                                                                                    .getValue(
                                                                                            HorizontalMachineBlock
                                                                                                    .FACING),
                                                                            0)
                                                                    * DEG);
                                            return be.hasDrive;
                                        })),
                        StationRenders::blockBox,
                        StationRenders::blockBox);

        private static final PosedParts<TransporterRocketBlockEntity> TRANSPORTER =
                new PosedParts<>(
                        List.of(
                                new Part<TransporterRocketBlockEntity>(
                                        StationModels.transporter_pad,
                                        "pipe",
                                        StationModels.transporter_pad_tex,
                                        true,
                                        (be, pt, out) -> {
                                            padBase(be, out)
                                                    .translate(0F, 0.75F, -0.75F)
                                                    .rotateX(
                                                            Mth.clamp(pipeEngage(be, pt), 0, 1)
                                                                    * -30.0F
                                                                    * DEG)
                                                    .translate(0F, -0.75F, 0.75F);
                                            return true;
                                        }),
                                new Part<TransporterRocketBlockEntity>(
                                        ResourceManager.miner_rocket,
                                        "Cylinder",
                                        StationModels.miner_rocket_tex,
                                        false,
                                        (be, pt, out) -> {
                                            padBase(be, out)
                                                    .translate(
                                                            0F,
                                                            rocketY(
                                                                    be.launchTicks,
                                                                    be.hasRocket,
                                                                    pt),
                                                            0F);
                                            return be.launchTicks < 100;
                                        })),
                        pos ->
                                new AABB(
                                        pos.getX() - 2,
                                        pos.getY(),
                                        pos.getZ() - 2,
                                        pos.getX() + 3,
                                        pos.getY() + 2,
                                        pos.getZ() + 3),
                        pos ->
                                new AABB(
                                        pos.getX() - 2,
                                        pos.getY(),
                                        pos.getZ() - 2,
                                        pos.getX() + 3,
                                        pos.getY() + 204,
                                        pos.getZ() + 3));

        private static final PosedParts<GasDockBlockEntity> GAS_DOCK =
                new PosedParts<>(
                        List.of(
                                new Part<GasDockBlockEntity>(
                                        ResourceManager.miner_rocket,
                                        "Cylinder",
                                        StationModels.gas_rocket_tex,
                                        false,
                                        (be, pt, out) -> {
                                            out.translate(
                                                    0.5F,
                                                    rocketY(be.launchTicks, be.hasRocket, pt),
                                                    0.5F);
                                            return be.launchTicks < 100;
                                        })),
                        StationRenders::gasDockBox,
                        pos -> gasDockBox(pos).expandTowards(0, 103, 0));

        private static final PosedParts<HTR3BlockEntity> HTR3 =
                new PosedParts<>(htr3Parts(), StationRenders::htr3Box, StationRenders::htr3Box);
    }

    private StationRenders() {}

    private interface ArmAngle<T> {
        float at(T blockEntity, float partialTick);
    }

    private static <T extends BlockEntity> List<Part<T>> stationArms(
            Identifier texture, ArmAngle<T> angle) {
        List<Part<T>> arms = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int turn = i * 90;
            arms.add(
                    new Part<T>(
                            StationModels.docking_port,
                            "ArmZP",
                            texture,
                            true,
                            (be, pt, out) -> {
                                float arm = angle.at(be, pt);
                                out.translate(0.5F, 1.0F, 0.5F)
                                        .rotateY(
                                                Facing.yaw(
                                                                be.getBlockState()
                                                                        .getValue(
                                                                                MultiblockCoreBlock
                                                                                        .FACING),
                                                                0)
                                                        * DEG)
                                        .rotateY(turn * DEG)
                                        .translate(0F, -1.75F, -2F)
                                        .rotateX(-arm * DEG)
                                        .translate(0F, 1.75F, 2F);
                                return true;
                            }));
        }
        return arms;
    }

    private static Matrix4f padBase(TransporterRocketBlockEntity be, Matrix4f out) {
        return out.translate(0.5F, 0F, 0.5F)
                .rotateY(
                        Facing.yaw(be.getBlockState().getValue(MultiblockCoreBlock.FACING), 90)
                                * DEG);
    }

    private static float pipeEngage(TransporterRocketBlockEntity pad, float interp) {
        if (pad.launchTicks >= 0) return !pad.hasRocket ? (pad.launchTicks + interp) * 0.25F : 1.0F;
        return 1 - (-pad.launchTicks - 1 + interp) * 0.25F;
    }

    private static float rocketY(int launchTicks, boolean hasRocket, float interp) {
        return 0.75F + Mth.clamp(launchTicks + (hasRocket ? -interp : interp), 0, 200);
    }

    private record Htr3Timeline(double sway, double h, double v, double piston, double cover) {

        static Htr3Timeline of(HTR3BlockEntity be, float pt) {
            double t = be.lastTime + (be.time - be.lastTime) * pt;

            double swayTimer = (t / 3D) % (Math.PI * 4);
            double sway =
                    (Math.sin(swayTimer)
                                    + Math.sin(swayTimer * 2)
                                    + Math.sin(swayTimer * 4)
                                    + 2.23255D)
                            * 0.5;

            double bellTimer = (t / 5D) % (Math.PI * 4);
            double h = (Math.sin(bellTimer + Math.PI) + Math.sin(bellTimer * 1.5D)) / 1.90596D;
            double v = (Math.sin(bellTimer) + Math.sin(bellTimer * 1.5D)) / 1.90596D;

            double pistonTimer = (t / 5D) % (Math.PI * 2);
            double piston = NtmMath.flatSine(pistonTimer);

            double coverTimer = (t / 5D) % (Math.PI * 4);
            double cover =
                    (Math.sin(coverTimer) + Math.sin(coverTimer * 2) + Math.sin(coverTimer * 4))
                            * 0.5;

            return new Htr3Timeline(sway, h, v, piston, cover);
        }
    }

    private interface Htr3Pose {
        void apply(Htr3Timeline timeline, Matrix4f out);
    }

    private static Part<HTR3BlockEntity> htr3(String group, Htr3Pose pose) {
        return new Part<HTR3BlockEntity>(
                StationModels.htr3,
                group,
                ResourceManager.lpw2_tex,
                true,
                (be, pt, out) -> {
                    out.translate(0.5F, -3.0F, 0.5F)
                            .rotateY(
                                    Facing.yaw(
                                                    be.getBlockState()
                                                            .getValue(MultiblockCoreBlock.FACING),
                                                    90)
                                            * DEG);
                    pose.apply(Htr3Timeline.of(be, pt), out);
                    return true;
                });
    }

    private static List<Part<HTR3BlockEntity>> htr3Parts() {
        double shroud = 0.125D;
        double magnitude = 2D;
        return List.of(
                htr3(
                        "Piston",
                        (tl, out) ->
                                out.translate(0F, 0F, (float) (-tl.sway() * 0.125))
                                        .translate(
                                                0F, 0F, (float) (tl.piston() * 0.375D + 0.375D))),
                htr3(
                        "Engine",
                        (tl, out) ->
                                out.translate(0F, 0F, (float) (-tl.sway() * 0.125))
                                        .translate(0F, 3.5F, -1F)
                                        .rotateY((float) (tl.v() * magnitude) * DEG)
                                        .rotateX((float) (tl.h() * magnitude) * DEG)
                                        .translate(0F, -3.5F, 1F)),
                htr3("ShroudH", (tl, out) -> out.translate(0F, (float) (-tl.h() * shroud), 0F)),
                htr3("ShroudV", (tl, out) -> out.translate((float) (tl.v() * shroud), 0F, 0F)),
                htr3("CoverTop", (tl, out) -> out.translate(0F, 0F, (float) (-tl.cover() * 0.125))),
                htr3(
                        "CoverBottom",
                        (tl, out) -> out.translate(0F, 0F, (float) (-tl.cover() * 0.125))),
                htr3("SuspensionBottom", StationRenders::suspension),
                htr3("SuspensionTop", StationRenders::suspension));
    }

    private static void suspension(Htr3Timeline tl, Matrix4f out) {
        out.translate(0F, 0F, 3.5F)
                .scale(1F, 1F, (float) ((3 + tl.cover() * 0.125) / 3))
                .translate(0F, 0F, -3.5F);
    }

    private static AABB stationBox(BlockPos pos) {
        return new AABB(
                pos.getX() - 2,
                pos.getY() - 2,
                pos.getZ() - 2,
                pos.getX() + 3,
                pos.getY() + 2,
                pos.getZ() + 3);
    }

    public static void launcherRocketOrigin(PoseStack pose, float yaw, RocketStruct rocket) {
        pose.translate(0.5F, 1.0F, 0.5F);
        pose.mulPose((Quaternionfc) Axis.YP.rotationDegrees(yaw));
        pose.translate(0, -rocket.getHeight() - 0.5, 0);
    }

    public static AABB launcherRocketBox(BlockPos pos, RocketStruct rocket) {
        return new AABB(
                pos.getX() - 2,
                pos.getY() + 0.5 - rocket.getHeight(),
                pos.getZ() - 2,
                pos.getX() + 3,
                pos.getY() + 1,
                pos.getZ() + 3);
    }

    private static AABB blockBox(BlockPos pos) {
        return new AABB(pos);
    }

    private static AABB gasDockBox(BlockPos pos) {
        return new AABB(
                pos.getX() - 1,
                pos.getY(),
                pos.getZ() - 1,
                pos.getX() + 2,
                pos.getY() + 1,
                pos.getZ() + 2);
    }

    private static AABB htr3Box(BlockPos pos) {
        return new AABB(
                pos.getX() - 10,
                pos.getY() - 3,
                pos.getZ() - 10,
                pos.getX() + 11,
                pos.getY() + 4,
                pos.getZ() + 11);
    }

    public static void registerBlockEntityRenderers(
            ClientRegistry.BlockEntityRendererRegistrar reg) {
        reg.accept(
                StationBlockEntitiesSpace.ORBITAL_STATION.get(),
                context -> new PosedPartsRenderer<>(Parts.STATION));
        reg.accept(
                StationBlockEntitiesSpace.ORBITAL_STATION_COMPUTER.get(),
                context -> new PosedPartsRenderer<>(Parts.COMPUTER));
        reg.accept(
                StationBlockEntitiesSpace.TRANSPORTER_ROCKET.get(),
                context -> new PosedPartsRenderer<>(Parts.TRANSPORTER));
        reg.accept(
                StationBlockEntitiesSpace.GAS_DOCK.get(),
                context -> new PosedPartsRenderer<>(Parts.GAS_DOCK));
        reg.accept(
                StationBlockEntitiesSpace.HTR3.get(),
                context -> new PosedPartsRenderer<>(Parts.HTR3));
        reg.accept(
                StationBlockEntitiesSpace.ORBITAL_STATION_LAUNCHER.get(),
                context -> new StationLauncherRenderer(Parts.LAUNCHER));
    }

    public static void registerVisualizers() {
        visualize(StationBlockEntitiesSpace.ORBITAL_STATION.get(), () -> Parts.STATION);
        visualize(StationBlockEntitiesSpace.ORBITAL_STATION_COMPUTER.get(), () -> Parts.COMPUTER);
        visualize(StationBlockEntitiesSpace.TRANSPORTER_ROCKET.get(), () -> Parts.TRANSPORTER);
        visualize(StationBlockEntitiesSpace.GAS_DOCK.get(), () -> Parts.GAS_DOCK);
        visualize(StationBlockEntitiesSpace.HTR3.get(), () -> Parts.HTR3);
        Supplier<MeshPart[]> launcherArms =
                Suppliers.memoize(() -> PosedPartsVisual.meshesOf(Parts.LAUNCHER));
        RocketPronter pronter = new RocketPronter();
        SimpleBlockEntityVisualizer.builder(
                        StationBlockEntitiesSpace.ORBITAL_STATION_LAUNCHER.get())
                .factory(
                        (context, be, pt) ->
                                new StationLauncherVisual(
                                        context,
                                        be,
                                        pt,
                                        Parts.LAUNCHER,
                                        launcherArms.get(),
                                        pronter))
                .apply();
        SimpleBlockEntityVisualizer.builder(StationBlockEntitiesSpace.XENON_THRUSTER.get())
                .factory(XenonThrusterVisual::new)
                .apply();
        SimpleBlockEntityVisualizer.builder(StationBlockEntitiesSpace.HTRF4_NEO.get())
                .factory(HTRNeoVisual::new)
                .apply();
    }

    private static <T extends BlockEntity> void visualize(
            BlockEntityType<T> type, Supplier<PosedParts<T>> parts) {
        Supplier<MeshPart[]> meshes =
                Suppliers.memoize(() -> PosedPartsVisual.meshesOf(parts.get()));
        SimpleBlockEntityVisualizer.builder(type)
                .factory(
                        (context, be, pt) ->
                                new PosedPartsVisual<>(context, be, pt, parts.get(), meshes.get()))
                .apply();
    }
}
