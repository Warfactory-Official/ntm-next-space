// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.special.ItemCustomLore;
import com.hbm.lib.Library;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import static com.hbmspace.inventory.OreDictManagerSpace.*;

public final class PartsItemsSpace {
    public static final Reg.Handle<Item> INGOT_CM242 = plain("ingot_cm242");
    public static final Reg.Handle<Item> INGOT_CM243 = plain("ingot_cm243");
    public static final Reg.Handle<Item> INGOT_CM244 = plain("ingot_cm244");
    public static final Reg.Handle<Item> INGOT_CM245 = plain("ingot_cm245");
    public static final Reg.Handle<Item> INGOT_CM246 = plain("ingot_cm246");
    public static final Reg.Handle<Item> INGOT_CM247 = plain("ingot_cm247");
    public static final Reg.Handle<Item> INGOT_CM_FUEL = plain("ingot_cm_fuel");
    public static final Reg.Handle<Item> INGOT_CM_MIX = plain("ingot_cm_mix");
    public static final Reg.Handle<Item> INGOT_BK247 = plain("ingot_bk247");
    public static final Reg.Handle<Item> INGOT_CF251 = plain("ingot_cf251");
    public static final Reg.Handle<Item> INGOT_CF252 = plain("ingot_cf252");
    public static final Reg.Handle<Item> INGOT_ES253 = plain("ingot_es253");
    public static final Reg.Handle<Item> INGOT_ES255 = plain("ingot_es255");
    public static final Reg.Handle<ItemCustomLore> INGOT_TT =
            lore("ingot_tt", "ingot_techtactium", Rarity.UNCOMMON);
    public static final Reg.Handle<ItemCustomLore> INGOT_TTAS =
            lore("ingot_ttas", "ingot_techtactium_as", Rarity.RARE);
    public static final Reg.Handle<ItemCustomLore> INGOT_PALLADIUM =
            lore("ingot_palladium", "ingot_palladium", Rarity.COMMON);
    public static final Reg.Handle<Item> INGOT_MENTHOL = plain("ingot_menthol");
    public static final Reg.Handle<ItemCustomLore> INGOT_CN989 =
            lore("ingot_cn989", "ingot_cn989", Rarity.EPIC);
    public static final Reg.Handle<ItemCustomLore> INGOT_GWENIUM =
            lore("ingot_gwenium", "ingot_gwenium", Rarity.COMMON);
    public static final Reg.Handle<Item> BILLET_BK247 = plain("billet_bk247");
    public static final Reg.Handle<Item> BILLET_CM242 = plain("billet_cm242");
    public static final Reg.Handle<Item> BILLET_CM243 = plain("billet_cm243");
    public static final Reg.Handle<Item> BILLET_CM244 = plain("billet_cm244");
    public static final Reg.Handle<Item> BILLET_CM245 = plain("billet_cm245");
    public static final Reg.Handle<Item> BILLET_CM246 = plain("billet_cm246");
    public static final Reg.Handle<Item> BILLET_CM247 = plain("billet_cm247");
    public static final Reg.Handle<Item> BILLET_CM_FUEL = plain("billet_cm_fuel");
    public static final Reg.Handle<Item> BILLET_CM_MIX = plain("billet_cm_mix");
    public static final Reg.Handle<Item> BILLET_CF251 = plain("billet_cf251");
    public static final Reg.Handle<Item> BILLET_CF252 = plain("billet_cf252");
    public static final Reg.Handle<Item> BILLET_ES253 = plain("billet_es253");
    public static final Reg.Handle<ItemCustomLore> BILLET_CN989 =
            lore("billet_cn989", "billet_cn989", Rarity.EPIC);
    public static final Reg.Handle<Item> BILLET_MENTHOL = plain("billet_menthol");
    public static final Reg.Handle<Item> WOODEMIUM_BRIQUETTE =
            plain("woodemium_briquette", "briquette_woodemium");
    public static final Reg.Handle<ItemCustomLore> POWDER_CN989 =
            lore("powder_cn989", "powder_cn989", Rarity.EPIC);
    public static final Reg.Handle<ItemCustomLore> POWDER_WD2004_TINY =
            lore("powder_wd2004_tiny", "powder_wd2004_tiny", Rarity.EPIC);
    public static final Reg.Handle<ItemCustomLore> POWDER_WD2004 =
            lore("powder_wd2004", "powder_wd2004", Rarity.EPIC);
    public static final Reg.Handle<Item> NICKEL_SALTS = plain("nickel_salts");
    public static final Reg.Handle<Item> AMMONIUM_NITRATE = plain("ammonium_nitrate");
    public static final Reg.Handle<Item> BALL_FERRIC_CLAY =
            plain("ball_ferric_clay", "ferric_clay");
    public static final Reg.Handle<ItemCustomLore> CRYSTAL_MINERAL =
            lore("crystal_mineral", "crystal_mineral", Rarity.COMMON);
    public static final Reg.Handle<ItemCustomLore> CRYSTAL_CLEANED =
            lore("crystal_cleaned", "crystal_mineralcf", Rarity.COMMON);
    public static final Reg.Handle<ItemCustomLore> MINERAL_DUST =
            lore("mineral_dust", "powder_mineral", Rarity.COMMON);
    public static final Reg.Handle<Item> CRYSTAL_NICKEL = plain("crystal_nickel");
    public static final Reg.Handle<Item> CRYSTAL_NIOBIUM = plain("crystal_niobium");
    public static final Reg.Handle<Item> CRYSTAL_ZINC = plain("crystal_zinc");
    public static final Reg.Handle<Item> FLESH = plain("flesh");
    public static final Reg.Handle<Item> FLESH_WAFER = food("flesh_wafer", "blood_wafer", 5, 0.6F);
    public static final Reg.Handle<Item> GRILLED_FLESH =
            food("grilled_flesh", "blood_patty", 6, 0.8F);
    public static final Reg.Handle<ItemCustomLore> FLESH_BURGER =
            HbmSpace.REG.item(
                    "flesh_burger",
                    ItemCustomLore::new,
                    () ->
                            new Item.Properties()
                                    .food(
                                            new FoodProperties.Builder()
                                                    .nutrition(7)
                                                    .saturationModifier(1.0F)
                                                    .build()));
    public static final Reg.Handle<ItemCustomLore> STICK_PVC =
            lore("stick_pvc", "stick_pvc", Rarity.COMMON);
    public static final Reg.Handle<ItemCustomLore> STICK_VINYL =
            lore("stick_vinyl", "stick_vinyl", Rarity.COMMON);
    public static final Reg.Handle<Item> NUGGET_BK247 = plain("nugget_bk247");
    public static final Reg.Handle<Item> NUGGET_CM_MIX = plain("nugget_cm_mix");
    public static final Reg.Handle<Item> NUGGET_CM242 = plain("nugget_cm242");
    public static final Reg.Handle<Item> NUGGET_CM243 = plain("nugget_cm243");
    public static final Reg.Handle<Item> NUGGET_CM244 = plain("nugget_cm244");
    public static final Reg.Handle<Item> NUGGET_CM245 = plain("nugget_cm245");
    public static final Reg.Handle<Item> NUGGET_CM246 = plain("nugget_cm246");
    public static final Reg.Handle<Item> NUGGET_CM247 = plain("nugget_cm247");
    public static final Reg.Handle<Item> NUGGET_CM_FUEL = plain("nugget_cm_fuel");
    public static final Reg.Handle<Item> NUGGET_CF251 = plain("nugget_cf251");
    public static final Reg.Handle<Item> NUGGET_CF252 = plain("nugget_cf252");
    public static final Reg.Handle<Item> NUGGET_ES253 = plain("nugget_es253");
    public static final Reg.Handle<ItemCustomLore> NUGGET_CN989 =
            lore("nugget_cn989", "nugget_cn989", Rarity.EPIC);
    public static final Reg.Handle<Item> NUGGET_MENTHOL = plain("nugget_menthol");
    public static final Reg.Handle<ItemCustomLore> PLATE_CN989 =
            lore("plate_cn989", "plate_cn989", Rarity.EPIC);
    public static final Reg.Handle<Item> RAG_BLOOD = plain("rag_blood");
    public static final Reg.Handle<Item> BERYLLIUM_MIRROR = plain("beryllium_mirror");
    public static final Reg.Handle<Item> BLADE_SYNGAS = plain("blade_syngas");
    public static final Reg.Handle<Item> TURBINE_SYNGAS = plain("turbine_syngas");

