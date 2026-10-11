// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.material;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.MaterialShapeRoster;
import com.hbm.items.special.Autogen;
import com.hbm.items.special.MaterialShapeItem;
import com.hbm.lib.Library;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import static com.hbm.inventory.material.MaterialShapes.*;
import static com.hbm.inventory.material.Mats.MAT_LANTHANIUM;
import static com.hbm.inventory.material.Mats.MAT_MINGRADE;
import static com.hbm.inventory.material.Mats.MAT_PVC;
import static com.hbm.inventory.material.Mats.MAT_RUBBER;
import static com.hbmspace.inventory.material.MatsSpace.*;

public final class MaterialShapeItemsSpace {
    static {
        named("ingot_titanium", INGOT, MAT_NICKEL);
        named("ingot_red_copper", BILLET, MAT_MINGRADE);
        named("ingot_red_copper", INGOT, MAT_ZINC);
        named("ingot_tungsten", INGOT, MAT_GALLIUM);
        named("ingot_tungsten", INGOT, MAT_GAAS, Rarity.RARE);
        named("ingot_tungsten", INGOT, MAT_IRIDIUM, Rarity.RARE);
        named("ingot_steel", INGOT, MAT_STAINLESS);
        named("ingot_mud", INGOT, MAT_RICH_MAGMA);
        named("ingot_australium", INGOT, MAT_TASMANITE, Rarity.UNCOMMON);
        named("ingot_australium", INGOT, MAT_AYERITE, Rarity.UNCOMMON);
        named("ingot_osmiridium", INGOT, MAT_HAFNIUM);
        named("billet_gh336", BILLET, MAT_GAAS);
        named("powder_titanium", DUST, MAT_NICKEL);
        named("powder_copper", DUST, MAT_ZINC);
        named("powder_copper", DUSTTINY, MAT_GALLIUM);
        named("powder_copper", DUST, MAT_GALLIUM);
        named("powder_bakelite", DUST, MAT_RUBBER);
        named("powder_bakelite", DUST, MAT_PVC);
        named("nugget_zirconium", NUGGET, MAT_NICKEL);
        named("nugget_zirconium", NUGGET, MAT_GAAS);
        named("nugget_zirconium", NUGGET, MAT_ZINC);
        named("nugget_zirconium", NUGGET, MAT_GALLIUM);
        named("nugget_osmiridium", NUGGET, MAT_HAFNIUM);
        named("nugget_osmiridium", NUGGET, MAT_LANTHANIUM);
        named("plate_titanium", PLATE, MAT_NICKEL);
        named("plate_steel", PLATE, MAT_STAINLESS);

        Autogen.register(HbmSpace.MOD_ID);
        for (MaterialShapes shape : MaterialShapes.allShapes) {
            if (shape.autogenItemPrefix == null) continue;
            Identifier anchor = null;
            for (NTMMaterial mat : Mats.orderedList) {
                RegistryHandle<MaterialShapeItem> handle = Autogen.handle(shape, mat);
                if (handle == null) continue;
                if (!handle.id().getNamespace().equals(HbmSpace.MOD_ID)) {
                    anchor = handle.id();
                } else if (anchor == null) {
                    throw new IllegalStateException(
                            handle.id() + " precedes every NTM item of its family");
                } else {
                    CreativeContents.insertAfter(
                            Tab.PARTS, anchor, () -> new ItemStack(handle.get()));
                }
            }
        }
    }

    private MaterialShapeItemsSpace() {}

    private static void named(String anchor, MaterialShapes shape, NTMMaterial mat) {
        named(anchor, shape, mat, Item.Properties::new);
    }

    private static void named(String anchor, MaterialShapes shape, NTMMaterial mat, Rarity rarity) {
        named(anchor, shape, mat, () -> new Item.Properties().rarity(rarity));
    }

    private static void named(
            String anchor, MaterialShapes shape, NTMMaterial mat, Supplier<Item.Properties> props) {
        RegistryHandle<MaterialShapeItem> handle =
                MaterialShapeRoster.registerNamed(HbmSpace.MOD_ID, shape, mat, props);
        CreativeContents.insertAfter(
                Tab.PARTS, Library.id(anchor), () -> new ItemStack(handle.get()));
    }
}
