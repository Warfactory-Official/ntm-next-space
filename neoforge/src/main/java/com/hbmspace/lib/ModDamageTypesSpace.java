// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.lib;

import com.hbmspace.HbmSpace;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypesSpace {

    public static final ResourceKey<DamageType> EVE = key("eve");
    public static final ResourceKey<DamageType> OXYPRIME = key("oxyprime");
    public static final ResourceKey<DamageType> OILEDUP = key("oiledup");
    public static final ResourceKey<DamageType> RUN = key("run");
    public static final ResourceKey<DamageType> NITAN = key("nitan");

    private ModDamageTypesSpace() {}

    private static ResourceKey<DamageType> key(String id) {
        return ResourceKey.create(
                Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, id));
    }
}