    static {
        after(
                "ingot_am_mix",
                INGOT_CM242,
                INGOT_CM243,
                INGOT_CM244,
                INGOT_CM245,
                INGOT_CM246,
                INGOT_CM247,
                INGOT_CM_FUEL,
                INGOT_CM_MIX,
                INGOT_BK247,
                INGOT_CF251,
                INGOT_CF252,
                INGOT_ES253,
                INGOT_ES255);
        after("hbmspace:ingot_gaas", INGOT_TT, INGOT_TTAS);
        after("hbmspace:ingot_iridium", INGOT_PALLADIUM);
        after("ingot_bscco", INGOT_MENTHOL);
        after("ingot_gh336", INGOT_CN989);
        after("ingot_electronium", INGOT_GWENIUM);
        after(
                "billet_am_mix",
                BILLET_BK247,
                BILLET_CM242,
                BILLET_CM243,
                BILLET_CM244,
                BILLET_CM245,
                BILLET_CM246,
                BILLET_CM247,
                BILLET_CM_FUEL,
                BILLET_CM_MIX,
                BILLET_CF251,
                BILLET_CF252,
                BILLET_ES253,
                BILLET_CN989);
        after("billet_nuclear_waste", BILLET_MENTHOL);
        after("briquette_wood", WOODEMIUM_BRIQUETTE);
        after("powder_cs137_tiny", POWDER_CN989);
        after("powder_euphemium", POWDER_WD2004_TINY, POWDER_WD2004);
        after("powder_cement", NICKEL_SALTS);
        after("cordite", AMMONIUM_NITRATE);
        after("ball_fireclay", BALL_FERRIC_CLAY);
        after("gem_alexandrite", CRYSTAL_NICKEL, CRYSTAL_NIOBIUM, CRYSTAL_ZINC);
        after("bio_wafer", FLESH, FLESH_WAFER, GRILLED_FLESH, FLESH_BURGER, STICK_PVC, STICK_VINYL);
        after(
                "nugget_am_mix",
                NUGGET_BK247,
                NUGGET_CM_MIX,
                NUGGET_CM242,
                NUGGET_CM243,
                NUGGET_CM244,
                NUGGET_CM245,
                NUGGET_CM246,
                NUGGET_CM247,
                NUGGET_CM_FUEL,
                NUGGET_CF251,
                NUGGET_CF252,
                NUGGET_ES253,
                NUGGET_CN989);
        after("nugget_lead", NUGGET_MENTHOL);
        after("plate_dineutronium", PLATE_CN989);
        after("rag_piss", RAG_BLOOD);
        after("photo_panel", BERYLLIUM_MIRROR);
        after("blade_titanium", BLADE_SYNGAS);
        after("turbine_tungsten", TURBINE_SYNGAS);
    }

