/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.client.model;

import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.client.model.BlockModel;
import com.hbm.client.model.CarrierRoot;
import com.hbm.client.model.Meshes;
import com.hbm.client.model.ObjUnbakedGeometry;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbmspace.HbmSpace;
import com.hbmspace.blocks.machine.SolarBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public record SolarPanelModel() implements BlockModel<HFRWavefrontObject> {

    private static final Identifier OBJ =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "models/machines/solar_panel.obj");
    private static final String[] PANEL = {"solar_panel"};
    private static final String[] FRONT = {"connector_front"};
    private static final String[] SIDE = {"connector_side"};
    private static final String[] TOP = {"connector_top"};
    private static final String[] BOTTOM = {"connector_bottom"};

    private static Matrix4f pose(float yaw) {
        return new Matrix4f().translate(0.5F, 0F, 0.5F).rotateY((float) Math.toRadians(yaw));
    }

    @Override
    public HFRWavefrontObject prepare() {
        return Meshes.load(OBJ);
    }

    @Override
    public BlockStateModel.UnbakedRoot root(
            HFRWavefrontObject prepared, Block block, BlockState state) {
        return new Root(
                BlockModel.base(block),
                prepared,
                state.getValue(MultiblockCoreBlock.FACING),
                SolarBlock.arms(state));
    }

    public record Root(Identifier carrier, HFRWavefrontObject obj, Direction facing, int arms)
            implements CarrierRoot {

        private boolean arm(Direction dir) {
            return (arms & 1 << dir.ordinal()) != 0;
        }

        @Override
        public QuadCollection quads(
                BlockState blockState,
                ModelBaker baker,
                ResolvedModel carrier,
                TextureSlots slots) {
            Material.Baked material =
                    BlockModel.slot(baker, carrier, slots, ObjUnbakedGeometry.SINGLE_SLOT);
            List<ObjUnbakedGeometry.Group> groups = new ArrayList<>();
            groups.add(
                    ObjUnbakedGeometry.Group.rawSmooth(
                            obj, PANEL, material, pose(Facing.yaw(facing, 90))));
            if (arm(Direction.SOUTH))
                groups.add(ObjUnbakedGeometry.Group.rawCutout(obj, FRONT, material, pose(0)));
            if (arm(Direction.NORTH))
                groups.add(ObjUnbakedGeometry.Group.rawCutout(obj, SIDE, material, pose(0)));
            if (arm(Direction.EAST))
                groups.add(ObjUnbakedGeometry.Group.rawCutout(obj, FRONT, material, pose(90)));
            if (arm(Direction.WEST))
                groups.add(ObjUnbakedGeometry.Group.rawCutout(obj, SIDE, material, pose(90)));
            if (arm(Direction.UP))
                groups.add(ObjUnbakedGeometry.Group.rawCutout(obj, TOP, material, pose(0)));
            if (arm(Direction.DOWN))
                groups.add(ObjUnbakedGeometry.Group.rawCutout(obj, BOTTOM, material, pose(0)));
            return ObjUnbakedGeometry.bakeGroups(groups, 0F, ObjUnbakedGeometry.RAW_OFFSET_Y, 0F);
        }

        @Override
        public Object visualEqualityGroup(BlockState state) {
            return facing.ordinal() << 6 | arms;
        }
    }
}
