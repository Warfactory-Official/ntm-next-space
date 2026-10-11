// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.inventory.fluid;

import com.hbm.inventory.fluid.NTMFluids;
import com.hbmspace.HbmSpace;
import net.minecraft.world.level.material.Fluid;

public final class FluidsSpace {
    static {
        NTMFluids.register(HbmSpace.MOD_ID, "NITROGEN", "MERCURY");
        NTMFluids.register(HbmSpace.MOD_ID, "AMMONIA", "HCL");
        NTMFluids.register(HbmSpace.MOD_ID, "HYDRAZINE", "NITAN");
        NTMFluids.register(HbmSpace.MOD_ID, "BLOODGAS", "BLOOD");
        NTMFluids.register(HbmSpace.MOD_ID, "EARTHAIR", "CHLOROETHANE");
        NTMFluids.register(HbmSpace.MOD_ID, "HCL", "NITRIC_ACID");
        NTMFluids.register(HbmSpace.MOD_ID, "MINSOL", "ENDERJUICE");
        NTMFluids.register(HbmSpace.MOD_ID, "NEON", "ARGON");
        NTMFluids.register(HbmSpace.MOD_ID, "ARGON", "XENON");
        NTMFluids.register(HbmSpace.MOD_ID, "KRYPTON", "PLASMA_BF");
        NTMFluids.register(HbmSpace.MOD_ID, "COFFEE", "HONEY");
        NTMFluids.register(HbmSpace.MOD_ID, "TEA", "NEON");
        NTMFluids.register(HbmSpace.MOD_ID, "HONEY", "TEA");
        NTMFluids.register(HbmSpace.MOD_ID, "MILK", "COFFEE");
        NTMFluids.register(HbmSpace.MOD_ID, "SMILK", "MILK");
        NTMFluids.register(HbmSpace.MOD_ID, "CCL", "SMILK");
        NTMFluids.register(HbmSpace.MOD_ID, "EVEAIR", "DUNAAIR");
        NTMFluids.register(HbmSpace.MOD_ID, "KMnO4", "HTCO4");
        NTMFluids.register(HbmSpace.MOD_ID, "CHLOROMETHANE", "LITHCARBONATE");
        NTMFluids.register(HbmSpace.MOD_ID, "METHANOL", "CHLOROMETHANE");
        NTMFluids.register(HbmSpace.MOD_ID, "BROMINE", "SULFURIC_ACID");
        NTMFluids.register(HbmSpace.MOD_ID, "CHLOROETHANE", "POLYTHYLENE");
        NTMFluids.register(HbmSpace.MOD_ID, "POLYTHYLENE", "METHANOL");
        NTMFluids.register(HbmSpace.MOD_ID, "FLUORINE", "CHLORINE");
        NTMFluids.register(HbmSpace.MOD_ID, "TEKTOAIR", "NGAS");
        NTMFluids.register(HbmSpace.MOD_ID, "ELBOWGREASE", "CCL");
        NTMFluids.register(HbmSpace.MOD_ID, "NMASSTETRANOL", "ETHANOL");
        NTMFluids.register(HbmSpace.MOD_ID, "NMASS", "MINSOL");
        NTMFluids.register(HbmSpace.MOD_ID, "SCUTTERBLOOD", "BLOOD_HOT");
        NTMFluids.register(HbmSpace.MOD_ID, "HTCO4", "RADIOSOLVENT");
        NTMFluids.register(HbmSpace.MOD_ID, "JOOLGAS", "EVEAIR");
        NTMFluids.register(HbmSpace.MOD_ID, "SARNUSGAS", "JOOLGAS");
        NTMFluids.register(HbmSpace.MOD_ID, "UGAS", "SARNUSGAS");
        NTMFluids.register(HbmSpace.MOD_ID, "NGAS", "UGAS");
        NTMFluids.register(HbmSpace.MOD_ID, "EMILK", "ELBOWGREASE");
        NTMFluids.register(HbmSpace.MOD_ID, "CMILK", "EMILK");
        NTMFluids.register(HbmSpace.MOD_ID, "CREAM", "CMILK");
        NTMFluids.register(HbmSpace.MOD_ID, "MAGMA", "BALEFIRE_FUEL");
        NTMFluids.register(HbmSpace.MOD_ID, "DICYANOACETYLENE", "CONGLOMERA");
        NTMFluids.register(HbmSpace.MOD_ID, "BRINE", "MAGMA");
        NTMFluids.register(HbmSpace.MOD_ID, "CONGLOMERA", "BRINE");
        NTMFluids.register(HbmSpace.MOD_ID, "DUNAAIR", "EARTHAIR");
        NTMFluids.register(HbmSpace.MOD_ID, "SUPERHEATED_HYDROGEN", "MUG_HOT");
        NTMFluids.register(HbmSpace.MOD_ID, "GAS_WATZ", "SUPERHEATED_HYDROGEN");
        NTMFluids.register(HbmSpace.MOD_ID, "URANIUM_BROMIDE", "BROMINE");
        NTMFluids.register(HbmSpace.MOD_ID, "AQUEOUS_COPPER", "THORIUM_BROMIDE");
        NTMFluids.register(HbmSpace.MOD_ID, "COPPERSULFATE", "AQUEOUS_COPPER");
        NTMFluids.register(HbmSpace.MOD_ID, "THORIUM_BROMIDE", "URANIUM_BROMIDE");
        NTMFluids.register(HbmSpace.MOD_ID, "GASEOUS_URANIUM_BROMIDE", "GAS_WATZ");
        NTMFluids.register(HbmSpace.MOD_ID, "GASEOUS_PLUTONIUM_BROMIDE", "GASEOUS_URANIUM_BROMIDE");
        NTMFluids.register(
                HbmSpace.MOD_ID, "GASEOUS_SCHRABIDIUM_BROMIDE", "GASEOUS_PLUTONIUM_BROMIDE");
        NTMFluids.register(
                HbmSpace.MOD_ID, "GASEOUS_THORIUM_BROMIDE", "GASEOUS_SCHRABIDIUM_BROMIDE");
        NTMFluids.register(HbmSpace.MOD_ID, "HGAS", "DICYANOACETYLENE");
        NTMFluids.register(HbmSpace.MOD_ID, "AQUEOUS_NICKEL", "ALUMINA");
        NTMFluids.register(HbmSpace.MOD_ID, "VINYL", "HGAS");
        NTMFluids.register(HbmSpace.MOD_ID, "TCRUDE", "VINYL");
        NTMFluids.register(HbmSpace.MOD_ID, "CBENZ", "TCRUDE");
        NTMFluids.register(HbmSpace.MOD_ID, "HALOLIGHT", "CBENZ");
        NTMFluids.register(HbmSpace.MOD_ID, "LITHYDRO", "COPPERSULFATE");
        NTMFluids.register(HbmSpace.MOD_ID, "LITHCARBONATE", "LITHYDRO");
        NTMFluids.move("XENON", "KRYPTON");
    }

