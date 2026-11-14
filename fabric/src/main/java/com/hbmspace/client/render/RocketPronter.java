/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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

import com.hbm.client.model.Meshes;
import com.hbm.items.weapon.CustomMissilePartItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.HbmSpace;
import com.hbmspace.handler.RocketPart;
import com.hbmspace.handler.RocketStruct.RocketStage;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.items.weapon.MissilePartsSpace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public final class RocketPronter {

    private static final Identifier FAIRING = space("models/missile_parts/mp_w_fairing.obj");
    private static final Identifier FAIRING_TEX =
            space("textures/models/missile_parts/warheads/mp_w_fairing.png");
    private static final Identifier LEGS_DEPLOYED =
            space("models/missile_parts/rp_s_20_leggy_deployed.obj");
    private static final Identifier SHROUD = space("models/missile_parts/mp_f_20_6_usa.obj");
    private static final Identifier DROP_POD = space("models/missile_parts/rp_drop_pod.obj");
    private static final Identifier DROP_POD_TEX =
            space("textures/models/missile_parts/warheads/rp_drop_pod.png");

    private final Map<Identifier, HFRWavefrontObject> meshes = new ConcurrentHashMap<>();
    private final Map<Identifier, HFRWavefrontObject> flatMeshes = new ConcurrentHashMap<>();

    public void pront(
            RocketStruct rocket,
            @Nullable PodPose pod,
            PoseStack pose,
            SubmitNodeCollector collector,
            int light,
            boolean deployed,
            int decoupleTimer,
            int shroudTimer,
            float partialTick) {
        visit(
                rocket,
                pod,
                deployed,
                decoupleTimer,
                shroudTimer,
                partialTick,
                pose,
                piece ->
                        collector.submitCustomGeometry(
                                pose,
                                RenderTypes.entityCutoutCull(piece.texture()),
                                (p, buffer) -> piece.render(p, buffer, light)));
    }

    public void visit(
            RocketStruct rocket,
            @Nullable PodPose pod,
            boolean deployed,
            int decoupleTimer,
            int shroudTimer,
            float partialTick,
            PoseStack pose,
            PieceVisitor visitor) {
        pose.pushPose();

        boolean hasShroud = false;

        for (RocketStage stage : rocket.stages) {
            RocketPart fuselage = stage.fuselage();
            RocketPart fins = stage.fins();
            RocketPart thruster = stage.thruster();
            int stack = stage.getStack();
            int cluster = stage.getCluster();

            if (deployed && thruster != null && fins != null && fins.height > thruster.height) {
                pose.translate(0, fins.height - thruster.height, 0);
            }

            for (int c = 0; c < cluster; c++) {
                pose.pushPose();

                if (decoupleTimer > 0) {
                    float decoupleLerp = decoupleTimer + partialTick;
                    pose.translate(0, -decoupleLerp, 0);
                    pose.mulPose((Quaternionfc) Axis.XP.rotationDegrees(decoupleLerp));
                }

                if (c > 0) {
                    float spin = (float) c / (float) (cluster - 1);
                    pose.mulPose((Quaternionfc) Axis.YP.rotationDegrees(360.0F * spin));

                    if (fuselage != null) {
                        pose.translate(RocketPart.radius(fuselage.bottom), 0, 0);
                    } else if (thruster != null) {
                        pose.translate(RocketPart.radius(thruster.top), 0, 0);
                    }
                }

                if (thruster != null) {
                    if (hasShroud && fuselage != null) {
                        if (shroudTimer > 0) {
                            float shroudLerp = shroudTimer + partialTick;
                            pose.pushPose();
                            pose.translate(0, -shroudLerp, 0);
                            pose.mulPose((Quaternionfc) Axis.XP.rotationDegrees(shroudLerp * 0.5F));
                        }

                        Identifier shroud = shroudOf(fuselage);
                        visitor.accept(
                                new Piece(
                                        shroud,
                                        mesh(shroud),
                                        -1,
                                        ResourceManager.universal_tex,
                                        (float) thruster.height));

                        if (shroudTimer > 0) pose.popPose();
                    }

                    if (!hasShroud || shroudTimer > 0) visitor.accept(piece(thruster, deployed));

                    pose.translate(0, thruster.height, 0);
                }

                if (fuselage != null) {
                    if (fins != null) visitor.accept(piece(fins, deployed));

                    for (int s = 0; s < stack; s++) {
                        visitor.accept(piece(fuselage, deployed));
                        pose.translate(0, fuselage.height, 0);
                    }
                }

                pose.popPose();
            }

            if (thruster != null) pose.translate(0, thruster.height, 0);
            if (fuselage != null) pose.translate(0, fuselage.height * stack, 0);

            deployed = false;
            decoupleTimer = 0;
            if (hasShroud) shroudTimer = 0;
            hasShroud = true;
        }

        if (rocket.capsule != null) {
            if (pod != null && rocket.capsule.item == MissilePartsSpace.RP_POD_20.get()) {
                visitPod(pod, pose, visitor);
            } else {
                visitor.accept(piece(rocket.capsule, false));
            }
        }

        pose.popPose();
    }

    private void visitPod(PodPose pod, PoseStack pose, PieceVisitor visitor) {
        HFRWavefrontObject mesh = mesh(DROP_POD);

        if (pod.inOrbit()) {
            visitor.accept(new Piece(DROP_POD, mesh, -1, DROP_POD_TEX, Float.NaN));
            return;
        }

        visitor.accept(new Piece(DROP_POD, mesh, mesh.partId("DropPod"), DROP_POD_TEX, Float.NaN));

        pose.pushPose();
        pose.translate(0.69291F, 2.8333F, 0);
        pose.mulPose((Quaternionfc) Axis.ZP.rotationDegrees(pod.door()));
        pose.translate(-0.69291F, -2.8333F, 0);
        visitor.accept(new Piece(DROP_POD, mesh, mesh.partId("Door"), DROP_POD_TEX, Float.NaN));
        pose.popPose();

        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.mulPose((Quaternionfc) Axis.YP.rotationDegrees(i * 90 - 45));
            pose.translate(0.46194, 3.5, 0);
            pose.mulPose((Quaternionfc) Axis.ZP.rotationDegrees(pod.airbrake()));
            pose.translate(-0.46194, -3.5, 0);
            pose.mulPose((Quaternionfc) Axis.YP.rotationDegrees(45));
            visitor.accept(
                    new Piece(DROP_POD, mesh, mesh.partId("Airbrake0"), DROP_POD_TEX, Float.NaN));
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0, -pod.legExtension() * 0.5, 0);
        visitor.accept(new Piece(DROP_POD, mesh, mesh.partId("Legs"), DROP_POD_TEX, Float.NaN));
        pose.popPose();
    }

    private Piece piece(RocketPart part, boolean deployed) {
        Identifier mesh =
                deployed && part.item == MissilePartsSpace.RP_LEGS_20.get()
                        ? LEGS_DEPLOYED
                        : meshOf(part);
        return new Piece(mesh, mesh(mesh), -1, skinOf(part), Float.NaN);
    }

    private static Identifier shroudOf(RocketPart fuselage) {
        if (fuselage.item == MissilePartsSpace.RP_FUSELAGE_20_3.get()
                || fuselage.item == MissilePartsSpace.RP_FUSELAGE_20_1.get()) {
            return SHROUD;
        }
        return meshOf(fuselage);
    }

    private static Identifier meshOf(RocketPart part) {
        if (!(part.item instanceof CustomMissilePartItem missile) || missile.mesh == null)
            return FAIRING;
        return missile.mesh;
    }

    private static Identifier skinOf(RocketPart part) {
        if (!(part.item instanceof CustomMissilePartItem missile) || missile.skin == null)
            return FAIRING_TEX;
        return missile.skin;
    }

    private HFRWavefrontObject mesh(Identifier id) {
        return meshes.computeIfAbsent(
                id,
                key ->
                        MissilePartsSpace.VERTEX_NORMAL_MESHES.contains(key)
                                ? Meshes.load(key)
                                : Meshes.faceNormals(key));
    }

    public HFRWavefrontObject machine(Identifier id, boolean flat) {
        return flat
                ? flatMeshes.computeIfAbsent(id, key -> Meshes.flatShaded(Meshes.load(key)))
                : meshes.computeIfAbsent(id, Meshes::load);
    }

    private static Identifier space(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }

    public record Piece(
            Identifier id, HFRWavefrontObject mesh, int group, Identifier texture, float clipTop) {

        public void render(PoseStack.Pose pose, VertexConsumer buffer, int light) {
            int first = group < 0 ? 0 : group;
            int last = group < 0 ? mesh.groups.length - 1 : group;
            for (int part = first; part <= last; part++) {
                if (Float.isNaN(clipTop)) {
                    mesh.renderPart(pose, buffer, light, -1, part);
                } else {
                    mesh.renderPartClipped(pose, buffer, light, -1, part, 0, -1, 0, -clipTop);
                }
            }
        }
    }

    @FunctionalInterface
    public interface PieceVisitor {
        void accept(Piece piece);
    }

    public record PodPose(boolean inOrbit, float door, float airbrake, float legExtension) {}
}
