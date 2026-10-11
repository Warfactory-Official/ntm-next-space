// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

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