    private FluidsSpace() {}

    public static Fluid nitrogen() {
        return NTMFluids.byName("NITROGEN");
    }

    public static Fluid ammonia() {
        return NTMFluids.byName("AMMONIA");
    }

    public static Fluid hydrazine() {
        return NTMFluids.byName("HYDRAZINE");
    }

    public static Fluid bloodgas() {
        return NTMFluids.byName("BLOODGAS");
    }

    public static Fluid earthair() {
        return NTMFluids.byName("EARTHAIR");
    }

    public static Fluid hcl() {
        return NTMFluids.byName("HCL");
    }

    public static Fluid minsol() {
        return NTMFluids.byName("MINSOL");
    }

    public static Fluid neon() {
        return NTMFluids.byName("NEON");
    }

    public static Fluid argon() {
        return NTMFluids.byName("ARGON");
    }

    public static Fluid krypton() {
        return NTMFluids.byName("KRYPTON");
    }

    public static Fluid coffee() {
        return NTMFluids.byName("COFFEE");
    }

    public static Fluid tea() {
        return NTMFluids.byName("TEA");
    }

    public static Fluid honey() {
        return NTMFluids.byName("HONEY");
    }

    public static Fluid milk() {
        return NTMFluids.byName("MILK");
    }

    public static Fluid smilk() {
        return NTMFluids.byName("SMILK");
    }

    public static Fluid ccl() {
        return NTMFluids.byName("CCL");
    }

    public static Fluid eveair() {
        return NTMFluids.byName("EVEAIR");
    }

    public static Fluid kmno4() {
        return NTMFluids.byName("KMnO4");
    }