    private PartsItemsSpace() {}

    private static Reg.Handle<Item> plain(String name) {
        return HbmSpace.REG.item(name, Item::new, Item.Properties::new);
    }

    private static Reg.Handle<Item> plain(String name, String texture) {
        return plain(name);
    }

    private static Reg.Handle<Item> food(
            String name, String texture, int nutrition, float saturation) {
        return HbmSpace.REG.item(
                name,
                Item::new,
                () ->
                        new Item.Properties()
                                .food(
                                        new FoodProperties.Builder()
                                                .nutrition(nutrition)
                                                .saturationModifier(saturation)
                                                .build()));
    }

    private static Reg.Handle<ItemCustomLore> lore(String name, String texture, Rarity rarity) {
        Reg.Handle<ItemCustomLore> handle =
                HbmSpace.REG.item(
                        name, ItemCustomLore::new, () -> new Item.Properties().rarity(rarity));
        return texture.equals(name) ? handle : handle;
    }

    @SafeVarargs
    private static void after(String anchor, Reg.Handle<? extends Item>... entries) {
        Identifier id = anchor.contains(":") ? Identifier.parse(anchor) : Library.id(anchor);
        for (Reg.Handle<? extends Item> entry : entries) {
            CreativeContents.insertAfter(Tab.PARTS, id, () -> new ItemStack(entry.get()));
        }
    }
}
