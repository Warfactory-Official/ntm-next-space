// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.handler.FuelHandler;
import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.ItemExpensive;
import com.hbm.items.ModItems;
import com.hbm.items.machine.EnumCokeType;
import com.hbm.lib.Library;
import com.hbm.registration.ItemFamily;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.items.special.ItemMineralOreSpace;
import java.util.Locale;
import mov.movblock.tenon.strip.api.DropSafe;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.hbm.inventory.OreDictManager.COALCOKE;
import static com.hbm.inventory.OreDictManager.LIGCOKE;
import static com.hbm.inventory.OreDictManager.PETCOKE;
import static com.hbmspace.inventory.OreDictManagerSpace.PENTLANDITE;

public final class ComponentItemsSpace {
    public static final Reg.Handle<Item> CIRCUIT_GAAS = circuit("gaas");
    public static final Reg.Handle<Item> CIRCUIT_AVIONICS = circuit("avionics");
    public static final Reg.Handle<Item> CIRCUIT_AERO = circuit("aero");
    public static final Reg.Handle<Item> CIRCUIT_CAPACITOR_LANTHANIUM =
            circuit("capacitor_lanthanium");
    public static final Reg.Handle<Item> CIRCUIT_PROCESST1 = circuit("processt1");
    public static final Reg.Handle<Item> CIRCUIT_PROCESST2 = circuit("processt2");
    public static final Reg.Handle<Item> CIRCUIT_PROCESST3 = circuit("processt3");
    public static final Reg.Handle<Item> CIRCUIT_GASCHIP = circuit("gaschip");
    public static final Reg.Handle<Item> CIRCUIT_HFCHIP = circuit("hfchip");
    public static final Reg.Handle<Item> CIRCUIT_MOLYCHIP = circuit("molychip");
    public static final ItemFamily<ChunkType, Item> CHUNK_ORE =
            HbmSpace.REG
                    .family(
                            "chunk_ore",
                            ChunkType.class,
                            (props, type) -> new Item(props),
                            Item.Properties::new)
                    .joins(ModItems.CHUNK_ORE);
    public static final ItemFamily<ExpensiveType, ItemExpensive> ITEM_EXPENSIVE =
            HbmSpace.REG
                    .family(
                            "item_expensive",
                            ExpensiveType.class,
                            (props, type) -> new ItemExpensive(props),
                            Item.Properties::new)
                    .joins(ModItems.ITEM_EXPENSIVE);
    public static final ItemFamily<EnumCokeType, Item> POWDER_COKE =
            HbmSpace.REG.family(
                    "powder_coke",
                    EnumCokeType.class,
                    (props, type) -> new Item(props),
                    Item.Properties::new);
    public static final ItemFamily<ItemMineralOreSpace.Clump, ItemMineralOreSpace> CLUMP =
            HbmSpace.REG.family(
                    ItemMineralOreSpace.Clump.class,
                    clump -> "clump_" + clump.name().toLowerCase(Locale.ROOT),
                    (props, clump) -> new ItemMineralOreSpace(props, clump),
                    Item.Properties::new);

    @DropSafe
    private static TagKey<Item> cokeDust(EnumCokeType type) {
        return switch (type) {
            case COAL -> COALCOKE.dust();
            case LIGNITE -> LIGCOKE.dust();
            case PETROLEUM -> PETCOKE.dust();
        };
    }

    static {
        after("circuit_bismoid", CIRCUIT_AVIONICS, CIRCUIT_AERO, CIRCUIT_GAAS);
        after(
                "circuit_controller_quantum",
                CIRCUIT_CAPACITOR_LANTHANIUM,
                CIRCUIT_PROCESST1,
                CIRCUIT_PROCESST2,
                CIRCUIT_PROCESST3,
                CIRCUIT_GASCHIP,
                CIRCUIT_HFCHIP,
                CIRCUIT_MOLYCHIP);
        after("chunk_ore_cryolite", CHUNK_ORE.handle(ChunkType.PENTLANDITE));
        after(
                "item_expensive_degenerate_matter",
                ITEM_EXPENSIVE.handle(ExpensiveType.STAINLESS_BEAM),
                ITEM_EXPENSIVE.handle(ExpensiveType.AVIONICS),
                ITEM_EXPENSIVE.handle(ExpensiveType.ASTRO));
        after(
                "coke_petroleum",
                POWDER_COKE.handle(EnumCokeType.COAL),
                POWDER_COKE.handle(EnumCokeType.LIGNITE),
                POWDER_COKE.handle(EnumCokeType.PETROLEUM));
        for (ItemMineralOreSpace.Clump clump : ItemMineralOreSpace.Clump.values()) {
            CreativeContents.insertAfter(
                    Tab.PARTS,
                    Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "mineral_dust"),
                    () -> new ItemStack(CLUMP.get(clump)));
        }
        FuelHandler.addon(
                out -> {
                    for (EnumCokeType type : EnumCokeType.values())
                        out.accept(POWDER_COKE.get(type), 200 * 16);
                });
    }

    private ComponentItemsSpace() {}

    private static Reg.Handle<Item> circuit(String type) {
        return HbmSpace.REG.item("circuit_" + type, Item::new, Item.Properties::new);
    }

    @SafeVarargs
    private static void after(String anchor, Reg.Handle<? extends Item>... entries) {
        for (Reg.Handle<? extends Item> entry : entries) {
            CreativeContents.insertAfter(
                    Tab.PARTS, Library.id(anchor), () -> new ItemStack(entry.get()));
        }
    }

    public enum ChunkType {
        PENTLANDITE
    }

    public enum ExpensiveType {
        STAINLESS_BEAM,
        AVIONICS,
        ASTRO
    }
}
