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

package com.hbmspace.client.render;

import com.hbm.client.model.Meshes;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.HbmSpace;
import net.minecraft.resources.Identifier;

public final class StationModels {

    public static final HFRWavefrontObject docking_port =
            Meshes.load(id("models/machines/docking_port.obj"));
    public static final HFRWavefrontObject orbital_computer =
            Meshes.load(id("models/machines/spaceship_computer.obj"));
    public static final HFRWavefrontObject xenon_thruster =
            Meshes.load(id("models/xenon_thruster.obj"));
    public static final HFRWavefrontObject htr3 = Meshes.load(id("models/machines/htr3.obj"));
    public static final HFRWavefrontObject htrf4_neo = Meshes.load(id("models/fusion/htrf4.obj"));
    public static final HFRWavefrontObject transporter_pad =
            Meshes.load(id("models/machines/transporter_pad.obj"));

    public static final Identifier docking_port_tex =
            id("textures/block/models/machines/docking_port.png");
    public static final Identifier docking_port_launcher_tex =
            id("textures/block/models/machines/docking_port_launcher.png");
    public static final Identifier orbital_computer_tex =
            id("textures/block/models/machines/spaceship_computer.png");
    public static final Identifier xenon_thruster_tex =
            id("textures/block/models/xenon_thruster.png");
    public static final Identifier xenon_exhaust_tex = id("textures/models/xenon_trail.png");
    public static final Identifier htrf4_neo_tex = id("textures/block/models/fusion/htrf4.png");
    public static final Identifier htrf4_exhaust_tex = id("textures/models/fusion/htrf4_trail.png");
    public static final Identifier transporter_pad_tex =
            id("textures/block/models/machines/transporter_pad.png");
    public static final Identifier miner_rocket_tex = id("textures/models/minerrocket.png");
    public static final Identifier gas_rocket_tex = id("textures/models/gasrocket.png");

    private StationModels() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }
}