    public static Fluid chloromethane() {
        return NTMFluids.byName("CHLOROMETHANE");
    }

    public static Fluid methanol() {
        return NTMFluids.byName("METHANOL");
    }

    public static Fluid bromine() {
        return NTMFluids.byName("BROMINE");
    }

    public static Fluid chloroethane() {
        return NTMFluids.byName("CHLOROETHANE");
    }

    public static Fluid polythylene() {
        return NTMFluids.byName("POLYTHYLENE");
    }

    public static Fluid fluorine() {
        return NTMFluids.byName("FLUORINE");
    }

    public static Fluid tektoair() {
        return NTMFluids.byName("TEKTOAIR");
    }

    public static Fluid elbowgrease() {
        return NTMFluids.byName("ELBOWGREASE");
    }

    public static Fluid nmasstetranol() {
        return NTMFluids.byName("NMASSTETRANOL");
    }

    public static Fluid nmass() {
        return NTMFluids.byName("NMASS");
    }

    public static Fluid scutterblood() {
        return NTMFluids.byName("SCUTTERBLOOD");
    }

    public static Fluid htco4() {
        return NTMFluids.byName("HTCO4");
    }

    public static Fluid joolgas() {
        return NTMFluids.byName("JOOLGAS");
    }

    public static Fluid sarnusgas() {
        return NTMFluids.byName("SARNUSGAS");
    }

    public static Fluid ugas() {
        return NTMFluids.byName("UGAS");
    }

    public static Fluid ngas() {
        return NTMFluids.byName("NGAS");
    }

    public static Fluid emilk() {
        return NTMFluids.byName("EMILK");
    }

    public static Fluid cmilk() {
        return NTMFluids.byName("CMILK");
    }

    public static Fluid cream() {
        return NTMFluids.byName("CREAM");
    }

    public static Fluid magma() {
        return NTMFluids.byName("MAGMA");
    }

    public static Fluid dicyanoacetylene() {
        return NTMFluids.byName("DICYANOACETYLENE");
    }

    public static Fluid brine() {
        return NTMFluids.byName("BRINE");
    }

    public static Fluid conglomera() {
        return NTMFluids.byName("CONGLOMERA");
    }

    public static Fluid dunaair() {
        return NTMFluids.byName("DUNAAIR");
    }

    public static Fluid superheatedHydrogen() {
        return NTMFluids.byName("SUPERHEATED_HYDROGEN");
    }

    public static Fluid gasWatz() {
        return NTMFluids.byName("GAS_WATZ");
    }

    public static Fluid uraniumBromide() {
        return NTMFluids.byName("URANIUM_BROMIDE");
    }

    public static Fluid aqueousCopper() {
        return NTMFluids.byName("AQUEOUS_COPPER");
    }

    public static Fluid coppersulfate() {
        return NTMFluids.byName("COPPERSULFATE");
    }

    public static Fluid thoriumBromide() {
        return NTMFluids.byName("THORIUM_BROMIDE");
    }

    public static Fluid gaseousUraniumBromide() {
        return NTMFluids.byName("GASEOUS_URANIUM_BROMIDE");
    }

    public static Fluid gaseousPlutoniumBromide() {
        return NTMFluids.byName("GASEOUS_PLUTONIUM_BROMIDE");
    }

    public static Fluid gaseousSchrabidiumBromide() {
        return NTMFluids.byName("GASEOUS_SCHRABIDIUM_BROMIDE");
    }

    public static Fluid gaseousThoriumBromide() {
        return NTMFluids.byName("GASEOUS_THORIUM_BROMIDE");
    }

    public static Fluid hgas() {
        return NTMFluids.byName("HGAS");
    }

    public static Fluid aqueousNickel() {
        return NTMFluids.byName("AQUEOUS_NICKEL");
    }

    public static Fluid vinyl() {
        return NTMFluids.byName("VINYL");
    }

    public static Fluid tcrude() {
        return NTMFluids.byName("TCRUDE");
    }

    public static Fluid cbenz() {
        return NTMFluids.byName("CBENZ");
    }

    public static Fluid halolight() {
        return NTMFluids.byName("HALOLIGHT");
    }

    public static Fluid lithydro() {
        return NTMFluids.byName("LITHYDRO");
    }

    public static Fluid lithcarbonate() {
        return NTMFluids.byName("LITHCARBONATE");
    }
}
