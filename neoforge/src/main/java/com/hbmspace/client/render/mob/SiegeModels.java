// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.google.common.base.Suppliers;
import com.hbm.client.model.Meshes;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.mob.siege.SiegeTier;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;

final class SiegeModels {

    static final Supplier<HFRWavefrontObject> MINI_UFO = mesh("mini_ufo");
    static final Supplier<HFRWavefrontObject> SIEGE_UFO = mesh("siege_ufo");
    static final Supplier<HFRWavefrontObject> ANGEL_SMOOTH = mesh("balefire_angel");
    static final Supplier<HFRWavefrontObject> ANGEL =
            Suppliers.memoize(() -> Meshes.flatShaded(ANGEL_SMOOTH.get()));
    static final Identifier ANGEL_TEX = id("textures/entity/bfa_body.png");
    static final List<Identifier> UFO_SKINS = skins("ufo_siege_");
    static final List<Identifier> CRAFT_SKINS = skins("siege_craft_");

    private SiegeModels() {}

    private static Supplier<HFRWavefrontObject> mesh(String name) {
        return Suppliers.memoize(() -> Meshes.load(id("models/mobs/" + name + ".obj")));
    }

    private static List<Identifier> skins(String prefix) {
        return SiegeTier.TIERS.stream()
                .map(tier -> id("textures/entity/" + prefix + tier.name + ".png"))
                .toList();
    }

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }
}
