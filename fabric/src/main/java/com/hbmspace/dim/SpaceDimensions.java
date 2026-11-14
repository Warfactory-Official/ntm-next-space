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

package com.hbmspace.dim;

import com.hbmspace.HbmSpace;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class SpaceDimensions {

    public static final ResourceKey<Level> MUN = key("mun");
    public static final ResourceKey<Level> DUNA = key("duna");
    public static final ResourceKey<Level> IKE = key("ike");
    public static final ResourceKey<Level> EVE = key("eve");
    public static final ResourceKey<Level> DRES = key("dres");
    public static final ResourceKey<Level> MOHO = key("moho");
    public static final ResourceKey<Level> MINMUS = key("minmus");
    public static final ResourceKey<Level> LAYTHE = key("laythe");
    public static final ResourceKey<Level> ORBIT = key("orbit");
    public static final ResourceKey<Level> TEKTO = key("tekto");
    public static final ResourceKey<Level> THATMO = key("thatmo");

    private SpaceDimensions() {}

    private static ResourceKey<Level> key(String path) {
        return ResourceKey.create(
                Registries.DIMENSION, Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path));
    }
}
