// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class ModelMoonCow extends QuadrupedModel<LivingEntityRenderState> {

    public ModelMoonCow(ModelPart root) {
        super(root);
    }

    public ModelPart head() {
        return head;
    }

    private static MeshDefinition mesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -6.0F, 8, 8, 6)
                        .texOffs(22, 0)
                        .addBox(-5.0F, -5.0F, -4.0F, 1, 3, 1)
                        .texOffs(22, 0)
                        .addBox(4.0F, -5.0F, -4.0F, 1, 3, 1),
                PartPose.offset(0.0F, 4.0F, -8.0F));
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(18, 4)
                        .addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10)
                        .texOffs(52, 0)
                        .addBox(-2.0F, 2.0F, -8.0F, 4, 6, 1),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2.0D), 0.0F, 0.0F));
        CubeListBuilder leg =
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-4.0F, 12.0F, -6.0F));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(4.0F, 12.0F, -6.0F));
        return mesh;
    }

    public static LayerDefinition adult() {
        return LayerDefinition.create(mesh(), 64, 32);
    }

    public static LayerDefinition baby() {
        return adult().apply(CowModel.BABY_TRANSFORMER);
    }
}
