// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.items.weapon;

import com.hbm.items.CreativeContents.Tab;
import com.hbm.items.CreativeContents;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.lib.Library;
import com.hbm.registration.ItemFamily;
import com.hbm.registration.Reg;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.items.ItemVOTVdrive;
import com.hbmspace.items.ModDataComponentsSpace;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MissilePartsSpace {

    private static final String NTM = "hbm:";
    private static final String SPACE = HbmSpace.MOD_ID + ":";

    public static final Set<Identifier> VERTEX_NORMAL_MESHES =
            Set.of(
                    meshOf(NTM + "mp_f_10_kerosene"),
                    meshOf(NTM + "mp_f_10_long_kerosene"),
                    meshOf(NTM + "mp_f_10_15_kerosene"),
                    meshOf(NTM + "mp_f_15_kerosene"),
                    meshOf(NTM + "mp_f_15_hydrogen"),
                    meshOf(NTM + "mp_f_15_20_kerosene"),
                    meshOf(SPACE + "mp_f_20_usa"),
                    meshOf(SPACE + "mp_f_20_6_usa"),
                    meshOf(SPACE + "mp_f_20_3_usa"),
                    meshOf(SPACE + "mp_f_20_1_usa"),
                    meshOf(SPACE + "mp_f_20_neo"),
                    meshOf(SPACE + "mp_w_fairing"),
                    meshOf(SPACE + "rp_landing_capsule"),
                    meshOf(SPACE + "rp_drop_pod"));

    public static final Reg.Handle<ItemCustomMissilePart> MP_THRUSTER_10_HYDRAZINE =
            part(
                    "mp_thruster_10_hydrazine",
                    "mp_thruster",
                    NTM + "mp_t_10_solid",
                    NTM + thruster("mp_t_10_solid"),
                    0.5F,
                    1F,
                    true,
                    p ->
                            p.makeThruster(MissileKindsSpace.HYDRAZINE, 1F, 1.5F, PartSize.SIZE_10)
                                    .setHealth(15F));
    public static final Reg.Handle<ItemCustomMissilePart> MP_THRUSTER_20_METHALOX =
            part(
                    "mp_thruster_20_methalox",
                    "mp_thruster",
                    NTM + "mp_t_20_kerosene",
                    SPACE + thruster("mp_t_20_methalox"),
                    2F,
                    2F,
                    true,
                    p ->
                            p.makeThruster(MissileKindsSpace.METHALOX, 1F, 100F, PartSize.SIZE_20)
                                    .setHealth(30F));
    public static final Reg.Handle<ItemCustomMissilePart> MP_THRUSTER_20_METHALOX_DUAL =
            part(
                    "mp_thruster_20_methalox_dual",
                    "mp_thruster",
                    NTM + "mp_t_20_kerosene_dual",
                    SPACE + thruster("mp_t_20_methalox_dual"),
                    2F,
                    2F,
                    true,
                    p ->
                            p.makeThruster(MissileKindsSpace.METHALOX, 1F, 100F, PartSize.SIZE_20)
                                    .setHealth(30F));
    public static final Reg.Handle<ItemCustomMissilePart> MP_THRUSTER_20_METHALOX_TRIPLE =
            part(
                    "mp_thruster_20_methalox_triple",
                    "mp_thruster",
                    NTM + "mp_t_20_kerosene_triple",
                    SPACE + thruster("mp_t_20_methalox_dual"),
                    2F,
                    2F,
                    true,
                    p ->
                            p.makeThruster(MissileKindsSpace.METHALOX, 1F, 100F, PartSize.SIZE_20)
                                    .setHealth(30F));
    public static final Reg.Handle<ItemCustomMissilePart> MP_THRUSTER_20_HYDROGEN =
            part(
                    "mp_thruster_20_hydrogen",
                    "mp_thruster",
                    NTM + "mp_t_20_kerosene",
                    SPACE + thruster("mp_t_20_hydrogen"),
                    2F,
                    2F,
                    true,
                    p ->
                            p.makeThruster(FuelType.HYDROGEN, 1F, 100F, PartSize.SIZE_20)
                                    .setHealth(30F));
    public static final Reg.Handle<ItemCustomMissilePart> MP_THRUSTER_20_HYDROGEN_DUAL =
            part(
                    "mp_thruster_20_hydrogen_dual",
                    "mp_thruster",
                    NTM + "mp_t_20_kerosene_dual",
                    SPACE + thruster("mp_t_20_hydrogen_dual"),
                    2F,
                    2F,
                    true,
                    p ->
                            p.makeThruster(FuelType.HYDROGEN, 1F, 100F, PartSize.SIZE_20)
                                    .setHealth(30F));
    public static final Reg.Handle<ItemCustomMissilePart> MP_THRUSTER_20_HYDROGEN_TRIPLE =
            part(
                    "mp_thruster_20_hydrogen_triple",
                    "mp_thruster",
                    NTM + "mp_t_20_kerosene_triple",
                    SPACE + thruster("mp_t_20_hydrogen_dual"),
                    2F,
                    2F,
                    true,
                    p ->
                            p.makeThruster(FuelType.HYDROGEN, 1F, 100F, PartSize.SIZE_20)
                                    .setHealth(30F));
    public static final Reg.Handle<ItemCustomMissilePart> MP_THRUSTER_20_HYDRAZINE =
            part(
                    "mp_thruster_20_hydrazine",
                    "mp_thruster",
                    SPACE + "mp_t_20_hydrazine",
                    SPACE + thruster("mp_t_20_hydrazine"),
                    3F,
                    2.5F,
                    true,
                    p ->
                            p.makeThruster(MissileKindsSpace.HYDRAZINE, 1F, 100F, PartSize.SIZE_20)
                                    .setHealth(35F));

    public static final Reg.Handle<ItemCustomMissilePart> MP_FUSELAGE_10_HYDRAZINE =
            part(
                    "mp_fuselage_10_hydrazine",
                    "mp_fuselage",
                    NTM + "mp_f_10_kerosene",
                    SPACE + fuselage("mp_f_10_hydrazine"),
                    4F,
                    3F,
                    true,
                    p ->
                            p.makeFuselage(
                                            MissileKindsSpace.HYDRAZINE,
                                            2000F,
                                            PartSize.SIZE_10,
                                            PartSize.SIZE_10)
                                    .setHealth(25F));

    public static final Reg.Handle<ItemCustomMissilePart> RP_FUSELAGE_20_12 =
            part(
                    "rp_f_20_12",
                    "mp_fuselage",
                    SPACE + "mp_f_20_usa",
                    SPACE + fuselage("mp_f_20_kerosene_usa"),
                    12F,
                    8F,
                    true,
                    p ->
                            p.makeFuselage(
                                    MissileKindsSpace.ANY,
                                    64_000F,
                                    PartSize.SIZE_20,
                                    PartSize.SIZE_20));
    public static final Reg.Handle<ItemCustomMissilePart> RP_FUSELAGE_20_6 =
            part(
                    "rp_f_20_6",
                    "mp_fuselage",
                    SPACE + "mp_f_20_6_usa",
                    SPACE + fuselage("mp_f_20_kerosene_usa"),
                    6F,
                    4.5F,
                    true,
                    p ->
                            p.makeFuselage(
                                    MissileKindsSpace.ANY,
                                    32_000F,
                                    PartSize.SIZE_20,
                                    PartSize.SIZE_20));
    public static final Reg.Handle<ItemCustomMissilePart> RP_FUSELAGE_20_3 =
            part(
                    "rp_f_20_3",
                    "mp_fuselage",
                    SPACE + "mp_f_20_3_usa",
                    SPACE + fuselage("mp_f_20_kerosene"),
                    3F,
                    2.5F,
                    true,
                    p ->
                            p.makeFuselage(
                                    MissileKindsSpace.ANY,
                                    16_000F,
                                    PartSize.SIZE_20,
                                    PartSize.SIZE_20));
    public static final Reg.Handle<ItemCustomMissilePart> RP_FUSELAGE_20_1 =
            part(
                    "rp_f_20_1",
                    "mp_fuselage",
                    SPACE + "mp_f_20_1_usa",
                    SPACE + fuselage("mp_f_20_kerosene"),
                    1F,
                    1.5F,
                    true,
                    p ->
                            p.makeFuselage(
                                    MissileKindsSpace.ANY,
                                    6_000F,
                                    PartSize.SIZE_20,
                                    PartSize.SIZE_20));
    public static final Reg.Handle<ItemCustomMissilePart> RP_FUSELAGE_20_12_HYDRAZINE =
            part(
                    "mp_fuselage_20_hydrazine",
                    "mp_fuselage",
                    SPACE + "mp_f_20_neo",
                    SPACE + fuselage("mp_f_20_hydrazine"),
                    10F,
                    8F,
                    true,
                    p ->
                            p.makeFuselage(
                                            MissileKindsSpace.HYDRAZINE,
                                            12_500F,
                                            PartSize.SIZE_20,
                                            PartSize.SIZE_20)
                                    .setHealth(25F));
    public static final Reg.Handle<ItemCustomMissilePart> RP_LEGS_20 =
            part(
                    "rp_l_20",
                    "mp_fuselage",
                    SPACE + "rp_s_20_leggy",
                    NTM + "textures/models/thegadget3_.png",
                    2.4F,
                    3F,
                    false,
                    p -> p.makeStability(0F, PartSize.SIZE_20));
    public static final Reg.Handle<ItemCustomMissilePart> RP_CAPSULE_20 =
            part(
                    "rp_c_20",
                    "mp_warhead",
                    SPACE + "rp_landing_capsule",
                    SPACE + warhead("rp_landing_capsule"),
                    3.5F,
                    2.25F,
                    false,
                    p -> p.makeWarhead(MissileKindsSpace.APOLLO, 15F, 8F, PartSize.SIZE_20));
    public static final Reg.Handle<ItemCustomMissilePart> RP_STATION_CORE_20 =
            part(
                    "rp_sc_20",
                    "mp_warhead",
                    SPACE + "mp_w_fairing",
                    SPACE + warhead("mp_w_fairing"),
                    7F,
                    5F,
                    false,
                    p -> p.makeWarhead(MissileKindsSpace.SATELLITE, 15F, 64F, PartSize.SIZE_20));
    public static final Reg.Handle<ItemCustomMissilePart> RP_POD_20 =
            part(
                    "rp_pod_20",
                    "mp_warhead",
                    SPACE + "rp_drop_pod",
                    SPACE + warhead("rp_drop_pod"),
                    3F,
                    2.25F,
                    false,
                    p -> p.makeWarhead(MissileKindsSpace.APOLLO, 15F, 4F, PartSize.SIZE_20));

    public static final Reg.Handle<ItemCustomRocket> ROCKET_CUSTOM =
            HbmSpace.REG.item(
                    "rocket_custom",
                    ItemCustomRocket::new,
                    () -> new Item.Properties().stacksTo(1));

    public static final Reg.Handle<Item> HARD_DRIVE =
            HbmSpace.REG.item("hard_drive", Item::new, Item.Properties::new);

    public static final ItemFamily<SolarSystem.Body, ItemVOTVdrive> HARD_DRIVE_FULL =
            HbmSpace.REG.family(
                    "hard_drive_full",
                    SolarSystem.Body.class,
                    ItemVOTVdrive::new,
                    () -> new Item.Properties().stacksTo(1));

    static {
        Identifier after = Library.id("mp_thruster_10_solid");
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(MP_THRUSTER_10_HYDRAZINE));
        after = Library.id("mp_thruster_20_kerosene_triple");
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(MP_THRUSTER_20_METHALOX));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(MP_THRUSTER_20_METHALOX_DUAL));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(MP_THRUSTER_20_METHALOX_TRIPLE));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(MP_THRUSTER_20_HYDROGEN));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(MP_THRUSTER_20_HYDROGEN_DUAL));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(MP_THRUSTER_20_HYDROGEN_TRIPLE));
        CreativeContents.insertAfter(
                Tab.MISSILES,
                Library.id("mp_thruster_20_solid_multier"),
                stack(MP_THRUSTER_20_HYDRAZINE));
        CreativeContents.insertAfter(
                Tab.MISSILES, Library.id("mp_fuselage_10_solid"), stack(MP_FUSELAGE_10_HYDRAZINE));
        after = Library.id("mp_c_5");
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_FUSELAGE_20_12));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_FUSELAGE_20_6));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_FUSELAGE_20_3));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_FUSELAGE_20_1));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_FUSELAGE_20_12_HYDRAZINE));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_LEGS_20));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_CAPSULE_20));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_STATION_CORE_20));
        CreativeContents.insertAfter(Tab.MISSILES, after, stack(RP_POD_20));
        CreativeContents.insertAfter(Tab.PARTS, Library.id("part_grip_pvc"), stack(HARD_DRIVE));
        for (SolarSystem.Body body : SolarSystem.Body.values()) {
            CreativeContents.insertBefore(
                    Tab.CONTROL,
                    Library.id("pellet_rtg_radium"),
                    () -> {
                        ItemStack stack = HARD_DRIVE_FULL.stack(body);
                        stack.set(
                                ModDataComponentsSpace.DRIVE.get(),
                                new ItemVOTVdrive.Data(
                                        1, 0, Optional.empty(), true, false, "", "", false));
                        return stack;
                    });
        }
    }

    private MissilePartsSpace() {}

    private static Reg.Handle<ItemCustomMissilePart> part(
            String name,
            String particle,
            String mesh,
            String skin,
            float height,
            float guiHeight,
            boolean stacks,
            UnaryOperator<ItemCustomMissilePart> build) {
        Identifier meshId = meshOf(mesh);
        Reg.Handle<ItemCustomMissilePart> h =
                HbmSpace.REG.item(
                        name,
                        props ->
                                build.apply(new ItemCustomMissilePart(props))
                                        .setAssembly(
                                                height, guiHeight, meshId, Identifier.parse(skin)),
                        stacks ? Item.Properties::new : () -> new Item.Properties().stacksTo(1));
        return h;
    }

    public static float fuselageLift(float guiHeight) {
        return 2.5F - 5F / guiHeight;
    }

    private static Identifier meshOf(String mesh) {
        int colon = mesh.indexOf(':');
        return Identifier.parse(
                mesh.substring(0, colon + 1)
                        + "models/missile_parts/"
                        + mesh.substring(colon + 1)
                        + ".obj");
    }

    private static String thruster(String name) {
        return "textures/models/missile_parts/thrusters/" + name + ".png";
    }

    private static String fuselage(String name) {
        return "textures/models/missile_parts/fuselages/" + name + ".png";
    }

    private static String warhead(String name) {
        return "textures/models/missile_parts/warheads/" + name + ".png";
    }

    private static Supplier<ItemStack> stack(Reg.Handle<? extends Item> handle) {
        return () -> new ItemStack(handle.get());
    }
}
